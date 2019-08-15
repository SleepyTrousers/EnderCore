package com.enderio.core.common.util.stackable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NullHelper;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IProducer {

  default @Nullable Block getBlock() {
    return null;
  };

  default @Nullable Item getItem() {
    return null;
  };

  default @Nonnull Block getBlockNN() {
    return NullHelper.notnull(getBlock(), "Block ", this, " is unexpectedly missing").delegate.get();
  }

  default @Nonnull Item getItemNN() {
    return NullHelper.notnull(getItem(), "Item ", this, " is unexpectedly missing").delegate.get();
  }

}
