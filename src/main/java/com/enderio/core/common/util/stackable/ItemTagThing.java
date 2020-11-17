package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

class ItemTagThing implements IThing.Zwieback {

  private final @Nonnull ITag.INamedTag<Item> tag;
  private @Nonnull NNList<Item> taggedItems = new NNList<Item>();

  ItemTagThing(@Nonnull ITag.INamedTag<Item> tag) {
    this.tag = tag;
  }

  @Override
  public @Nonnull NNList<IThing> bake() {
    taggedItems = NNList.wrap(tag.getAllElements());
    return new NNList<>(this);
  }

  @Override
  public @Nullable IThing rebake() {
    // BIG TODO: REBAKE WHEN TAGS ARE RELOADED
    return taggedItems.isEmpty() ? null : this;
  }

  @Override
  public boolean is(@Nullable Item item) {
    for (Item tagItem : taggedItems) {
      if (tagItem == item) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean is(@Nullable ItemStack itemStack) {
    for (Item tagItem : taggedItems) {
      // TODO: Do we have alternatives to the old OreThing checks that we should use?
      if (itemStack != null && !itemStack.isEmpty() && itemStack.getItem() == tagItem) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean is(@Nullable Block block) {
    for (Item tagItem : taggedItems) {
      if (block != null && (Item.getItemFromBlock(block) == tagItem.getItem() || Block.getBlockFromItem(tagItem.getItem()) == block)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @Nonnull NNList<Item> getItems() {
    return taggedItems;
  }

  @Override
  public @Nonnull NNList<ItemStack> getItemStacks() {
    NNList<ItemStack> result = new NNList<ItemStack>();
    for (Item tagItem : taggedItems) {
      if (!result.contains(tagItem)) { // <--- WRONG, tagItem is an Item not ItemStack now
        result.add(new ItemStack(tagItem.getItem()));
      }
    }
    return result;
  }

  @Override
  public @Nonnull NNList<Block> getBlocks() {
    NNList<Block> result = new NNList<Block>();
    for (Item tagItem : taggedItems) {
      Block block = Block.getBlockFromItem(tagItem.getItem());
      if (block != Blocks.AIR) {
        result.add(block);
      }
    }
    return result;
  }

  public @Nonnull ITag.INamedTag<Item> getTag() {
    return tag;
  }

  @Override
  public String toString() {
    return String.format("ItemTagThing [name=%s, ores=%s]", tag.getName(), taggedItems);
  }

}