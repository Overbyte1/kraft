package client;

public interface Client {
    boolean set(String key);
    boolean mset(String[] keys);
    boolean del(String key);
    boolean mdel(String[] keys);
    byte[]  get(String key);
    byte[][] mget(String[] keys);
}
