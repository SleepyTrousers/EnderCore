package com.enderio.core.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.util.IItemReceptor;

import cofh.api.transport.IItemDuct;
import cpw.mods.fml.common.Loader;

public class ItemUtil {

    public static final List<IItemReceptor> receptors = new ArrayList<IItemReceptor>();
    private static final Random rand = new Random();

    static {
        try {
            Class.forName("crazypants.util.BuildcraftUtil");
        } catch (Exception e) {
            if (Loader.isModLoaded("BuildCraft|Transport")) {
                Log.warn(
                        "ItemUtil: Could not register Build Craft pipe handler. Machines will not be able to output to BC pipes.");
            } // Don't log if BC isn't installed, but we still check in case another mod is using their API
        }
    }

    /**
     * Turns a String into an item that can be used in a recipe. This is one of:
     * <ul>
     * <li>String</li>
     * <li>Item</li>
     * <li>Block</li>
     * <li>ItemStack</li>
     * </ul>
     * Because this method can return a String, it is highly recommended that you use the result in a
     * {@link ShapedOreRecipe} or {@link ShapelessOreRecipe}.
     * 
     * @param string The String to parse.
     * @return An object for use in recipes.
     */
    public static Object parseStringIntoRecipeItem(String string) {
        return parseStringIntoRecipeItem(string, false);
    }

    /**
     * Turns a String into an item that can be used in a recipe. This is one of:
     * <ul>
     * <li>String</li>
     * <li>Item</li>
     * <li>Block</li>
     * <li>ItemStack</li>
     * </ul>
     * Because this method can return a String, it is highly recommended that you use the result in a
     * {@link ShapedOreRecipe} or {@link ShapelessOreRecipe}.
     * 
     * @see #parseStringIntoItemStack(String)
     * @param string         The String to parse.
     * @param forceItemStack True if the result should be forced to be an ItemStack.
     * @return AN object for use in recipes.
     */
    public static Object parseStringIntoRecipeItem(String string, boolean forceItemStack) {
        if ("null".equals(string)) {
            return null;
        } else if (OreDictionary.getOres(string).isEmpty()) {
            ItemStack stack = null;

            String[] info = string.split(";");
            Object temp = null;
            int damage = OreDictionary.WILDCARD_VALUE;
            temp = Item.itemRegistry.getObject(info[0]);
            if (info.length > 1) {
                damage = Integer.parseInt(info[1]);
            }

            if (temp instanceof Item) {
                stack = new ItemStack((Item) temp, 1, damage);
            } else if (temp instanceof Block) {
                stack = new ItemStack((Block) temp, 1, damage);
            } else if (temp instanceof ItemStack) {
                stack = ((ItemStack) temp).copy();
                stack.setItemDamage(damage);
            } else {
                throw new IllegalArgumentException(
                        string + " is not a vaild string. Strings should be either an oredict name, or in the format objectname;damage (damage is optional)");
            }

            return stack;
        } else if (forceItemStack) {
            return OreDictionary.getOres(string).get(0).copy();
        } else {
            return string;
        }
    }

    /**
     * Turns a string into an ItemStack.
     * <p>
     * This is basically a convenience method that casts the result of
     * {@link #parseStringIntoRecipeItem(String, boolean)}, but with one extra feature.
     * <p>
     * A '#' character may be used at the end of the string to signify stack size, e.g. "minecraft:diamond#2" would be a
     * stack of 2 diamonds.
     * <p>
     * The stack size will automatically be clamped to be below the give Item's max stack size.
     * 
     * @param string The String to parse.
     * @return An ItemStack the string represents.
     */
    public static ItemStack parseStringIntoItemStack(String string) {
        int size = 1;
        int numIdx = string.indexOf('#');
        int nbtIdx = string.indexOf('$');

        NBTTagCompound tag = null;

        if (numIdx > -1) {
            String num = string.substring(numIdx + 1, nbtIdx == -1 ? string.length() : nbtIdx);

            try {
                size = Integer.parseInt(num);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(num + " is not a valid stack size", e);
            }

            string = string.replace('#' + num, "");
            nbtIdx -= num.length() + 1;
        }

        if (nbtIdx > -1) {
            String nbt = string.substring(nbtIdx + 1);
            try {
                tag = (NBTTagCompound) JsonToNBT.func_150315_a(nbt);
            } catch (NBTException e) {
                throw new IllegalArgumentException(nbt + " is not valid NBT json.", e);
            }

            string = string.replace('$' + nbt, "");
        }

        ItemStack stack = (ItemStack) parseStringIntoRecipeItem(string, true);
        stack.stackSize = MathHelper.clamp_int(size, 1, stack.getMaxStackSize());
        stack.setTagCompound(tag);
        return stack;
    }

