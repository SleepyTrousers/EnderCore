package com.enderio.core.common.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public class NNMap<K, V> extends HashMap<K, V> {

  private static final long serialVersionUID = -2844252319683263440L;

  private final @Nonnull V defaultValue;

  public NNMap(@Nonnull V defaultValue) {
    super();
    this.defaultValue = defaultValue;
  }

  public NNMap(@Nonnull V defaultValue, int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    this.defaultValue = defaultValue;
  }

  public NNMap(@Nonnull V defaultValue, int initialCapacity) {
    super(initialCapacity);
    this.defaultValue = defaultValue;
  }

  public NNMap(@Nonnull V defaultValue, Map<? extends K, ? extends V> m) {
    super(m);
    this.defaultValue = defaultValue;
  }

  @Override
  public @Nonnull V get(Object key) {
    final V v = super.get(key);
    return v != null ? v : defaultValue;
  }

}
