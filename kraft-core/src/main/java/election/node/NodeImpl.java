package election.node;

import election.handler.*;
import election.role.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.RpcHandler;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;
import schedule.TaskScheduleExecutor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NodeImpl implements Node {
    private static final Logger logger = LoggerFactory.getLogger(NodeImpl.class);
    //当前角色：LeaderRole、CandidateRole或FollowerRole之一
    private AbstractRole currentRole;
    //所有节点信息，包括地址、matchIndex、nextIndex等
    private NodeGroup nodeGroup;

    private RpcHandler rpcHandler;

    private TaskScheduleExecutor scheduleExecutor;
    //TODO：配置类
    private long electionTimeout;
    private long heartBeatTimeout;




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
        becomeToRole(new FollowerRole(null, RoleType.FOLLOWER, 0, null));


    }

    @Override
    public void stop() {

    }

    @Override
    public void apply() {

    }

    //TODO:becomeToRole
    private void becomeToRole(AbstractRole role) {
        currentRole = role;
    }

    private void startElection() {
        logger.debug("start election, new term is {}", currentRole.getCurrentTerm() + 1);

        //将角色更改为Candidate，term + 1
        becomeToRole(new CandidateRole(currentRole.getCurrentTerm() + 1));
        //提交选举超时任务
        ScheduledFuture<?> future = scheduleExecutor.schedule(this::electionTimeout, electionTimeout,
                TimeUnit.MILLISECONDS);
        //向所有节点发送请求投票rpc
        //TODO：设置lastLogIndex、lastLogTerm
        rpcHandler.sendRequestVoteMessage(currentRole.getCurrentTerm(), currentRole.getNodeId(), 0, 0);


    }
    private void electionTimeout() {
        logger.debug("election timeout, current term is {}", currentRole.getCurrentTerm());
        startElection();
    }

    private class CandidateMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(election.handler.CandidateMessageHandler.class);

        public CandidateMessageHandler(Logger logger) {
            super(logger);
        }

        @Override
        public void handleAppendEntriesRequest(AppendEntriesResultMessage appendRequestMsg) {
            long term = appendRequestMsg.getTerm();
            if(term > currentRole.getCurrentTerm()) {
                logger.debug("receive AppendEntriesResultMessage, term is {}", term);
                //TODO：取消选举超时任务

                becomeToRole(new FollowerRole(term));
                return;
            }
            logger.info("receive unexpect AppendEntriesResultMessage, currentTerm is {}, receive term is {}",
                    currentRole.getCurrentTerm(), term);

        }

        @Override
        public void handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
            long term = requestVoteMessage.getTerm();
            long currentTerm = currentRole.getCurrentTerm();
            //如果requestVoteMessage.term < currentTerm，不投票，并返回currentTerm
            if(term < currentTerm) {
                rpcHandler.sendRequestVoteResultMessage(currentTerm, false);
            //如果如果requestVoteMessage.term == currentTerm，不投票，因为票已经投给了自己
            } else if(term == currentTerm) {
                rpcHandler.sendRequestVoteResultMessage(currentTerm, false);
                //如果requestVoteMessage.term > currentTerm，如果自己的日志更加新则不投票，否则投票。变成Follower
            } else {
                //TODO:添加实现
                //为进行测试，默认不投票
                becomeToRole(new FollowerRole(currentTerm));
                logger.debug("voteFor {}", requestVoteMessage.getCandidateId());
                rpcHandler.sendRequestVoteResultMessage(currentTerm, false);
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
                becomeToRole(new FollowerRole(term));
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
                becomeToRole(new LeaderRole(term + 1));
                //TODO：取消选举超时任务
                logger.info("current node become leader, term is {}", currentRole.getCurrentTerm());
                //TODO:发送空的AppendEntries消息
                rpcHandler.sendAppendEntriesMessage(currentRole.getCurrentTerm(), currentRole.getNodeId(),
                        0, 0, null, 0);

            }

        }

        @Override
        public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage) {
            logger.warn("receive AppendEntriesResultMessage, term is {}", appendEntriesResultMessage.getTerm());
        }
    }

    class FollowerMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(election.handler.FollowerMessageHandler.class);


        public FollowerMessageHandler(Logger logger) {
            super(logger);
        }

        @Override
        public void handleAppendEntriesRequest(AppendEntriesResultMessage appendRequestMsg) {
            logger.debug("receive AppendEntriesResultMessage, term is {}", appendRequestMsg.getTerm());
        }

        @Override
        public void handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
            long term = requestVoteMessage.getTerm();
            if(term <= currentRole.getCurrentTerm()) {
                logger.info("receive RequestVoteMessage, receive term is {}, but currentTerm is {}",
                        term, currentRole.getCurrentTerm());
                return;
            }
            //默认进行投票
            rpcHandler.sendRequestVoteResultMessage(term, true);
        }

        @Override
        public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {
            logger.warn("receive illegal RequestVoteResultMessage, current role is Follower");
        }

        @Override
        public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage) {
            logger.warn("receive illegal RequestVoteMessage, current role is Follower");
        }
    }

    class LeaderMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
        private final Logger logger = LoggerFactory.getLogger(election.handler.LeaderMessageHandler.class);

        public LeaderMessageHandler(Logger logger) {
            super(logger);
        }

        @Override
        public void handleAppendEntriesRequest(AppendEntriesResultMessage appendRequestMsg) {
            logger.warn("receive AppendEntriesResultMessage, term is {}", appendRequestMsg.getTerm());
            long term = appendRequestMsg.getTerm();
            if(term > currentRole.getCurrentTerm()) {
                logger.info("become Follower from Leader");
                //TODO:设置选举超时任务
                becomeToRole(new FollowerRole(term));
                logger.info("begin to commit");
                return;
            }
        }

        @Override
        public void handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
            logger.warn("receive AppendEntriesResultMessage, term is {}", requestVoteMessage.getTerm());
            long term = requestVoteMessage.getTerm();
            if(term > currentRole.getCurrentTerm()) {
                logger.info("become Follower from Leader");
                //TODO:设置选举超时任务
                becomeToRole(new FollowerRole(term));
                return;
            }
        }

        @Override
        public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {
            logger.info("receive RequestVoteResultMessage, but current node has become Leader");
        }

        @Override
        public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage) {
            logger.debug("receive AppendEntriesResultMessage");
        }
    }
}
