package server;

import common.message.Connection;
import common.message.DelCommand;
import common.message.GetCommand;
import common.message.SetCommand;
import election.node.Node;
import election.statemachine.StateMachine;
import rpc.NodeEndpoint;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KVStore {
    /*
    Node需要提供的接口：
    1. 判断当前节点是否为Leader，决定是否要重定向到Leader
    2. 获取Leader的地址信息，返回客户端进行重定向

     */
    private Node node;
    private Map<String, String> storeMap = new HashMap<>();
    private Map<String, Connection> connectorMap = new ConcurrentHashMap<>();

    private void redirectOrFail(Connection connection) {
        NodeEndpoint leaderNodeEndpoint = node.getLeaderNodeEndpoint();
        if(leaderNodeEndpoint != null) {
//            connection.reply();
        }
        //Leader不存在

    }

    //处理get命令
    public void handleGetCommand(Connection<GetCommand> connection) {
        /*
        1. 判断当前节点是否是Leader，如果是则从Map中获取数据返回，否则
        2. 获取Leader的地址信息返回重定向消息，如果不存在Leader则返回错误
         */
        if(!node.isLeader()) {
            redirectOrFail(connection);
            return;
        }


    }

    //处理set命令
    public void handleSetCommand(Connection<SetCommand> connection) {

    }

    //处理del命令
    public void handleDelCommand(Connection<DelCommand> connection) {

    }

    private void doGet(GetCommand getCommand) {

    }
    private void doSet(SetCommand setCommand) {

    }
    private void doDel(DelCommand delCommand) {

    }

    private class DefaultStateMachine implements StateMachine {
        @Override
        public boolean apply(int[] command) {

            return false;
        }

    }

}
