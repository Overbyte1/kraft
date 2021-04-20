package client.config;

import java.io.IOException;
import java.io.InputStream;

public interface ConfigLoader {
    ClientConfig load(InputStream inputStream) throws IOException;
}
