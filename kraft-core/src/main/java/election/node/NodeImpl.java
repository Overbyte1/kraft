package election.node;

import election.config.GlobalConfig;
import election.exception.IndexException;
import election.handler.*;
import election.log.DefaultLog;
import election.log.Log;
import election.log.entry.Entry;
import election.role.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.NodeEndpoint;
import rpc.RpcHandler;
import rpc.exception.NetworkException;
import rpc.handler.ServiceInboundHandler;
import rpc.message.*;
import schedule.TaskScheduleExecutor;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NodeImpl implements Node {
    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);
    private final ServiceInboundHandler serviceInboundHandler = ServiceInboundHandler.getInstance();
    //当前角色：LeaderRole、CandidateRole或FollowerRole之一
    private volatile AbstractRole currentRole;
    //所有节点信息，包括地址、matchIndex、nextIndex等
    private NodeGroup nodeGroup;

    private RpcHandler rpcHandler;

    private TaskScheduleExecutor scheduleExecutor;

    private NodeId currentNodeId;

    private DefaultLog log;

    private Log log1;

    private Random random = new Random();
    //TODO：配置类
    private GlobalConfig config;

    ScheduledFuture<?> electionTimeoutFuture;
    ScheduledFuture<?> replicationReadTimeoutFuture;
    ScheduledFuture<?> replicationTimeoutFuture;


    public NodeImpl(AbstractRole currentRole, NodeGroup nodeGroup, RpcHandler rpcHandler, TaskScheduleExecutor scheduleExecutor, GlobalConfig config) {
        this.currentRole = currentRole;
        this.nodeGroup = nodeGroup;
        this.rpcHandler = rpcHandler;
        this.scheduleExecutor = scheduleExecutor;
        this.config = config;
    }

    public NodeImpl(NodeGroup nodeGroup, RpcHandler rpcHandler, TaskScheduleExecutor scheduleExecutor,
                    DefaultLog log, GlobalConfig config, NodeId currentNodeId) {
        this.nodeGroup = nodeGroup;
        this.rpcHandler = rpcHandler;
        this.scheduleExecutor = scheduleExecutor;
        this.log = log;
        this.config = config;
        this.currentNodeId = currentNodeId;
    }

    @Override
    public void start() {
        /*
        1. 创建当前角色为Follower
        2. 恢复Follower的持久性数据
        3. Follower等待心跳超时后变更成Candidate发起选举，发起选举需要各个节点的IP以及端口号，通过配置进行读取
        4. Follower根据投票RPC的结果做出下一步决策：
            4.1 成功：变成Leader节点，向其他节点发送心跳消息
            4.2 失败：
                （1）收到Leader节点发来的心跳或AppendRpc，说明其他节点成为Leader，变成Follower，设置voteFor
                （2）没赢得选票，自己的term或日志不够新
            4.3 选举超时，term + 1，发起新一轮选举
         */
        logger.info("current node is starting......");
        //TODO:change
        becomeToRole(new FollowerRole(currentNodeId, 0));
//        registerHandler(currentRole);
//        startElection();
        //scheduleExecutor.schedule(this::electionTimeout, config.getMinElectionTimeout(), TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean apply(byte[] command) {
        return false;
    }

    private void becomeToRole(AbstractRole targetRole) {
        logger.debug("current role become {} from {}", targetRole, currentRole);

        AbstractRole sourceRole = currentRole;
        if(sourceRole instanceof LeaderRole) {
            replicationTimeoutFuture.cancel(false);
        } else if(sourceRole instanceof FollowerRole) {
            replicationReadTimeoutFuture.cancel(false);
        } else if(sourceRole instanceof CandidateRole){
            electionTimeoutFuture.cancel(false);
            //TODO：断开网络连接
        }
        currentRole = targetRole;
        //设置超时任务
        if(targetRole instanceof FollowerRole) {
            //设置心跳超时任务
            replicationReadTimeoutFuture = scheduleExecutor.schedule(this::logReplicationReadTimeout, config.getLogReplicationResultTimeout(), TimeUnit.MILLISECONDS);
        } else if(targetRole instanceof  CandidateRole) {
            //重设选举超时任务
            int maxTimeout = config.getMaxElectionTimeout(), minTimeout = config.getMinElectionTimeout();
            int timeout = random.nextInt(maxTimeout - minTimeout) + minTimeout;
            electionTimeoutFuture = scheduleExecutor.schedule(this::electionTimeout, timeout, TimeUnit.MILLISECONDS );
        } else { //LeaderRole
            //定时发送心跳包任务
            replicationTimeoutFuture = scheduleExecutor.schedule(this::logReplicationTask, config.getLogReplicationInterval(), TimeUnit.MILLISECONDS);
        }
        registerHandler(targetRole);
    }

    /**
     * 定时发送日志，即时没有新的日志也要发送空的日志作为心跳
     */
    private void logReplicationTask() {
        logger.debug("start replicating log, current node is {}, current term is {}", currentNodeId, currentRole.getCurrentTerm());
        //为每个节点都维护上一次复制日志的时间
        Collection<GroupMember> groupMembers = nodeGroup.getAllGroupMember();
        for (GroupMember member : groupMembers) {
            //TODO：配置 以及 考虑隐藏 ReplicationState
            if(member.shouldReplication(config.getLogReplicationInterval())) {
                AppendEntriesMessage message = log.createAppendEntriesMessage(currentRole.getNodeId(), currentRole.getCurrentTerm(),
                        member.getReplicationState().getNextIndex());
                //更新日志发送时间
                member.updateReplicationTime();
                rpcHandler.sendAppendEntriesMessage(message, member.getNodeEndpoint());
            }
        }
    }
    private void registerHandler(AbstractRole targetRole) {
        Class clazz = targetRole.getClass();
        MessageHandler handler = getMessageHandler(clazz);
        //TODO:根据不同role 移除或注册handler，需要确保更换的原子性 或者 处理时能够识别 角色已经改变
        serviceInboundHandler.registerHandler(AppendEntriesMessage.class, handler);
        serviceInboundHandler.registerHandler(AppendEntriesResultMessage.class, handler);
        serviceInboundHandler.registerHandler(RequestVoteMessage.class, handler);
        serviceInboundHandler.registerHandler(RequestVoteResultMessage.class, handler);
    }

    /**
     * 根据角色类型获取 消息处理器（MessageHandler）
     * @param roleClazz
     * @return
     */
    private MessageHandler getMessageHandler(Class roleClazz) {
        if(roleClazz == LeaderRole.class) {
            return new LeaderMessageHandler(logger);
        } else if(roleClazz == CandidateRole.class) {
            return new CandidateMessageHandler(logger);
        } else if(roleClazz == FollowerRole.class){
            return new FollowerMessageHandler(logger);
        } else { //TODO:不可能出现的情况，方便调试，Remove it
            logger.error("cannot find MessageHandler, role type is {}", roleClazz);
            return null;
        }
    }

    private void logReplicationReadTimeout() {
        logger.debug("log replication read timeout");
        startElection();
    }

    private void startElection() {
        logger.debug("start election, new term is {}", currentRole.getCurrentTerm() + 1);



        //将角色更改为Candidate，term + 1
        becomeToRole(new CandidateRole(currentRole.getNodeId(), currentRole.getCurrentTerm() + 1, null));
        //提交选举超时任务
//        electionTimeoutFuture = scheduleExecutor.schedule(this::electionTimeout, config.getMinElectionTimeout(),
//                TimeUnit.MILLISECONDS);
        //向所有节点发送请求投票rpc
        //TODO：设置lastLogIndex、lastLogTerm
        rpcHandler.sendRequestVoteMessage(new RequestVoteMessage(currentRole.getCurrentTerm(), currentRole.getNodeId(),
                0, 0), getAllNodeEndpoint());
    }
    private NodeEndpoint getNodeEndpoint(NodeId nodeId) {
        GroupMember groupMember = nodeGroup.getGroupMember(nodeId);
        if(groupMember == null) {
            logger.warn("NodeEndpoint of nodeId {} not exist", nodeId);
            throw new NetworkException("NodeEndpoint not exist");
        }
        return groupMember.getNodeEndpoint();
    }
    private Set<NodeEndpoint> getAllNodeEndpoint() {
        Collection<GroupMember> allGroupMember = nodeGroup.getAllGroupMember();
        Set<NodeEndpoint> set = new HashSet<>();
        for (GroupMember groupMember : allGroupMember) {
            set.add(groupMember.getNodeEndpoint());
        }
        return set;
    }
    private void electionTimeout() {
        logger.debug("election timeout, current term is {}", currentRole.getCurrentTerm());
        startElection();
    }
    private void logReplicationTimeout() {
        long currentTerm = currentRole.getCurrentTerm();
        logger.info("logReplication timeout, current term is {}", currentTerm);
        becomeToRole(new CandidateRole(currentRole.getNodeId(), currentTerm + 1));

    }


    private class CandidateMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(CandidateMessageHandler.class);

        public CandidateMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message) {
            AppendEntriesMessage appendRequestMsg = message.getBody();
            long term = appendRequestMsg.getTerm();
            if(term >= currentRole.getCurrentTerm()) {
                logger.debug("receive AppendEntriesResultMessage, term is {}", term);
                //electionTimeoutFuture.cancel(true);

                //TODO：断开与其他节点的网络连接
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));

                List<Entry> entryList = appendRequestMsg.getLogEntryList();
                long preTerm = appendRequestMsg.getPreLogTerm();
                long preLogIndex = appendRequestMsg.getPreLogIndex();
                long currentLogIndex = entryList.get(0).getIndex();
                if (log.appendEntries(preTerm, preLogIndex, currentLogIndex, entryList)) {
                    return new AppendEntriesResultMessage(term, true);
                }
            }
            logger.info("receive unexpect AppendEntriesResultMessage, currentTerm is {}, receive term is {}",
                    currentRole.getCurrentTerm(), term);
            return new AppendEntriesResultMessage(currentRole.getCurrentTerm(), false);
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message) {
            RequestVoteMessage requestVoteMessage = message.getBody();
            long term = requestVoteMessage.getTerm();
            long currentTerm = currentRole.getCurrentTerm();
            //如果requestVoteMessage.term < currentTerm，不投票，并返回currentTerm
            if(term < currentTerm) {
                return new RequestVoteResultMessage(currentTerm, false);
            //如果如果requestVoteMessage.term == currentTerm，不投票，因为票已经投给了自己
            } else if(term == currentTerm) {
                return new RequestVoteResultMessage(currentTerm, false);
                //TODO:如果requestVoteMessage.term > currentTerm，如果自己的日志更加新则不投票，否则投票。变成Follower
            } else {
                //TODO：断开与其他节点的网络连接
                becomeToRole(new FollowerRole(currentRole.getNodeId(), currentTerm));
                logger.debug("voteFor {}", requestVoteMessage.getCandidateId());
                //rpcHandler.sendRequestVoteResultMessage(currentTerm, false, nodeEndpoint);
                return new RequestVoteResultMessage(currentTerm, true);
            }
        }

        /**
         * 处理RequestVoteResult
         * 1. 检查如果voteResultMessage.term，如果大于 currentTerm，则当前角色变为Follower
         * 2. 如果voteResultMessage.voteGranted==true，检查当前角色是否还是Candidate，如果是则当前票数+1，之后检查总票数是否过半
         * 3. 如果voteResultMessage.voteGranted==false，直接返回
         * @param message
         */
        @Override
        public void handleRequestVoteResult(AbstractMessage<RequestVoteResultMessage> message) {
            RequestVoteResultMessage voteResultMessage = message.getBody();
            long messageTerm = voteResultMessage.getTerm();
            long currentTerm = currentRole.getCurrentTerm();
            boolean voteGranted = voteResultMessage.isVoteGranted();
            NodeId currentNodeId = currentRole.getNodeId();
            if(messageTerm > currentTerm) {
                becomeToRole(new FollowerRole(currentNodeId, messageTerm));
                return;
            }
            if(!voteGranted) {
                return;
            }
            if(!(currentRole instanceof CandidateRole)) {
                logger.debug("receive requestVoteResult: {}, but current role was not Candidate!", message);
                return;
            }
            //投票结果为 true
            CandidateRole candidateRole = (CandidateRole)currentRole;
            candidateRole.incVoteCount();
            logger.debug("receive vote counts are {}, major count is {}", candidateRole.getVoteCount(),
                    nodeGroup.getSize() / 2);
            //票数过半，转换成Leader，取消选举超时任务，发送空的AppendEntries消息
            if(candidateRole.getVoteCount() > nodeGroup.getSize() / 2) {
                becomeToRole(new LeaderRole(currentNodeId, messageTerm));
                //取消选举超时任务
                //electionTimeoutFuture.cancel(true);
                logger.info("current node {} become leader,current term is {}", currentNodeId, currentTerm);
                //TODO:发送空的AppendEntries消息
                //Set<NodeEndpoint> allNodeEndpoint = getAllNodeEndpoint();
                //TODO:初始化Leader需要维护的状态 replicationState，更新commitIndex
                long nextIndex = log.getLastLogIndex();
                nodeGroup.resetReplicationState(nextIndex);

                log.appendEmptyEntry(messageTerm);
                //构造空的AppendEntriesMessage发送

            }
        }

        @Override
        public void handleAppendEntriesResult(AbstractMessage<AppendEntriesResultMessage> message) {
            AppendEntriesResultMessage entriesResultMessage = message.getBody();
            logger.warn("receive AppendEntriesResultMessage, term is {}", entriesResultMessage.getTerm());
        }
    }

    class FollowerMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(FollowerMessageHandler.class);

        public FollowerMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message) {
            AppendEntriesMessage appendRequestMsg = message.getBody();

            logger.debug("receive AppendEntriesResultMessage, term is {}", appendRequestMsg.getTerm());

            long currentTerm = currentRole.getCurrentTerm();
            long term = appendRequestMsg.getTerm();
            if(currentTerm > term) {
                return new AppendEntriesResultMessage(currentTerm, false);
            }
            if(currentTerm < term) {
                //TODO：断开与其他节点的网络连接，保留与Leader的连接
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));
            }

            List<Entry> entryList = appendRequestMsg.getLogEntryList();

            long preTerm = appendRequestMsg.getPreLogTerm();
            long preLogIndex = appendRequestMsg.getPreLogIndex();
            //long currentLogIndex = entryList.get(0).getIndex();

            //Leader会定时发送AppendEntriesMessage，但是里面可能没有新的日志作为作为心跳消息
            GroupMember member = nodeGroup.getGroupMember(message.getNodeId());
            if(log1.appendGeneralEntriesFromLeader(preTerm, preLogIndex, entryList, member.getReplicationState())) {
                //更新nextIndex和matchIndex

                return new AppendEntriesResultMessage(currentTerm, true);
            }
            return new AppendEntriesResultMessage(currentTerm, false);
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message) {
            RequestVoteMessage requestVoteMessage = message.getBody();
            long messageTerm = requestVoteMessage.getTerm();
            if(messageTerm <= currentRole.getCurrentTerm()) {
                logger.info("receive RequestVoteMessage, receive term is {}, but currentTerm is {}",
                        messageTerm, currentRole.getCurrentTerm());
                return new RequestVoteResultMessage(messageTerm, false);
            }
            //TODO:如果message中的日志比较新，则投票，否则不投票

            return new RequestVoteResultMessage(messageTerm, true);
        }

        @Override
        public void handleRequestVoteResult(AbstractMessage<RequestVoteResultMessage> message) {
            logger.warn("receive illegal RequestVoteResultMessage, current role is Follower");
        }

        @Override
        public void handleAppendEntriesResult(AbstractMessage<AppendEntriesResultMessage> message) {
            logger.warn("receive illegal RequestVoteMessage, current role is Follower");
        }
    }

    class LeaderMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(LeaderMessageHandler.class);

        public LeaderMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message) {
            AppendEntriesMessage appendRequestMsg = message.getBody();

            logger.warn("receive AppendEntriesResultMessage, term is {}", appendRequestMsg.getTerm());
            long term = appendRequestMsg.getTerm();
            long currentTerm = currentRole.getCurrentTerm();
            if(term > currentTerm) {
                logger.info("become Follower from Leader, new term is {}", term);
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));

                List<Entry> entryList = appendRequestMsg.getLogEntryList();
                long preTerm = appendRequestMsg.getPreLogTerm();
                long preLogIndex = appendRequestMsg.getPreLogIndex();
                long currentLogIndex = entryList.get(0).getIndex();

                if(log.appendEntries(preTerm, preLogIndex, currentLogIndex, entryList)) {
                    return new AppendEntriesResultMessage(term, false);
                }
                return new AppendEntriesResultMessage(term, true);
            }
            return new AppendEntriesResultMessage(currentTerm, false);
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message) {
            RequestVoteMessage requestVoteMessage = message.getBody();
            logger.warn("receive AppendEntriesResultMessage, term is {}", requestVoteMessage.getTerm());
            long term = requestVoteMessage.getTerm();
            if(term > currentRole.getCurrentTerm()) {
                logger.info("become Follower from Leader");
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));

                return new RequestVoteResultMessage(term, true);
            }
            return new RequestVoteResultMessage(currentRole.getCurrentTerm(), false);
        }

        @Override
        public void handleRequestVoteResult(AbstractMessage<RequestVoteResultMessage> message) {
            logger.info("receive RequestVoteResultMessage, but current node has become Leader, current term is {}",
                    currentRole.getCurrentTerm());
        }

        @Override
        public void handleAppendEntriesResult(AbstractMessage<AppendEntriesResultMessage> message) {
            AppendEntriesResultMessage entriesResultMessage = message.getBody();
            NodeId fromId = message.getNodeId();
            logger.debug("receive AppendEntriesResultMessage: {}, but current role is Leader", entriesResultMessage);

            long term = entriesResultMessage.getTerm();
            boolean success = entriesResultMessage.isSuccess();

            if(success) {
                //TODO：commitIndex推进需要过半matchIndex以及term，只有日志条目的term和自己的term一致才能更新commitIndex
                //TODO：将日志相关逻辑尽量封装到Log中
                /*
                如果存在一个满足 N > commitIndex的 N，并且大多数的 matchIndex[i] ≥ N成立，
                并且 log[N].term == currentTerm 成立，那么令 commitIndex 等于这个 N
                 */
                ReplicationState replicationState = nodeGroup.getGroupMember(fromId).getReplicationState();
                replicationState.incMatchIndex();
                long lastLogIndex = log.getLastLogIndex();

                if(lastLogIndex > replicationState.getNextIndex()) {
                    replicationState.incNextIndex();
                }
                //判断matchIndex是否过半
                //nodeGroup.isMajorMatchIndex(0);
                log.advanceCommit(currentRole.getCurrentTerm());
                return;
            }

            //fail
            if(term > currentRole.getCurrentTerm()) {
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));
                return;
            }
            GroupMember member = nodeGroup.getGroupMember(fromId);
            //preLogTerm and preLogIndex 不匹配，减少 nextIndex 并重试

            try {
                member.getReplicationState().decNextIndex(1);
            } catch (IndexException exception) {
                logger.error("the nextIndex of node {} cannot be less than 0", fromId);
            }
            //TODO:完成Leader的日志复制以及心跳消息。定时任务，通过记录每次的发送时间来判断是否要发送log消息
        }
    }
}
