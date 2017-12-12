package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

interface IThing {

  @Nullable
  IThing bake();
  
  boolean is(@Nullable Item item);

  boolean is(@Nullable ItemStack itemStack);

  boolean is(@Nullable Block block);

  @Nonnull
  NNList<Item> getItems();

  @Nonnull
  NNList<ItemStack> getItemStacks();

  @Nonnull
  NNList<Block> getBlocks();

}