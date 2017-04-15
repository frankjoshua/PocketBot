package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

/**
 * Created by josh on 1/24/2016.
 */
public class FaceInfo {
    final public int id;
    final public int icon;
    final public int background;
    final public String name;
    final public String info;
    final public boolean locked;

    FaceInfo(final int id, int icon, int background, final String name, final String info, final boolean locked) {
        this.id = id;
        this.icon = icon;
        this.background = background;
        this.name = name;
        this.info = info;
        this.locked = locked;
    }

    @Override
    public String toString() {
        return name;
    }
}
