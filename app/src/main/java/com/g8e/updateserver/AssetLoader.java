package com.g8e.updateserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssetLoader {

    public List<Asset> getAssets(String directoryPath) throws IOException, URISyntaxException {
        List<Asset> assets = new ArrayList<>();
        URL resourceUrl = this.getClass().getResource(directoryPath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + directoryPath);
        }

        // Check if the resource is inside a JAR file
        if ("jar".equals(resourceUrl.getProtocol())) {
            // Resource is inside a JAR file, handle it with JarFile or ZipFileSystem
            String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));

            // Fix for the issue: Ensure URI is correct and use FileSystems to access the
            // JAR
            Path jarFilePath = Paths.get(new URI("jar:file:" + jarPath)); // Ensure the "jar:file:" prefix is included

            try (FileSystem fs = FileSystems.newFileSystem(jarFilePath, (Map<String, ?>) null)) {
                Path path = fs.getPath(directoryPath);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                            List<Asset> subAssets = this.getAssets("/data/" + entry.getFileName().toString());
                            assets.add(new Asset(entry.getFileName().toString(), "directory", subAssets));
                        } else {
                            byte[] data = Files.readAllBytes(entry);
                            assets.add(new Asset(entry.getFileName().toString(), "file", data));
                        }
                    }
                }
            }
        } else {
            // Resource is on the filesystem
            Path path = Paths.get(resourceUrl.toURI());
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
                        List<Asset> subAssets = this.getAssets("/data/" + entry.getFileName().toString());
                        assets.add(new Asset(entry.getFileName().toString(), "directory", subAssets));
                    } else {
                        byte[] data = Files.readAllBytes(entry);
                        assets.add(new Asset(entry.getFileName().toString(), "file", data));
                    }
                }
            }
        }
        return assets;
    }

    public static class Asset {
        private String name;
        private String type;
        private Object data; // Can be a byte array for files or List<Asset> for directories

        public Asset(String name, String type, Object data) {
            this.name = name;
            this.type = type;
            this.data = data;
        }

        @Override
        public String toString() {
            return "Asset{name='" + name + "', type='" + type + "', data="
                    + (data instanceof byte[] ? "[byte array]" : data) + '}';
        }
    }

}
