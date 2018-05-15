package com.enderio.core.common.transform;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.objectweb.asm.Type;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion("1.12.2")
//we want deobf no matter what
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
public class EnderCorePlugin implements IFMLLoadingPlugin {
  public static boolean runtimeDeobfEnabled = false;
  
  private static EnderCorePlugin instance;
  
  static class InterfacePatchData {
    public final String target, source;

    InterfacePatchData(String target, String source) {
      this.target = target;
      this.source = source;
    }
    
    void initialize() {
      try {
        Class<?> cls = Class.forName(source);
        System.out.println("[EnderCore ASM] Force loaded source for interface patch: " + cls);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  List<InterfacePatchData> ifacePatches = new ArrayList<>();
  
  public EnderCorePlugin() {
    if (instance != null) {
      throw new IllegalStateException("EnderCorePlugin was instantiated twice?");
    }
    instance = this;
  }
  
  @Nonnull
  public static EnderCorePlugin instance() {
    EnderCorePlugin inst = instance;
    if (inst == null) {
      throw new IllegalStateException("EnderCorePlugin accessed too early!");
    }
    return inst;
  }
  
  public void addInterfacePatch(String target, String source) {
    InterfacePatchData data = new InterfacePatchData(target, source);
    ifacePatches.add(data);
  }
  
  public void loadPatchSources() {
    findAnnotationPatches();
    
    ifacePatches.forEach(InterfacePatchData::initialize);
  }
  
  private void findAnnotationPatches() {
    try {
      Field fDiscoverer = Loader.class.getDeclaredField("discoverer");
      fDiscoverer.setAccessible(true);
      
      ModDiscoverer discoverer = (ModDiscoverer) fDiscoverer.get(Loader.instance());
      ASMDataTable asmData = discoverer.getASMTable();
      
      Set<ASMData> data = asmData.getAll(SimpleMixin.class.getName());
      for (ASMData d : data) {
        System.out.println("Found annotation mixin: " + d.getClassName());
        addInterfacePatch(((Type) d.getAnnotationInfo().get("value")).getClassName(), d.getClassName());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String[] getASMTransformerClass() {
    return new String[] { 
        "com.enderio.core.common.transform.EnderCoreTransformer",
        "com.enderio.core.common.transform.InterfacePatcher" };
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
