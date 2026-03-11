package team8.catan.io;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathResolver {
    public Path resolveInputPath(Path configDirectory, String configuredPath) {
        Path rawPath = Path.of(configuredPath);
        if (rawPath.isAbsolute()) {
            return rawPath.normalize();
        }

        Path fromConfigDir = configDirectory.resolve(rawPath).normalize();
        if (Files.exists(fromConfigDir)) {
            return fromConfigDir;
        }

        Path fromWorkingDir = rawPath.normalize();
        if (Files.exists(fromWorkingDir)) {
            return fromWorkingDir;
        }

        return fromConfigDir;
    }

    public Path resolveOutputPath(Path configDirectory, String configuredPath) {
        Path rawPath = Path.of(configuredPath);
        if (rawPath.isAbsolute()) {
            return rawPath.normalize();
        }

        Path fromWorkingDir = rawPath.normalize();
        Path workingParent = fromWorkingDir.getParent();
        if (workingParent == null || Files.exists(workingParent)) {
            return fromWorkingDir;
        }

        if (Files.isDirectory(configDirectory)) {
            return configDirectory.resolve(rawPath).normalize();
        }

        return fromWorkingDir;
    }
}
