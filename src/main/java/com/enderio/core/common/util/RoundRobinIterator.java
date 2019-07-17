package com.enderio.core.common.util;

import java.util.Iterator;
import java.util.List;

public class RoundRobinIterator<T> implements Iterable<T>, Iterator<T> {

  private int index = -1;
  private int currentCount = 0;
  private final List<T> itOver;

  public RoundRobinIterator(List<T> itOver) {
    this.itOver = itOver;
  }

  private RoundRobinIterator(int index, int currentCount, List<T> itOver) {
    this.index = index;
    this.currentCount = currentCount;
    this.itOver = itOver;
  }

  @Override
  public Iterator<T> iterator() {
    currentCount = 0;
    return this;
  }

  @Override
  public boolean hasNext() {
    return !itOver.isEmpty() && currentCount < itOver.size();
  }

  @Override
  public T next() {
    if (itOver.isEmpty()) {
      return null;
    }
    currentCount++;
    index++;
    if (index >= itOver.size()) {
      index = 0;
    }
    return itOver.get(index);
  }

  @Override
  public void remove() {
  }

  /**
   * Used to reset the iterator to its initial state
   */
  public void reset() {
    index = -1;
  }

  public RoundRobinIterator<T> copy() {
    return new RoundRobinIterator<>(index, currentCount, itOver);
  }

}
