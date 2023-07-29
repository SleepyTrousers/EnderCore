package com.enderio.core.common.util;

import javax.annotation.Nonnull;

public class NullHelper {

  private NullHelper() {
  }

  @Nonnull
  public static <P> P notnull(P o, @Nonnull String message) {
    if (o == null) {
      throw new NullPointerException("Houston we have a problem: '" + message + "'. "
          + "Please report that on our bugtracker unless you are using some old version. Thank you.");
    }
    return o;
  }

  @Nonnull
  public static <P> P notnullJ(P o, @Nonnull String message) {
    if (o == null) {
      throw new NullPointerException("There was a problem with Java: The call '" + message
          + "' returned null even though it should not be able to do that. Is you Java broken? "
          + "Are you using a version that is much newer than the one Ender IO was developed with?");
    }
    return o;
  }

  @Nonnull
  public static <P> P notnullM(P o, @Nonnull String message) {
    if (o == null) {
      throw new NullPointerException("There was a problem with Minecraft: The call '" + message
          + "' returned null even though it should not be able to do that. Is you Minecraft broken? Did some other mod brake it?");
    }
    return o;
  }

  @Nonnull
  public static <P> P notnullF(P o, @Nonnull String message) {
    if (o == null) {
      throw new NullPointerException("There was a problem with Forge: The call '" + message
          + "' returned null even though it should not be able to do that. Is you Forge broken? Did some other mod brake it? "
          + "Are you using a version that is much newer than the one Ender IO Addons was developed with?");
    }
    return o;
  }

}
