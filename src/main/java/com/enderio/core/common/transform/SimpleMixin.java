package com.enderio.core.common.transform;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.enderio.core.common.transform.SimpleMixin.SimpleMixinList;

/**
 * Marks a class which should be "mixed-in" to another at runtime. This can be used for modular implementation where it is required that a core class implements
 * an API interface.
 * <p>
 * All methods except constructors and abstracts will be added to the target class, as well as any implemented interfaces (but <em>not</em> superclasses).
 * <p>
 * There are no stipulations on a type marked with this interface, other than that it <em>cannot</em> transitively classload the target class.
 * <p>
 * In simple terms, this means that the annotated class cannot reference the target class in any way. If you need to extend the target class, have it extend a
 * dummy class which implements the same interface as the target class instead.
 */
@Deprecated
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SimpleMixinList.class)
@Documented
public @interface SimpleMixin {

  Class<?> value();

  String[] dependencies() default {};

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Deprecated
  public @interface SimpleMixinList {

    SimpleMixin[] value();

  }

}
