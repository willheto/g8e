package com.g8e.gameserver.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.g8e.gameserver.World;
import com.g8e.gameserver.models.Item;
import com.google.gson.Gson;

public class ItemsManager {
    private Item[] items = new Item[1000];
    private World world;

    public ItemsManager(World world) {
        loadItems();
        this.world = world;
    }

    private void loadItems() {
        URL itemsUrl = getClass().getResource("/data/scripts/items.json");

        if (itemsUrl == null) {
            throw new IllegalArgumentException("Resource not found: /data/items.json");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(itemsUrl.openStream()))) {
            Gson gson = new Gson();
            Item[] loadedItems = gson.fromJson(reader, Item[].class);
            System.arraycopy(loadedItems, 0, items, 0, loadedItems.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Item getItemByID(int itemID) {
        return items[itemID];
    }

    public void spawnItem(int x, int y, int itemID) {
        Item item = getItemByID(itemID);
        item.setWorldX(x);
        item.setWorldY(y);
        world.items.add(item);
    }

    public void removeItem(String uniqueItemID) {
        world.items.removeIf(item -> item.getUniqueID().equals(uniqueItemID));
    }
}
