package com.g8e.gameserver.pathfinding;

import java.util.Objects;

import com.g8e.gameserver.models.TilePosition;

public class Node {
    public TilePosition tile;
    public Node parent;
    public double gCost; // Cost from start to this node
    public double hCost; // Heuristic cost to the end
    public double fCost; // Total cost (g + h)

    public Node(TilePosition tile) {
        this.tile = tile;
    }

    public void calculateFCost() {
        fCost = gCost + hCost;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Node))
            return false;
        Node other = (Node) obj;
        return Objects.equals(tile, other.tile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tile);
    }
}
