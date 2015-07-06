package com.meow.thaithien.testonedrive;

import android.util.Log;

import java.util.ArrayList;

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

    public static OneDriveItem FindFolder(String name,ArrayList<OneDriveItem> inputArray)
    {
        OneDriveItem result = null;
        for (int i=0;i<inputArray.size();i++)
        {

            String tmp_name = inputArray.get(i).getName();
            String tmp_type=inputArray.get(i).getType();
            if (tmp_type.equals("folder")||tmp_type.equals("album"))
            if (tmp_name.equals(name))
            {
                result = inputArray.get(i);
                return result;
            }

        }
        return result;
    }
}
