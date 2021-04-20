package server.config;

import java.io.IOException;
import java.io.InputStream;

public interface ConfigLoader {
    ServerConfig load(InputStream inputStream) throws IOException;
}
