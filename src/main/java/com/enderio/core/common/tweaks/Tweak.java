package com.enderio.core.common.tweaks;

import com.enderio.core.common.config.ConfigHandler;

public abstract class Tweak {

    private String name, comment;

    public Tweak(String key, String comment) {
        this.name = key;
        this.comment = comment;
        if (ConfigHandler.instance().addBooleanFor(this)) {
            load();
        }
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public abstract void load();
}
