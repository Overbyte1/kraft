package client;

public interface Client {
    String VERSION = "0.0.1";
    boolean set(String key, byte[] value);
    boolean mset(String[] keys, byte[][] value);
    boolean del(String key);
    boolean mdel(String[] keys);
    byte[]  get(String key);
    byte[][] mget(String[] keys);
}
