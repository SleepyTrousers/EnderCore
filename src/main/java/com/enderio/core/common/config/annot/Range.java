package com.enderio.core.common.config.annot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents the range a config value can be, i.e. a minimum and maximum value.
 * This is applied forcefully, any value past one of the extremes will be
 * clamped inside this range. Has no effect is this field is not also annotated
 * with {@link Config}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Range {

  /**
   * The min value of the config.
   * <p>
   * For non-numeric values, or if there is no min value, this should remain
   * unset.
   * 
   * @return A double minimum value for the config.
   */
  double min() default Double.MIN_VALUE;

  /**
   * The max value of the config.
   * <p>
   * For non-numeric values, or if there is no max value, this should remain
   * unset.
   * 
   * @return A double maximum value for the config.
   */
  double max() default Double.MAX_VALUE;
}
