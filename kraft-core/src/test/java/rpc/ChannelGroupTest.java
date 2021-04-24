package rpc;

import election.node.NodeGroup;
import election.node.NodeId;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChannelGroupTest {

    @Test
    public void getChannel() {
        ChannelGroup channelGroup = new ChannelGroup(new NodeGroup());
        NodeId nodeId = new NodeId("A");
        NioChannel nioChannel = new NioChannel(new NioSocketChannel());
        channelGroup.addChannel(nodeId, nioChannel);
        assertEquals(nodeId, channelGroup.getNodeId(nioChannel));
        assertEquals(nioChannel, channelGroup.getChannel(nodeId));
    }

}