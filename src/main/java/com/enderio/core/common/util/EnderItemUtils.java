package com.enderio.core.common.util;

import java.util.Random;

import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@UtilityClass
public class EnderItemUtils
{
    private final Random rand = new Random();

    /**
     * Turns a String into an item that can be used in a recipe. This is one of:
     * <ul>
     * <li>String</li>
     * <li>Item</li>
     * <li>Block</li>
     * <li>ItemStack</li>
     * </ul>
     * Because this method can return a String, it is highly recommended that you use the result in a {@link ShapedOreRecipe} or
     * {@link ShapelessOreRecipe}.
     * 
     * @param string
     *            The String to parse.
     * @return An object for use in recipes.
     */
    public Object parseStringIntoRecipeItem(String string)
    {
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
     * Because this method can return a String, it is highly recommended that you use the result in a {@link ShapedOreRecipe} or
     * {@link ShapelessOreRecipe}.
     * 
     * @see #parseStringIntoItemStack(String)
     * @param string
     *            The String to parse.
     * @param forceItemStack
     *            True if the result should be forced to be an ItemStack.
     * @return AN object for use in recipes.
     */
    public Object parseStringIntoRecipeItem(String string, boolean forceItemStack)
    {
        if ("null".equals(string))
        {
            return null;
        }
        else if (OreDictionary.getOres(string).isEmpty())
        {
            ItemStack stack = null;

            String[] info = string.split(";");
            Object temp = null;
            int damage = OreDictionary.WILDCARD_VALUE;
            temp = Item.itemRegistry.getObject(info[0]);
            if (info.length > 1)
            {
                damage = Integer.parseInt(info[1]);
            }

            if (temp instanceof Item)
            {
                stack = new ItemStack((Item) temp, 1, damage);
            }
            else if (temp instanceof Block)
            {
                stack = new ItemStack((Block) temp, 1, damage);
            }
            else if (temp instanceof ItemStack)
            {
                stack = ((ItemStack) temp).copy();
                stack.setItemDamage(damage);
            }
            else
            {
                throw new IllegalArgumentException(string
                        + " is not a vaild string. Strings should be either an oredict name, or in the format objectname;damage (damage is optional)");
            }

            return stack;
        }
        else if (forceItemStack)
        {
            return OreDictionary.getOres(string).get(0).copy();
        }
        else
        {
            return string;
        }
    }

    /**
     * Turns a string into an ItemStack.
     * <p>
     * This is basically a convenience method that casts the result of {@link #parseStringIntoRecipeItem(String, boolean)}, but with one extra
     * feature.
     * <p>
     * A '#' character may be used at the end of the string to signify stack size, e.g. "minecraft:diamond#2" would be a stack of 2 diamonds.
     * <p>
     * The stack size will automatically be clamped to be below the give Item's max stack size.
     * 
     * @param string
     *            The String to parse.
     * @return An ItemStack the string represents.
     */
    public ItemStack parseStringIntoItemStack(String string)
    {
        int size = 1;
        int idx = string.indexOf('#');

        if (idx != -1)
        {
            String num = string.substring(idx + 1);

            try
            {
                size = Integer.parseInt(num);
            }
            catch (NumberFormatException e)
            {
                throw new IllegalArgumentException(num + " is not a valid stack size");
            }

            string = string.substring(0, idx);
        }

        ItemStack stack = (ItemStack) parseStringIntoRecipeItem(string, true);
        stack.stackSize = MathHelper.clamp_int(size, 1, stack.getMaxStackSize());
        return stack;
    }

    /**
     * Returns the appropriate config string for the given {@link ItemStack}
     * <p>
     * This does not take into account ore dict.
     * 
     * @param stack
     *            The {@link ItemStack} to serialize
     * @param damage
     *            If damage should be taken into account
     * @param size
     *            If stack size should be taken into account
     * @return A string that will be the equivalent of if {@link ItemStack stack} was constructed from it using
     *         {@link #parseStringIntoItemStack(String)}
     */
    public String getStringForItemStack(ItemStack stack, boolean damage, boolean size)
    {
        if (stack == null)
        {
            return null;
        }

        String base = Item.itemRegistry.getNameForObject(stack.getItem());

        if (damage)
        {
            base += ";" + stack.getItemDamage();
        }

        if (size)
        {
            base += "#" + stack.stackSize;
        }

        return base;
    }

    /**
     * Tests if two {@link ItemStack}s are completely equal, forgoing stack size. This means that for this method to return true, Item type, damage
     * value, and NBT data of both ItemStacks must be identical.
     * 
     * @param s1
     *            The first ItemStack to compare.
     * @param s2
     *            The second ItemStack to compare.
     * @author powercrystals
     */
    public boolean stacksEqual(ItemStack s1, ItemStack s2)
    {
        if (s1 == null && s2 == null)
            return true;
        if (s1 == null || s2 == null)
            return false;
        if (!s1.isItemEqual(s2))
            return false;
        if (s1.getTagCompound() == null && s2.getTagCompound() == null)
            return true;
        if (s1.getTagCompound() == null || s2.getTagCompound() == null)
            return false;
        return s1.getTagCompound().equals(s2.getTagCompound());
    }

    /**
     * Spawns an ItemStack into the world with motion that simulates a normal block drop.
     * 
     * @param world
     *            The world object.
     * @param item
     *            The ItemStack to spawn.
     * @param x
     *            X coordinate of the block in which to spawn the entity.
     * @param y
     *            Y coordinate of the block in which to spawn the entity.
     * @param z
     *            Z coordinate of the block in which to spawn the entity.
     */
    public void spawnItemInWorldWithRandomMotion(World world, ItemStack item, int x, int y, int z)
    {
        if (item != null)
        {
            spawnItemInWorldWithRandomMotion(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, item));
        }
    }

    /**
     * Spawns an EntityItem into the world with motion that simulates a normal block drop.
     * 
     * @param entity
     *            The entity to spawn.
     */
    public void spawnItemInWorldWithRandomMotion(EntityItem entity)
    {
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
     * @param stack
     *            The ItemStack to check.
     * @param oredict
     *            The oredict name.
     * @return True if the ItemStack matches the name passed.
     */
    public boolean itemStackMatchesOredict(ItemStack stack, String oredict)
    {
        int[] ids = OreDictionary.getOreIDs(stack);
        for (int i : ids)
        {
            String name = OreDictionary.getOreName(i);
            if (name.equals(oredict))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets an NBT tag from an ItemStack, creating it if needed. The tag returned will always be the same as the one on the stack.
     * 
     * @param stack
     *            The ItemStack to get the tag from.
     * @return An NBTTagCompound from the stack.
     */
    public NBTTagCompound getNBTTag(ItemStack stack)
    {
        if (!stack.hasTagCompound())
        {
            stack.stackTagCompound = new NBTTagCompound();
        }
        return stack.stackTagCompound;
    }
}
