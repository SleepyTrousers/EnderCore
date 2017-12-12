package com.enderio.core.common.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.AbstractConfigHandler.RestartReqs;
import com.enderio.core.common.config.annot.Comment;
import com.enderio.core.common.config.annot.Config;
import com.enderio.core.common.config.annot.NoSync;
import com.enderio.core.common.config.annot.Range;
import com.enderio.core.common.config.annot.RestartReq;
import com.enderio.core.common.event.ConfigFileChangedEvent;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.util.Bound;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

/**
 * This class can be used to automatically process {@link Config} annotations on fields, and sync the data in those fields to clients. It will also
 * automatically respond to all config changed events and handle them appropriately.
 *
 * @see #process(boolean)
 */
public class ConfigProcessor {
  public interface IReloadCallback {
    void callback(@Nonnull ConfigProcessor inst);
  }

  /**
   * A simple adapter for reading custom config types.
   *
   * @param <BASE>
   *          Must be a possible class that can be used in configs. This consists of:
   *          <ul>
   *          <code>
   * <li>Boolean</li>
   * <li>Integer</li>
   * <li>Double</li>
   * <li>String</li>
   * <li>boolean[]</li>
   * <li>int[]</li>
   * <li>double[]</li>
   * <li>String[]</li>
   * </code>
   *          </ul>
   * @param <ACTUAL>
   *          The actual type of this adapter. This is what the field type must be for this adapter to be applied.
   */
  public interface ITypeAdapter<ACTUAL, BASE> {
    TypeToken<ACTUAL> getActualType();

    Property.Type getType();

    /**
     * If this binds to a primitive type, return it here (e.g. int.class). Otherwise, return null.
     *
     * @return The class for this ITypeAdapter's primitive type.
     */
    @Nullable
    Class<?> getPrimitiveType();

    ACTUAL createActualType(BASE base);

    @Nonnull
    BASE createBaseType(@Nonnull ACTUAL actual);
  }

  static final @Nonnull Map<String, ConfigProcessor> processorMap = Maps.newHashMap();

  protected final @Nonnull List<ITypeAdapter<?, ?>> adapters = Lists.newArrayList();

  protected final @Nonnull String modid;

  protected final @Nonnull Class<?> configs;
  protected final @Nonnull Configuration configFile;
  protected final @Nullable IReloadCallback callback;

  protected @Nonnull Map<String, Object> configValues = Maps.newHashMap();
  protected final @Nonnull Map<String, Object> defaultValues = Maps.newHashMap();
  protected final @Nonnull Map<String, Object> originalValues = Maps.newHashMap();

  protected final @Nonnull Set<String> sections = Sets.newHashSet();

  /**
   * This constructor omits the callback arg.
   *
   * @see #ConfigProcessor(Class, File, String, IReloadCallback)
   */
  public ConfigProcessor(@Nonnull Class<?> configs, @Nonnull File configFile, @Nonnull String modid) {
    this(configs, configFile, modid, null);
  }

  /**
   * Constructs a new ConfigProcessor to read and set {@link Config} values.
   *
   * @param configs
   *          The class which contains your {@link Config} annotations
   * @param configFile
   *          The file to use as the configuration file
   * @param modid
   *          The modid of the owner mod
   * @param callback
   *          an {@link IReloadCallback} object which will be called whenever config values are edited.
   */
  public ConfigProcessor(@Nonnull Class<?> configs, @Nonnull File configFile, @Nonnull String modid, @Nullable IReloadCallback callback) {
    this(configs, new Configuration(configFile), modid, callback);
  }

  /**
   * Use this constructor if you are using a {@code ConfigProcessor} alongside an {@link AbstractConfigHandler}. Do not pass a handler that has not been
   * {@link AbstractConfigHandler#initialize(File) initialized}!
   *
   * @param configs
   *          The class which contains your {@link Config} annotations (typically same class as your {@link AbstractConfigHandler handler})
   * @param handler
   *          Your {@link AbstractConfigHandler}
   */
  public ConfigProcessor(@Nonnull Class<?> configs, @Nonnull AbstractConfigHandler handler) {
    this(configs, handler, null);
  }

