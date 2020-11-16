package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

class ItemThing implements IThing {

  private final @Nonnull Item thing;

  ItemThing(@Nonnull Item item) {
    this.thing = item;
  }

  @Override
  public @Nonnull NNList<IThing> bake() {
    return new NNList<>(this);
  }

  @Override
  public boolean is(@Nullable Item item) {
    return item == thing;
  }

  @Override
  public boolean is(@Nullable ItemStack itemStack) {
    return itemStack != null && itemStack.getItem() == thing;
  }

  @Override
  public boolean is(@Nullable Block block) {
    return block != null && (Item.getItemFromBlock(block) == thing || Block.getBlockFromItem(thing) == block);
  }

  @Override
  public @Nonnull NNList<Item> getItems() {
    return new NNList<Item>(thing);
  }

  @Override
  public @Nonnull NNList<ItemStack> getItemStacks() {
    return new NNList<ItemStack>(new ItemStack(thing));
  }

  @Override
  public @Nonnull NNList<Block> getBlocks() {
    Block block = Block.getBlockFromItem(thing);
    return block != Blocks.AIR ? new NNList<Block>(block) : NNList.<Block> emptyList();
  }

  @Override
  public String toString() {
    return String.format("ItemThing [thing=%s]", thing);
  }

}