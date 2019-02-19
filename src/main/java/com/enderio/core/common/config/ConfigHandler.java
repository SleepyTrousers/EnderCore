package com.enderio.core.common.config;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import javax.annotation.Nonnull;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.ConfigProcessor.IReloadCallback;
import com.enderio.core.common.config.JsonConfigReader.ModToken;
import com.enderio.core.common.config.annot.Comment;
import com.enderio.core.common.config.annot.Config;
import com.enderio.core.common.config.annot.NoSync;
import com.enderio.core.common.config.annot.Range;
import com.enderio.core.common.config.annot.RestartReq;
import com.enderio.core.common.handlers.RightClickCropHandler;
import com.enderio.core.common.handlers.RightClickCropHandler.LegacyPlantInfo;
import com.enderio.core.common.tweaks.Tweak;
import com.enderio.core.common.tweaks.Tweaks;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.Fluid;

public class ConfigHandler extends AbstractConfigHandler implements ITweakConfigHandler, IReloadCallback {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(value = ElementType.FIELD)
  private static @interface InvisibleInt {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(value = ElementType.FIELD)
  private static @interface InvisIgnore {
  }

  private static final String sectionGeneral = Configuration.CATEGORY_GENERAL;
  private static final String sectionEnchants = "enchants";

  @Config
  @Comment({
      "Control the behavior of invisible mode (disables all gameplay features). Having this setting be different between client and server could cause some desync, but otherwise is harmless.",
      "0 - Default. Lets other mods request invisible mode. If none do, invisible mode is off.", "-1 (or below) - Never invisible, even if mods request it.",
      "1 (or above) - Always invisible, even if no mods request it." })
  public static int invisibleMode = 0;

  @Config
  @Comment({ "Show oredictionary names of every item in its tooltip.", "0 - Off", "1 - Always on", "2 - Only with shift", "3 - Only in debug mode" })
  @Range(min = 0, max = 3)
  @NoSync
  public static int showOredictTooltips = 2;

  @Config
  @Comment({ "Show item registry names in tooltips.", "0 - Off", "1 - Always on", "2 - Only with shift", "3 - Only in debug mode" })
  @Range(min = 0, max = 3)
  @NoSync
  public static int showRegistryNameTooltips = 3;

  @Config
  @Comment({ "Show durability on item tooltips.", "0 - Off", "1 - Always on", "2 - Only with shift", "3 - Only in debug mode" })
  @Range(min = 0, max = 3)
  @NoSync
  public static int showDurabilityTooltips = 1;

  @Config
  @Comment("The way the game should have been made (Yes this is the fireworks thing).")
  public static boolean betterAchievements = false;

  @Config
  @Comment("Fireworks display on new years eve")
  public static boolean newYearsFireworks = true;

  @Config
  @Comment("Disabling this option will prevent any crops added to the config json from being right clickable.")
  public static boolean allowCropRC = true;

  @Config
  @Comment("Disabling this option will prevent tick speedup (i.e. torcherino) on any TE that uses the base TE class from EnderCore")
  @InvisIgnore
  public static boolean allowExternalTickSpeedup = true;

  @Config(sectionEnchants)
  @Comment("Allow the XP Boost enchant to be registered.")
  @RestartReq(RestartReqs.REQUIRES_MC_RESTART)
  public static boolean allowXPBoost = true;

  @Config(sectionEnchants)
  @Comment("Allow the Auto Smelt enchant to be registered.")
  @RestartReq(RestartReqs.REQUIRES_MC_RESTART)
  public static boolean allowAutoSmelt = true;

  // @Config("test")
  // public static List<Integer> test1 = Lists.newArrayList(1, 2, 3);
  // @Config("test")
  // public static List<Double> test2 = Lists.newArrayList(0.1, 0.2, 0.3);
  // @Config("test")
  // public static List<Boolean> test3 = Lists.newArrayList(true, false, true);
  // @Config("test")
  // public static List<String> test4 = Lists.newArrayList("test1", "test2", "test3");

  private static ConfigHandler INSTANCE;

  public static File configFolder, enderConfigFolder;
  public static File configFile;
  public static ConfigProcessor processor;

  public static ConfigHandler instance() {
    if (INSTANCE == null) {
      INSTANCE = new ConfigHandler();
    }
    return INSTANCE;
  }

  @Config
  @Comment("Amount of water in mB that a water bottle contains")
  @Range(min = 1, max = 1000)
  public static int waterBottleAmount = Fluid.BUCKET_VOLUME / 3 /* net.minecraft.block.BlockCauldron.LEVEL */;

  protected ConfigHandler() {
    super(EnderCore.MODID);
  }

  @Override
  public void init() {
    addSection(sectionGeneral);
    addSection(sectionEnchants);
    addSection("tweaks");
    addSection("invisibility");
    processor = new ConfigProcessor(getClass(), this, this) {

      @Override
      protected Object getConfigValue(@Nonnull String section, @Nonnull String[] commentLines, @Nonnull Field f, @Nonnull Object defVal) {
        Object res = super.getConfigValue(section, commentLines, f, defVal);
        if (f.getName() == "invisibleMode") {
          if (res.equals(0)) {
            return EnderCore.instance.invisibilityRequested() ? 1 : -1;
          }
        } else if (invisibleMode == 1 && !f.isAnnotationPresent(InvisIgnore.class)) {
          if (f.getType() == int.class) {
            return 0;
          } else if (f.getType() == boolean.class) {
            return false;
          }
        }
        return res;
      }
    };
    processor.process(true);
  }

  @Override
  protected void reloadIngameConfigs() {
    Tweaks.loadIngameTweaks();
  }

  @Override
  protected void reloadNonIngameConfigs() {
    Tweaks.loadNonIngameTweaks();
  }

  @Override
  public void callback(@Nonnull ConfigProcessor inst) {
    reloadAllConfigs();
  }

  @Override
  public boolean addBooleanFor(Tweak tweak) {
    activateSection("tweaks");
    boolean ret = getValue(tweak.getName(), tweak.getComment(), tweak.enabledByDefault(), tweak.getRestartReq());
    return invisibleMode == 1 ? false : ret;
  }

  public boolean showInvisibleWarning() {
    activateSection("invisibility");
    ConfigCategory cat = getConfig().getCategory("invisibility");
    boolean ret = false;
    if (!cat.containsKey("invisibilityWarning")) {
      ret = true;
    }
    boolean val = getValue("invisibilityWarning", "If set to true, the invisibility warning will show every time the user logs in.", false);
    saveConfigFile();
    return ret ? ret : val;
  }

  public void loadRightClickCrops() {
    JsonConfigReader<LegacyPlantInfo> reader = new JsonConfigReader<LegacyPlantInfo>(new ModToken(EnderCore.class, EnderCore.DOMAIN + "/config"),
        enderConfigFolder.getAbsolutePath() + "/cropconfig.json", LegacyPlantInfo.class);
    for (LegacyPlantInfo i : reader) {
      if (i.init("in configfile 'cropConfig.json'")) {
        RightClickCropHandler.INSTANCE.addCrop(i);
      }
    }
  }
}
