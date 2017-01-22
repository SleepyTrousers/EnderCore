package com.enderio.core.common.util;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * An object to represent a bounds limit on a property.
 *
 * @param <T>
 *          The type of the bound.
 */
@Immutable
public final class Bound<T extends Number & Comparable<T>> {

  public static final @Nonnull Bound<Double> MAX_BOUND = Bound.of(Double.MIN_VALUE, Double.MAX_VALUE);

  public static @Nonnull <T extends Number & Comparable<T>> Bound<T> of(@Nonnull T min, @Nonnull T max) {
    return new Bound<T>(min, max);
  }

  public final @Nonnull T min;
  public final @Nonnull T max;

  private Bound(final @Nonnull T min, final @Nonnull T max) {
    this.min = min;
    this.max = max;
  }

  public @Nonnull T getMin() {
    return this.min;
  }

  public @Nonnull T getMax() {
    return this.max;
  }

  public @Nonnull T clamp(@Nonnull T val) {
    return val.compareTo(min) < 0 ? min : val.compareTo(max) > 0 ? max : val;
  }

  @Override
  public boolean equals(final java.lang.Object o) {
    if (o == this)
      return true;
    if (!(o instanceof Bound))
      return false;
    final Bound<?> other = (Bound<?>) o;
    final java.lang.Object this$min = this.getMin();
    final java.lang.Object other$min = other.getMin();
    if (!this$min.equals(other$min))
      return false;
    final java.lang.Object this$max = this.getMax();
    final java.lang.Object other$max = other.getMax();
    if (!this$max.equals(other$max))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final java.lang.Object $min = this.getMin();
    result = result * PRIME + ($min.hashCode());
    final java.lang.Object $max = this.getMax();
    result = result * PRIME + ($max.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "Bound(min=" + this.getMin() + ", max=" + this.getMax() + ")";
  }

  public @Nonnull Bound<T> withMin(final @Nonnull T newMin) {
    return this.min == newMin ? this : new Bound<T>(newMin, this.max);
  }

  public @Nonnull Bound<T> withMax(final @Nonnull T newMax) {
    return this.max == newMax ? this : new Bound<T>(this.min, newMax);
  }
}