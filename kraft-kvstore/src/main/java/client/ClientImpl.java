package client;

public class ClientImpl implements Client {

    @Override
    public boolean set(String key) {
        return false;
    }

    @Override
    public boolean mset(String[] keys) {
        return false;
    }

    @Override
    public boolean del(String key) {
        return false;
    }

    @Override
    public boolean mdel(String[] keys) {
        return false;
    }

    @Override
    public byte[] get(String key) {
        return new byte[0];
    }

    @Override
    public byte[][] mget(String[] keys) {
        return new byte[0][];
    }
}
