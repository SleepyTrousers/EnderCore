package com.enderio.core.common.util;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Untested way to save arbitrary to a game save. The name to ID map will be hot-swapped when a new save is loaded.
 */
public class WorldCache<I> {

    public static final int MAX_ID = Short.MAX_VALUE;

    protected String ident;
    protected boolean locked;

    protected BitSet usedIDs = new BitSet(MAX_ID);
    protected Set<Integer> blockedIDs = Sets.newHashSet();

    protected BiMap<I, String> objToName = HashBiMap.create();

    protected BiMap<String, Integer> nameToID = HashBiMap.create();

    public WorldCache(String ident) {
        this(ident, true);
    }

    protected WorldCache(String ident, boolean registerToEventBus) {
        this.ident = ident;
        if (registerToEventBus) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            try {
                saveData(getSaveFile());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            try {
                loadData(getSaveFile());
            } catch (IOException e) {
                Throwables.propagate(e);
            }
        }
        locked = true;
    }

    protected void loadData(File file) throws IOException {
        if (!file.createNewFile()) {
            NBTTagCompound tag = null;
            try {
                tag = CompressedStreamTools.read(file);
            } catch (Exception e) {
                generateIDs();
                return;
            }
            if (tag.hasKey("ItemData")) {
                // name <-> id mappings
                NBTTagList list = tag.getTagList("ItemData", Constants.NBT.TAG_COMPOUND);
                for (int i = 0; i < list.tagCount(); i++) {
                    NBTTagCompound dataTag = list.getCompoundTagAt(i);
                    int id = dataTag.getInteger("V");
                    String name = dataTag.getString("K");
                    nameToID.put(name, id);
                    if (objToName.values().contains(name)) {
                        usedIDs.set(id, true);
                    }
                }

                // blocked ids
                for (int id : tag.getIntArray("BlockedItemIds")) {
                    blockedIDs.add(id);
                }

                blockOldIDs();
                mergeNewIDs();
                return;
            }
        }
        generateIDs();
    }

    protected void saveData(File file) throws IOException {
        NBTTagCompound data = new NBTTagCompound();

        // name <-> id mappings
        NBTTagList dataList = new NBTTagList();
        for (String key : nameToID.keySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("K", key);
            tag.setInteger("V", nameToID.get(key));
            dataList.appendTag(tag);
        }
        data.setTag("ItemData", dataList);
        // blocked ids
        data.setIntArray("BlockedItemIds", ArrayUtils.toPrimitive(blockedIDs.toArray(new Integer[0])));

        CompressedStreamTools.write(data, file);
    }

    protected void blockOldIDs() {
        for (String s : nameToID.keySet()) {
            if (!objToName.containsValue(s)) {
                blockedIDs.add(nameToID.get(s));
            }
        }
        Iterator<String> iter = nameToID.keySet().iterator();
        while (iter.hasNext()) {
            if (blockedIDs.contains(nameToID.get(iter.next()))) {
                iter.remove();
            }
        }
    }

    protected void mergeNewIDs() {
        for (String s : objToName.values()) {
            Integer id = nameToID.get(s);
            if (id != null) {
                setID(s);
            }
        }
    }

    protected void generateIDs() {
        for (Entry<I, String> e : objToName.entrySet()) {
            if (!nameToID.containsKey(e.getValue())) {
                setID(e.getValue());
            }
        }
    }

    protected int getNextAvailableID(String name) {
        Integer nextID = nameToID.get(name);
        if (nextID != null) {
            return nextID;
        } else {
            nextID = usedIDs.nextClearBit(0);
        }
        while (blockedIDs.contains(nextID) || nameToID.values().contains(nextID)) {
            usedIDs.nextClearBit(nextID++);
        }
        return nextID;
    }

    protected void setID(String name) {
        setID(name, getNextAvailableID(name));
    }

    protected void setID(String name, int id) {
        nameToID.put(name, id);
        usedIDs.set(id, true);
    }

    protected File getSaveFile() {
        return new File(DimensionManager.getCurrentSaveRootDirectory(), ident + ".json");
    }

    public void addObject(I object, String name) {
        if (locked) {
            throw new IllegalStateException("Cannot add to world cache after world load!");
        }
        objToName.put(object, name);
    }

    public int getID(I obj) {
        return nameToID.get(getName(obj));
    }

    public String getName(I obj) {
        return objToName.get(obj);
    }

    public I getObject(int id) {
        return getObject(nameToID.inverse().get(id));
    }

    public I getObject(String string) {
        return objToName.inverse().get(string);
    }

    public Iterator<I> getObjects() {
        return objToName.keySet().iterator();
    }

    public TIntObjectMap<I> getEnumeratedObjects() {
        TIntObjectMap<I> ret = new TIntObjectHashMap<I>();
        for (int i = 0; i <= MAX_ID && usedIDs.nextSetBit(i) >= 0; i++) {
            if (usedIDs.get(i)) {
                ret.put(i, getObject(i));
            }
        }
        return ret;
    }
}
