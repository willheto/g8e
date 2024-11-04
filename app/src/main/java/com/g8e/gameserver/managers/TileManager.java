package com.g8e.gameserver.managers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.g8e.gameserver.World;
import com.g8e.gameserver.pathfinding.Pathfinding;

public class TileManager {

    private World world;
    public Tile[] tile;
    public int[][] mapTileNum;

    public TileManager(World world) {
        this.world = world;
        tile = new Tile[10];
        mapTileNum = new int[world.maxWorldCol][world.maxWorldRow];

        getTiles();
        loadMap("/data/map/worldmap.txt");
        this.world.pathfinding = new Pathfinding(mapTileNum, world);
    }

    public Tile getTileByXandY(int x, int y) {
        return tile[mapTileNum[x][y]];
    }

    public void getTiles() {
        try {
            setup(0, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setup(int index, boolean collision) {
        try {
            tile[index] = new Tile();
            tile[index].collision = collision;
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
