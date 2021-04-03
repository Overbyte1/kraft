package rpc;

import java.io.Serializable;

public class Endpoint implements Serializable {
    private final String ipAddress;
    private final int port;


    public Endpoint(String addr, int p) {
        ipAddress = addr;
        port = p;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public static Endpoint creatEndpoint(String addr, int port) {
        return new Endpoint(addr, port);
    }

    @Override
    public String toString() {
        return "Endpoint{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Endpoint endpoint = (Endpoint) o;

        if (port != endpoint.port) return false;
        return ipAddress != null ? ipAddress.equals(endpoint.ipAddress) : endpoint.ipAddress == null;
    }

    @Override
    public int hashCode() {
        int result = ipAddress != null ? ipAddress.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }
}
