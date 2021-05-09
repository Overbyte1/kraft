package tools;

import client.SocketChannelImpl;
import common.message.command.GetCommand;
import common.message.command.SetCommand;
import common.message.response.Response;
import common.message.response.ResponseType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Benchmark {
    private static StringBuilder sb =  new StringBuilder();
    private static Random random = new Random();
    private static String[] keys = new String[100000];
    private static int idx = 0;
    public static void main(String[] args) throws IOException {

        String key = "test_key", value = "test_value";
        String ip = "localhost";
        int port = 8101;
        //TODO:fix bugLeader发送给Follower的日志复制消息还已经被附加，但还未收到响应，此时客户端又来了请求，Leader发送给Follower旧日志导致日志附加失败

        int testNum =100000;
//        System.out.println(getRandomKey(8));
//        System.out.println(getRandomKey(8));
//        System.out.println(idx);
        //System.out.println(Arrays.toString(getRandomValue(8)));
        SocketChannelImpl socketChannel = new SocketChannelImpl();
        testSet(key, value, testNum, ip, port, socketChannel);
        testRead(key, testNum, ip, port, socketChannel);

    }
    public static void testRead(String key,int testNum, String ip, int port, SocketChannelImpl socketChannel) {
        System.out.println("start read test");
        //SocketChannelImpl socketChannel = new SocketChannelImpl();
        GetCommand command = new GetCommand(key);
        socketChannel.send(ip, port, command);
        long start = System.currentTimeMillis();
        for(int i = 1; i <= testNum; i++) {
            //command = new GetCommand(keys[random.nextInt(keys.length)]);
            command.setKey(keys[random.nextInt(keys.length)]);
            //System.out.println(command);
            Response<?> msg = (Response<?>)socketChannel.send(ip, port, command);
            //System.out.println(msg.getBody());
            if(msg.getType() == ResponseType.SUCCEED) {
                //System.out.println(msg.getBody());
                if(i % 10000 == 0) {
                    System.out.println(i + " " + (System.currentTimeMillis() - start));
                }
            } else {
                System.out.println("error: " + msg);
                return;
            }
        }
    }
    public static void testSet(String key, String val, int testNum, String ip, int port, SocketChannelImpl socketChannel) {
        System.out.println("start set test");
        //SocketChannelImpl socketChannel = new SocketChannelImpl();
        SetCommand command = new SetCommand(key, val.getBytes());
        socketChannel.send(ip, port, command);
        long start = System.currentTimeMillis();
        for(int i = 1; i <= testNum; i++) {
            //command = new SetCommand(getRandomKey(8), getRandomValue(8));
            command.setKey(getRandomKey(8));
            command.setValue(getRandomValue(8));
            //System.out.println(command);
            Response<?> msg = (Response<?>)socketChannel.send(ip, port, command);
            //System.out.println(msg.getBody());
            if(msg.getType() == ResponseType.SUCCEED) {
                if(i % 10000 == 0) {
                    System.out.println(i + " " + (System.currentTimeMillis() - start));

                }
            } else {
                System.out.println("error: " + msg);
                return;
            }
        }
    }
    public static String getRandomKey(int n) {
        if(sb.length() > 0) {
            sb.delete(0, sb.length());
        }
        for(int i = 0; i < n; i++) {
            sb.append(random.nextInt(10));
        }
        String key = sb.toString();
        keys[idx++] = key;
        return key;
    }
    public static byte[] getRandomValue(int n) {
        byte[] bytes = new byte[n];
        for(int i = 0; i < n; i++) {
            bytes[i] = (byte)random.nextInt(127);
        }
        return bytes;
    }

}
