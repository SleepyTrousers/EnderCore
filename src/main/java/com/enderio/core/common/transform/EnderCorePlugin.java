package com.enderio.core.common.transform;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;


@MCVersion("1.8.9")
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
// we want deobf no matter what
public class EnderCorePlugin implements IFMLLoadingPlugin {
  public static boolean runtimeDeobfEnabled = false;

  @Override
  public String[] getASMTransformerClass() {
    return new String[] { "com.enderio.core.common.transform.EnderCoreTransformer" };
  }

  @Override
  public String getModContainerClass() {
    return null;
  }

  @Override
  public String getSetupClass() {
    return null;
  }

  @Override
  public void injectData(Map<String, Object> data) {
    runtimeDeobfEnabled = (Boolean) data.get("runtimeDeobfuscationEnabled");
  }

  @Override
  public String getAccessTransformerClass() {
    return null;
  }
}
