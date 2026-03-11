package team8.catan.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextResourceLoader {
    private TextResourceLoader() {
    }

    public static String load(Path resolvedPath, String configuredPath, Class<?> anchor) throws IOException {
        if (Files.exists(resolvedPath)) {
            return Files.readString(resolvedPath);
        }

        String configuredResourcePath = configuredPath.replace('\\', '/');
        InputStream configuredResource = anchor.getClassLoader().getResourceAsStream(configuredResourcePath);
        if (configuredResource != null) {
            try (InputStream in = configuredResource) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        String resolvedResourcePath = resolvedPath.toString().replace('\\', '/');
        InputStream resolvedResource = anchor.getClassLoader().getResourceAsStream(resolvedResourcePath);
        if (resolvedResource != null) {
            try (InputStream in = resolvedResource) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        throw new java.nio.file.NoSuchFileException(
            "Could not locate file at '" + resolvedPath + "' or classpath resource '" + configuredPath + "'"
        );
    }
}
