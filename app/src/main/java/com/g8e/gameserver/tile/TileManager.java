package com.g8e.gameserver.tile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.g8e.gameserver.World;

public class TileManager {

    private World world;
    public Tile[] tile;
    public int[][] mapTileNum;

    public TileManager(World world) {
        this.world = world;
        tile = new Tile[50];
        mapTileNum = new int[world.maxWorldCol][world.maxWorldRow];

        getTiles();
        loadMap("/data/map/tutorial_island.txt");
    }

    public Tile getTileByXandY(int x, int y) {
        try {
            int index = mapTileNum[x][y];
            return tile[index];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getTiles() {
        try {
            this.setup(0, true);
            this.setup(1, true);
            this.setup(2, true);
            this.setup(3, true);
            this.setup(4, true);
            this.setup(5, true);
            this.setup(6, true);
            this.setup(7, true);
            this.setup(8, true);

            this.setup(9, false);

            this.setup(10, false);

            this.setup(12, true);

            this.setup(13, true);
            this.setup(14, true);

            this.setup(15, true);
            this.setup(16, true);
            this.setup(17, true);
            this.setup(18, true);

            this.setup(19, true);
            this.setup(21, false);
            this.setup(22, false);
            this.setup(24, false);
            this.setup(25, false);
            this.setup(26, false);
            this.setup(27, false);
            this.setup(28, false);
            this.setup(29, false);
            this.setup(30, true);
            this.setup(31, true);
            this.setup(36, false);
            this.setup(37, false);
            this.setup(38, false);
            this.setup(39, false);
            this.setup(41, false);

            this.setup(40, false);
            this.setup(41, false);

            this.setup(42, true);

            this.setup(43, true);

            this.setup(44, true);

            this.setup(45, true);
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

    public void loadMap(String filePath) {

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

                    mapTileNum[col][row] = num;
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
