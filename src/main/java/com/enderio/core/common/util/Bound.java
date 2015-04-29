package com.enderio.core.common.util;

import javax.annotation.concurrent.Immutable;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

/**
 * An object to represent a bounds limit on a property.
 * 
 * @param <T>
 *            The type of the bound.
 */
@Immutable
@Value
@AllArgsConstructor(staticName = "of")
public class Bound<T extends Number & Comparable<T>>
{
    public static final Bound<Double> MAX_BOUND = Bound.of(Double.MIN_VALUE, Double.MAX_VALUE);

    @Wither
    public T min, max;

    public T clamp(T val)
    {
        return val.compareTo(min) < 0 ? min : val.compareTo(max) > 0 ? max : val;
    }
}