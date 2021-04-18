package config;

import java.io.IOException;
import java.io.InputStream;

public interface ConfigLoader {
    ClusterConfig load(InputStream inputStream) throws IOException;
}
