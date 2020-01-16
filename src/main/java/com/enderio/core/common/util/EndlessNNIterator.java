package com.enderio.core.common.util;

import java.util.Iterator;

import javax.annotation.Nonnull;

import com.enderio.core.common.util.NNList.NNIterator;

public class EndlessNNIterator<T> implements Iterable<T>, NNIterator<T> {

  private int index = -1;
  private final @Nonnull NNList<T> itOver;

  public EndlessNNIterator(@Nonnull NNList<T> itOver) {
    this.itOver = itOver;
    if (itOver.isEmpty()) {
      throw new RuntimeException("Cannot iterate over empty list");
    }
  }

  @Override
  public @Nonnull Iterator<T> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public @Nonnull T next() {
    if (++index >= itOver.size()) {
      index = 0;
    }
    return itOver.get(index);
  }

  @Override
  public void remove() {
  }

}
