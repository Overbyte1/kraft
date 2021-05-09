package election.node;

import com.google.common.util.concurrent.FutureCallback;
import config.ClusterConfig;
import election.config.GlobalConfig;
import election.exception.IncompleteArgumentException;
import election.exception.IndexException;
import election.handler.AbstractMessageHandler;
import election.handler.MessageHandler;
import election.handler.RequestHandler;
import election.handler.ResponseHandler;
import election.role.*;
import election.statemachine.StateMachine;
import log.Log;
import log.LogImpl;
import log.entry.Entry;
import log.store.FileLogStore;
import log.store.LogStore;
import log.store.MemoryLogStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.ChannelGroup;
import rpc.NodeEndpoint;
import rpc.RpcHandler;
import rpc.RpcHandlerImpl;
import rpc.exception.NetworkException;
import rpc.handler.ServiceInboundHandler;
import rpc.message.*;
import schedule.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class NodeImpl implements Node {
    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);
    private final ServiceInboundHandler serviceInboundHandler = ServiceInboundHandler.getInstance();
    //当前角色：LeaderRole、CandidateRole或FollowerRole之一
    private volatile AbstractRole currentRole;
    //所有节点信息，包括地址、matchIndex、nextIndex等
    private NodeGroup nodeGroup;

    private RpcHandler rpcHandler;

    //private TaskScheduleExecutor scheduleExecutor;
    private TaskExecutor taskExecutor = new SingleThreadTaskExecutor();

    private TaskScheduler taskScheduler;
    private static final FutureCallback<Object> LOGGING_FUTURE_CALLBACK = new FutureCallback<Object>() {
        @Override
        public void onSuccess(@Nullable Object result) {
        }

        @Override
        public void onFailure(@Nonnull Throwable t) {
            logger.warn("failure", t);
        }
    };


    private NodeId currentNodeId;

    //private DefaultLog log;

    private Log log;

    private Random random = new Random();
    //private GlobalConfig config;
    //private ClusterConfig config;
    private long logReplicationInterval;

    private volatile ElectionTimeoutFuture electionTimeoutFuture;
    //private volatile LogReplicationReadFuture replicationReadTimeoutFuture;
    private volatile LogReplicationFuture replicationTimeoutFuture;


    private NodeImpl(AbstractRole currentRole, NodeGroup nodeGroup, RpcHandler rpcHandler, TaskScheduler taskScheduler) {
        this.currentRole = currentRole;
        this.nodeGroup = nodeGroup;
        this.rpcHandler = rpcHandler;
        this.taskScheduler = taskScheduler;
//        this.config = config;
    }

    private NodeImpl(NodeGroup nodeGroup, RpcHandler rpcHandler, TaskScheduler taskScheduler,
                    Log log, NodeId currentNodeId, long logReplicationInterval) {
        this.nodeGroup = nodeGroup;
        this.rpcHandler = rpcHandler;
        this.taskScheduler = taskScheduler;
        this.log = log;
        //this.config = config;
        this.currentNodeId = currentNodeId;
        this.logReplicationInterval = logReplicationInterval;
    }

    public static NodeBuilder builder() {
        return new NodeBuilder();
    }

    public static class NodeBuilder {
        private NodeId nodeId;
        private NodeGroup nodeGroup;
        private RpcHandler rpcHandler;
        private StateMachine stateMachine;
        private TaskScheduler taskScheduler;
        private LogStore logStore;
        private Log log;

        private int port = 8888;
        private int logReplicationInterval = 500;
        private int connectTimeout = 3000;
        private int minElectionTimeout = 6000;
        private int maxElectionTimeout = 10000;

        public static NodeBuilder builder() {
            return new NodeBuilder();
        }

        /**
         * 设置nodeId，必须被调用
         * @param id
         * @return
         */
        public NodeBuilder withId(String id) {
            this.nodeId = new NodeId(id);
            return this;
        }
        public NodeBuilder withId(NodeId nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /**
         * 设置集群的所有成员，必须被调用
         * @param nodeList
         * @return
         */
        public NodeBuilder withNodeList(List<NodeEndpoint> nodeList) {
            if(nodeId == null) {
                throw new IncompleteArgumentException("node id was not set");
            }
            nodeGroup = new NodeGroup();
            for (NodeEndpoint nodeEndpoint : nodeList) {
                //TODO:深拷贝
                nodeGroup.addGroupMember(nodeEndpoint.getNodeId(), new GroupMember(nodeEndpoint));
            }
            nodeGroup.setSelfNodeId(nodeId);
            return this;
        }

        /**
         * 设置状态机，必须被调用
         * @param stateMachine
         * @return
         */
        public NodeBuilder withStateMachine(StateMachine stateMachine) {
            this.stateMachine = stateMachine;
            return this;
        }

        /**
         * 设置选举的最小、最大超时时间，可选
         * @param minElectionTimeout
         * @param maxElectionTimeout
         * @return
         */
        public NodeBuilder withElectionTimeout(int minElectionTimeout, int maxElectionTimeout) {
            assert(minElectionTimeout > 0 && maxElectionTimeout > 0 && maxElectionTimeout >= minElectionTimeout);
            this.minElectionTimeout = minElectionTimeout;
            this.maxElectionTimeout = maxElectionTimeout;
            //taskScheduler = new SingleThreadTaskScheduler(minElectionTimeout, maxElectionTimeout, logReplicationTimeout);
            return this;
        }
        public NodeBuilder withLogReplicationInterval(int interval) {
            this.logReplicationInterval = interval;
            return this;
        }

        public NodeBuilder withPath(String path) throws IOException {
            logStore = new FileLogStore(path);
            return this;
        }
        public NodeBuilder withListenPort(int port) {
            assert(port > 0);
            this.port = port;
            return this;
        }

        /**
         * 使用内存储日志，内存数据容易丢失，调试时使用
         * @return
         */
        public NodeBuilder withMemLogStore() {
            logStore = new MemoryLogStore();
            return this;
        }

        public Node build() {
            if(nodeGroup == null) {
                throw new IncompleteArgumentException("node list was not set"   );
            }
            if(stateMachine == null) {
                throw new IncompleteArgumentException("StateMachine was not set");
            }
            if(nodeGroup == null) {
                throw new IncompleteArgumentException("node list was not set");
            }
            if(nodeId == null) {
                throw new IncompleteArgumentException("node id was not set");
            }
            taskScheduler = new SingleThreadTaskScheduler(minElectionTimeout, maxElectionTimeout, logReplicationInterval);
            log = new LogImpl(logStore, stateMachine, nodeGroup);
            ChannelGroup channelGroup = new ChannelGroup(nodeGroup);
            rpcHandler = new RpcHandlerImpl(channelGroup, port, connectTimeout, nodeId);
            //TODO：打印所有配置信息
            return new NodeImpl(nodeGroup, rpcHandler, taskScheduler, log, nodeId, logReplicationInterval);
        }
        public Node justBuild(ClusterConfig config, StateMachine stateMachine) throws IOException {
            return withId(config.getSelfId())
                    .withListenPort(config.getPort())
                    .withLogReplicationInterval(config.getLogReplicationInterval())
                    .withStateMachine(stateMachine)
                    .withNodeList(config.getMembers())
                    .withElectionTimeout(config.getMinElectionTimeout(), config.getMaxElectionTimeout())
                    .withPath(config.getPath())
                    .build();
        }
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
        logger.info("current node id is {}", currentNodeId);
        becomeToRole(new FollowerRole(currentNodeId, 0));
        rpcHandler.initialize();
//        registerHandler(currentRole);
//        startElection();
        //scheduleExecutor.schedule(this::electionTimeout, config.getMinElectionTimeout(), TimeUnit.MILLISECONDS);

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean appendLog(byte[] command) {
        if(currentRole instanceof LeaderRole) {
            //添加日志后马上进行日志复制，避免命令执行的延迟过大，如果对实时性要求不高可等待一小段时间后再进行批量日志复制操作，提高吞吐量
            taskExecutor.submit(
                    () -> {
                        log.appendGeneralEntry(currentRole.getCurrentTerm(), command);
                        replicationTimeoutFuture.cancel();
                        doReplication(true);
                        replicationTimeoutFuture = taskScheduler.scheduleLogReplicationTask(this::logReplicationTask);
                    }, LOGGING_FUTURE_CALLBACK
            );
        }
        return false;
    }

    /**
     * currentRole使用volatile保证可见性
     * @return
     */
    @Override
    public boolean isLeader() {
        return currentRole instanceof LeaderRole;
    }

    @Override
    public RoleType getRoleType() {
        return currentRole.getRoleType();
    }

    @Override
    public long getCurrentTerm() {
        return currentRole.getCurrentTerm();
    }

    @Override
    public NodeEndpoint getLeaderNodeEndpoint() {
        Callable<NodeEndpoint> task = () -> {
            //Candidate的voteFor是他自身nodeId
            NodeId voteFor = currentRole.getLeaderId();
            if (voteFor != null) {
                return nodeGroup.getGroupMember(voteFor).getNodeEndpoint();
            }
            return null;

        };
        Future<NodeEndpoint> future = taskExecutor.submit(task);
        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        log.registerStateMachine(stateMachine);
    }

    private void becomeToRole(AbstractRole targetRole) {

        String fromName = "null", targetName = targetRole.getClass().getSimpleName();
        if(currentRole != null) {
            fromName = currentRole.getClass().getSimpleName();
        }
        logger.debug("current role become {} from {}", targetName, fromName);

        AbstractRole sourceRole = currentRole;
        if(sourceRole instanceof LeaderRole) {
            replicationTimeoutFuture.cancel();
            replicationTimeoutFuture = null;
            //清除所有member的ReplicationState避免内存泄漏
            clearReplicationState();
        } else if(sourceRole instanceof FollowerRole) {
            electionTimeoutFuture.cancel();
            electionTimeoutFuture = null;
        } else if(sourceRole instanceof CandidateRole){
            electionTimeoutFuture.cancel();
            electionTimeoutFuture = null;
        }
        assert replicationTimeoutFuture == null;
        assert electionTimeoutFuture == null;
        currentRole = targetRole;
        //设置超时任务
        if(targetRole instanceof FollowerRole) {
            //设置心跳超时任务
            electionTimeoutFuture = taskScheduler.scheduleElectionTimeoutTask(this::electionTimeout);
        } else if(targetRole instanceof  CandidateRole) {
            //重设选举超时任务
            electionTimeoutFuture = taskScheduler.scheduleElectionTimeoutTask(this::electionTimeout);

        } else if(targetRole instanceof LeaderRole){ //LeaderRole
            //定时发送心跳包任务
            replicationTimeoutFuture = taskScheduler.scheduleLogReplicationTask(this::logReplicationTask);
        }
        registerHandler(targetRole);
    }
    private void resetReplicationReadTimeoutTask() {
        electionTimeoutFuture.cancel();
        electionTimeoutFuture = taskScheduler.scheduleElectionTimeoutTask(this::electionTimeout);
    }
    private void resetReplicationTask() {
        replicationTimeoutFuture.cancel();
        replicationTimeoutFuture = taskScheduler.scheduleLogReplicationTask(this::logReplicationTask);
    }

    /**
     * 当前节点的角色由Leader变为Follower后，需要清除ReplicationState
     *
     */
    private void clearReplicationState() {
        Collection<GroupMember> allGroupMember = nodeGroup.getAllGroupMember();
        for (GroupMember member : allGroupMember) {
            member.setReplicationState(null);
        }
    }
    private void initReplicationState() {
        Collection<GroupMember> allGroupMember = nodeGroup.getAllGroupMember();
        for (GroupMember member : allGroupMember) {
            if(member.getNodeEndpoint().getNodeId().equals(currentNodeId)) continue;

            member.setReplicationState(new ReplicationState(log.getLastLogIndex(), 0));
        }
    }

    /**
     * 定时发送日志，即时没有新的日志也要发送空的日志作为心跳
     */
    private void logReplicationTask() {
        taskExecutor.submit(
                () -> {
                    doReplication(false);
                }, LOGGING_FUTURE_CALLBACK
        );
    }

    private void doReplication(boolean executeImmediately) {
        logger.debug("start replicating log, current node is {}, current term is {}", currentNodeId, currentRole.getCurrentTerm());
        //为每个节点都维护上一次复制日志的时间
        Collection<GroupMember> groupMembers = nodeGroup.getAllGroupMember();
        AppendEntriesMessage generalMessage = null, emptyMessage = null;
        for (GroupMember member : groupMembers) {
            //跳过自己
            if(member.getNodeEndpoint().getNodeId().equals(currentNodeId)) continue;

            if(executeImmediately || member.shouldReplication(logReplicationInterval)) {
                AppendEntriesMessage message;
                long nextIndex = member.getReplicationState().getNextIndex();
                long term = currentRole.getCurrentTerm();

                //如果该节点在线
                if(member.isInline()) {
                    //判断是否能复用generalMessage，也就是复用 给上一个节点发送的消息
                    if(generalMessage != null && generalMessage.getTerm() == term
                            && generalMessage.getPreLogIndex() == nextIndex - 1) {
                        message = generalMessage;
                    } else {
                        message = log.createAppendEntriesMessage(currentNodeId, term, nextIndex);
                        generalMessage = message;
                    }
                    //增加nextIndex,TODO:解决Follower附加日志失败的处理
                    member.getReplicationState().incNextIndex(message.getEntryList().size());

                } else { //若节点不在线则发送心跳包探测其存活状态
                    //判断是否能复用emptyMessage
                    if(emptyMessage != null && emptyMessage.getTerm() == term
                            && emptyMessage.getPreLogIndex() == nextIndex - 1) {
                        message = emptyMessage;
                    } else {
                        message = log.createEmptyAppendEntriesMessage(currentNodeId, term, nextIndex);
                        emptyMessage = message;
                    }
                }
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
        } else {
            logger.error("cannot find MessageHandler, role type is {}", roleClazz);
            return null;
        }
    }

    private void logReplicationReadTimeout() {
        taskExecutor.submit(
                () -> {
                    logger.debug("log replication read timeout");
                    //becomeToRole(new CandidateRole(currentNodeId, currentRole.getCurrentTerm()));
                    startElection();
                }
        );
    }

    private void startElection() {
        logger.debug("start election, new term is {}", currentRole.getCurrentTerm() + 1);

        //将角色更改为Candidate，term + 1
        becomeToRole(new CandidateRole(currentNodeId, currentRole.getCurrentTerm() + 1, currentNodeId));

        //向所有节点发送请求投票rpc
        RequestVoteMessage requestVoteMessage = log.createRequestVoteMessage(currentNodeId, currentRole.getCurrentTerm());
        rpcHandler.sendRequestVoteMessage(requestVoteMessage, getAllNodeEndpointSet());
    }
    private NodeEndpoint getNodeEndpoint(NodeId nodeId) {
        GroupMember groupMember = nodeGroup.getGroupMember(nodeId);
        if(groupMember == null) {
            logger.warn("NodeEndpoint of nodeId {} not exist", nodeId);
            throw new NetworkException("NodeEndpoint not exist");
        }
        return groupMember.getNodeEndpoint();
    }

    @Override
    public NodeEndpoint[] getAllNodeEndpoint() {
        Collection<GroupMember> allGroupMember = nodeGroup.getAllGroupMember();
        NodeEndpoint[] result = new NodeEndpoint[allGroupMember.size()];
        int idx = 0;
        for (GroupMember groupMember : allGroupMember) {
            result[idx] = groupMember.getNodeEndpoint();
            idx++;
        }
        return result;
    }

    private Set<NodeEndpoint> getAllNodeEndpointSet() {
        Collection<GroupMember> allGroupMember = nodeGroup.getAllGroupMember();
        Set<NodeEndpoint> set = new HashSet<>();
        for (GroupMember groupMember : allGroupMember) {
            set.add(groupMember.getNodeEndpoint());
        }
        return set;
    }
    private void electionTimeout() {
        taskExecutor.submit(
                () -> {

                    logger.debug("election timeout, current term is {}", currentRole.getCurrentTerm());
                    startElection();
                }, LOGGING_FUTURE_CALLBACK
        );
    }

    public void setCurrentRole(AbstractRole currentRole) {
        this.currentRole = currentRole;
    }

    public void setNodeGroup(NodeGroup nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setRpcHandler(RpcHandler rpcHandler) {
        this.rpcHandler = rpcHandler;
    }

    public void setTaskScheduler(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void setCurrentNodeId(NodeId currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public void setLog(Log log) {
        this.log = log;
    }

//    public void setConfig(ClusterConfig config) {
//        this.config = config;
//    }
    //    private void logReplicationTimeout() {
//        long currentTerm = currentRole.getCurrentTerm();
//        logger.info("logReplication timeout, current term is {}", currentTerm);
//        becomeToRole(new CandidateRole(currentRole.getNodeId(), currentTerm + 1));
//
//    }


    private class CandidateMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(CandidateMessageHandler.class);

        public CandidateMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler, taskExecutor);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message) {
            AppendEntriesMessage appendRequestMsg = message.getBody();
            long term = appendRequestMsg.getTerm();
            if(term >= currentRole.getCurrentTerm()) {
                logger.debug("receive AppendEntriesResultMessage, term is {}", term);
                //electionTimeoutFuture.cancel(true);

                becomeToRole(new FollowerRole(currentRole.getNodeId(), term, appendRequestMsg.getLeaderId()));

                List<Entry> entryList = appendRequestMsg.getLogEntryList();
                long preTerm = appendRequestMsg.getPreLogTerm();
                long preLogIndex = appendRequestMsg.getPreLogIndex();
                long leaderCommit = appendRequestMsg.getLeaderCommit();
                ReplicationState state = nodeGroup.getGroupMember(message.getNodeId()).getReplicationState();

                if (log.appendGeneralEntriesFromLeader(preTerm, preLogIndex, entryList, leaderCommit)) {
                    return new AppendEntriesResultMessage(term, true, entryList.size());
                }
            }
            logger.info("receive unexpect AppendEntriesResultMessage, currentTerm is {}, receive term is {}",
                    currentRole.getCurrentTerm(), term);
            return new AppendEntriesResultMessage(currentRole.getCurrentTerm(), false);
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message) {
            RequestVoteMessage requestVoteMessage = message.getBody();
            logger.debug("receive RequestVoteMessage, term is {}", requestVoteMessage.getTerm());
            long messageTerm = requestVoteMessage.getTerm();
            long currentTerm = currentRole.getCurrentTerm();


            long lastLogTerm = requestVoteMessage.getLastLogTerm();
            long lastLogIndex = requestVoteMessage.getLastLogIndex();
        if(messageTerm > currentTerm) {
                //boolean voteGranted = false;
                boolean voteGranted = !log.isNewerThan(lastLogTerm, lastLogIndex);
                NodeId voteFor = null;
                if(voteGranted) {
                    voteFor = requestVoteMessage.getCandidateId();
                }
                becomeToRole(new FollowerRole(currentRole.getNodeId(), messageTerm, voteFor));
                logger.debug("vote for {}", requestVoteMessage.getCandidateId());
                return new RequestVoteResultMessage(messageTerm, voteGranted);
            }
            return new RequestVoteResultMessage(currentRole.getCurrentTerm(), false);
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
                //变成Follower而不投票
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
                logger.info("current node {} become leader,current term is {}", currentNodeId, currentTerm);

                //TODO:初始化Leader需要维护的状态 replicationState，更新commitIndex
                //long nextIndex = log.getLastLogIndex();
                //nodeGroup.resetReplicationState(nextIndex);

                log.appendEmptyEntry(messageTerm);
                initReplicationState();

                becomeToRole(new LeaderRole(currentNodeId, messageTerm));
            }
        }

        @Override
        public void handleAppendEntriesResult(AbstractMessage<AppendEntriesResultMessage> message) {
            AppendEntriesResultMessage entriesResultMessage = message.getBody();
            logger.warn("receive AppendEntriesResultMessage, term is {}", entriesResultMessage.getTerm());
        }
    }

    /**
     * Follower处理各类消息的逻辑
     */
    class FollowerMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(FollowerMessageHandler.class);

        public FollowerMessageHandler(Logger logger) {
            super(logger, nodeGroup, rpcHandler, taskExecutor);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message) {
            AppendEntriesMessage appendRequestMsg = message.getBody();

            logger.debug("receive AppendEntriesRequestMessage, term is {}", appendRequestMsg.getTerm());

            long currentTerm = currentRole.getCurrentTerm();
            long term = appendRequestMsg.getTerm();
            if(currentTerm > term) {
                return new AppendEntriesResultMessage(currentTerm, false);
            }
            if(currentTerm < term) {
                //TODO：设置voteFor？
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term, appendRequestMsg.getLeaderId()));
            }

            List<Entry> entryList = appendRequestMsg.getLogEntryList();

            long preTerm = appendRequestMsg.getPreLogTerm();
            long preLogIndex = appendRequestMsg.getPreLogIndex();
            long leaderCommit = appendRequestMsg.getLeaderCommit();
            //long currentLogIndex = entryList.get(0).getIndex();

            //重设日志读取超时任务
            resetReplicationReadTimeoutTask();
            //Leader会定时发送AppendEntriesMessage，但是里面可能没有新的日志作为作为心跳消息
            GroupMember member = nodeGroup.getGroupMember(message.getNodeId());
            if(log.appendGeneralEntriesFromLeader(preTerm, preLogIndex, entryList, leaderCommit)) {
                return new AppendEntriesResultMessage(currentTerm, true, entryList.size());
            }
            return new AppendEntriesResultMessage(currentTerm, false);
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message) {
            RequestVoteMessage requestVoteMessage = message.getBody();
            logger.debug("receive RequestVoteMessage, term is {}", requestVoteMessage.getTerm());
            long messageTerm = requestVoteMessage.getTerm();
            long currentTerm = currentRole.getCurrentTerm();


            long lastLogTerm = requestVoteMessage.getLastLogTerm();
            long lastLogIndex = requestVoteMessage.getLastLogIndex();
            if(messageTerm > currentTerm) {
                //boolean voteGranted = false;
                boolean voteGranted = !log.isNewerThan(lastLogTerm, lastLogIndex);
                NodeId voteFor = null;
                if(voteGranted) {
                    voteFor = requestVoteMessage.getCandidateId();
                }
                becomeToRole(new FollowerRole(currentRole.getNodeId(), messageTerm, voteFor));
                logger.debug("vote for {}", requestVoteMessage.getCandidateId());
                return new RequestVoteResultMessage(messageTerm, voteGranted);
            } else if(messageTerm == currentTerm) {
                NodeId voteFor = currentRole.getVoteFor();
                if(voteFor == null && !log.isNewerThan(lastLogTerm, lastLogIndex)) {
                    becomeToRole(new FollowerRole(currentNodeId, messageTerm, requestVoteMessage.getCandidateId()));
                    return new RequestVoteResultMessage(messageTerm, true);
                }
            }
            return new RequestVoteResultMessage(currentRole.getCurrentTerm(), false);
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
            super(logger, nodeGroup, rpcHandler, taskExecutor);
        }

        @Override
        public AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message) {
            AppendEntriesMessage appendRequestMsg = message.getBody();

            logger.warn("receive AppendEntriesResultMessage, term is {}", appendRequestMsg.getTerm());
            long term = appendRequestMsg.getTerm();
            long currentTerm = currentRole.getCurrentTerm();
            if(term > currentTerm) {
                logger.info("detect new Leader, Leader's node id is {}. " +
                        "current role become Follower from Leader, new term is {}, old term is {}",
                        message.getNodeId(), term, currentTerm);
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term, appendRequestMsg.getLeaderId()));

                List<Entry> entryList = appendRequestMsg.getLogEntryList();
                long preTerm = appendRequestMsg.getPreLogTerm();
                long preLogIndex = appendRequestMsg.getPreLogIndex();
                long leaderCommit = appendRequestMsg.getLeaderCommit();

                if(log.appendGeneralEntriesFromLeader(preTerm, preLogIndex, entryList, leaderCommit)) {
                    return new AppendEntriesResultMessage(term, true, entryList.size());
                }
            }
            return new AppendEntriesResultMessage(currentTerm, false);
        }

        @Override
        public RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message) {
            RequestVoteMessage requestVoteMessage = message.getBody();
            logger.debug("receive RequestVoteMessage, term is {}", requestVoteMessage.getTerm());
            long messageTerm = requestVoteMessage.getTerm();
            long currentTerm = currentRole.getCurrentTerm();


            long lastLogTerm = requestVoteMessage.getLastLogTerm();
            long lastLogIndex = requestVoteMessage.getLastLogIndex();
            if(messageTerm > currentTerm) {
                //boolean voteGranted = false;
                boolean voteGranted = !log.isNewerThan(lastLogTerm, lastLogIndex);
                NodeId voteFor = null;
                if(voteGranted) {
                    voteFor = requestVoteMessage.getCandidateId();
                }
                becomeToRole(new FollowerRole(currentRole.getNodeId(), messageTerm, voteFor));
                logger.debug("vote for {}", requestVoteMessage.getCandidateId());
                return new RequestVoteResultMessage(messageTerm, voteGranted);
            }
            return new RequestVoteResultMessage(currentRole.getCurrentTerm(), false);
        }

        @Override
        public void handleRequestVoteResult(AbstractMessage<RequestVoteResultMessage> message) {
            logger.debug("receive RequestVoteResultMessage, but current node has become Leader, current term is {}",
                    currentRole.getCurrentTerm());
        }

        @Override
        public void handleAppendEntriesResult(AbstractMessage<AppendEntriesResultMessage> message) {
            AppendEntriesResultMessage entriesResultMessage = message.getBody();
            NodeId fromId = message.getNodeId();

            long term = entriesResultMessage.getTerm();
            boolean success = entriesResultMessage.isSuccess();

            if(success) {
                ReplicationState replicationState = nodeGroup.getGroupMember(fromId).getReplicationState();
                //增加nextIndex和matchIndex
                log.updateReplicationState(replicationState, entriesResultMessage.getLogNum());
                log.advanceCommitForLeader(currentRole.getCurrentTerm());
                logger.debug("node {} 's nextIndex is {}, matchIndex is {}", nodeGroup.getGroupMember(fromId),
                        replicationState.getNextIndex(), replicationState.getMatchIndex());
                return;
            }
            //fail
            logger.debug("receive AppendEntriesResultMessage: {}, but current role is Leader, current term is {}",
                    entriesResultMessage, currentRole.getCurrentTerm());

            if(term > currentRole.getCurrentTerm()) {
                logger.info("detect remote node's term {} > current term {}, become Follower from Leader, remote node is {}",
                        term, currentRole.getCurrentTerm(), message.getNodeId());
                becomeToRole(new FollowerRole(currentRole.getNodeId(), term, null));
                return;
            }

            //preLogTerm and preLogIndex 不匹配，减少 nextIndex 并重试
            GroupMember member = nodeGroup.getGroupMember(fromId);
            try {
                //TODO：考虑二分查找进行优化，但需要修改AppendEntriesResultMessage消息格式
                member.getReplicationState().decNextIndex(1);
                logger.debug("decrease nextIndex of node {}, current nextIndex is {}",
                        member.getNodeEndpoint(), member.getReplicationState().getNextIndex());
            } catch (IndexException exception) {
                logger.error("the nextIndex of node {} must be greater than 0", fromId);
            }
            //完成Leader的日志复制以及心跳消息。定时任务，通过记录每次的发送时间来判断是否要发送log消息
        }
    }
}
