package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

class ProducerThing implements IThing {

  private final @Nonnull IProducer producer;

  ProducerThing(@Nonnull IProducer producer) {
    this.producer = producer;
  }

  @Override
  public @Nonnull NNList<IThing> bake() {
    Block block = producer.getBlock();
    if (block != null) {
      return new BlockThing(block).bake();
    }
    Item item = producer.getItem();
    if (item != null) {
      return new ItemThing(item).bake();
    }
    return NNList.emptyList();
  }

  @Override
  public boolean is(@Nullable Item item) {
    return false;
  }

  @Override
  public boolean is(@Nullable ItemStack itemStack) {
    return false;
  }

  @Override
  public boolean is(@Nullable Block block) {
    return false;
  }

  @Override
  public @Nonnull NNList<Item> getItems() {
    return NNList.<Item> emptyList();
  }

  @Override
  public @Nonnull NNList<ItemStack> getItemStacks() {
    return NNList.<ItemStack> emptyList();
  }

  @Override
  public @Nonnull NNList<Block> getBlocks() {
    return NNList.<Block> emptyList();
  }

  @Override
  public String toString() {
    return String.format("ProducerThing []");
  }

}