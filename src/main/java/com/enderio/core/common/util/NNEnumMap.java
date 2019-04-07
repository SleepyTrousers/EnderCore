package com.enderio.core.common.util;

import java.util.EnumMap;

import javax.annotation.Nonnull;

/**
 * An EnumMap that defaults to a specified value instead of <code>null</code>.
 * <p>
 * Note that this only changes the behavior of {@link #get(Object)} and {@link #getOrDefault(Object, Object)}, not {@link #values()}, {@link #entrySet()},
 * {@link #remove(Object)}, {@link #containsValue(Object)}, etc. Those will still show the underlying <code>null</code> and see a difference between keys that
 * were set to <code>null</code> and those that were set to the default value manually.
 * 
 * @author Henry Loenwind
 */
public class NNEnumMap<K extends Enum<K>, V> extends EnumMap<K, V> {

  private static final long serialVersionUID = 5242971202252653482L;

  private final @Nonnull V defaultValue;

  public NNEnumMap(@Nonnull Class<K> keyType, @Nonnull V defaultValue) {
    super(keyType);
    this.defaultValue = defaultValue;
  }

  @Override
  public @Nonnull V get(Object key) {
    return NullHelper.first(super.get(key), defaultValue);
  }

  @Override
  public @Nonnull V getOrDefault(Object key, V defaultValueParam) {
    return NullHelper.first(super.get(key), defaultValueParam, defaultValue);
  }

}
