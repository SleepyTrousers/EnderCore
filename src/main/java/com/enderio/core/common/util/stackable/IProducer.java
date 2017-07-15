package com.enderio.core.common.util.stackable;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

public interface IProducer {

  default @Nullable Block getBlock() {
    return null;
  };

  default @Nullable Item getItem() {
    return null;
  };

}
