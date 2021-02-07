package election;

import election.role.AbstractRole;
import rpc.Endpoint;

import java.util.Map;

public class NodeImpl implements Node {
    //当前角色：LeaderRole、CandidateRole或FollowerRole之一
    private AbstractRole currentRole;
    //所有节点的信息
    private Map<NodeId, Endpoint> nodesMap;



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
        
    }

    @Override
    public void stop() {

    }

    @Override
    public void apply() {

    }
}
