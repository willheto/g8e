package com.g8e.gameserver.models;

import java.util.List;

import com.g8e.gameserver.World;
import com.g8e.gameserver.enums.Direction;
import com.g8e.util.Logger;

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

    public TilePosition wanderAreaFromOriginalTile;

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
        this.targetTile = null;

        this.newTargetTile = new TilePosition(
                this.wanderAreaFromOriginalTile.x + (int) (Math.random() * 5) - 2,
                this.wanderAreaFromOriginalTile.y + (int) (Math.random() * 5) - 2);
    }

    protected boolean isTargetTileNotWithinWanderArea() {
        TilePosition targetTile = this.getTarget();
        if (targetTile == null) {
            return false;
        }

        return targetTile.x < this.originalWorldX - this.wanderAreaFromOriginalTile.x
                || targetTile.x > this.originalWorldX + this.wanderAreaFromOriginalTile.x
                || targetTile.y < this.originalWorldY - this.wanderAreaFromOriginalTile.y
                || targetTile.y > this.originalWorldY + this.wanderAreaFromOriginalTile.y;

    }

    protected void moveTowardsTarget() {

        TilePosition target = this.getTarget();
        if (target == null) {
            return;
        }

        List<TilePosition> path = this.world.pathfinding.findPath(new TilePosition(this.worldX, this.worldY), target);

        if (this instanceof Combatant) {
            if (path.size() == 1 && ((Combatant) this).targetedEntityID != null) {
                Entity entity = this.world.getEntityByID(((Combatant) this).targetedEntityID);
                if (entity != null && entity instanceof Combatant) {
                    ((Combatant) this).attackEntity((Combatant) entity);
                    this.nextTileDirection = null;
                    this.targetTile = null;
                    return;
                }
            }
        }
        List<TilePosition> pathToNewTarget = null;

        if (this.newTargetTile != null && this.targetTile != null && this.targetTile != this.newTargetTile) {
            TilePosition nextStep = path.size() > 0 ? path.get(1) : new TilePosition(this.worldX, this.worldY);
            pathToNewTarget = this.world.pathfinding.findPath(nextStep, this.newTargetTile);
        }

        if (path.size() > 1) {
            boolean isLastStep = path.size() == 2;
            this.moveAlongPath(path.get(1), path.get(2), pathToNewTarget != null ? pathToNewTarget.get(1) : null,
                    isLastStep);
        }
    }

    protected void moveAlongPath(TilePosition nextStep, TilePosition nextNextStep, TilePosition pathToNewTarget,
            boolean isLastStep) {
        int deltaX = pathToNewTarget != null ? pathToNewTarget.x - nextStep.x : nextStep.x - this.worldX;
        int deltaY = pathToNewTarget != null ? pathToNewTarget.y - nextStep.y : nextStep.y - this.worldY;

        int stepSize = 1;
        Double distance = Math.sqrt(deltaY * deltaY + deltaX * deltaX);

        if (pathToNewTarget != null) {
            this.worldX = nextStep.x;
            this.worldY = nextStep.y;
            this.nextTileDirection = this.getDirection(deltaX, deltaY);
        } else {
            if (this.newTargetTile == null) {
                if (distance > stepSize) {
                    this.worldX += deltaX / distance * stepSize;
                    this.worldY += deltaY / distance * stepSize;
                } else {
                    this.worldX = nextStep.x;
                    this.worldY = nextStep.y;
                }
            } else {
                this.nextTileDirection = this.getDirection(deltaX, deltaY);
            }

            if (isLastStep && this.newTargetTile == null) {
                if (this.targetObjectID != null) {
                    Item item = this.world.getItemByID(this.targetObjectID);

                    if (item == null) {
                        Logger.printInfo("Too late, it's gone!");
                        return;
                    }

                    if (this instanceof Player) {
                        ((Player) this).takeItem(item);
                    }
                }

                this.targetObjectID = null;
                this.targetTile = null;
                this.nextTileDirection = null;
            } else if (this.newTargetTile == null) {
                int nextDeltaX = nextNextStep.x - nextStep.x;
                int nextDeltaY = nextNextStep.y - nextStep.y;
                this.nextTileDirection = this.getDirection(nextDeltaX, nextDeltaY);
            }

        }
        this.targetTile = this.newTargetTile != null ? this.newTargetTile : this.targetTile;
        this.newTargetTile = null;
    }

    protected Direction getDirection(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) {
            return null;
        }

        if (deltaX == 0 && deltaY == -1) {
            return Direction.NORTH;
        }

        if (deltaX == 0 && deltaY == 1) {
            return Direction.SOUTH;
        }

        if (deltaX == -1 && deltaY == 0) {
            return Direction.WEST;
        }

        if (deltaX == 1 && deltaY == 0) {
            return Direction.EAST;
        }

        return null;
    }

    protected TilePosition getTarget() {
        // if instance of player or npc

        if (this instanceof Combatant) {
            if (((Combatant) this).targetedEntityID != null) {

                Entity entity = this.world.getEntityByID(((Combatant) this).targetedEntityID);
                if (entity == null) {
                    Logger.printDebug("Entity not found: " + ((Combatant) this).targetedEntityID);
                    return null;
                }

                boolean isOneTileAway = Math.abs(entity.worldX - this.worldX)
                        + Math.abs(entity.worldY - this.worldY) == 1;

                if (isOneTileAway) {
                    return new TilePosition(this.worldX, this.worldY);
                }

                TilePosition[] tilesNextToTarget = new TilePosition[] {
                        new TilePosition(this.worldX - 1, this.worldY),
                        new TilePosition(this.worldX + 1, this.worldY),
                        new TilePosition(this.worldX, this.worldY - 1),
                        new TilePosition(this.worldX, this.worldY + 1)
                };

                TilePosition closestTile = null;

                for (TilePosition tile : tilesNextToTarget) {
                    int distance = Math.abs(tile.x - entity.worldX) + Math.abs(tile.y - entity.worldY);
                    if (closestTile == null || distance < Math.abs(closestTile.x - entity.worldX)
                            + Math.abs(closestTile.y - entity.worldY)) {
                        closestTile = tile;
                    }
                }

                return closestTile;
            }
        }

        return this.targetTile != null ? this.targetTile : this.newTargetTile;
    }

}