    /**
     * Returns the appropriate config string for the given {@link ItemStack}
     * <p>
     * This does not take into account ore dict.
     * 
     * @param stack  The {@link ItemStack} to serialize
     * @param damage If damage should be taken into account
     * @param size   If stack size should be taken into account
     * @return A string that will be the equivalent of if {@link ItemStack stack} was constructed from it using
     *         {@link #parseStringIntoItemStack(String)}
     */
    public static String getStringForItemStack(ItemStack stack, boolean damage, boolean size) {
        if (stack == null) {
            return null;
        }

        String base = Item.itemRegistry.getNameForObject(stack.getItem());

        if (damage) {
            base += ";" + stack.getItemDamage();
        }

        if (size) {
            base += "#" + stack.stackSize;
        }

        return base;
    }

    /**
     * Tests if two {@link ItemStack}s are completely equal, forgoing stack size. This means that for this method to
     * return true, Item type, damage value, and NBT data of both ItemStacks must be identical.
     * 
     * @param s1 The first ItemStack to compare.
     * @param s2 The second ItemStack to compare.
     * @author powercrystals
     */
    public static boolean stacksEqual(ItemStack s1, ItemStack s2) {
        if (s1 == null && s2 == null) return true;
        if (s1 == null || s2 == null) return false;
        if (!s1.isItemEqual(s2)) return false;
        if (s1.getTagCompound() == null && s2.getTagCompound() == null) return true;
        if (s1.getTagCompound() == null || s2.getTagCompound() == null) return false;
        return s1.getTagCompound().equals(s2.getTagCompound());
    }

