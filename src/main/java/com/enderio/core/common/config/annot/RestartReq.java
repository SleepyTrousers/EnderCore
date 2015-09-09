package com.enderio.core.common.config.annot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.enderio.core.common.config.AbstractConfigHandler.RestartReqs;

/**
 * Represents the restart requirements of a config value. Use this if your
 * config will have no effect if changed while the game is running. Has no
 * effect is this field is not also annotated with {@link Config}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface RestartReq {
  /**
   * What requirements this config has for restarting the game.
   * 
   * @see RestartReqs#NONE
   * @see RestartReqs#REQUIRES_WORLD_RESTART
   * @see RestartReqs#REQUIRES_MC_RESTART
   */
  RestartReqs value() default RestartReqs.NONE;
}
