package com.meow.thaithien.testonedrive;

/**
 * Created by Thien on 7/3/2015.
 */
public class OneDriveItem {
    public String name;
    public String id;
    public String type;

    public OneDriveItem(String name, String id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public OneDriveItem() {
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
