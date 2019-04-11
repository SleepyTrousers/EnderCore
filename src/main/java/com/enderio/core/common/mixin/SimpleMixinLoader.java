package com.enderio.core.common.mixin;

import com.enderio.core.common.transform.EnderCorePlugin;

public class SimpleMixinLoader {

  public static void loadMixinSources(Object mod) {
    EnderCorePlugin.instance().loadMixinSources(mod);
  }

  public static void loadMixinSources(Package filter) {
    EnderCorePlugin.instance().loadMixinSources(filter);
  }

  public static void loadMixinSources(String packageFilter) {
    EnderCorePlugin.instance().loadMixinSources(packageFilter);
  }

}
