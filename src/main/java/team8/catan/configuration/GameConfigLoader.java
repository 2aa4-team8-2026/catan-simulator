package configuration;

import java.io.IOException;
import java.nio.file.Path;

// todo: add javadoc
public abstract class GameConfigLoader {
    public abstract GameConfig load(Path path) throws IOException;
}
