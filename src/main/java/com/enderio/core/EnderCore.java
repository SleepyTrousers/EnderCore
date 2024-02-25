package com.enderio.core;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.ClientCommandHandler;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enderio.core.api.common.config.IConfigHandler;
import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.Handlers;
import com.enderio.core.common.Lang;
import com.enderio.core.common.OreDict;
import com.enderio.core.common.command.CommandReloadConfigs;
import com.enderio.core.common.command.CommandScoreboardInfo;
import com.enderio.core.common.compat.CompatRegistry;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.enchant.EnchantAutoSmelt;
import com.enderio.core.common.enchant.EnchantXPBoost;
import com.enderio.core.common.imc.IMCRegistry;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.util.EnderFileUtils;
import com.enderio.core.common.util.PermanentCache;
import com.enderio.core.common.util.TextureErrorRemover;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
        modid = EnderCore.MODID,
        name = EnderCore.NAME,
        version = EnderCoreTags.VERSION,
        dependencies = "after:ttCore; after:gtnhlib@[0.0.10,)",
        guiFactory = "com.enderio.core.common.config.BaseConfigFactory")
public class EnderCore implements IEnderMod {

    public static final String MODID = "endercore";
    public static final String NAME = "EnderCore";
    public static final String BASE_PACKAGE = "com.enderio";

    public static final Logger logger = LogManager.getLogger(NAME);
    public static final Lang lang = new Lang(MODID);

    @Instance(MODID)
    public static EnderCore instance;

    @SidedProxy(serverSide = "com.enderio.core.common.CommonProxy", clientSide = "com.enderio.core.client.ClientProxy")
    public static CommonProxy proxy;

    public List<IConfigHandler> configs = Lists.newArrayList();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (Loader.isModLoaded("ttCore")) {
            proxy.throwModCompatibilityError(
                    lang.localize("error.ttcore.1"),
                    lang.localize("error.ttcore.2"),
                    lang.localize("error.ttcore.3"));
        }

        if (event.getSide().isClient()) {
            TextureErrorRemover.beginIntercepting();
        }

        ConfigHandler.configFolder = event.getModConfigurationDirectory();
        ConfigHandler.enderConfigFolder = new File(ConfigHandler.configFolder.getPath() + "/" + MODID);
        ConfigHandler.configFile = new File(
                ConfigHandler.enderConfigFolder.getPath() + "/" + event.getSuggestedConfigurationFile().getName());

        if (!ConfigHandler.configFile.exists() && event.getSuggestedConfigurationFile().exists()) {
            try {
                FileUtils.copyFile(event.getSuggestedConfigurationFile(), ConfigHandler.configFile);
            } catch (IOException e) {
                Throwables.propagate(e);
            }
            EnderFileUtils.safeDelete(event.getSuggestedConfigurationFile());
        }

        ConfigHandler.instance().initialize(ConfigHandler.configFile);
        Handlers.preInit(event);

        CompatRegistry.INSTANCE.handle(event);
        OreDict.registerVanilla();

        EnchantXPBoost.INSTANCE.register();
        EnchantAutoSmelt.INSTANCE.register();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        EnderPacketHandler.init();

        for (IConfigHandler c : configs) {
            c.initHook();
        }

        Handlers.register(event);
        CompatRegistry.INSTANCE.handle(event);
        ClientCommandHandler.instance.registerCommand(CommandReloadConfigs.CLIENT);
        if (event.getSide().isServer()) {
            ((CommandHandler) MinecraftServer.getServer().getCommandManager())
                    .registerCommand(CommandReloadConfigs.SERVER);
        }

        IMCRegistry.INSTANCE.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        for (IConfigHandler c : configs) {
            c.postInitHook();
        }

        CompatRegistry.INSTANCE.handle(event);
        ConfigHandler.instance().loadRightClickCrops();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandScoreboardInfo());
        PermanentCache.saveCaches();
    }

    @EventHandler
    public void onIMCEvent(IMCEvent event) {
        IMCRegistry.INSTANCE.handleEvent(event);
    }

    @Override
    public String modid() {
        return MODID;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String version() {
        return EnderCoreTags.VERSION;
    }
}
