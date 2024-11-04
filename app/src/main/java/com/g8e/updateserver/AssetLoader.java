package com.g8e.updateserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class AssetLoader {

    public List<Asset> getAssets(String directoryPath) throws IOException, URISyntaxException {
        List<Asset> assets = new ArrayList<>();

        // Load the resource URL
        URL resourceUrl = getClass().getResource(directoryPath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + directoryPath);
        }

        // Use toURI to get a valid URI
        Path path = Paths.get(resourceUrl.toURI());

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
