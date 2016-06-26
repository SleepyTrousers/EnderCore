package com.enderio.core.common;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDict {

  public static void registerVanilla() {
    safeRegister("barsIron", Blocks.IRON_BARS);
    safeRegister("blockHopper", Blocks.HOPPER);         
    safeRegister("itemCoal", Items.COAL);
    safeRegister("itemCharcoal", new ItemStack(Items.COAL, 1, 1));   
    safeRegister("pearlEnderEye", Items.ENDER_EYE);
    safeRegister("itemBlazeRod", Items.BLAZE_ROD);
    safeRegister("itemBlazePowder", Items.BLAZE_POWDER);
    safeRegister("itemClay", Items.CLAY_BALL);
    safeRegister("itemFlint", Items.FLINT);
    safeRegister("itemGhastTear", Items.GHAST_TEAR);    
    safeRegister("itemLeather", Items.LEATHER);
    safeRegister("slabWoodOak", new ItemStack(Blocks.WOODEN_SLAB, 1, 0));
    safeRegister("slabWoodSpruce", new ItemStack(Blocks.WOODEN_SLAB, 1, 1));
    safeRegister("slabWoodBirch", new ItemStack(Blocks.WOODEN_SLAB, 1, 2));
    safeRegister("slabWoodJungle", new ItemStack(Blocks.WOODEN_SLAB, 1, 3));
    safeRegister("slabWoodAcacia", new ItemStack(Blocks.WOODEN_SLAB, 1, 4));
    safeRegister("slabWoodDarkOak", new ItemStack(Blocks.WOODEN_SLAB, 1, 5));
    safeRegister("slabStone", new ItemStack(Blocks.STONE_SLAB, 1, 0));
    safeRegister("slabSandstone", new ItemStack(Blocks.STONE_SLAB, 1, 1));
    safeRegister("slabCobblestone", new ItemStack(Blocks.STONE_SLAB, 1, 3));
    safeRegister("slabBricks", new ItemStack(Blocks.STONE_SLAB, 1, 4));
    safeRegister("slabStoneBricks", new ItemStack(Blocks.STONE_SLAB, 1, 5));
    safeRegister("slabNetherBrick", new ItemStack(Blocks.STONE_SLAB, 1, 6));
    safeRegister("slabQuartz", new ItemStack(Blocks.STONE_SLAB, 1, 7));
  }

  public static void safeRegister(String name, Block block) {
    safeRegister(name, Item.getItemFromBlock(block));
  }

  public static void safeRegister(String name, Item item) {
    safeRegister(name, new ItemStack(item));
  }

  public static void safeRegister(String name, ItemStack stack) {
    if (!isRegistered(stack, OreDictionary.getOres(name))) OreDictionary.registerOre(name, stack);
  }

  private static boolean isRegistered(ItemStack stack, List<ItemStack> toCheck) {
    for (ItemStack check : toCheck) {
      if (stack != null && stack.getItem() == check.getItem() && (stack.getItemDamage() == check.getItemDamage() || stack.getItemDamage() == OreDictionary.WILDCARD_VALUE)) {
        return true;
      }
    }
    return false;
  }

  private OreDict() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }
}