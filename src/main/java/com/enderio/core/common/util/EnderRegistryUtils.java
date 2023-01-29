package com.enderio.core.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import org.apache.logging.log4j.Level;

import com.google.common.base.Throwables;
import com.google.common.collect.ObjectArrays;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderException;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.registry.GameData;

/**
 * This class provides a few utility methods to regain the ability to register items/blocks to a modid other than the
 * active mod's. This is useful when registering items/blocks inside Compat classes (as the active mod at that time is
 * always ttCore).
 */
public class EnderRegistryUtils {

    private static Method getMain;
    private static Method registerItem;
    private static Method registerBlock;

    static {
        try {
            getMain = GameData.class.getDeclaredMethod("getMain");
            getMain.setAccessible(true);
            registerItem = GameData.class.getDeclaredMethod("registerItem", Item.class, String.class, int.class);
            registerItem.setAccessible(true);
            registerBlock = GameData.class.getDeclaredMethod("registerBlock", Block.class, String.class, int.class);
            registerBlock.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers an item with a custom modid prefix. This method exists due to the fact that FML dropped support for
     * registering items to modids other than the currently active container.
     * 
     * @param item  The item to register
     * @param modid The modid to register the item to
     * @param name  The name of the item
     */
    public static void registerItem(Item item, String modid, String name) {
        try {
            registerItem.invoke(getMain.invoke(null), item, modid + ":" + name, -1);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Registers a block with a custom modid prefix. This method exists due to the fact that FML dropped support for
     * registering blocks to modids other than the currently active container.
     * 
     * @param block The block to register
     * @param modid The modid to register the block to
     * @param name  The name of the block
     */
    public static void registerBlock(Block block, String modid, String name) {
        registerBlock(block, null, modid, name);
    }

    /**
     * Registers a block with a custom modid prefix. This method exists due to the fact that FML dropped support for
     * registering blocks to modids other than the currently active container.
     * 
     * @param block        The block to register
     * @param itemclass    The class of the ItemBlock to associate with this block
     * @param modid        The modid to register the block to
     * @param name         The name of the block
     * @param itemCtorArgs Objects to pass as arguments to the ItemBlock.
     */
    public static void registerBlock(Block block, Class<? extends ItemBlock> itemclass, String modid, String name,
            Object... itemCtorArgs) {
        if (Loader.instance().isInState(LoaderState.CONSTRUCTING)) {
            FMLLog.warning(
                    "The mod %s is attempting to register a block whilst it it being constructed. This is bad modding practice - please use a proper mod lifecycle event.",
                    Loader.instance().activeModContainer());
        }
        try {
            assert block != null : "registerBlock: block cannot be null";
            ItemBlock i = null;
            if (itemclass != null) {
                Class<?>[] ctorArgClasses = new Class<?>[itemCtorArgs.length + 1];
                ctorArgClasses[0] = Block.class;
                for (int idx = 1; idx < ctorArgClasses.length; idx++) {
                    ctorArgClasses[idx] = itemCtorArgs[idx - 1].getClass();
                }
                Constructor<? extends ItemBlock> itemCtor = itemclass.getConstructor(ctorArgClasses);
                i = itemCtor.newInstance(ObjectArrays.concat(block, itemCtorArgs));
            }
            // block registration has to happen first
            registerBlock.invoke(getMain.invoke(null), block, modid + ":" + name, -1);
            if (i != null) {
                registerItem(i, modid, name);
            }
        } catch (Exception e) {
            FMLLog.log(Level.ERROR, e, "Caught an exception during block registration");
            throw new LoaderException(e);
        }
    }
}
