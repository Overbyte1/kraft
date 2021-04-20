package server;

import client.handler.LeaderHandler;
import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import common.message.command.*;
import common.message.response.*;
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
import server.config.ServerConfigLoader;
import server.handler.*;
import server.store.KVStore;
import server.store.MemHTKVStore;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.assertEquals;

public class KVDatabaseImplTest {
    private NodeMock node;
    private KVDatabase kvDatabase;
    private Channel channel;
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
    private TestHandle testHandle = new TestHandle(cyclicBarrier);
    private KVStore kvStore = new MemHTKVStore();
    @Before
    public void init() throws IOException {
        node = new NodeMock();
        kvDatabase = new KVDatabaseImpl(node, new ServerConfigLoader().load(null));
        kvDatabase.start();
        kvDatabase.registerCommandHandler(GetCommand.class, new GetCommandHandler(kvStore));
        kvDatabase.registerCommandHandler(SetCommand.class, new SetCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(DelCommand.class, new DelCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MDelCommand.class, new MDelCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MSetCommand.class, new MSetCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MGetCommand.class, new MGetCommandHandler(kvStore));
        kvDatabase.registerCommandHandler(LeaderCommand.class, new LeaderCommandHandler(node));
        kvDatabase.registerCommandHandler(ServerListCommand.class, new ServerListCommandHandler(node));

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
        Response<SinglePayloadResult> response = new Response(ResponseType.SUCCEED, new SinglePayloadResult(StatusCode.SUCCEED_OK, null));
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
        Response<SinglePayloadResult> response = new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
        assertEquals(response, receiveMessage);

        GetCommand getCommand = new GetCommand( "kk");
        channel.writeAndFlush(getCommand);
        cyclicBarrier.await();
        receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        response = new Response(ResponseType.SUCCEED, new SinglePayloadResult(StatusCode.SUCCEED_OK, value.getBytes()));
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
        Response<SinglePayloadResult> response = new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
        assertEquals(response, receiveMessage);

        GetCommand getCommand = new GetCommand("kk");
        channel.writeAndFlush(getCommand);
        cyclicBarrier.await();

        receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        response = new Response(ResponseType.SUCCEED, new SinglePayloadResult(StatusCode.SUCCEED_OK, null));
        assertEquals(response, receiveMessage);

        cyclicBarrier.reset();
    }
    @Test
    public void testMSetCommand() throws BrokenBarrierException, InterruptedException {
        String[] keys = new String[]{"a", "b", "c", "d"};
        String[] values = new String[]{"11", "22", "33", "44"};
        byte[][] bytes = new byte[values.length][];
        for(int i = 0; i < values.length; i++) {
            bytes[i] = values[i].getBytes();
        }
        MSetCommand command = new MSetCommand(keys, bytes);
        channel.writeAndFlush(command);
        cyclicBarrier.await();

        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<SinglePayloadResult> response = new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
        assertEquals(response, receiveMessage);

        MGetCommand mGetCommand = new MGetCommand(keys);

        //GetCommand getCommand = new GetCommand("a");
        channel.writeAndFlush(mGetCommand);
        cyclicBarrier.await();

        receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        response = new Response(ResponseType.SUCCEED, new MultiPayloadResult(StatusCode.SUCCEED_OK, bytes));
        assertEquals(response, receiveMessage);

        cyclicBarrier.reset();

    }
    @Test
    public void testMDelCommand() throws BrokenBarrierException, InterruptedException {
        testMSetCommand();
        String[] keys = new String[]{"a", "b", "c", "d"};
        MDelCommand mDelCommand = new MDelCommand(keys);
        channel.writeAndFlush(mDelCommand);

        cyclicBarrier.await();

        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        Response<SinglePayloadResult> response = new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
        assertEquals(response, receiveMessage);


        for(int i = 0; i < keys.length; i++) {
            GetCommand getCommand = new GetCommand(keys[i]);
            channel.writeAndFlush(getCommand);
            cyclicBarrier.await();
            receiveMessage = testHandle.getReceiveMessage();
            assertEquals(new Response<SinglePayloadResult>(ResponseType.SUCCEED, new SinglePayloadResult(StatusCode.SUCCEED_OK, null)), receiveMessage);

        }

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
    @Test
    public void testLeaderCommand() throws BrokenBarrierException, InterruptedException {
        NodeEndpoint nodeEndpoint = new NodeEndpoint(new NodeId("A"), new Endpoint("localhost", 8848));
        node.setNodeEndpoint(nodeEndpoint);
        channel.writeAndFlush(new LeaderCommand());
        cyclicBarrier.await();

        Object receiveMessage = testHandle.getReceiveMessage();
        assertEquals(true, receiveMessage instanceof Response);
        assertEquals(true, ((Response)receiveMessage).getBody() instanceof RedirectResult);
        assertEquals(nodeEndpoint, ((Response) receiveMessage).getBody());
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