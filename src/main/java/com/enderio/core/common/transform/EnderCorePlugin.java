package com.enderio.core.common.transform;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import com.google.common.base.MoreObjects;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion("1.12.2")
// we want deobf no matter what
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE)
public class EnderCorePlugin implements IFMLLoadingPlugin {

  static final Logger mainLogger = LogManager.getLogger("EnderCore ASM");
  static final Logger mixinLogger = LogManager.getLogger("EnderCore Mixins");

  public static boolean runtimeDeobfEnabled = false;

  private static EnderCorePlugin instance;

  static class MixinData {
    public final String source, target;

    MixinData(String source, String target) {
      this.source = source;
      this.target = target;
    }

    void initialize(String clazz) {
      try {
        Class<?> cls = Class.forName(clazz);
        cls.getName(); // Make sure it's loaded
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + source.hashCode();
      result = prime * result + target.hashCode();
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      MixinData other = (MixinData) obj;
      return source.equals(other.source) && target.equals(other.target);
    }
  }

  Set<MixinData> mixins = new LinkedHashSet<>();

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

  public void loadMixinSources(Object mod) {
    loadMixinSources(mod.getClass().getPackage());
  }

  public void loadMixinSources(Package filter) {
    loadMixinSources(filter.getName());
  }

  public void loadMixinSources(String packageFilter) {
    List<MixinData> fromAnnotations = findAnnotationMixins(packageFilter);
    fromAnnotations.forEach(mixins::add);
    fromAnnotations.forEach(m -> m.initialize(m.source));
    fromAnnotations.forEach(m -> m.initialize(m.target));
  }

  private List<MixinData> findAnnotationMixins(String packageFilter) {
    List<MixinData> ret = new ArrayList<>();
    try {
      Field fDiscoverer = Loader.class.getDeclaredField("discoverer");
      fDiscoverer.setAccessible(true);

      ModDiscoverer discoverer = (ModDiscoverer) fDiscoverer.get(Loader.instance());
      ASMDataTable asmData = discoverer.getASMTable();

      Set<ASMData> data = new HashSet<>();
      data.addAll(asmData.getAll(com.enderio.core.common.mixin.SimpleMixin.class.getName()));
      data.addAll(asmData.getAll(com.enderio.core.common.transform.SimpleMixin.class.getName()));
      for (ASMData d : data) {
        if (!d.getClassName().startsWith(packageFilter)) {
          continue;
        }
        mixinLogger.info("Found annotation mixin: {}", d.getClassName());
        @SuppressWarnings("unchecked")
        List<String> dependencies = MoreObjects.firstNonNull((List<String>) d.getAnnotationInfo().get("dependencies"), Collections.<String> emptyList());
        List<String> missingDependencies = dependencies.stream().filter(m -> !Loader.isModLoaded(m) && !ModAPIManager.INSTANCE.hasAPI(m))
            .collect(Collectors.toList());
        if (missingDependencies.size() == 0) {
          ret.add(new MixinData(d.getClassName(), ((Type) d.getAnnotationInfo().get("value")).getClassName()));
          mixinLogger.info("Registered mixin.");
        } else {
          mixinLogger.info("Skipping mixin due to missing dependencies: {}", missingDependencies);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return ret;
  }

  @Override
  public String[] getASMTransformerClass() {
    return new String[] { "com.enderio.core.common.transform.EnderCoreTransformer", "com.enderio.core.common.transform.SimpleMixinPatcher" };
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
