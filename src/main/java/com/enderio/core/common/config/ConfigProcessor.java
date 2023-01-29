package com.enderio.core.common.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;

/**
 * This class can be used to automatically process {@link Config} annotations on fields, and sync the data in those
 * fields to clients. It will also automatically respond to all config changed events and handle them appropriately.
 * 
 * @see #process(boolean)
 */
public class ConfigProcessor {

    public interface IReloadCallback {

        void callback(ConfigProcessor inst);
    }

    /**
     * A simple adapter for reading custom config types.
     *
     * @param <BASE>   Must be a possible class that can be used in configs. This consists of:
     *                 <ul>
     *                 <code>
     * <li>Boolean</li>
     * <li>Integer</li>
     * <li>Double</li>
     * <li>String</li>
     * <li>boolean[]</li>
     * <li>int[]</li>
     * <li>double[]</li>
     * <li>String[]</li>
     * </code>
     *                 </ul>
     * @param <ACTUAL> The actual type of this adapter. This is what the field type must be for this adapter to be
     *                 applied.
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

        BASE createBaseType(ACTUAL actual);
    }

    static final Map<String, ConfigProcessor> processorMap = Maps.newHashMap();

    private final List<ITypeAdapter<?, ?>> adapters = Lists.newArrayList();

    final String modid;

    private final Class<?> configs;
    private final Configuration configFile;
    private final IReloadCallback callback;

    Map<String, Object> configValues = Maps.newHashMap();
    Map<String, Object> defaultValues = Maps.newHashMap();
    Map<String, Object> originalValues = Maps.newHashMap();

    private Set<String> sections = Sets.newHashSet();

    /**
     * This constructor omits the callback arg.
     * 
     * @see #ConfigProcessor(Class, File, String, IReloadCallback)
     */
    public ConfigProcessor(Class<?> configs, File configFile, String modid) {
        this(configs, configFile, modid, null);
    }

    /**
     * Constructs a new ConfigProcessor to read and set {@link Config} values.
     * 
     * @param configs    The class which contains your {@link Config} annotations
     * @param configFile The file to use as the configuration file
     * @param modid      The modid of the owner mod
     * @param callback   an {@link IReloadCallback} object which will be called whenever config values are edited.
     */
    public ConfigProcessor(Class<?> configs, File configFile, String modid, IReloadCallback callback) {
        this(configs, new Configuration(configFile), modid, callback);
    }

    /**
     * Use this constructor if you are using a {@code ConfigProcessor} alongside an {@link AbstractConfigHandler}. Do
     * not pass a handler that has not been {@link AbstractConfigHandler#initialize(File) initialized}!
     * 
     * @param configs The class which contains your {@link Config} annotations (typically same class as your
     *                {@link AbstractConfigHandler handler})
     * @param handler Your {@link AbstractConfigHandler}
     */
    public ConfigProcessor(Class<?> configs, AbstractConfigHandler handler) {
        this(configs, handler, null);
    }

    /**
     * Use this constructor if you are using a {@code ConfigProcessor} alongside an {@link AbstractConfigHandler}. Do
     * not pass a handler that has not been {@link AbstractConfigHandler#initialize(File) initialized}!
     * 
     * @param configs  The class which contains your {@link Config} annotations (typically same class as your
     *                 {@link AbstractConfigHandler handler})
     * @param handler  Your {@link AbstractConfigHandler}
     * @param callback an {@link IReloadCallback} object which will be called whenever config values are edited.
     */
    public ConfigProcessor(Class<?> configs, AbstractConfigHandler handler, IReloadCallback callback) {
        this(configs, handler.config, handler.modid, callback);
    }

    private ConfigProcessor(Class<?> configs, Configuration configFile, String modid, IReloadCallback callback) {
        this.configs = configs;
        this.configFile = configFile;
        this.modid = modid;
        this.callback = callback;
        processorMap.put(modid, this);
        FMLCommonHandler.instance().bus().register(this);
        adapters.addAll(TypeAdapterBase.all);
    }

