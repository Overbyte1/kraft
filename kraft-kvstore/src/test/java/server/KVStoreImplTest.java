package server;

import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import common.message.*;
import election.node.Node;
import election.node.NodeId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class KVStoreImplTest  {
    private NodeMock node;
    private KVStore kvStore;
    private Channel channel;
    private TestHandle testHandle = new TestHandle();
    @Before
    public void init() {
        node = new NodeMock();
        kvStore = new KVStoreImpl(node);
        kvStore.start();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress("localhost", 8848)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder())
                                .addLast(new FrameEncoder())
                                .addLast(new ProtocolDecoder())
                                .addLast(new ProtocolEncoder())
                                .addLast(testHandle)
                                .addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect().sync();
            System.out.println("connect established");
            channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testHandleGetCommand() throws InterruptedException {

        GetCommand command = new GetCommand(UUID.randomUUID().toString(), "kk");
        channel.writeAndFlush(command);
        Thread.sleep(1000);
        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<GeneralResult> response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);
        Thread.sleep(10000);
    }
    @Test
    public void testHandleSetCommand() throws InterruptedException {
        String key = "kk", value = "xx";
        SetCommand command = new SetCommand(UUID.randomUUID().toString(), key, value.getBytes());
        channel.writeAndFlush(command);
        Thread.sleep(1000);
        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<GeneralResult> response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);

        GetCommand getCommand = new GetCommand(UUID.randomUUID().toString(), "kk");
        channel.writeAndFlush(getCommand);
        Thread.sleep(1000);
        receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, value.getBytes()));
        assertEquals(response, receiveMessage);
        Thread.sleep(3000);
    }

    @Test
    public void testHandleDelCommand() throws InterruptedException {
        testHandleSetCommand();
        String key = "kk", value = "xx";
        DelCommand delCommand = new DelCommand(UUID.randomUUID().toString(), key);
        channel.writeAndFlush(delCommand);
        Thread.sleep(1000);
        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<GeneralResult> response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);

        GetCommand getCommand = new GetCommand(UUID.randomUUID().toString(), "kk");
        channel.writeAndFlush(getCommand);
        Thread.sleep(1000);
        receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);
        Thread.sleep(3000);
    }
    @Test
    public void testRedirect() throws InterruptedException {
        node.setLeader(false);
        NodeEndpoint nodeEndpoint = new NodeEndpoint(new NodeId("A"), new Endpoint("localhost", 8848));
        node.setNodeEndpoint(nodeEndpoint);

        RedirectResult result = new RedirectResult(nodeEndpoint);

        GetCommand command = new GetCommand(UUID.randomUUID().toString(), "kk");
        channel.writeAndFlush(command);
        Thread.sleep(1000);
        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        assertEquals(true, ((Response)receiveMessage).getBody() instanceof RedirectResult);
        RedirectResult redirectResult =  (RedirectResult) ((Response)receiveMessage).getBody();
        assertEquals(result, redirectResult);
    }
}
class TestHandle extends ChannelInboundHandlerAdapter {
    private Object receiveMessage;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client receive message: " + msg);
        receiveMessage = msg;
        super.channelRead(ctx, msg);
    }

    public Object getReceiveMessage() {
        return receiveMessage;
    }
    public void resetMsg() {
        receiveMessage = null;
    }
}