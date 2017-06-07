package com.enderio.core.common.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class NNMap<K, V> extends HashMap<K, V> {

  public NNMap() {
    super();
  }

  public NNMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  public NNMap(int initialCapacity) {
    super(initialCapacity);
  }

  public NNMap(Map<? extends K, ? extends V> m) {
    super(m);
  }

  private static final long serialVersionUID = -2844252319683263440L;

  @Override
  public abstract @Nonnull V get(Object key);

  protected @Nullable V superGet(Object key) {
    return super.get(key);
  }

  @Override
  public V put(K key, @SuppressWarnings("null") @Nonnull V value) {
    return super.put(key, value);
  }

  public static class Default<K, V> extends NNMap<K, V> {

    private static final long serialVersionUID = -4833754907686663472L;

    private final @Nonnull V defaultValue;

    public Default(@Nonnull V defaultValue) {
      super();
      this.defaultValue = defaultValue;
    }

    public Default(@Nonnull V defaultValue, int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
      this.defaultValue = defaultValue;
    }

    public Default(@Nonnull V defaultValue, int initialCapacity) {
      super(initialCapacity);
      this.defaultValue = defaultValue;
    }

    public Default(@Nonnull V defaultValue, Map<? extends K, ? extends V> m) {
      super(m);
      this.defaultValue = defaultValue;
    }

    @Override
    public @Nonnull V get(Object key) {
      final V v = super.superGet(key);
      return v != null ? v : defaultValue;
    }
  }

  public static class Brutal<K, V> extends NNMap<K, V> {

    private static final long serialVersionUID = 6239371965003245475L;

    public Brutal() {
      super();
    }

    public Brutal(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
    }

    public Brutal(int initialCapacity) {
      super(initialCapacity);
    }

    public Brutal(Map<? extends K, ? extends V> m) {
      super(m);
    }

    @Override
    public @Nonnull V get(Object key) {
      final V v = super.superGet(key);
      if (v == null) {
        throw new NullPointerException();
      }
      return v;
    }

  }
}
