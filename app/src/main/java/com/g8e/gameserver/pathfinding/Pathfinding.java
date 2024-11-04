package com.g8e.gameserver.pathfinding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import com.g8e.gameserver.World;
import com.g8e.gameserver.managers.Tile;
import com.g8e.gameserver.models.TilePosition;
import com.g8e.util.Logger;

public class Pathfinding {
    private World world;
    private TilePosition[][] grid;
    private int width, height;

    public Pathfinding(int[][] map, World world) {
        Logger.printDebug(map.length + " " + map[0].length);
        this.world = world;
        this.width = map.length;
        this.height = map[0].length;
        this.grid = new TilePosition[width][height];
    }

    public List<TilePosition> findPath(TilePosition start, TilePosition target) {
        PriorityQueue<Node> openSet = new PriorityQueue<>((a, b) -> Double.compare(a.fCost, b.fCost));
        HashSet<Node> closedSet = new HashSet<>();

        Node startNode = new Node(start);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node currentNode = openSet.poll();

            // Check if we reached the target
            if (currentNode.tile.equals(target)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode);

            for (TilePosition neighbor : getNeighbors(currentNode.tile)) {
                Tile neighborTile = this.world.tileManager.getTileByXandY(neighbor.x, neighbor.y);
                boolean isCollision = neighborTile.collision;
                if (isCollision || closedSet.contains(new Node(neighbor))) {
                    continue; // Ignore non-walkable or already evaluated nodes
                }

                Node neighborNode = new Node(neighbor);
                double newCostToNeighbor = currentNode.gCost + getDistance(currentNode.tile, neighbor);

                // If this path to the neighbor is better or the neighbor is not in the open set
                if (newCostToNeighbor < neighborNode.gCost || !openSet.contains(neighborNode)) {
                    neighborNode.gCost = newCostToNeighbor;
                    neighborNode.hCost = getDistance(neighbor, target); // Use target to calculate heuristic
                    neighborNode.calculateFCost();
                    neighborNode.parent = currentNode;

                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return null; // No path found
    }

    private List<TilePosition> reconstructPath(Node targetNode) {
        List<TilePosition> path = new ArrayList<>();
        Node current = targetNode;

        while (current != null) {
            path.add(current.tile);
            current = current.parent;
        }
        return path;
    }

    private List<TilePosition> getNeighbors(TilePosition tile) {
        List<TilePosition> neighbors = new ArrayList<>();
        int[] directions = { -1, 0, 1 }; // Left, Up, Right, Down

        for (int dx : directions) {
            for (int dy : directions) {
                if (Math.abs(dx) != Math.abs(dy)) { // Only diagonal moves
                    int newX = tile.x + dx;
                    int newY = tile.y + dy;

                    if (isInBounds(newX, newY)) {
                        neighbors.add(grid[newX][newY]);
                    }
                }
            }
        }
        return neighbors;
    }

    private boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private double getDistance(TilePosition a, TilePosition b) {
        int dstX = Math.abs(a.x - b.x);
        int dstY = Math.abs(a.y - b.y);
        return dstX + dstY; // Manhattan distance
    }
}
