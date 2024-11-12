package com.g8e.gameserver.network.actions;

public class ChangeAppearanceAction extends Action {
    private int skinColor;
    private int hairColor;
    private int shirtColor;

    public ChangeAppearanceAction(String playerID, int skinColor, int hairColor, int shirtColor) {
        this.action = "changeAppearance";
        this.playerID = playerID;
        this.skinColor = skinColor;
        this.hairColor = hairColor;
        this.shirtColor = shirtColor;
    }

    public int getSkinColor() {
        return skinColor;
    }

    public int getHairColor() {
        return hairColor;
    }

    public int getShirtColor() {
        return shirtColor;
    }

}
