package com.g8e.gameserver.tile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;
import com.g8e.gameserver.World;
import java.awt.image.BufferedImage;

public class TileManager {

    private World world;
    public Tile[] tile;
    public int[][] mapTileNumLayer1;
    public int[][] mapTileNumLayer2;
    public int[][] mapTileNumLayer3;
    public int[][] mapTileNumLayer4;
    public int chunkSize = 10;

    public TileManager(World world) {
        this.world = world;
        tile = new Tile[8000];

        // Initialize tile maps for each layer
        mapTileNumLayer1 = new int[world.maxWorldCol][world.maxWorldRow];
        mapTileNumLayer2 = new int[world.maxWorldCol][world.maxWorldRow];
        mapTileNumLayer3 = new int[world.maxWorldCol][world.maxWorldRow];
        mapTileNumLayer4 = new int[world.maxWorldCol][world.maxWorldRow];

        getTiles();
        loadMap("/data/map/worldmap_back_1.csv", 1); // Load layer 1 map
        loadMap("/data/map/worldmap_back_2.csv", 2); // Load layer 2 map
        loadMap("/data/map/worldmap_fore_1.csv", 3); // Load layer 3 map
        loadMap("/data/map/worldmap_fore_2.csv", 4); // Load layer 4 map

    }

    public TilePosition getClosestWalkableTile(int x, int y) {
        try {
            int distance = 0;

            while (true) {
                // Search in increasing distance from the target tile
                for (int i = -distance; i <= distance; i++) {
                    for (int j = -distance; j <= distance; j++) {
                        // Only check the outermost tiles of the current square (Manhattan distance)
                        if (i == -distance || i == distance || j == -distance || j == distance) {
                            int newX = x + i;
                            int newY = y + j;

                            // Check bounds to avoid IndexOutOfBoundsException
                            if (newX >= 0 && newX < world.maxWorldCol && newY >= 0 && newY < world.maxWorldRow) {
                                if (!getCollisionByXandY(newX, newY)) {
                                    return new TilePosition(newX, newY);
                                }
                            }
                        }
                    }
                }
                // If no walkable tile found, expand the search area by increasing the distance
                distance++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // In case no walkable tile is found (very unlikely in a proper world setup)
    }

    public int getChunkByWorldXandY(int worldX, int worldY) {
        try {
            // world is divided into chunks of 10x10 tiles
            // starting from top left corner of the world
            int chunkX = worldX / chunkSize;
            int chunkY = worldY / chunkSize;
            return chunkX + chunkY * (world.maxWorldCol / chunkSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int[] getNeighborChunks(int chunk) {
        try {
            int[] neighbors = new int[8];
            int chunkX = chunk % (world.maxWorldCol / chunkSize);
            int chunkY = chunk / (world.maxWorldCol / chunkSize);

            neighbors[0] = chunkX - 1 + (chunkY - 1) * (world.maxWorldCol / chunkSize);
            neighbors[1] = chunkX + (chunkY - 1) * (world.maxWorldCol / chunkSize);
            neighbors[2] = chunkX + 1 + (chunkY - 1) * (world.maxWorldCol / chunkSize);
            neighbors[3] = chunkX - 1 + chunkY * (world.maxWorldCol / chunkSize);
            neighbors[4] = chunkX + 1 + chunkY * (world.maxWorldCol / chunkSize);
            neighbors[5] = chunkX - 1 + (chunkY + 1) * (world.maxWorldCol / chunkSize);
            neighbors[6] = chunkX + (chunkY + 1) * (world.maxWorldCol / chunkSize);
            neighbors[7] = chunkX + 1 + (chunkY + 1) * (world.maxWorldCol / chunkSize);

            return neighbors;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Is this even needed anymore?
    public Tile getTileByXandY(int x, int y) {
        try {
            // For now, return the tile from Layer 1
            int index = mapTileNumLayer1[x][y];
            return tile[index];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean getCollisionByXandY(int x, int y) {
        try {
            // Check if x and y are within the bounds of the map arrays
            if (x < 0 || y < 0 || x >= mapTileNumLayer1.length || y >= mapTileNumLayer1[0].length) {
                return false; // Out of bounds, no collision
            }

            int index1 = mapTileNumLayer1[x][y];
            int index2 = mapTileNumLayer2[x][y];
            int index3 = mapTileNumLayer3[x][y];
            int index4 = mapTileNumLayer4[x][y];
            // Check for valid tile indices and if any of them have a collision
            if ((index1 >= 0 && tile[index1].collision) ||
                    (index2 >= 0 && tile[index2].collision) ||
                    (index3 >= 0 && tile[index3].collision) ||
                    (index4 >= 0 && tile[index4].collision)) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void getTiles() {
        try {
            BufferedImage tileSheet = ImageIO.read(getClass().getResourceAsStream("/data/map/tilesheet.png"));

            // split tile sheet into 16 x 16 tiles
            int numTilesAcross = tileSheet.getWidth() / 16;
            tile = new Tile[numTilesAcross * numTilesAcross];

            for (int col = 0; col < numTilesAcross; col++) {
                for (int row = 0; row < numTilesAcross; row++) {
                    int index = col + row * numTilesAcross;
                    if (index == 1158 || index == 1159 || index == 1270 || index == 1271) { // water
                        tile[index] = new Tile(true, index);
                    } else if (index >= 3512 && index <= 3519) { // fences part 1
                        tile[index] = new Tile(true, index);
                    } else if (index >= 3624 && index <= 3631) { // fences part 2
                        tile[index] = new Tile(true, index);
                    } else if (index >= 3736 && index <= 3743) { // fences part 3
                        tile[index] = new Tile(true, index);
                    } else if (index >= 3848 && index <= 3855) { // fences part 4
                        tile[index] = new Tile(true, index);
                    } else if (index >= 3960 && index <= 3967) { // fences part 5
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4184 && index <= 4191) { // fences part 6
                        tile[index] = new Tile(true, index);
                    } else if (index >= 3601 && index <= 3614) { // tree trunks
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4056 && index <= 4059) { // tree trunks
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4168 && index <= 4171) { // tree trunks
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4833 && index <= 4846) { // tree trunks
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4721 && index <= 4734) { // tree trunks
                        tile[index] = new Tile(true, index);
                    } else if (index >= 1424 && index <= 1431) { // walls
                        tile[index] = new Tile(true, index);
                    } else if (index >= 1312 && index <= 1319) { // walls
                        tile[index] = new Tile(true, index);
                    } else if (index >= 1200 && index <= 1207) { // walls
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4587 && index <= 4590) { // bench
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4699 && index <= 4702) { // bench
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4362 && index <= 4367) { // boxes
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4474 && index <= 4478) { // boxes
                        tile[index] = new Tile(true, index);
                    } else if (index >= 1088 && index <= 1095) { // walls
                        tile[index] = new Tile(true, index);
                    } else if (index >= 976 && index <= 983) { // walls
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4408 && index <= 4415) { // walls
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4284 && index <= 4287) { // bushes
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4396 && index <= 4399) { // bushes
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4084 && index <= 4087) { // wells
                        tile[index] = new Tile(true, index);
                    } else if (index >= 4196 && index <= 4199) { // wells
                        tile[index] = new Tile(true, index);
                    }

                    else {
                        tile[index] = new Tile(false, index);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setup(int index, boolean collision) {
        try {
            tile[index] = new Tile(collision, index);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to load maps for different layers
    public void loadMap(String filePath, int layer) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (col < world.maxWorldCol && row < world.maxWorldRow) {
                String line = br.readLine();

                while (col < world.maxWorldCol) {
                    String numbers[] = line.split(",");
                    int num = Integer.parseInt(numbers[col]);

                    // Depending on the layer, assign the number to the respective map
                    if (layer == 1) {
                        mapTileNumLayer1[col][row] = num;
                    } else if (layer == 2) {
                        mapTileNumLayer2[col][row] = num;
                    } else if (layer == 3) {
                        mapTileNumLayer3[col][row] = num;
                    } else if (layer == 4) {
                        mapTileNumLayer4[col][row] = num;
                    }
                    col++;
                }
                if (col == world.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
