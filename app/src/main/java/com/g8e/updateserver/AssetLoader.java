package com.g8e.updateserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AssetLoader {

    public List<Asset> getAssets(String directoryPath) throws IOException {
        List<Asset> assets = new ArrayList<>();

        // Load the resource URL
        URL resourceUrl = getClass().getResource(directoryPath);
        if (resourceUrl == null) {
            throw new IllegalArgumentException("Resource not found: " + directoryPath);
        }

        // Check if the resource is a directory or inside a JAR file
        if (resourceUrl.toString().startsWith("jar:file:")) {
            // Handle resources inside a JAR file
            try (JarFile jarFile = new JarFile(
                    resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!")))) {
                jarFile.stream()
                        .filter(entry -> entry.getName().startsWith(directoryPath.substring(1))) // Filter entries by
                                                                                                 // directory
                        .forEach(entry -> {
                            try {
                                if (entry.isDirectory()) {
                                    // Handle directories
                                    List<Asset> subAssets = getAssets("/" + entry.getName());
                                    assets.add(new Asset(entry.getName(), "directory", subAssets));
                                } else {
                                    // Read file data
                                    try (InputStream inputStream = jarFile.getInputStream(entry)) {
                                        byte[] data = inputStream.readAllBytes();
                                        assets.add(new Asset(entry.getName(), "file", data));
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } else {
            // Handle filesystem directories
            Path path;
            try {
                path = Paths.get(resourceUrl.toURI());
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                    for (Path entry : stream) {
                        if (Files.isDirectory(entry)) {
                            // Pass the relative path of the subdirectory correctly
                            List<Asset> subAssets = getAssets("/" + entry.getFileName().toString());
                            assets.add(new Asset(entry.getFileName().toString(), "directory", subAssets));
                        } else {
                            // Read file data
                            byte[] data = Files.readAllBytes(entry);
                            assets.add(new Asset(entry.getFileName().toString(), "file", data));
                        }
                    }
                }
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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