    /**
     * Spawns an ItemStack into the world with motion that simulates a normal block drop.
     * 
     * @param world The world object.
     * @param item  The ItemStack to spawn.
     * @param x     X coordinate of the block in which to spawn the entity.
     * @param y     Y coordinate of the block in which to spawn the entity.
     * @param z     Z coordinate of the block in which to spawn the entity.
     */
    public static void spawnItemInWorldWithRandomMotion(World world, ItemStack item, int x, int y, int z) {
        if (item != null) {
            spawnItemInWorldWithRandomMotion(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, item));
        }
    }

    /**
     * Spawns an EntityItem into the world with motion that simulates a normal block drop.
     * 
     * @param entity The entity to spawn.
     */
    public static void spawnItemInWorldWithRandomMotion(EntityItem entity) {
        entity.delayBeforeCanPickup = 10;

        float f = (rand.nextFloat() * 0.1f) - 0.05f;
        float f1 = (rand.nextFloat() * 0.1f) - 0.05f;
        float f2 = (rand.nextFloat() * 0.1f) - 0.05f;

        entity.motionX += f;
        entity.motionY += f1;
        entity.motionZ += f2;

        entity.worldObj.spawnEntityInWorld(entity);
    }

    /**
     * Returns true if the given stack has the given Ore Dictionary name applied to it.
     * 
     * @param stack   The ItemStack to check.
     * @param oredict The oredict name.
     * @return True if the ItemStack matches the name passed.
     */
    public static boolean itemStackMatchesOredict(ItemStack stack, String oredict) {
        int[] ids = OreDictionary.getOreIDs(stack);
        for (int i : ids) {
            String name = OreDictionary.getOreName(i);
            if (name.equals(oredict)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets an NBT tag from an ItemStack, creating it if needed. The tag returned will always be the same as the one on
     * the stack.
     * 
     * @param stack The ItemStack to get the tag from.
     * @return An NBTTagCompound from the stack.
     */
    public static NBTTagCompound getNBTTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.stackTagCompound = new NBTTagCompound();
        }
        return stack.stackTagCompound;
    }

    public static String getDurabilityString(ItemStack item) {
        if (item == null) {
            return null;
        }
        return EnderCore.lang.localize("tooltip.durability") + " "
                + (item.getMaxDamage() - item.getItemDamage())
                + "/"
                + item.getMaxDamage();
    }

    public static NBTTagCompound getOrCreateNBT(ItemStack stack) {
        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        }
        return stack.stackTagCompound;
    }

    public static int doInsertItem(Object into, ItemStack item, ForgeDirection side) {
        if (into == null || item == null) {
            return 0;
        }
        if (into instanceof ISidedInventory) {
            return ItemUtil.doInsertItemInv((ISidedInventory) into, item, side, true);
        } else if (into instanceof IInventory) {
            return ItemUtil.doInsertItemInv(getInventory((IInventory) into), item, side, true);
        } else if (into instanceof IItemDuct) {
            return ItemUtil.doInsertItem((IItemDuct) into, item, side);
        }

        for (IItemReceptor rec : receptors) {
            if (rec.canInsertIntoObject(into, side)) {
                return rec.doInsertItem(into, item, side);
            }
        }

        return 0;
    }

    public static int doInsertItem(IItemDuct con, ItemStack item, ForgeDirection inventorySide) {
        int startedWith = item.stackSize;
        ItemStack remaining = con.insertItem(inventorySide, item);
        if (remaining == null) {
            return startedWith;
        }
        return startedWith - remaining.stackSize;
    }

    public static int doInsertItem(IInventory inv, int startSlot, int endSlot, ItemStack item) {
        return doInsertItemInv(
                inv,
                null,
                invSlotter.getInstance(startSlot, endSlot),
                item,
                ForgeDirection.UNKNOWN,
                true);
    }

    public static int doInsertItem(IInventory inv, int startSlot, int endSlot, ItemStack item, boolean doInsert) {
        return doInsertItemInv(
                inv,
                null,
                invSlotter.getInstance(startSlot, endSlot),
                item,
                ForgeDirection.UNKNOWN,
                doInsert);
    }

    /*
     * Insert items into an IInventory or an ISidedInventory.
     */
    private static int doInsertItemInv(IInventory inv, ItemStack item, ForgeDirection inventorySide, boolean doInsert) {
        final ISidedInventory sidedInv = inv instanceof ISidedInventory ? (ISidedInventory) inv : null;
        ISlotIterator slots;

        if (sidedInv != null) {
            if (inventorySide == null) {
                inventorySide = ForgeDirection.UNKNOWN;
            }
            // Note: This is not thread-safe. Change to getInstance() to constructor when needed (1.8++?).
            slots = sidedSlotter.getInstance(sidedInv.getAccessibleSlotsFromSide(inventorySide.ordinal()));
        } else {
            slots = invSlotter.getInstance(0, inv.getSizeInventory());
        }

        return doInsertItemInv(inv, sidedInv, slots, item, inventorySide, doInsert);
    }

    private static int doInsertItemInv(IInventory inv, ISidedInventory sidedInv, ISlotIterator slots, ItemStack item,
            ForgeDirection inventorySide, boolean doInsert) {
        int numInserted = 0;
        int numToInsert = item.stackSize;
        int firstFreeSlot = -1;

        // PASS1: Try to add to an existing stack
        while (numToInsert > 0 && slots.hasNext()) {
            final int slot = slots.nextSlot();
            if (sidedInv == null || sidedInv.canInsertItem(slot, item, inventorySide.ordinal())) {
                final ItemStack contents = inv.getStackInSlot(slot);
                if (contents != null) {
                    if (areStackMergable(contents, item)) {
                        final int freeSpace = Math.min(inv.getInventoryStackLimit(), contents.getMaxStackSize())
                                - contents.stackSize; // some inventories like using itemstacks with invalid stack sizes
                        if (freeSpace > 0) {
                            final int noToInsert = Math.min(numToInsert, freeSpace);
                            final ItemStack toInsert = item.copy();
                            toInsert.stackSize = contents.stackSize + noToInsert;
                            // isItemValidForSlot() may check the stacksize, so give it the number the stack would have
                            // in the end.
                            // If it does something funny, like "only even numbers", we are screwed.
                            if (sidedInv != null || inv.isItemValidForSlot(slot, toInsert)) {
                                numInserted += noToInsert;
                                numToInsert -= noToInsert;
                                if (doInsert) {
                                    inv.setInventorySlotContents(slot, toInsert);
                                }
                            }
                        }
                    }
                } else if (firstFreeSlot == -1) {
                    firstFreeSlot = slot;
                }
            }
        }

        // PASS2: Try to insert into an empty slot
        if (numToInsert > 0 && firstFreeSlot != -1) {
            final ItemStack toInsert = item.copy();
            toInsert.stackSize = min(numToInsert, inv.getInventoryStackLimit(), toInsert.getMaxStackSize()); // some
                                                                                                             // inventories
                                                                                                             // like
                                                                                                             // using
                                                                                                             // itemstacks
                                                                                                             // with
                                                                                                             // invalid
                                                                                                             // stack
                                                                                                             // sizes
            if (sidedInv != null || inv.isItemValidForSlot(firstFreeSlot, toInsert)) {
                numInserted += toInsert.stackSize;
                numToInsert -= toInsert.stackSize;
                if (doInsert) {
                    inv.setInventorySlotContents(firstFreeSlot, toInsert);
                }
            }
        }

        if (numInserted > 0 && doInsert) {
            inv.markDirty();
        }
        return numInserted;
    }

    private final static int min(int i1, int i2, int i3) {
        return i1 < i2 ? (i1 < i3 ? i1 : i3) : (i2 < i3 ? i2 : i3);
    }

    public static boolean isStackFull(ItemStack contents) {
        if (contents == null) {
            return false;
        }
        return contents.stackSize >= contents.getMaxStackSize();
    }

    public static IInventory getInventory(IInventory inv) {
        if (inv instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) inv;
            TileEntityChest neighbour = null;
            boolean reverse = false;
            if (chest.adjacentChestXNeg != null) {
                neighbour = chest.adjacentChestXNeg;
                reverse = true;
            } else if (chest.adjacentChestXPos != null) {
                neighbour = chest.adjacentChestXPos;
            } else if (chest.adjacentChestZNeg != null) {
                neighbour = chest.adjacentChestZNeg;
                reverse = true;
            } else if (chest.adjacentChestZPos != null) {
                neighbour = chest.adjacentChestZPos;
            }
            if (neighbour != null) {
                if (reverse) {
                    return new InventoryLargeChest("", neighbour, inv);
                } else {
                    return new InventoryLargeChest("", inv, neighbour);
                }
            }
        }
        return inv;
    }

    /**
     * Checks if items, damage and NBT are equal and the items are stackable.
     * 
     * @param s1
     * @param s2
     * @return True if the two stacks are mergeable, false otherwise.
     */
    public static boolean areStackMergable(ItemStack s1, ItemStack s2) {
        if (s1 == null || s2 == null || !s1.isStackable() || !s2.isStackable()) {
            return false;
        }
        if (!s1.isItemEqual(s2)) {
            return false;
        }
        return ItemStack.areItemStackTagsEqual(s1, s2);
    }

    /**
     * Checks if items, damage and NBT are equal.
     * 
     * @param s1
     * @param s2
     * @return True if the two stacks are equal, false otherwise.
     */
    public static boolean areStacksEqual(ItemStack s1, ItemStack s2) {
        if (s1 == null || s2 == null) {
            return false;
        }
        if (!s1.isItemEqual(s2)) {
            return false;
        }
        return ItemStack.areItemStackTagsEqual(s1, s2);
    }

    private interface ISlotIterator {

        int nextSlot();

        boolean hasNext();
    }

    private final static class invSlotter implements ISlotIterator {

        private static final invSlotter me = new invSlotter();
        private int end;
        private int current;

        public final static invSlotter getInstance(int start, int end) {
            me.end = end;
            me.current = start;
            return me;
        }

        @Override
        public final int nextSlot() {
            return current++;
        }

        @Override
        public final boolean hasNext() {
            return current < end;
        }
    }

    private final static class sidedSlotter implements ISlotIterator {

        private static final sidedSlotter me = new sidedSlotter();
        private int[] slots;
        private int current;

        public final static sidedSlotter getInstance(int[] slots) {
            me.slots = slots;
            me.current = 0;
            return me;
        }

        @Override
        public final int nextSlot() {
            return slots[current++];
        }

        @Override
        public final boolean hasNext() {
            return slots != null && current < slots.length;
        }
    }

}