  /**
   * Use this constructor if you are using a {@code ConfigProcessor} alongside an {@link AbstractConfigHandler}. Do not pass a handler that has not been
   * {@link AbstractConfigHandler#initialize(File) initialized}!
   *
   * @param configs
   *          The class which contains your {@link Config} annotations (typically same class as your {@link AbstractConfigHandler handler})
   * @param handler
   *          Your {@link AbstractConfigHandler}
   * @param callback
   *          an {@link IReloadCallback} object which will be called whenever config values are edited.
   */
  public ConfigProcessor(@Nonnull Class<?> configs, @Nonnull AbstractConfigHandler handler, @Nullable IReloadCallback callback) {
    this(configs, handler.getConfig(), handler.modid, callback);
  }

  protected ConfigProcessor(@Nonnull Class<?> configs, @Nonnull Configuration configFile, @Nonnull String modid, @Nullable IReloadCallback callback) {
    this.configs = configs;
    this.configFile = configFile;
    this.modid = modid;
    this.callback = callback;
    processorMap.put(modid, this);
    MinecraftForge.EVENT_BUS.register(this);
    adapters.addAll(TypeAdapterBase.all);
  }

  public <ACTUAL, BASE> ConfigProcessor addAdapter(ITypeAdapter<ACTUAL, BASE> adapter) {
    adapters.add(adapter);
    return this;
  }

  @SafeVarargs
  public final <ACTUAL, BASE> ConfigProcessor addAdapters(ITypeAdapter<ACTUAL, BASE>... adaptersIn) {
    for (ITypeAdapter<ACTUAL, BASE> adapter : adaptersIn) {
      addAdapter(adapter);
    }
    return this;
  }

