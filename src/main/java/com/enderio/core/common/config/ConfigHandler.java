package com.enderio.core.common.config;

import java.io.File;

import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.Handlers.Handler.HandlerType;
import com.enderio.core.common.Handlers.Handler.Inst;
import com.enderio.core.common.config.ConfigProcessor.IReloadCallback;
import com.enderio.core.common.config.JsonConfigReader.ModToken;
import com.enderio.core.common.config.annot.Comment;
import com.enderio.core.common.config.annot.Config;
import com.enderio.core.common.config.annot.NoSync;
import com.enderio.core.common.config.annot.Range;
import com.enderio.core.common.config.annot.RestartReq;
import com.enderio.core.common.handlers.RightClickCropHandler;
import com.enderio.core.common.handlers.RightClickCropHandler.PlantInfo;
import com.enderio.core.common.tweaks.Tweak;
import com.enderio.core.common.tweaks.Tweaks;

import net.minecraftforge.common.config.Configuration;

@Handler(value = HandlerType.FML, getInstFrom = Inst.METHOD)
public class ConfigHandler extends AbstractConfigHandler implements ITweakConfigHandler, IReloadCallback
{
    private static final String sectionGeneral = Configuration.CATEGORY_GENERAL;
    private static final String sectionEnchants = "enchants";

    @Config
    @Comment({"Show oredictionary names of every item in its tooltip.", "0 - Off", "1 - Always on", "2 - Only with shift", "3 - Only in debug mode"})
    @Range(min = 0, max = 3)
    @NoSync
    public static int showOredictTooltips = 1;

    @Config
    @Comment({ "Show item registry names in tooltips.", "0 - Off", "1 - Always on", "2 - Only with shift", "3 - Only in debug mode" })
    @Range(min = 0, max = 3)
    @NoSync
    public static int showRegistryNameTooltips = 3;

    @Config
    @Comment({ "Removes all void fog.", "0 = off", "1 = DEFAULT worldtype only", "2 = all world types" })
    @NoSync
    @Range(min = 0, max = 2)
    public static int disableVoidFog = 1;

    @Config
    @Comment("The max amount of XP levels an anvil recipe can use.")
    public static int anvilMaxLevel = 40;

    @Config
    @Comment("The way the game should have been made (Yes this is the fireworks thing).")
    public static boolean betterAchievements = true;

    @Config
    @Comment("Disabling this option will prevent any crops added to the config json from being right clickable.")
    public static boolean allowCropRC = true;

    @Config
    @Comment({ "0 - Do nothing", "1 - Remove stacktraces, leave 1-line missing texture errors", "2 - Remove all missing texture errors completely." })
    @NoSync
    @Range(min = 0, max = 2)
    public static int textureErrorRemover = 1;

    @Config
    @Comment({ "Controls the default sorting on the mod list GUI.", "0 - Default sort (load order)", "1 - A to Z sort", "2 - Z to A sort" })
    @NoSync
    @Range(min = 0, max = 2)
    public static int defaultModSort = 1;

    @Config(sectionEnchants)
    @Comment("Enchant ID for the XP boost enchant.")
    @RestartReq(RestartReqs.REQUIRES_MC_RESTART)
    @Range(min = 0, max = 255)
    public static int enchantIDXPBoost = 45;

    @Config(sectionEnchants)
    @Comment("Allow the XP Boost enchant to be registered.")
    @RestartReq(RestartReqs.REQUIRES_MC_RESTART)
    public static boolean allowXPBoost = true;

    @Config(sectionEnchants)
    @Comment("Enchant ID for the Auto Smelt enchant.")
    @RestartReq(RestartReqs.REQUIRES_MC_RESTART)
    @Range(min = 0, max = 255)
    public static int enchantIDAutoSmelt = 46;

    @Config(sectionEnchants)
    @Comment("Allow the Auto Smelt enchant to be registered.")
    @RestartReq(RestartReqs.REQUIRES_MC_RESTART)
    public static boolean allowAutoSmelt = true;

    @Config(sectionEnchants)
    @Comment("Allow the Auto Smelt enchant to work with Fortune.")
    public static boolean allowAutoSmeltWithFortune = true;
    
//    @Config("test")
//    public static List<Integer> test1 = Lists.newArrayList(1, 2, 3);
//    @Config("test")
//    public static List<Double> test2 = Lists.newArrayList(0.1, 0.2, 0.3);
//    @Config("test")
//    public static List<Boolean> test3 = Lists.newArrayList(true, false, true);
//    @Config("test")
//    public static List<String> test4 = Lists.newArrayList("test1", "test2", "test3");

    private static ConfigHandler INSTANCE;

    public static File configFolder, enderConfigFolder;
    public static File configFile;
    public static ConfigProcessor processor;

    public static ConfigHandler instance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ConfigHandler();
        }
        return INSTANCE;
    }

    protected ConfigHandler()
    {
        super(EnderCore.MODID);
    }

    @Override
    public void init()
    {
        addSection(sectionGeneral);
        addSection(sectionEnchants);
        addSection("tweaks");
        processor = new ConfigProcessor(getClass(), this, this);
        processor.process(true);
    }

    @Override
    protected void reloadIngameConfigs()
    {
        Tweaks.loadIngameTweaks();
    }

    @Override
    protected void reloadNonIngameConfigs()
    {
        Tweaks.loadNonIngameTweaks();
    }

    @Override
    public void callback(ConfigProcessor inst)
    {
        Tweaks.loadIngameTweaks();
    }

    @Override
    public boolean addBooleanFor(Tweak tweak)
    {
        activateSection("tweaks");
        return getValue(tweak.getName(), tweak.getComment(), true);
    }

    public void loadRightClickCrops()
    {
        JsonConfigReader<PlantInfo> reader = new JsonConfigReader<PlantInfo>(new ModToken(EnderCore.class, EnderCore.MODID.toLowerCase() + "/config"),
                enderConfigFolder.getAbsolutePath() + "/cropConfig.json", PlantInfo.class);
        for (PlantInfo i : reader)
        {
            i.init();
            RightClickCropHandler.INSTANCE.addCrop(i);
        }
    }
}
