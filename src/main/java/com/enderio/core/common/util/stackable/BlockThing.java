package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemStack;

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
    for (Item candidate : Item.REGISTRY) {
      if (candidate instanceof ItemBlockSpecial && ((ItemBlockSpecial) candidate).getBlock() == block) {
        return candidate;
      }
      if (candidate instanceof ItemBlock && ((ItemBlock) candidate).getBlock() == block) {
        // "bad data" case, but mods may produce it...
        return candidate;
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