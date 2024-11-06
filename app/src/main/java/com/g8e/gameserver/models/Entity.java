package com.g8e.gameserver.models;

import java.util.List;

import com.g8e.gameserver.World;
import com.g8e.gameserver.enums.Direction;
import com.g8e.gameserver.pathfinding.PathNode;
import com.g8e.gameserver.tile.Tile;
import com.g8e.gameserver.tile.TilePosition;

public abstract class Entity {
    public String entityID;
    public transient World world;

    public int originalWorldX;
    public int originalWorldY;
    public int worldX;
    public int worldY;

    public TilePosition targetTile = null;
    public TilePosition newTargetTile = null;
    public Direction nextTileDirection = null;

    public String name;
    public String examineText;
    public int type; // 0 = player, 1 = npc, 2 = monster

    protected int tickCounter = 0;
    public int followCounter = 0;
    public int shouldFollow = 0;
    public String targetObjectID = null;
    public Direction facingDirection = Direction.DOWN;

    public int wanderAreaFromOriginalTile = 5;
    protected List<PathNode> currentPath;
    protected TilePosition targetEntityLastPosition;

    public Entity(String entityID, World world, int worldX, int worldY, String name, String examineText, int type) {
        this.entityID = entityID;
        this.world = world;
        this.originalWorldX = worldX;
        this.originalWorldY = worldY;
        this.worldX = worldX;
        this.worldY = worldY;
        this.name = name;
        this.examineText = examineText;
        this.type = type;
    }

    public abstract void update();

    protected void setNewTargetTileWithingWanderArea() {
        this.newTargetTile = new TilePosition(
                this.originalWorldX + (int) (Math.random() * (this.wanderAreaFromOriginalTile * 2 + 1)
                        - this.wanderAreaFromOriginalTile),

                this.originalWorldY + (int) (Math.random() * (this.wanderAreaFromOriginalTile * 2 + 1)
                        - this.wanderAreaFromOriginalTile));

    }

    protected boolean isTargetTileNotWithinWanderArea() {
        TilePosition targetTile = this.getTarget();
        if (targetTile == null) {
            return false;
        }

        return Math.abs(targetTile.x - this.originalWorldX) > this.wanderAreaFromOriginalTile
                || Math.abs(targetTile.y - this.originalWorldY) > this.wanderAreaFromOriginalTile;

    }

    private TilePosition getPositionOneTileAwayFromTarget(TilePosition target) {
        int deltaX = target.x - this.worldX;
        int deltaY = target.y - this.worldY;

        if (deltaX == 0 && deltaY == 0) {
            return null;
        }

        // Create an array of tile offsets around the target
        TilePosition[] tilesAroundTarget = new TilePosition[] {
                new TilePosition(target.x, target.y - 1), // Up
                new TilePosition(target.x + 1, target.y), // Right
                new TilePosition(target.x, target.y + 1), // Down
                new TilePosition(target.x - 1, target.y), // Left
        };

        TilePosition closestTile = null;
        double minDistance = Double.MAX_VALUE;

        // Check each tile and calculate its distance from the current position
        for (TilePosition tile : tilesAroundTarget) {
            Tile currentTile = this.world.tileManager.getTileByXandY(tile.x, tile.y);

            if (currentTile != null && !currentTile.collision) {
                // Calculate Euclidean distance to the current position
                double distance = Math.sqrt(Math.pow(tile.x - this.worldX, 2) + Math.pow(tile.y - this.worldY, 2));

                if (distance < minDistance) {
                    minDistance = distance;
                    closestTile = tile;
                }
            }
        }

        return closestTile;
    }

    private void setAttackTargetTile() {
        Entity entity = this.world.getEntityByID(((Combatant) this).targetedEntityID);
        if (entity != null && entity instanceof Combatant) {

            TilePosition entityTile = new TilePosition(entity.worldX, entity.worldY);

            if (this.targetEntityLastPosition != null &&
                    this.targetEntityLastPosition.getX() == entityTile.getX()
                    && this.targetEntityLastPosition.getY() == entityTile.getY()) {
                return;
            }
            this.targetEntityLastPosition = entityTile;
            this.newTargetTile = getPositionOneTileAwayFromTarget(entityTile);

        } else {
            ((Combatant) this).targetedEntityID = null;
        }

    }

    protected void moveTowardsTarget() {

        if (this instanceof Combatant && ((Combatant) this).targetedEntityID != null) {
            setAttackTargetTile();
        }

        TilePosition target = this.getTarget();
        if (target == null) {
            return;
        }

        if (this.newTargetTile != null) {
            if (this.targetTile != null) {
                this.worldX = this.currentPath.get(1).x;
                this.worldY = this.currentPath.get(1).y;
            }
            currentPath = this.world.pathFinder.findPath(this.worldX, this.worldY, target.x, target.y);
            if (currentPath.size() < 2) {
                currentPath = null;
                this.newTargetTile = null;
                this.targetTile = null;
                this.nextTileDirection = null;
                return;
            }
            this.targetTile = newTargetTile;
            this.newTargetTile = null;

            int deltaX = currentPath.get(1).x - this.worldX;
            int deltaY = currentPath.get(1).y - this.worldY;
            Direction nextTileDirection = this.getDirection(deltaX, deltaY);
            this.nextTileDirection = nextTileDirection;
            return;
        }

        if (currentPath == null || currentPath.size() == 0) {
            return;
        }

        // Already at target
        if (currentPath.size() == 1) {
            this.nextTileDirection = null;
            this.targetTile = null;
            this.newTargetTile = null;
            return;
        }

        // Last step
        if (currentPath.size() == 2) {
            PathNode nextStep = currentPath.get(1);
            moveAlongPath(nextStep);
        } else if (currentPath.size() > 2) {
            PathNode nextStep = currentPath.get(1);
            PathNode nextNextStep = currentPath.get(2);
            moveAlongPath(nextStep, nextNextStep);
        }

    }

    protected void moveAlongPath(PathNode nextStep, PathNode nextNextStep) {
        this.worldX = nextStep.x;
        this.worldY = nextStep.y;

        Direction nextTileDirection = this.getDirection(nextNextStep.x - nextStep.x, nextNextStep.y - nextStep.y);
        this.nextTileDirection = nextTileDirection;

        currentPath.remove(0);
    }

    // Last step
    protected void moveAlongPath(PathNode nextStep) {
        this.nextTileDirection = null;
        this.worldX = nextStep.x;
        this.worldY = nextStep.y;
        this.targetTile = null;
        currentPath = null;

    }

    protected Direction getDirection(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) {
            return null;
        }

        if (deltaX == 0 && deltaY == -1) {
            return Direction.UP;
        }

        if (deltaX == 0 && deltaY == 1) {
            return Direction.DOWN;
        }

        if (deltaX == -1 && deltaY == 0) {
            return Direction.LEFT;
        }

        if (deltaX == 1 && deltaY == 0) {
            return Direction.RIGHT;
        }

        return null;
    }

    protected TilePosition getTarget() {

        return this.newTargetTile != null ? this.newTargetTile : this.targetTile;
    }

}
