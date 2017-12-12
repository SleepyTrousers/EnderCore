package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RebuildableThings extends Things {

  private final @Nonnull NNList<String> nameList = new NNList<>();

  public @Nonnull NNList<String> getNameList() {
    return nameList;
  }

  public RebuildableThings(String... names) {
    super();
    init(names);
  }

  public @Nonnull Things addAll(@Nonnull NNList<String> names) {
    for (String name : names) {
      add(name);
    }
    return this;
  }

  @Override
  public @Nonnull Things add(@Nullable Item item) {
    if (item != null) {
      nameList.add("item:" + item.getRegistryName());
    }
    return super.add(item);
  }

  @Override
  public @Nonnull Things add(@Nullable ItemStack itemStack) {
    if (itemStack != null && !itemStack.isEmpty()) {
      nameList.add("item:" + itemStack.getItem().getRegistryName());
    }
    return super.add(itemStack);
  }

  @Override
  public @Nonnull Things add(@Nullable Block block) {
    if (block != null) {
      nameList.add("block:" + block.getRegistryName());
    }
    return super.add(block);
  }

  @Override
  public @Nonnull Things add(@Nullable String name) {
    if (name != null) {
      nameList.add(name);
    }
    return super.add(name);
  }

  @Override
  public @Nonnull Things add(@Nullable IProducer producer) {
    if (producer != null) {
      Block block = producer.getBlock();
      if (block != null) {
        nameList.add("block:" + block.getRegistryName());
      }
      Item item = producer.getItem();
      if (item != null) {
        nameList.add("item:" + item.getRegistryName());
      }
    }
    return super.add(producer);
  }

  @Override
  public @Nonnull Things add(@Nullable ResourceLocation resourceLocation) {
    if (resourceLocation != null) {
      if (net.minecraft.block.Block.REGISTRY.containsKey(resourceLocation)) {
        Block block = net.minecraft.block.Block.REGISTRY.getObject(resourceLocation);
        nameList.add("block:" + block.getRegistryName());
      }
      // this ugly thing seems to be what Forge wants you to use
      Item item = net.minecraft.item.Item.REGISTRY.getObject(resourceLocation);
      if (item != null) {
        nameList.add("item:" + item.getRegistryName());
      }
    }
    return super.add(resourceLocation);
  }

  @Override
  public @Nonnull Things addOredict(@Nullable String name) {
    if (name != null) {
      nameList.add("oredict:" + name);
    }
    return super.addOredict(name);
  }

  @Override
  public @Nonnull Things add(@Nullable Things otherThings) {
    if (otherThings instanceof RebuildableThings) {
      nameList.addAll(((RebuildableThings) otherThings).nameList);
    }
    return super.add(otherThings);
  }

}
