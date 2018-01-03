package com.enderio.core.common.util.blockiterators;

import java.util.Iterator;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;

public abstract class AbstractBlockIterator implements Iterable<BlockPos>, Iterator<BlockPos> {

  protected final @Nonnull BlockPos base;

  protected AbstractBlockIterator(@Nonnull BlockPos base) {
    this.base = base;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("You can't remove blocks silly!");
  }

  @Override
  public @Nonnull Iterator<BlockPos> iterator() {
    return this;
  }

}
