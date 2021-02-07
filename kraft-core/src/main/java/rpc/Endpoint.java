package rpc;

public class Endpoint {
    private final String ipAddress;
    private final int port;


    public Endpoint(String addr, int p) {
        ipAddress = addr;
        port = p;
    }

    public static Endpoint creatEndpoint(String addr, int port) {
        return new Endpoint(addr, port);
    }
}