    public <ACTUAL, BASE> ConfigProcessor addAdapter(ITypeAdapter<ACTUAL, BASE> adapter) {
        adapters.add(adapter);
        return this;
    }

    public <ACTUAL, BASE> ConfigProcessor addAdapters(ITypeAdapter<ACTUAL, BASE>... adapters) {
        for (ITypeAdapter<ACTUAL, BASE> adapter : adapters) {
            addAdapter(adapter);
        }
        return this;
    }

    /**
     * Processes all the configs in this processors class, optionally loading them from file first.
     * 
     * @param load If true, the values from the file will be loaded. Otherwise, the values existing in memory will be
     *             used.
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
    private boolean processField(Field f) throws Exception {
        Config cfg = f.getAnnotation(Config.class);
        if (cfg == null) {
            return false;
        }
        String name = f.getName();
        Object value = defaultValues.get(name);
        if (value == null) {
            value = f.get(null);
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
    private Object getConfigValue(String section, String[] commentLines, Field f, Object defVal) {
        Property prop = null;
        Object res = null;
        Bound<Double> bound = getBound(f);
        ITypeAdapter adapter = getAdapterFor(f);
        String comment = StringUtils.join(commentLines, "\n");
        if (adapter != null) {
            defVal = adapter.createBaseType(defVal);
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
                        res = AbstractConfigHandler
                                .boundDoubleArr(prop, Bound.of(bound.min.doubleValue(), bound.max.doubleValue()));
                    } else {
                        prop = configFile.get(section, f.getName(), (Double) defVal, comment);
                        res = AbstractConfigHandler.boundValue(
                                prop,
                                Bound.of(bound.min.doubleValue(), bound.max.doubleValue()),
                                (Double) defVal);
                    }
                    break;
                case INTEGER:
                    if (defVal.getClass().isArray()) {
                        prop = configFile.get(section, f.getName(), (int[]) defVal, comment);
                        res = AbstractConfigHandler
                                .boundIntArr(prop, Bound.of(bound.min.intValue(), bound.max.intValue()));
                    } else {
                        prop = configFile.get(section, f.getName(), (Integer) defVal, comment);
                        res = AbstractConfigHandler.boundValue(
                                prop,
                                Bound.of(bound.min.intValue(), bound.max.intValue()),
                                (Integer) defVal);
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
        throw new IllegalArgumentException(
                String.format("No adapter for type %s in class %s, field %s", f.getGenericType(), configs, f));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ITypeAdapter getAdapterFor(Field f) {
        TypeToken<?> t = TypeToken.of(f.getGenericType());
        Class<?> c = f.getType();
        for (ITypeAdapter adapter : adapters) {
            if ((c.isPrimitive() && c == adapter.getPrimitiveType()) || adapter.getActualType().isAssignableFrom(t)) {
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

    public void syncTo(Map<String, Object> values) {
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
                        EnderCore.logger.debug(
                                "Config {}.{} differs from new data. Changing from {} to {}",
                                configs.getName(),
                                f.getName(),
                                oldVal,
                                newVal);
                        f.set(null, newVal);
                        changed = true;
                    }
                    if (changed && callback != null) {
                        callback.callback(this);
                    }
                } else if (annot != null) {
                    EnderCore.logger.debug(
                            "Skipping syncing field {}.{} as it was marked NoSync",
                            configs.getName(),
                            f.getName());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String[] getComment(Field f) {
        Comment c = f.getAnnotation(Comment.class);
        return c == null ? new String[0] : c.value();
    }

    private Bound<Double> getBound(Field f) {
        Range r = f.getAnnotation(Range.class);
        return r == null ? Bound.MAX_BOUND : Bound.of(r.min(), r.max());
    }

    private boolean getNoSync(Field f) {
        return f.getAnnotation(NoSync.class) != null;
    }

    private RestartReqs getRestartReq(Field f) {
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
        if (event.modID.equals(modid)) {
            process(false);
        }
    }

    @SubscribeEvent
    public void onConfigFileChanged(ConfigFileChangedEvent event) {
        if (event.modID.equals(modid)) {
            process(true);
        }
    }
}
