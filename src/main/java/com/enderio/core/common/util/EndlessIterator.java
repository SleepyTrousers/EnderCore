package com.enderio.core.common.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class EndlessIterator<T> implements Iterable<T>, Iterator<T> {

  private int index = -1;
  private final List<T> itOver;

  public EndlessIterator(List<T> itOver) {
    this.itOver = itOver;
    if (itOver.isEmpty()) {
      throw new RuntimeException("Cannot iterate over empty list");
    }
  }

  @SafeVarargs
  public EndlessIterator(T... itOver) {
    this(Arrays.asList(itOver));
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    return true;
  }

  @Override
  public T next() {
    if (++index >= itOver.size()) {
      index = 0;
    }
    return itOver.get(index);
  }

  @Override
  public void remove() {
  }

}
