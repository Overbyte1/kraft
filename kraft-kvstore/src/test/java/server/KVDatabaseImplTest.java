package server;

import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import common.message.*;
import common.message.command.DelCommand;
import common.message.command.GetCommand;
import common.message.command.SetCommand;
import election.node.NodeId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rpc.Endpoint;
import rpc.NodeEndpoint;
import server.store.MemHTKVStore;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;

public class KVDatabaseImplTest {
    private NodeMock node;
    private KVDatabase kvDatabase;
    private Channel channel;
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
    private TestHandle testHandle = new TestHandle(cyclicBarrier);
    @Before
    public void init() {
        node = new NodeMock();
        kvDatabase = new KVDatabaseImpl(node, new MemHTKVStore());
        kvDatabase.start();
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
    @After
    public void destroy() throws InterruptedException {
        channel.close().sync();
    }


    @Test
    public void testHandleGetCommand() throws InterruptedException, BrokenBarrierException {

        GetCommand command = new GetCommand("kk");
        channel.writeAndFlush(command);
        cyclicBarrier.await();
        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<GeneralResult> response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);

        cyclicBarrier.reset();
    }
    @Test
    public void testHandleSetCommand() throws InterruptedException, BrokenBarrierException {
        String key = "kk", value = "xx";
        SetCommand command = new SetCommand(key, value.getBytes());
        channel.writeAndFlush(command);
        cyclicBarrier.await();
        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<GeneralResult> response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);

        GetCommand getCommand = new GetCommand( "kk");
        channel.writeAndFlush(getCommand);
        cyclicBarrier.await();
        receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, value.getBytes()));
        assertEquals(response, receiveMessage);


        cyclicBarrier.reset();
    }

    @Test
    public void testHandleDelCommand() throws InterruptedException, BrokenBarrierException {
        testHandleSetCommand();
        String key = "kk", value = "xx";
        DelCommand delCommand = new DelCommand(key);
        channel.writeAndFlush(delCommand);

        cyclicBarrier.await();

        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<GeneralResult> response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);

        GetCommand getCommand = new GetCommand("kk");
        channel.writeAndFlush(getCommand);
        cyclicBarrier.await();

        receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        response = new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);

        cyclicBarrier.reset();
    }
    @Test
    public void testRedirect() throws InterruptedException, BrokenBarrierException {
        node.setLeader(false);
        NodeEndpoint nodeEndpoint = new NodeEndpoint(new NodeId("A"), new Endpoint("localhost", 8848));
        node.setNodeEndpoint(nodeEndpoint);

        RedirectResult result = new RedirectResult(nodeEndpoint);

        GetCommand command = new GetCommand("kk");
        channel.writeAndFlush(command);
        cyclicBarrier.await();
        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        assertEquals(true, ((Response)receiveMessage).getBody() instanceof RedirectResult);
        RedirectResult redirectResult =  (RedirectResult) ((Response)receiveMessage).getBody();
        assertEquals(result, redirectResult);

        cyclicBarrier.reset();
    }
}
class TestHandle extends ChannelInboundHandlerAdapter {
    private Object receiveMessage;
    private CyclicBarrier cyclicBarrier;

    public TestHandle(CyclicBarrier cyclicBarrier) {
        this.cyclicBarrier = cyclicBarrier;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("client receive message: " + msg);
        receiveMessage = msg;
        cyclicBarrier.await();
        super.channelRead(ctx, msg);
    }

    public Object getReceiveMessage() {
        return receiveMessage;
    }
    public void resetMsg() {
        receiveMessage = null;
    }
}