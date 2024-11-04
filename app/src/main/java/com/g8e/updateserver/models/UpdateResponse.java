package com.g8e.updateserver.models;

import java.util.List;

import com.g8e.updateserver.AssetLoader.Asset;

public class UpdateResponse {
    private int type;
    private int cacheNumber;
    private List<Asset> assets;

    public UpdateResponse(int type, int cacheNumber, List<Asset> assets) {
        this.type = type;
        this.cacheNumber = cacheNumber;
        this.assets = assets;
    }

    public int getType() {
        return type;
    }

    public int getCacheNumber() {
        return cacheNumber;
    }

    public List<Asset> getAssets() {
        return assets;
    }
}
