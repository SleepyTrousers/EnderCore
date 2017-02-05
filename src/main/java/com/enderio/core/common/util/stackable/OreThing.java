package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

class OreThing implements IThing {

  private final @Nonnull String name;
  private @Nonnull NNList<ItemStack> ores = new NNList<ItemStack>();

  OreThing(@Nonnull String name) {
    this.name = name;
  }

  @Override
  public @Nullable IThing bake() {
    if (OreDictionary.doesOreNameExist(name)) {
      ores.clear();
      ores.addAll(OreDictionary.getOres(name));
      if (!ores.isEmpty()) {
        return this;
      }
    }
    return null;
  }

  @Override
  public boolean is(@Nullable Item item) {
    for (ItemStack oreStack : ores) {
      if (oreStack.getItem() == item) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean is(@Nullable ItemStack itemStack) {
    for (ItemStack oreStack : ores) {
      if (itemStack != null && !itemStack.isEmpty() && itemStack.getItem() == oreStack.getItem()
          && (!oreStack.getHasSubtypes() || oreStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || oreStack.getMetadata() == itemStack.getMetadata())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean is(@Nullable Block block) {
    for (ItemStack oreStack : ores) {
      if (block != null && (Item.getItemFromBlock(block) == oreStack.getItem() || Block.getBlockFromItem(oreStack.getItem()) == block)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @Nonnull NNList<Item> getItems() {
    NNList<Item> result = new NNList<Item>();
    for (ItemStack oreStack : ores) {
      if (!oreStack.isEmpty() && !result.contains(oreStack.getItem())) {
        result.add(oreStack.getItem());
      }
    }
    return result;
  }

  @Override
  public @Nonnull NNList<ItemStack> getItemStacks() {
    return ores;
  }

  @Override
  public @Nonnull NNList<Block> getBlocks() {
    NNList<Block> result = new NNList<Block>();
    for (ItemStack oreStack : ores) {
      if (!oreStack.isEmpty()) {
        Block block = Block.getBlockFromItem(oreStack.getItem());
        if (block != Blocks.AIR) {
          result.add(block);
        }
      }
    }
    return result;
  }

  @Override
  public @Nullable Object getRecipeObject() {
    return name;
  }

}