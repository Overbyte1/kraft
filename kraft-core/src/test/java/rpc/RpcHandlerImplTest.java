package rpc;

import election.handler.MessageHandler;
import election.node.GroupMember;
import election.node.NodeGroup;
import election.node.NodeId;
import election.node.ReplicationState;
import junit.framework.TestCase;
import rpc.handler.ServiceInboundHandler;
import rpc.message.AbstractMessage;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;

import java.util.ArrayList;
import java.util.List;

public class RpcHandlerImplTest extends TestCase {
    boolean res = false;
    private NodeGroup initNodeGroup() {
        NodeGroup nodeGroup = new NodeGroup();
        NodeId nodeId1 = new NodeId("B");
        GroupMember member1 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId1, new Endpoint("localhost", 8091)));
        NodeId nodeId2 = new NodeId("C");
        GroupMember member2 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId2, new Endpoint("localhost", 8092)));
        NodeId nodeId3 = new NodeId("D");
        GroupMember member3 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId3, new Endpoint("localhost", 8093)));
        NodeId nodeId4 = new NodeId("E");
        GroupMember member4 = new GroupMember(new ReplicationState(0, 0),
                new NodeEndpoint(nodeId4, new Endpoint("localhost", 8094)));

        nodeGroup.addGroupMember(nodeId1, member1);
        nodeGroup.addGroupMember(nodeId2, member2);
        nodeGroup.addGroupMember(nodeId3, member3);
        nodeGroup.addGroupMember(nodeId4, member4);
        return nodeGroup;
    }

    public void testInitialize() {
        NodeId nodeId = new NodeId("A");
        int port = 8887;
        ChannelGroup channelGroup = new ChannelGroup(nodeId);
        RpcHandlerImpl rpcHandler = new RpcHandlerImpl(channelGroup, port);
        rpcHandler.initialize();

        assertEquals(port, rpcHandler.getPort());
        assertNotNull(rpcHandler.getWorkerGroup());
        assertNotNull(rpcHandler.getBossGroup());
        assertEquals(channelGroup, rpcHandler.getChannelGroup());
    }

    public void testSendRequestVoteMessage() throws InterruptedException {
        NodeId selfId = new NodeId("A");
        NodeId otherNodeId = new NodeId("B");

        int selfPort = 8888, otherPort = 8889;

        ChannelGroup selfChannelGroup = new ChannelGroup(selfId);
        ChannelGroup otherChannelGroup = new ChannelGroup(otherNodeId);

        RpcHandlerImpl selfRpcHandler = new RpcHandlerImpl(selfChannelGroup, selfPort);
        RpcHandlerImpl otherRpcHandler = new RpcHandlerImpl(otherChannelGroup, otherPort);

        RequestVoteMessage sendMessage = new RequestVoteMessage();

        //初始化
        selfRpcHandler.initialize();
        otherRpcHandler.initialize();

        Object lockObj = new Object();

        ServiceInboundHandler.getInstance().registerHandler(RequestVoteMessage.class, new MessageHandler() {
            @Override
            public void handle(Object message) {
                if(message instanceof NodeId) {
                    assertEquals(selfId, message);
                    return;
                }
                synchronized (lockObj) {
                    Object receiveMsg = ((AbstractMessage) message).getBody();
                    res = sendMessage.equals(receiveMsg);
                    lockObj.notifyAll();
                }
            }
        });

        NodeEndpoint nodeEndpoint = new NodeEndpoint(otherNodeId, new Endpoint("localhost", otherPort));
        List<NodeEndpoint> list = new ArrayList<>();
        list.add(nodeEndpoint);
        //发送消息
        selfRpcHandler.sendRequestVoteMessage(sendMessage, list);
        synchronized (lockObj) {
            lockObj.wait(10000);
            assertEquals(true, res);
            res = false;
        }

    }

    public void testSendAppendEntriesMessage() throws InterruptedException {
        NodeId selfId = new NodeId("A");
        NodeId otherNodeId = new NodeId("B");

        int selfPort = 8890, otherPort = 8891;

        ChannelGroup selfChannelGroup = new ChannelGroup(selfId);
        ChannelGroup otherChannelGroup = new ChannelGroup(otherNodeId);

        RpcHandlerImpl selfRpcHandler = new RpcHandlerImpl(selfChannelGroup, selfPort);
        RpcHandlerImpl otherRpcHandler = new RpcHandlerImpl(otherChannelGroup, otherPort);

        //RequestVoteMessage sendMessage = new RequestVoteMessage();
        AppendEntriesMessage sendMessage = new AppendEntriesMessage(0, selfId, 0,
                0, 0, new ArrayList<>());

        //初始化
        selfRpcHandler.initialize();
        otherRpcHandler.initialize();

        Object lockObj = new Object();

        ServiceInboundHandler.getInstance().registerHandler(AppendEntriesMessage.class, new MessageHandler() {
            @Override
            public void handle(Object message) {
                if(message instanceof NodeId) {
                    assertEquals(selfId, message);
                    return;
                }
                synchronized (lockObj) {
                    Object receiveMsg = ((AbstractMessage) message).getBody();
                    res = sendMessage.equals(receiveMsg);
                    lockObj.notifyAll();
                }
            }
        });

        NodeEndpoint nodeEndpoint = new NodeEndpoint(otherNodeId, new Endpoint("localhost", otherPort));
        //发送消息
        selfRpcHandler.sendAppendEntriesMessage(sendMessage, nodeEndpoint);
        synchronized (lockObj) {
            lockObj.wait(10000);
            assertEquals(true, res);
        }
    }

    public void testTestSendAppendEntriesMessage() {

    }

    public void testSendRequestVoteResultMessage() {
    }

    public void testSendAppendEntriesResultMessage() {
    }
}