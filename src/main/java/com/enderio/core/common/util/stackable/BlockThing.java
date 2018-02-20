package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

class BlockThing implements IThing {

  private final @Nonnull Block thing;

  public BlockThing(@Nonnull Block block) {
    this.thing = block;
  }

  @Override
  public @Nullable IThing bake() {
    return this;
  }

  @Override
  public boolean is(@Nullable Item item) {
    return Item.getItemFromBlock(thing) == item || Block.getBlockFromItem(item) == thing;
  }

  @Override
  public boolean is(@Nullable ItemStack itemStack) {
    return itemStack != null && !itemStack.isEmpty()
        && (Item.getItemFromBlock(thing) == itemStack.getItem() || Block.getBlockFromItem(itemStack.getItem()) == thing);
  }

  @Override
  public boolean is(@Nullable Block block) {
    return this.thing == block;
  }

  @Override
  public @Nonnull NNList<Item> getItems() {
    Item item = Item.getItemFromBlock(thing);
    return item != Items.AIR ? new NNList<Item>(item) : NNList.<Item> emptyList();
  }

  @Override
  public @Nonnull NNList<ItemStack> getItemStacks() {
    Item item = Item.getItemFromBlock(thing);
    return item != Items.AIR ? new NNList<ItemStack>(new ItemStack(item)) : NNList.<ItemStack> emptyList();
  }

  @Override
  public @Nonnull NNList<Block> getBlocks() {
    return new NNList<Block>(thing);
  }

  @Override
  public String toString() {
    return String.format("BlockThing [thing=%s]", thing);
  }

}