package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.NNList.NNIterator;
import com.enderio.core.common.util.stackable.IThing.Zwieback;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ExclusionThing implements Zwieback {

  private final @Nonnull NNList<IThing> positive, negative;

  ExclusionThing(@Nonnull NNList<IThing> positive, @Nonnull NNList<IThing> negative) {
    this.positive = positive;
    this.negative = negative;
  }

  @Override
  @Nullable
  public IThing rebake() {
    for (IThing thing : positive) {
      if (thing instanceof Zwieback) {
        ((Zwieback) thing).rebake();
      }
    }
    for (IThing thing : negative) {
      if (thing instanceof Zwieback) {
        ((Zwieback) thing).rebake();
      }
    }
    return null;
  }

  @Override
  @Nonnull
  public NNList<IThing> bake() {
    return new NNList<>(this);
  }

  @Override
  public boolean is(@Nullable Item item) {
    for (IThing thing : negative) {
      if (thing.is(item)) {
        return false;
      }
    }
    for (IThing thing : positive) {
      if (thing.is(item)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean is(@Nullable ItemStack itemStack) {
    for (IThing thing : negative) {
      if (thing.is(itemStack)) {
        return false;
      }
    }
    for (IThing thing : positive) {
      if (thing.is(itemStack)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean is(@Nullable Block block) {
    for (IThing thing : negative) {
      if (thing.is(block)) {
        return false;
      }
    }
    for (IThing thing : positive) {
      if (thing.is(block)) {
        return true;
      }
    }
    return false;
  }

  @Override
  @Nonnull
  public NNList<Item> getItems() {
    NNList<Item> result = new NNList<>();
    for (IThing thing : positive) {
      result.addAll(thing.getItems());
    }
    for (NNIterator<Item> iterator = result.iterator(); iterator.hasNext();) {
      Item x = iterator.next();
      for (IThing thing : negative) {
        if (thing.is(x)) {
          iterator.remove();
        }
      }
    }
    return result;
  }

  @Override
  @Nonnull
  public NNList<ItemStack> getItemStacks() {
    NNList<ItemStack> result = new NNList<>();
    for (IThing thing : positive) {
      for (ItemStack stack : thing.getItemStacks()) {
        result.add(stack);
//        if (stack.getDamage() == OreDictionary.WILDCARD_VALUE) {
//          stack.getItem().getSubItems(CreativeTabs.SEARCH, result);
//        } else {
//          result.add(stack);
//        }
      }
    }
    for (NNIterator<ItemStack> iterator = result.iterator(); iterator.hasNext();) {
      ItemStack x = iterator.next();
      for (IThing thing : negative) {
        if (thing.is(x)) {
          iterator.remove();
        }
      }
    }
    return result;
  }

  @Override
  @Nonnull
  public NNList<Block> getBlocks() {
    NNList<Block> result = new NNList<>();
    for (IThing thing : positive) {
      result.addAll(thing.getBlocks());
    }
    for (NNIterator<Block> iterator = result.iterator(); iterator.hasNext();) {
      Block x = iterator.next();
      for (IThing thing : negative) {
        if (thing.is(x)) {
          iterator.remove();
        }
      }
    }
    return result;
  }

}
