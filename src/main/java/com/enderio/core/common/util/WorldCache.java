package com.enderio.core.common.util;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import lombok.SneakyThrows;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class WorldCache<K, V> extends HashMap<K, V>
{
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private transient File saveFile;

    public WorldCache(String ident)
    {
        super();
        saveFile = new File(DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath() + "/" + ident + ".json");
        MinecraftForge.EVENT_BUS.register(this);
        
    }
    
    @SubscribeEvent
    @SneakyThrows
    public void onWorldSave(WorldEvent.Save event)
    {
        new FileWriter(saveFile).write(gson.toJson(this));
    }
    
    private void readData(){}
}
