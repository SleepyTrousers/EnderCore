package com.enderio.core;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enderio.core.api.common.config.IConfigHandler;
import com.enderio.core.common.Lang;
import com.enderio.core.common.compat.CompatRegistry;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.imc.IMCRegistry;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.transform.EnderCorePlugin;
import com.enderio.core.common.tweaks.Tweaks;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.PermanentCache;
import com.enderio.core.common.util.stackable.Things;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EnderCore.MODID)
public class EnderCore implements IEnderMod {

  public static final @Nonnull String MODID = "endercore";
  public static final @Nonnull String NAME = "EnderCore";
  public static final @Nonnull String BASE_PACKAGE = "com.enderio";

  public static final @Nonnull Logger logger = NullHelper.notnull(LogManager.getLogger(NAME), "failed to aquire logger");
  public static final @Nonnull Lang lang = new Lang(MODID);

  public final @Nonnull List<IConfigHandler> configs = Lists.newArrayList();

  private final @Nonnull Set<String> invisibleRequesters = Sets.newHashSet();

  public EnderCore() {
//    EnderCorePlugin.instance().loadMixinSources(this);
    
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    bus.addListener(this::preInit);
    bus.addListener(this::postInit);
    bus.addListener(this::handleIMC);
    bus.addListener(this::loadComplete);
    bus.addListener(this::onServerStarting);
  }

  /**
   * Call this method BEFORE preinit (construction phase) to request that EnderCore start in invisible mode. This will disable ANY gameplay features unless the
   * user forcibly disables invisible mode in the config.
   */
  public void requestInvisibleMode() {
    final ModContainer activeModContainer = ModLoadingContext.get().getActiveContainer();
    if (activeModContainer != null) {
      invisibleRequesters.add(activeModContainer.getModInfo().getDisplayName());
    } else {
      invisibleRequesters.add("null");
    }
  }

  public boolean invisibilityRequested() {
    return !invisibleRequesters.isEmpty();
  }

  public @Nonnull Set<String> getInvisibleRequsters() {
    return ImmutableSet.copyOf(invisibleRequesters);
  }

  private void preInit(@Nonnull FMLCommonSetupEvent event) {
//    ConfigHandler.instance().initialize(NullHelper.notnullJ(ConfigHandler.configFile, "it was there a second ago, I swear!"));
//    Handlers.preInit(event);

//    CompatRegistry.INSTANCE.handle(event);
//
//    proxy.onPreInit(event);

//    Things.init(event);
    EnderPacketHandler.init();

    for (IConfigHandler c : configs) {
      c.initHook();
    }

//    Handlers.register(event);
//    CompatRegistry.INSTANCE.handle(event);

    IMCRegistry.INSTANCE.init();
  }

  private void postInit(@Nonnull InterModEnqueueEvent event) {
    Tweaks.loadLateTweaks();
    for (IConfigHandler c : configs) {
      c.postInitHook();
    }

    CompatRegistry.INSTANCE.handle(event);
    ConfigHandler.instance().loadRightClickCrops();
  }
  
  private void handleIMC(@Nonnull InterModProcessEvent event) {
	IMCRegistry.INSTANCE.handleEvent(event);
  }

  private void loadComplete(@Nonnull FMLLoadCompleteEvent event) {
    Things.init(event);
  }

  private void onServerStarting(@Nonnull FMLServerStartingEvent event) {
//    event.registerServerCommand(new CommandScoreboardInfo());
    PermanentCache.saveCaches();
  }

  @Override
  public @Nonnull String modid() {
    return MODID;
  }

  @Override
  public @Nonnull String name() {
    return NAME;
  }

  @Override
  public @Nonnull String version() {
    return ModList.get().getModContainerById(MODID).get().getModInfo().getVersion().toString();
  }
}
