package com.enderio.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enderio.core.api.common.config.IConfigHandler;
import com.enderio.core.client.render.IconUtil;
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
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = EnderCore.MODID, name = EnderCore.NAME, version = EnderCore.VERSION, dependencies = "after:ttCore", guiFactory = "com.enderio.core.common.config.BaseConfigFactory")
public class EnderCore implements IEnderMod {

  public static final String MODID = "endercore";
  public static final String NAME = "EnderCore";
  public static final String BASE_PACKAGE = "com.enderio";
  public static final String VERSION = "@VERSION@";

  public static final Logger logger = LogManager.getLogger(NAME);
  public static final Lang lang = new Lang(MODID);

  @Instance(MODID)
  public static EnderCore instance;

  @SidedProxy(serverSide = "com.enderio.core.common.CommonProxy", clientSide = "com.enderio.core.client.ClientProxy")
  public static CommonProxy proxy;

  public List<IConfigHandler> configs = Lists.newArrayList();

  private Set<String> invisibleRequesters = Sets.newHashSet();

  /**
   * Call this method BEFORE preinit (construction phase) to request that
   * EnderCore start in invisible mode. This will disable ANY gameplay features
   * unless the user forcibly disables invisible mode in the config.
   */
  public void requestInvisibleMode() {
    invisibleRequesters.add(Loader.instance().activeModContainer().getName());
  }

  public boolean invisibilityRequested() {
    return !invisibleRequesters.isEmpty();
  }

  public Set<String> getInvisibleRequsters() {
    return ImmutableSet.copyOf(invisibleRequesters);
  }

  @EventHandler
  public void preInit(FMLPreInitializationEvent event) {

    IconUtil.instance.init();
    
    ConfigHandler.configFolder = event.getModConfigurationDirectory();
    ConfigHandler.enderConfigFolder = new File(ConfigHandler.configFolder.getPath() + "/" + MODID);
    ConfigHandler.configFile = new File(ConfigHandler.enderConfigFolder.getPath() + "/" + event.getSuggestedConfigurationFile().getName());

    if(!ConfigHandler.configFile.exists() && event.getSuggestedConfigurationFile().exists()) {
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

    EnchantXPBoost.register();
    EnchantAutoSmelt.register();
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
    if(event.getSide().isServer()) {
      ((CommandHandler) MinecraftServer.getServer().getCommandManager()).registerCommand(CommandReloadConfigs.SERVER);
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
    return VERSION;
  }
}
