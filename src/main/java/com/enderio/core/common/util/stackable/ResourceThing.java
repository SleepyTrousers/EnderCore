package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

class ResourceThing implements IThing {

  private final @Nonnull ResourceLocation resourceLocation;

  ResourceThing(@Nonnull ResourceLocation resourceLocation) {
    this.resourceLocation = resourceLocation;
  }

  @Override
  public @Nonnull NNList<IThing> bake() {
    if (ForgeRegistries.BLOCKS.containsKey(resourceLocation)) {
      return new BlockThing(ForgeRegistries.BLOCKS.getValue(resourceLocation)).bake();
    }
    if (ForgeRegistries.ITEMS.containsKey(resourceLocation)) {
      return new ItemThing(ForgeRegistries.ITEMS.getValue(resourceLocation)).bake();
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
    return String.format("ResourceThing [resourceLocation=%s]", resourceLocation);
  }

}