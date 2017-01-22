package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.EnderCore;
import com.enderio.core.common.vecmath.Vector3f;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ItemUtil {

  /**
   * Turns a String into an item that can be used in a recipe. This is one of:
   * <ul>
   * <li>String</li>
   * <li>Item</li>
   * <li>Block</li>
   * <li>ItemStack</li>
   * </ul>
   * Because this method can return a String, it is highly recommended that you use the result in a {@link ShapedOreRecipe} or {@link ShapelessOreRecipe}.
   *
   * @param string
   *          The String to parse.
   * @return An object for use in recipes.
   */
  public static @Nullable Object parseStringIntoRecipeItem(@Nullable String string) {
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
   * Because this method can return a String, it is highly recommended that you use the result in a {@link ShapedOreRecipe} or {@link ShapelessOreRecipe}.
   *
   * @see #parseStringIntoItemStack(String)
   * @param string
   *          The String to parse.
   * @param forceItemStack
   *          True if the result should be forced to be an ItemStack.
   * @return AN object for use in recipes.
   */
  public static @Nullable Object parseStringIntoRecipeItem(@Nullable String string, boolean forceItemStack) {
    if (string == null || "null".equals(string)) {
      return null;
    } else if (OreDictionary.getOres(string).isEmpty()) {
      ItemStack stack = null;

      String[] info = string.split(";");
      Object temp = null;
      int damage = OreDictionary.WILDCARD_VALUE;
      temp = Item.REGISTRY.getObject(new ResourceLocation(NullHelper.notnullJ(info[0], "String.split()")));
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
   * This is basically a convenience method that casts the result of {@link #parseStringIntoRecipeItem(String, boolean)}, but with one extra feature.
   * <p>
   * A '#' character may be used at the end of the string to signify stack size, e.g. "minecraft:diamond#2" would be a stack of 2 diamonds.
   * <p>
   * The stack size will automatically be clamped to be below the give Item's max stack size.
   *
   * @param string
   *          The String to parse.
   * @return An ItemStack the string represents.
   */
  public static @Nonnull ItemStack parseStringIntoItemStack(@Nonnull String string) {
    String value = string;
    int size = 1;
    int numIdx = value.indexOf('#');
    int nbtIdx = value.indexOf('$');

    NBTTagCompound tag = null;

    if (numIdx != -1) {
      String num = value.substring(numIdx + 1, nbtIdx == -1 ? value.length() : nbtIdx);

      try {
        size = Integer.parseInt(num);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(num + " is not a valid stack size", e);
      }

      value = value.replace('#' + num, "");
      nbtIdx -= num.length() + 1;
    }

    if (nbtIdx != -1) {
      String nbt = NullHelper.notnullJ(value.substring(nbtIdx + 1), "String.substring()");
      try {
        tag = JsonToNBT.getTagFromJson(nbt);
      } catch (NBTException e) {
        throw new IllegalArgumentException(nbt + " is not valid NBT json.", e);
      }

      value = value.replace('$' + nbt, "");
    }

    ItemStack stack = (ItemStack) parseStringIntoRecipeItem(value, true);
    if (stack == null) {
      return ItemStack.EMPTY;
    }
    stack.setCount(MathHelper.clamp(size, 1, stack.getMaxStackSize()));
    stack.setTagCompound(tag);
    return stack;
  }

  /**
   * Returns the appropriate config string for the given {@link ItemStack}
   * <p>
   * This does not take into account ore dict.
   *
   * @param stack
   *          The {@link ItemStack} to serialize
   * @param damage
   *          If damage should be taken into account
   * @param size
   *          If stack size should be taken into account
   * @return A string that will be the equivalent of if {@link ItemStack stack} was constructed from it using {@link #parseStringIntoItemStack(String)}
   */
  public static String getStringForItemStack(@Nonnull ItemStack stack, boolean damage, boolean size) {
    if (stack.isEmpty()) {
      return null;
    }

    String base = stack.getItem().getRegistryName().toString();

    if (damage) {
      base += ";" + stack.getItemDamage();
    }

    if (size) {
      base += "#" + stack.getCount();
    }

    return base;
  }

  public static void spawnItemInWorldWithRandomMotion(@Nonnull World world, @Nonnull ItemStack item, @Nonnull BlockPos pos) {
    spawnItemInWorldWithRandomMotion(world, item, pos.getX(), pos.getY(), pos.getZ());
  }

  /**
   * Spawns an ItemStack into the world with motion that simulates a normal block drop.
   *
   * @param world
   *          The world object.
   * @param item
   *          The ItemStack to spawn.
   * @param x
   *          X coordinate of the block in which to spawn the entity.
   * @param y
   *          Y coordinate of the block in which to spawn the entity.
   * @param z
   *          Z coordinate of the block in which to spawn the entity.
   */
  public static void spawnItemInWorldWithRandomMotion(@Nonnull World world, @Nonnull ItemStack item, int x, int y, int z) {
    if (!item.isEmpty()) {
      spawnItemInWorldWithRandomMotion(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, item));
    }
  }

  /**
   * Spawns an ItemStack into the world with motion that simulates a normal block drop.
   *
   * @param world
   *          The world object.
   * @param item
   *          The ItemStack to spawn.
   * @param pos
   *          The block location to spawn the item.
   * @param hitX
   *          The location within the block to spawn the item at.
   * @param hitY
   *          The location within the block to spawn the item at.
   * @param hitZ
   *          The location within the block to spawn the item at.
   * @param scale
   *          The factor with which to push the spawn location out.
   */
  public static void spawnItemInWorldWithRandomMotion(@Nonnull World world, @Nonnull ItemStack item, @Nonnull BlockPos pos, float hitX, float hitY, float hitZ,
      float scale) {
    Vector3f v = new Vector3f((hitX - .5f), (hitY - .5f), (hitZ - .5f));
    v.normalize();
    v.scale(scale);
    float x = pos.getX() + .5f + v.x;
    float y = pos.getY() + .5f + v.y;
    float z = pos.getZ() + .5f + v.z;
    spawnItemInWorldWithRandomMotion(new EntityItem(world, x, y, z, item));
  }

  /**
   * Spawns an EntityItem into the world with motion that simulates a normal block drop.
   *
   * @param entity
   *          The entity to spawn.
   */
  public static void spawnItemInWorldWithRandomMotion(@Nonnull EntityItem entity) {
    entity.setDefaultPickupDelay();
    entity.world.spawnEntity(entity);
  }

  /**
   * Returns true if the given stack has the given Ore Dictionary name applied to it.
   *
   * @param stack
   *          The ItemStack to check.
   * @param oredict
   *          The oredict name.
   * @return True if the ItemStack matches the name passed.
   */
  public static boolean itemStackMatchesOredict(@Nonnull ItemStack stack, String oredict) {
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
   * Gets an NBT tag from an ItemStack, creating it if needed. The tag returned will always be the same as the one on the stack.
   *
   * @param stack
   *          The ItemStack to get the tag from.
   * @return An NBTTagCompound from the stack.
   */
  public static NBTTagCompound getNBTTag(ItemStack stack) {
    if (!stack.hasTagCompound()) {
      stack.setTagCompound(new NBTTagCompound());
    }
    return stack.getTagCompound();
  }

  public static String getDurabilityString(@Nonnull ItemStack item) {
    if (item.isEmpty()) {
      return null;
    }
    return EnderCore.lang.localize("tooltip.durability") + " " + (item.getMaxDamage() - item.getItemDamage()) + "/" + item.getMaxDamage();
  }

  public static NBTTagCompound getOrCreateNBT(ItemStack stack) {
    if (!stack.hasTagCompound()) {
      stack.setTagCompound(new NBTTagCompound());
    }
    return stack.getTagCompound();
  }

  public static boolean isStackFull(@Nonnull ItemStack contents) {
    return contents.getCount() >= contents.getMaxStackSize();
  }

  /**
   * Checks if items, damage and NBT are equal and the items are stackable.
   *
   * @param s1
   * @param s2
   * @return True if the two stacks are mergeable, false otherwise.
   */
  public static boolean areStackMergable(@Nonnull ItemStack s1, @Nonnull ItemStack s2) {
    if (s1.isEmpty() || s2.isEmpty() || !s1.isStackable() || !s2.isStackable()) {
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
  public static boolean areStacksEqual(@Nonnull ItemStack s1, @Nonnull ItemStack s2) {
    if (s1.isEmpty() || s2.isEmpty()) {
      return false;
    }
    if (!s1.isItemEqual(s2)) {
      return false;
    }
    return ItemStack.areItemStackTagsEqual(s1, s2);
  }

}
