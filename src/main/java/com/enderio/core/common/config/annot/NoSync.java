package com.enderio.core.common.config.annot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If annotation exists, this config will not be synced to the client upon
 * connection to a server. Has no effect is this field is not also annotated
 * with {@link Config}.
 * <p>
 * This is useful for configs that have no need to be the same between client
 * and server (or are purely client-sided).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface NoSync {

}
