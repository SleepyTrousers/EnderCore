package com.enderio.core.common.config.annot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contains the comment for a config option. Has no effect is this field is not
 * also annotated with {@link Config}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Comment {
  /**
   * The comment for the config option. Multiple strings will be split into
   * lines.
   */
  String[] value() default "";
}
