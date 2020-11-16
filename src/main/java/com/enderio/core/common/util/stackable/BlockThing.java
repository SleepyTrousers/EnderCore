package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.RegistryKey;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

class BlockThing implements IThing {

  private final @Nonnull Block thing;
  private final @Nullable Item blockItem;

  public BlockThing(@Nonnull Block block) {
    this.thing = block;
    this.blockItem = findBlockItem(block);
  }

  public static @Nullable Item findBlockItem(@Nonnull Block block) {
    Item item = Item.getItemFromBlock(block);
    if (item != Items.AIR) {
      return item;
    }
    for (Map.Entry<RegistryKey<Item>, Item>  candidate : ForgeRegistries.ITEMS.getEntries()) {
      Item blockItem = candidate.getValue();
      if (blockItem instanceof BlockItem && ((BlockItem) blockItem).getBlock() == block) {
        return blockItem;
      }
    }
    return null;
  }

  @Override
  public @Nonnull NNList<IThing> bake() {
    return new NNList<>(this);
  }

  @Override
  public boolean is(@Nullable Item item) {
    return blockItem == item;
  }

  @Override
  public boolean is(@Nullable ItemStack itemStack) {
    return itemStack != null && !itemStack.isEmpty() && is(itemStack.getItem());
  }

  @Override
  public boolean is(@Nullable Block block) {
    return this.thing == block;
  }

  @Override
  public @Nonnull NNList<Item> getItems() {
    return blockItem != null ? new NNList<Item>(blockItem) : NNList.<Item> emptyList();
  }

  @Override
  public @Nonnull NNList<ItemStack> getItemStacks() {
    return blockItem != null ? new NNList<ItemStack>(new ItemStack(blockItem)) : NNList.<ItemStack> emptyList();
  }

  @Override
  public @Nonnull NNList<Block> getBlocks() {
    return new NNList<Block>(thing);
  }

  @Override
  public String toString() {
    return String.format("BlockThing [thing=%s, item= %s]", thing, blockItem);
  }

}