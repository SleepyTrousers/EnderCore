package com.enderio.core.common.util;

import javax.annotation.Generated;
import javax.annotation.concurrent.Immutable;

/**
 * An object to represent a bounds limit on a property.
 * 
 * @param <T> The type of the bound.
 */
@Immutable
public final class Bound<T extends Number & Comparable<T>> {

    public static final Bound<Double> MAX_BOUND = Bound.of(Double.MIN_VALUE, Double.MAX_VALUE);

    public static <T extends Number & Comparable<T>> Bound<T> of(T min, T max) {
        return new Bound<T>(min, max);
    }

    public final T min;
    public final T max;

    private Bound(final T min, final T max) {
        this.min = min;
        this.max = max;
    }

    public T getMin() {
        return this.min;
    }

    public T getMax() {
        return this.max;
    }

    public T clamp(T val) {
        return val.compareTo(min) < 0 ? min : val.compareTo(max) > 0 ? max : val;
    }

    @Override
    @Generated("lombok")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof Bound)) return false;
        final Bound<?> other = (Bound<?>) o;
        final java.lang.Object this$min = this.getMin();
        final java.lang.Object other$min = other.getMin();
        if (this$min == null ? other$min != null : !this$min.equals(other$min)) return false;
        final java.lang.Object this$max = this.getMax();
        final java.lang.Object other$max = other.getMax();
        if (this$max == null ? other$max != null : !this$max.equals(other$max)) return false;
        return true;
    }

    @Override
    @Generated("lombok")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $min = this.getMin();
        result = result * PRIME + ($min == null ? 43 : $min.hashCode());
        final java.lang.Object $max = this.getMax();
        result = result * PRIME + ($max == null ? 43 : $max.hashCode());
        return result;
    }

    @Override
    @Generated("lombok")
    public java.lang.String toString() {
        return "Bound(min=" + this.getMin() + ", max=" + this.getMax() + ")";
    }

    @Generated("lombok")
    public Bound<T> withMin(final T min) {
        return this.min == min ? this : new Bound<T>(min, this.max);
    }

    @Generated("lombok")
    public Bound<T> withMax(final T max) {
        return this.max == max ? this : new Bound<T>(this.min, max);
    }
}
