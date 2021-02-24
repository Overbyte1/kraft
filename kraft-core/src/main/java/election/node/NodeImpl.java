package election.node;

import election.config.GlobalConfig;
import election.handler.*;
import election.log.DefaultLog;
import election.log.entry.Entry;
import election.role.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.NodeEndpoint;
import rpc.RpcHandler;
import rpc.exception.NetworkException;
import rpc.handler.ServiceInboundHandler;
import rpc.message.AppendEntriesMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;
import schedule.TaskScheduleExecutor;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NodeImpl implements Node {
    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);
    private final ServiceInboundHandler serviceInboundHandler = ServiceInboundHandler.getInstance();
    //当前角色：LeaderRole、CandidateRole或FollowerRole之一
    private AbstractRole currentRole;
    //所有节点信息，包括地址、matchIndex、nextIndex等
    private NodeGroup nodeGroup;

    private RpcHandler rpcHandler;

    private TaskScheduleExecutor scheduleExecutor;

    private DefaultLog log;
    //TODO：配置类
    private GlobalConfig config;

    ScheduledFuture<?> electionTimeoutFuture;


    public NodeImpl(AbstractRole currentRole, NodeGroup nodeGroup, RpcHandler rpcHandler, TaskScheduleExecutor scheduleExecutor, GlobalConfig config) {
        this.currentRole = currentRole;
        this.nodeGroup = nodeGroup;
        this.rpcHandler = rpcHandler;
        this.scheduleExecutor = scheduleExecutor;
        this.config = config;
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
        //TODO:change
        //becomeToRole(new FollowerRole(currentRole.getNodeId(), 0));
        registerHandler(currentRole);
        startElection();
        //scheduleExecutor.schedule(this::electionTimeout, config.getMinElectionTimeout(), TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean apply(byte[] command) {
        return false;
    }

    //TODO:becomeToRole
    private void becomeToRole(AbstractRole targetRole) {
        currentRole = targetRole;
        //设置超时任务
        if(targetRole instanceof FollowerRole) {

        } else if(targetRole instanceof  CandidateRole) {

        }
        registerHandler(targetRole);
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
        } else {
            logger.error("cannot find MessageHandler, role type is {}", roleClazz);
            //TODO：抛异常
            return null;
        }
    }

    private void startElection() {
        logger.debug("start election, new term is {}", currentRole.getCurrentTerm() + 1);

        //将角色更改为Candidate，term + 1
        becomeToRole(new CandidateRole(currentRole.getNodeId(), currentRole.getCurrentTerm() + 1, null));
        //提交选举超时任务
        //TODO:修改时间
        electionTimeoutFuture = scheduleExecutor.schedule(this::electionTimeout, config.getMinElectionTimeout(),
                TimeUnit.MILLISECONDS);
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
        becomeToRole(new CandidateRole(currentRole.getNodeId(), currentTerm));

    }

    private class CandidateMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(CandidateMessageHandler.class);

        public CandidateMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AppendEntriesMessage appendRequestMsg) {
            long term = appendRequestMsg.getTerm();
            if(term >= currentRole.getCurrentTerm()) {
                logger.debug("receive AppendEntriesResultMessage, term is {}", term);
                //TODO：取消选举超时任务
                electionTimeoutFuture.cancel(true);
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
        public RequestVoteResultMessage handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
            long term = requestVoteMessage.getTerm();
            long currentTerm = currentRole.getCurrentTerm();
            //如果requestVoteMessage.term < currentTerm，不投票，并返回currentTerm
            if(term < currentTerm) {
                return new RequestVoteResultMessage(currentTerm, false);
            //如果如果requestVoteMessage.term == currentTerm，不投票，因为票已经投给了自己
            } else if(term == currentTerm) {
                return new RequestVoteResultMessage(currentTerm, false);
                //如果requestVoteMessage.term > currentTerm，如果自己的日志更加新则不投票，否则投票。变成Follower
            } else {
                //TODO:添加实现
                //为进行测试，默认不投票
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
         * @param voteResultMessage
         */
        @Override
        public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {
            long term = voteResultMessage.getTerm();
            boolean voteGranted = voteResultMessage.isVoteGranted();

            if(term > currentRole.getCurrentTerm()) {
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));
                return;
            }
            if(!voteGranted) {
                return;
            }
            if(!(currentRole instanceof CandidateRole)) {
                logger.debug("receive requestVoteResult, but current role was not Candidate!");
                return;
            }
            CandidateRole candidateRole = (CandidateRole)currentRole;
            candidateRole.incVoteCount();
            logger.debug("receive vote counts are {}, major count is {}", candidateRole.getVoteCount(),
                    nodeGroup.getSize() / 2);
            //票数过半，转换成Leader，取消选举超时任务，发送空的AppendEntries消息
            if(candidateRole.getVoteCount() > nodeGroup.getSize() / 2) {
                becomeToRole(new LeaderRole(currentRole.getNodeId(), term));
                //取消选举超时任务
                electionTimeoutFuture.cancel(true);
                logger.info("current node {} become leader,current term is {}", currentRole.getNodeId(), currentRole.getCurrentTerm());
                //TODO:发送空的AppendEntries消息
                Set<NodeEndpoint> allNodeEndpoint = getAllNodeEndpoint();
                //TODO:初始化Leader需要维护的状态 replicationState，更新commitIndex
//                rpcHandler.sendAppendEntriesMessage(, currentRole.getCurrentTerm(),
//                        allNodeEndpoint);

            }

        }

        @Override
        public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage, NodeId fromId) {
            logger.warn("receive AppendEntriesResultMessage, term is {}", appendEntriesResultMessage.getTerm());
        }
    }

    class FollowerMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(FollowerMessageHandler.class);


        public FollowerMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AppendEntriesMessage appendRequestMsg) {
            logger.debug("receive AppendEntriesResultMessage, term is {}", appendRequestMsg.getTerm());
            long currentTerm = currentRole.getCurrentTerm();
            long term = appendRequestMsg.getTerm();
            if(currentTerm > term) {
                return new AppendEntriesResultMessage(currentTerm, false);
            }
            if(currentTerm < term) {
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));
            }

            List<Entry> entryList = appendRequestMsg.getLogEntryList();
            long preTerm = appendRequestMsg.getPreLogTerm();
            long preLogIndex = appendRequestMsg.getPreLogIndex();
            long currentLogIndex = entryList.get(0).getIndex();

            if(log.appendEntries(preTerm, preLogIndex, currentLogIndex, entryList)) {
                return new AppendEntriesResultMessage(currentTerm, true);
            }
            return new AppendEntriesResultMessage(currentTerm, false);
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
            long term = requestVoteMessage.getTerm();
            if(term <= currentRole.getCurrentTerm()) {
                logger.info("receive RequestVoteMessage, receive term is {}, but currentTerm is {}",
                        term, currentRole.getCurrentTerm());
                return null;
            }
            //默认进行投票
            //NodeEndpoint nodeEndpoint = getNodeEndpoint(currentRole.getNodeId());
            return new RequestVoteResultMessage(term, true);
            //rpcHandler.sendRequestVoteResultMessage(term, true, nodeEndpoint);
        }

        @Override
        public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {
            logger.warn("receive illegal RequestVoteResultMessage, current role is Follower");
        }

        @Override
        public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage, NodeId fromId) {
            logger.warn("receive illegal RequestVoteMessage, current role is Follower");
        }
    }

    class LeaderMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(LeaderMessageHandler.class);

        public LeaderMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AppendEntriesMessage appendRequestMsg) {
            logger.warn("receive AppendEntriesResultMessage, term is {}", appendRequestMsg.getTerm());
            long term = appendRequestMsg.getTerm();
            if(term > currentRole.getCurrentTerm()) {
                logger.info("become Follower from Leader");
                //TODO:设置选举超时任务
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));
                logger.info("begin to commit");
                return null;
            }
            return null;
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
            logger.warn("receive AppendEntriesResultMessage, term is {}", requestVoteMessage.getTerm());
            long term = requestVoteMessage.getTerm();
            if(term > currentRole.getCurrentTerm()) {
                logger.info("become Follower from Leader");
                //TODO:设置选举超时任务
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));
                return new RequestVoteResultMessage(term, true);
            }
            return new RequestVoteResultMessage(currentRole.getCurrentTerm(), false);
        }

        @Override
        public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {
            logger.info("receive RequestVoteResultMessage, but current node has become Leader");
        }

        @Override
        public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage, NodeId fromId) {
            logger.debug("receive AppendEntriesResultMessage: {}", appendEntriesResultMessage);
            long term = appendEntriesResultMessage.getTerm();
            boolean success = appendEntriesResultMessage.isSuccess();
            if(success) {
                //TODO：commitIndex推进需要过半matchIndex以及term，只有日志条目的term和自己的term一致才能更新commitIndex
            }
            //fail
            if(term > currentRole.getCurrentTerm()) {
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term));
                return;
            }
            //preLogTerm and preLogIndex 不匹配，减少 nextIndex 并重试
            log.decNextIndex(fromId);
            //创建 AppendEntriesMessage
            AppendEntriesMessage message =
                    log.createAppendEntriesMessage(currentRole.getNodeId(), currentRole.getCurrentTerm());
            //获取地址信息
            NodeEndpoint nodeEndpoint = getNodeEndpoint(fromId);
            List<NodeEndpoint> list = new ArrayList<>(1);
            list.add(nodeEndpoint);
            //发送
            rpcHandler.sendAppendEntriesMessage(message, list);
            /*TODO：ReplicationState 由谁负责：matchIndex和nextIndex，由log维护？
             */
        }
    }
}
