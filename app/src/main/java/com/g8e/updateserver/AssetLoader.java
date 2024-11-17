package com.g8e.updateserver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader {

    public List<Asset> getAssets(String directoryPath) throws IOException, URISyntaxException {
        List<Asset> assets = new ArrayList<>();

        // Load the resource URL
        URL resourceUrl = getClass().getResource(directoryPath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + directoryPath);
        }

        // Use toURI to get a valid URI
        URI uri = resourceUrl.toURI();

        // Check if the URI is pointing to a JAR file
        if (uri.getScheme().equals("jar")) {
            // URI inside a JAR file, need to handle it differently
            try (FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<String, Object>())) {
                Path path = fs.getPath(directoryPath);

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            // Recursively load assets from subdirectories inside the JAR
                            List<Asset> subAssets = getAssets("/data/" + entry.getFileName().toString());
                            assets.add(new Asset(entry.getFileName().toString(), "directory", subAssets));
                        } else {
                            // Read file data
                            byte[] data = Files.readAllBytes(entry);
                            assets.add(new Asset(entry.getFileName().toString(), "file", data));
                        }
                    }
                }
            }
        } else {
            // Resource is on the filesystem, handle it as before
            Path path = Paths.get(uri);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    if (Files.isDirectory(entry)) {
                        // Pass the relative path of the subdirectory correctly
                        List<Asset> subAssets = getAssets("/data/" + entry.getFileName().toString());
                        assets.add(new Asset(entry.getFileName().toString(), "directory", subAssets));
                    } else {
                        // Read file data
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