  /**
   * Processes all the configs in this processors class, optionally loading them from file first.
   *
   * @param load
   *          If true, the values from the file will be loaded. Otherwise, the values existing in memory will be used.
   */
  public void process(boolean load) {
    if (load) {
      configFile.load();
    }

    try {
      for (Field f : configs.getDeclaredFields()) {
        processField(f);
      }
      if (callback != null) {
        callback.callback(this);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    configFile.save();
  }

  // returns true if the config value changed
  protected boolean processField(Field f) throws Exception {
    Config cfg = f.getAnnotation(Config.class);
    if (cfg == null) {
      return false;
    }
    String name = f.getName();
    Object value = defaultValues.get(name);
    if (value == null) {
      value = NullHelper.notnull(f.get(null), "missing default value");
      defaultValues.put(name, value);
    }

    Object newValue = getConfigValue(cfg.value(), getComment(f), f, value);

    configValues.put(f.getName(), newValue);
    originalValues.put(f.getName(), newValue);
    f.set(null, newValue);

    sections.add(cfg.value());

    return !value.equals(newValue);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected Object getConfigValue(@Nonnull String section, @Nonnull String[] commentLines, @Nonnull Field f, @Nonnull Object defaultValue) {
    Property prop = null;
    Object res = null;
    Bound<Double> bound = getBound(f);
    ITypeAdapter adapter = getAdapterFor(f);
    String comment = StringUtils.join(commentLines, "\n");
    if (adapter != null) {
      Object defVal = adapter.createBaseType(defaultValue);
      switch (adapter.getType()) {
      case BOOLEAN:
        if (defVal.getClass().isArray()) {
          prop = configFile.get(section, f.getName(), (boolean[]) defVal, comment);
          res = prop.getBooleanList();
        } else {
          prop = configFile.get(section, f.getName(), (Boolean) defVal, comment);
          res = prop.getBoolean();
        }
        break;
      case DOUBLE:
        if (defVal.getClass().isArray()) {
          prop = configFile.get(section, f.getName(), (double[]) defVal, comment);
          res = AbstractConfigHandler.boundDoubleArr(prop, Bound.of(bound.min.doubleValue(), bound.max.doubleValue()));
        } else {
          prop = configFile.get(section, f.getName(), (Double) defVal, comment);
          res = AbstractConfigHandler.boundValue(prop, Bound.of(bound.min.doubleValue(), bound.max.doubleValue()), (Double) defVal);
        }
        break;
      case INTEGER:
        if (defVal.getClass().isArray()) {
          prop = configFile.get(section, f.getName(), (int[]) defVal, comment);
          res = AbstractConfigHandler.boundIntArr(prop, Bound.of(bound.min.intValue(), bound.max.intValue()));
        } else {
          prop = configFile.get(section, f.getName(), (Integer) defVal, comment);
          res = AbstractConfigHandler.boundValue(prop, Bound.of(bound.min.intValue(), bound.max.intValue()), (Integer) defVal);
        }
        break;
      case STRING:
        if (defVal.getClass().isArray()) {
          prop = configFile.get(section, f.getName(), (String[]) defVal, comment);
          res = prop.getStringList();
        } else {
          prop = configFile.get(section, f.getName(), (String) defVal, comment);
          res = prop.getString();
        }
        break;
      default:
        break;
      }
      if (res != null) {
        AbstractConfigHandler.setBounds(prop, bound);
        AbstractConfigHandler.addCommentDetails(prop, bound);
        getRestartReq(f).apply(prop);
        return adapter.createActualType(res);
      }
    }
    throw new IllegalArgumentException(String.format("No adapter for type %s in class %s, field %s", f.getGenericType(), configs, f));
  }

  protected ITypeAdapter<?, ?> getAdapterFor(@Nonnull Field f) {
    TypeToken<?> t = TypeToken.of(f.getGenericType());
    Class<?> c = f.getType();
    for (ITypeAdapter<?, ?> adapter : adapters) {
      if ((c.isPrimitive() && c == adapter.getPrimitiveType()) || adapter.getActualType().isSupertypeOf(t)) {
        return adapter;
      }
    }
    return null;
  }

  public ImmutableSet<String> sections() {
    return ImmutableSet.copyOf(sections);
  }

  public ConfigCategory getCategory(String category) {
    return configFile.getCategory(category);
  }

  public void syncTo(@Nonnull Map<String, Object> values) {
    this.configValues = values;
    for (String s : configValues.keySet()) {
      try {
        Field f = configs.getDeclaredField(s);
        Config annot = f.getAnnotation(Config.class);
        if (annot != null && !getNoSync(f)) {
          Object newVal = configValues.get(s);
          Object oldVal = f.get(null);
          boolean changed = false;
          if (!oldVal.equals(newVal)) {
            EnderCore.logger.debug("Config {}.{} differs from new data. Changing from {} to {}", configs.getName(), f.getName(), oldVal, newVal);
            f.set(null, newVal);
            changed = true;
          }
          if (changed && callback != null) {
            callback.callback(this);
          }
        } else if (annot != null) {
          EnderCore.logger.debug("Skipping syncing field {}.{} as it was marked NoSync", configs.getName(), f.getName());
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected @Nonnull String[] getComment(Field f) {
    Comment c = f.getAnnotation(Comment.class);
    return NullHelper.first(c == null ? null : c.value(), new String[0]);
  }

  protected Bound<Double> getBound(Field f) {
    Range r = f.getAnnotation(Range.class);
    return r == null ? Bound.MAX_BOUND : Bound.of(r.min(), r.max());
  }

  protected boolean getNoSync(Field f) {
    return f.getAnnotation(NoSync.class) != null;
  }

  protected RestartReqs getRestartReq(Field f) {
    RestartReq r = f.getAnnotation(RestartReq.class);
    return r == null ? RestartReqs.NONE : r.value();
  }

  /* Event Handling */

  @SubscribeEvent
  public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    EnderCore.logger.info("Sending server configs to client for {}", configs.getName());
    EnderPacketHandler.INSTANCE.sendTo(new PacketConfigSync(this), (EntityPlayerMP) event.player);
  }

  @SubscribeEvent
  public void onPlayerLogout(ClientDisconnectionFromServerEvent event) {
    syncTo(originalValues);
    EnderCore.logger.info("Reset configs to client values for {}", configs.getName());
  }

  @SubscribeEvent
  public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
    if (event.getModID().equals(modid)) {
      process(false);
    }
  }

  @SubscribeEvent
  public void onConfigFileChanged(ConfigFileChangedEvent event) {
    if (event.getModID().equals(modid)) {
      process(true);
    }
  }
}
