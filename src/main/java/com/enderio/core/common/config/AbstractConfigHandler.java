package com.enderio.core.common.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Property.Type;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.config.IConfigHandler;
import com.enderio.core.common.Lang;
import com.enderio.core.common.event.ConfigFileChangedEvent;
import com.enderio.core.common.util.Bound;
import com.google.common.collect.ImmutableList;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public abstract class AbstractConfigHandler implements IConfigHandler {

    /**
     * Represents a section in a config handler.
     */
    public class Section {

        public final String name;
        public final String lang;

        public Section(String name, String lang) {
            this.name = name;
            this.lang = "section." + lang;
        }

        private Section register() {
            sections.add(this);
            return this;
        }

        public String lc() {
            return name.toLowerCase(Locale.US);
        }
    }

    public enum RestartReqs {

        /**
         * No restart needed for this config to be applied. Default value.
         */
        NONE,

        /**
         * This config requires the world to be restarted to take effect.
         */
        REQUIRES_WORLD_RESTART,

        /**
         * This config requires the game to be restarted to take effect. {@code REQUIRES_WORLD_RESTART} is implied when
         * using this.
         */
        REQUIRES_MC_RESTART;

        public Property apply(Property prop) {
            if (this == REQUIRES_MC_RESTART) {
                prop.setRequiresMcRestart(true);
            } else if (this == REQUIRES_WORLD_RESTART) {
                prop.setRequiresWorldRestart(true);
            }
            return prop;
        }
    }

    String modid;
    Configuration config;

    private List<Section> sections = new ArrayList<Section>();
    private Section activeSection = null;

    protected AbstractConfigHandler(String modid) {
        this.modid = modid;
        FMLCommonHandler.instance().bus().register(this);
        EnderCore.instance.configs.add(this);
    }

    @Override
    public final void initialize(File cfg) {
        config = new Configuration(cfg);
        init();
        reloadAllConfigs();
        saveConfigFile();
    }

    protected void loadConfigFile() {
        config.load();
    }

    protected void saveConfigFile() {
        config.save();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(modid)) {
            EnderCore.logger.info("Reloading all configs for modid: " + modid);
            reloadAllConfigs();
            saveConfigFile();
        }
    }

    @SubscribeEvent
    public void onConfigFileChanged(ConfigFileChangedEvent event) {
        if (event.modID.equals(modid)) {
            EnderCore.logger.info("Reloading ingame configs for modid: " + modid);
            loadConfigFile();
            reloadIngameConfigs();
            event.setSuccessful();
            saveConfigFile();
        }
    }

    // convenience for reloading all configs
    private void reloadAllConfigs() {
        reloadNonIngameConfigs();
        reloadIngameConfigs();
    }

    /**
     * Called after config is loaded, but before properties are processed.
     * <p>
     * Use this method to add your sections and do other setup.
     */
    protected abstract void init();

    /**
     * Refresh all config values that can only be loaded when NOT in-game.
     * <p>
     * {@code reloadIngameConfigs()} will be called after this, do not duplicate calls in this method and that one.
     */
    protected abstract void reloadNonIngameConfigs();

    /**
     * Refresh all config values that can only be loaded when in-game.
     * <p>
     * This is separated from {@code reloadNonIngameConfigs()} because some values may not be able to be modified at
     * runtime.
     */
    protected abstract void reloadIngameConfigs();

    /**
     * Adds a section to your config to be used later
     * 
     * @param sectionName The name of the section. Will also be used as language key.
     * @return A {@link Section} representing your section in the config
     */
    protected Section addSection(String sectionName) {
        return addSection(sectionName, sectionName, null);
    }

    /**
     * Adds a section to your config to be used later
     * 
     * @param sectionName The name of the section
     * @param langKey     The language key to use to display your section name in the Config GUI
     * @return A {@link Section} representing your section in the config
     */
    protected Section addSection(String sectionName, String langKey) {
        return addSection(sectionName, langKey, null);
    }

    /**
     * Adds a section to your config to be used later
     * 
     * @param sectionName The name of the section
     * @param langKey     The language key to use to display your section name in the Config GUI
     * @param comment     The section comment
     * @return A {@link Section} representing your section in the config
     */
    protected Section addSection(String sectionName, String langKey, String comment) {
        Section section = new Section(sectionName, langKey);

        if (activeSection == null && sections.isEmpty()) {
            activeSection = section;
        }

        if (comment != null) {
            config.addCustomCategoryComment(sectionName, comment);
        }

        return section.register();
    }

    private void checkInitialized() {
        if (activeSection == null) {
            throw new IllegalStateException("No section is active!");
        }
    }

    /**
     * Activates a section
     * 
     * @param sectionName The name of the section
     * 
     * @throws IllegalArgumentException if {@code sectionName} is not valid
     */
    protected void activateSection(String sectionName) {
        Section section = getSectionByName(sectionName);
        if (section == null) {
            throw new IllegalArgumentException("Section " + sectionName + " does not exist!");
        }
        activateSection(section);
    }

    /**
     * Activates a section
     * 
     * @param section The section to activate
     */
    protected void activateSection(Section section) {
        activeSection = section;
    }

    /**
     * Gets a {@link Section} for a name
     * 
     * @param sectionName The name of the section
     * @return A section object representing the section in your config with this name
     */
    protected Section getSectionByName(String sectionName) {
        for (Section s : sections) {
            if (s.name.equalsIgnoreCase(sectionName)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets a value from this config handler
     * 
     * @param key        Name of the key for this property
     * @param defaultVal Default value so a new property can be created
     * @return The value of the property
     * 
     * @throws IllegalArgumentException If defaultVal is not a valid property type
     * @throws IllegalStateException    If there is no active section
     */
    protected <T> T getValue(String key, T defaultVal) {
        return getValue(key, defaultVal, RestartReqs.NONE);
    }

    /**
     * Gets a value from this config handler
     * 
     * @param key        Name of the key for this property
     * @param defaultVal Default value so a new property can be created
     * @param req        Restart requirement of the property to be created
     * @return The value of the property
     * 
     * @throws IllegalArgumentException If defaultVal is not a valid property type
     * @throws IllegalStateException    If there is no active section
     */
    protected <T> T getValue(String key, T defaultVal, RestartReqs req) {
        return getValue(key, null, defaultVal, req);
    }

    /**
     * Gets a value from this config handler
     * 
     * @param key        Name of the key for this property
     * @param defaultVal Default value so a new property can be created
     * @param bound      The bounds to set on this property
     * @return The value of the property
     * 
     * @throws IllegalArgumentException If defaultVal is not a valid property type
     * @throws IllegalStateException    If there is no active section
     */
    protected <T> T getValue(String key, T defaultVal, Bound<? extends Number> bound) {
        return getValue(key, null, defaultVal, bound);
    }

    /**
     * Gets a value from this config handler
     * 
     * @param key        Name of the key for this property
     * @param comment    The comment to put on this property
     * @param defaultVal Default value so a new property can be created
     * @return The value of the property
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    protected <T> T getValue(String key, String comment, T defaultVal) {
        return getValue(key, comment, defaultVal, RestartReqs.NONE);
    }

    /**
     * Gets a value from this config handler
     * 
     * @param key        Name of the key for this property
     * @param comment    The comment to put on this property
     * @param defaultVal Default value so a new property can be created
     * @param req        Restart requirement of the property to be created
     * @return The value of the property
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    protected <T> T getValue(String key, String comment, T defaultVal, RestartReqs req) {
        return getValue(key, comment, defaultVal, req, null);
    }

    /**
     * Gets a value from this config handler
     * 
     * @param key        Name of the key for this property
     * @param comment    The comment to put on this property
     * @param defaultVal Default value so a new property can be created
     * @param bound      The bounds to set on this property
     * @return The value of the property
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    protected <T> T getValue(String key, String comment, T defaultVal, Bound<? extends Number> bound) {
        return getValue(key, comment, defaultVal, RestartReqs.NONE, bound);
    }

    /**
     * Gets a value from this config handler
     * 
     * @param key        Name of the key for this property
     * @param comment    The comment to put on this property
     * @param defaultVal Default value so a new property can be created
     * @param req        Restart requirement of the property to be created
     * @param bound      The bounds to set on this property
     * @return The value of the property
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    protected <T> T getValue(String key, String comment, T defaultVal, RestartReqs req, Bound<? extends Number> bound) {
        Property prop = getProperty(key, defaultVal, req);
        prop.comment = comment;

        return getValue(prop, defaultVal, bound);
    }

    /**
     * Gets a value from a property
     * 
     * @param prop       Property to get value from
     * @param defaultVal Default value so a new property can be created
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    protected <T> T getValue(Property prop, T defaultVal) {
        return getValue(prop, defaultVal, null);
    }

    /**
     * Gets a value from a property
     * 
     * @param prop       Property to get value from
     * @param defaultVal Default value so a new property can be created
     * @param bound      The bounds to set on this property
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    @SuppressWarnings("unchecked")
    // we check type of defaultVal but compiler still complains about a cast to T
    protected <T> T getValue(Property prop, T defaultVal, Bound<? extends Number> bound) {
        checkInitialized();

        if (bound != null) {
            setBounds(prop, bound);
        } else {
            bound = Bound.MAX_BOUND;
        }

        addCommentDetails(prop, bound);

        if (defaultVal instanceof Integer) {
            Bound<Integer> b = Bound.of(bound.min.intValue(), bound.max.intValue());
            return (T) boundValue(prop, b, (Integer) defaultVal);
        }
        if (defaultVal instanceof Float) {
            Bound<Float> b = Bound.of(bound.min.floatValue(), bound.max.floatValue());
            return (T) boundValue(prop, b, (Float) defaultVal);
        }
        if (defaultVal instanceof Double) {
            Bound<Double> b = Bound.of(bound.min.doubleValue(), bound.max.doubleValue());
            return (T) boundValue(prop, b, (Double) defaultVal);
        }
        if (defaultVal instanceof Boolean) {
            return (T) Boolean.valueOf(prop.getBoolean());
        }
        if (defaultVal instanceof int[]) {
            return (T) prop.getIntList();
        }
        if (defaultVal instanceof String) {
            return (T) prop.getString();
        }
        if (defaultVal instanceof String[]) {
            return (T) prop.getStringList();
        }

        throw new IllegalArgumentException("default value is not a config value type.");
    }

    static void setBounds(Property prop, Bound<?> bound) throws IllegalArgumentException {
        if (bound.equals(Bound.MAX_BOUND)) {
            return;
        }
        if (prop.getType() == Type.INTEGER) {
            Bound<Integer> b = Bound.of(bound.min.intValue(), bound.max.intValue());
            prop.setMinValue(b.min);
            prop.setMaxValue(b.max);
        } else if (prop.getType() == Type.DOUBLE) {
            Bound<Double> b = Bound.of(bound.min.doubleValue(), bound.max.doubleValue());
            prop.setMinValue(b.min);
            prop.setMaxValue(b.max);
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "A mod tried to set bounds %s on a property that was not either of Integer of Double type.",
                            bound));
        }
    }

    static int[] boundIntArr(Property prop, Bound<Integer> bound) {
        int[] prev = prop.getIntList();
        int[] res = new int[prev.length];
        for (int i = 0; i < prev.length; i++) {
            res[i] = bound.clamp(prev[i]);
        }
        prop.set(res);
        return res;
    }

    static double[] boundDoubleArr(Property prop, Bound<Double> bound) {
        double[] prev = prop.getDoubleList();
        double[] res = new double[prev.length];
        for (int i = 0; i < prev.length; i++) {
            res[i] = bound.clamp(prev[i]);
        }
        prop.set(res);
        return res;
    }

    @SuppressWarnings("unchecked")
    static <T extends Number & Comparable<T>> T boundValue(Property prop, Bound<T> bound, T defVal)
            throws IllegalArgumentException {
        Object b = (Object) bound;
        if (defVal instanceof Integer) {
            return (T) boundInt(prop, (Bound<Integer>) b);
        }
        if (defVal instanceof Double) {
            return (T) boundDouble(prop, (Bound<Double>) b);
        }
        if (defVal instanceof Float) {
            return (T) boundFloat(prop, (Bound<Float>) b);
        }
        throw new IllegalArgumentException(bound.min.getClass().getName() + " is not a valid config type.");
    }

    private static Integer boundInt(Property prop, Bound<Integer> bound) {
        prop.set(bound.clamp(prop.getInt()));
        return Integer.valueOf(prop.getInt());
    }

    private static Double boundDouble(Property prop, Bound<Double> bound) {
        prop.set(bound.clamp(prop.getDouble()));
        return Double.valueOf(prop.getDouble());
    }

    private static Float boundFloat(Property prop, Bound<Float> bound) {
        return boundDouble(prop, Bound.of(bound.min.doubleValue(), bound.max.doubleValue())).floatValue();
    }

    private static Lang fmlLang = new Lang("fml.configgui.tooltip");

    static void addCommentDetails(Property prop, Bound<?> bound) {
        prop.comment += (prop.comment.isEmpty() ? "" : "\n");
        if (bound.equals(Bound.MAX_BOUND)) {
            prop.comment += fmlLang
                    .localize("default", prop.isList() ? Arrays.toString(prop.getDefaults()) : prop.getDefault());
        } else {
            boolean minIsInt = bound.min.doubleValue() == bound.min.intValue();
            boolean maxIsInt = bound.max.doubleValue() == bound.max.intValue();
            prop.comment += fmlLang.localize(
                    "defaultNumeric",
                    minIsInt ? bound.min.intValue() : bound.min,
                    maxIsInt ? bound.max.intValue() : bound.max,
                    prop.isList() ? Arrays.toString(prop.getDefaults()) : prop.getDefault());
        }
    }

    /**
     * Gets a property from this config handler
     * 
     * @param key        name of the key for this property
     * @param defaultVal default value so a new property can be created
     * @return The property in the config
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    protected <T> Property getProperty(String key, T defaultVal) {
        return getProperty(key, defaultVal, RestartReqs.NONE);
    }

    /**
     * Gets a property from this config handler
     * 
     * @param key        name of the key for this property
     * @param defaultVal default value so a new property can be created
     * @return The property in the config
     * 
     * @throws IllegalArgumentException if defaultVal is not a valid property type
     * @throws IllegalStateException    if there is no active section
     */
    protected <T> Property getProperty(String key, T defaultVal, RestartReqs req) {
        checkInitialized();
        Section section = activeSection;
        Property prop = null;

    // @formatter:off
        // same logic as above method, mostly
        if (defaultVal instanceof Integer)
        {
            prop = config.get(section.name, key, (Integer) defaultVal);
        }
        if (defaultVal instanceof Boolean)
        {
            prop = config.get(section.name, key, (Boolean) defaultVal);
        }
        if (defaultVal instanceof int[])
        {
            prop = config.get(section.name, key, (int[]) defaultVal);
        }
        if (defaultVal instanceof String)
        {
            prop = config.get(section.name, key, (String) defaultVal);
        }
        if (defaultVal instanceof String[])
        {
            prop = config.get(section.name, key, (String[]) defaultVal);
        }
        // @formatter:on

        if (defaultVal instanceof Float || defaultVal instanceof Double) {
            double val = defaultVal instanceof Float ? ((Float) defaultVal).doubleValue()
                    : ((Double) defaultVal).doubleValue();
            prop = config.get(section.name, key, val);
        }

        if (prop != null) {
            return req.apply(prop);
        }

        throw new IllegalArgumentException("default value is not a config value type.");
    }

    /**
     * @return If this config handler should recieve {@link #initHook()} and {@link #postInitHook()} during config
     *         reload events. If this returns false, these methods will only be called on load.
     *         <p>
     *         Defaults to false.
     */
    protected boolean shouldHookOnReload() {
        return true;
    }

    /* IConfigHandler impl */

    @Override
    public void initHook() {}

    @Override
    public void postInitHook() {}

    // no need to override these, they are merely utilities, and reference private fields anyways

    @Override
    public final List<Section> getSections() {
        return ImmutableList.copyOf(sections);
    }

    @Override
    public final ConfigCategory getCategory(String name) {
        return config.getCategory(name);
    }

    @Override
    public final String getModID() {
        return modid;
    }
}
