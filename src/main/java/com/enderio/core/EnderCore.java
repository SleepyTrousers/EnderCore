package com.enderio.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

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
import com.enderio.core.common.imc.IMCRegistry;
import com.enderio.core.common.mixin.SimpleMixinLoader;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.tweaks.Tweaks;
import com.enderio.core.common.util.EnderFileUtils;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.PermanentCache;
import com.enderio.core.common.util.stackable.Things;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.command.CommandHandler;
import net.minecraft.crash.CrashReportCategory;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.chunkio.ChunkIOExecutor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(modid = EnderCore.MODID, name = EnderCore.NAME, version = EnderCore.VERSION, guiFactory = "com.enderio.core.common.config.BaseConfigFactory")
public class EnderCore implements IEnderMod {

  public static final @Nonnull String MODID = "endercore";
  public static final @Nonnull String DOMAIN = MODID.toLowerCase(Locale.US);
  public static final @Nonnull String NAME = "EnderCore";
  public static final @Nonnull String BASE_PACKAGE = "com.enderio";
  public static final @Nonnull String VERSION = "@VERSION@";

  public static final @Nonnull Logger logger = NullHelper.notnull(LogManager.getLogger(NAME), "failed to aquire logger");
  public static final @Nonnull Lang lang = new Lang(MODID);

  @Instance(MODID)
  public static EnderCore instance;

  @SidedProxy(serverSide = "com.enderio.core.common.CommonProxy", clientSide = "com.enderio.core.client.ClientProxy")
  public static CommonProxy proxy;

  public final @Nonnull List<IConfigHandler> configs = Lists.newArrayList();

  private final @Nonnull Set<String> invisibleRequesters = Sets.newHashSet();

  public EnderCore() {
    SimpleMixinLoader.loadMixinSources(this);
  }

  /**
   * Call this method BEFORE preinit (construction phase) to request that EnderCore start in invisible mode. This will disable ANY gameplay features unless the
   * user forcibly disables invisible mode in the config.
   */
  public void requestInvisibleMode() {
    final ModContainer activeModContainer = Loader.instance().activeModContainer();
    if (activeModContainer != null) {
      invisibleRequesters.add(activeModContainer.getName());
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

  @EventHandler
  public void preInit(@Nonnull FMLPreInitializationEvent event) {
    ConfigHandler.configFolder = event.getModConfigurationDirectory();
    ConfigHandler.enderConfigFolder = new File(ConfigHandler.configFolder.getPath() + "/" + MODID);
    ConfigHandler.configFile = new File(ConfigHandler.enderConfigFolder.getPath() + "/" + event.getSuggestedConfigurationFile().getName());

    if (!ConfigHandler.configFile.exists() && event.getSuggestedConfigurationFile().exists()) {
      try {
        FileUtils.copyFile(event.getSuggestedConfigurationFile(), ConfigHandler.configFile);
      } catch (IOException e) {
        Throwables.propagate(e);
      }
      EnderFileUtils.safeDelete(event.getSuggestedConfigurationFile());
    }

    ConfigHandler.instance().initialize(NullHelper.notnullJ(ConfigHandler.configFile, "it was there a second ago, I swear!"));
    Handlers.preInit(event);

    CompatRegistry.INSTANCE.handle(event);

    proxy.onPreInit(event);
  }

  @EventHandler
  public void init(@Nonnull FMLInitializationEvent event) {
    OreDict.registerVanilla();
    Things.init(event);
    EnderPacketHandler.init();

    for (IConfigHandler c : configs) {
      c.initHook();
    }

    Handlers.register(event);
    CompatRegistry.INSTANCE.handle(event);
    if (event.getSide().isServer()) {
      ((CommandHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager()).registerCommand(CommandReloadConfigs.SERVER);
    } else {
      ClientCommandHandler.instance.registerCommand(CommandReloadConfigs.CLIENT);
    }

    IMCRegistry.INSTANCE.init();
  }

  @EventHandler
  public void postInit(@Nonnull FMLPostInitializationEvent event) {
    Tweaks.loadLateTweaks();
    for (IConfigHandler c : configs) {
      c.postInitHook();
    }

    CompatRegistry.INSTANCE.handle(event);
    ConfigHandler.instance().loadRightClickCrops();
  }

  @EventHandler
  public void loadComplete(@Nonnull FMLLoadCompleteEvent event) {
    Things.init(event);

    ThreadPoolExecutor fixedChunkExecutor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
        new ThreadFactory() {
          private AtomicInteger count = new AtomicInteger(1);

          @Override
          public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Chunk I/O Executor Thread-" + count.getAndIncrement());
            thread.setDaemon(true);
            return thread;
          }
        }) {

      @Override
      @SuppressWarnings({ "unchecked", "rawtypes" })
      protected void afterExecute(Runnable r, Throwable t) {
        if (t != null) {
          try {
            FMLLog.log.error("Unhandled exception loading chunk:", t);
            Object queuedChunk = ReflectionHelper.getPrivateValue((Class) r.getClass(), (Object) r, "chunkInfo");
            Class cls = queuedChunk.getClass();
            FMLLog.log.error(queuedChunk);
            int x = (Integer) ReflectionHelper.getPrivateValue(cls, queuedChunk, "x");
            int z = (Integer) ReflectionHelper.getPrivateValue(cls, queuedChunk, "z");
            FMLLog.log.error(CrashReportCategory.getCoordinateInfo(x << 4, 64, z << 4));
          } catch (Throwable t2) {
            FMLLog.log.error(t2);
          }
        }
      }
    };
    try {
      EnumHelper.setFailsafeFieldValue(ReflectionHelper.findField(ChunkIOExecutor.class, "pool"), null, fixedChunkExecutor);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @EventHandler
  public void onServerStarting(@Nonnull FMLServerStartingEvent event) {
    event.registerServerCommand(new CommandScoreboardInfo());
    PermanentCache.saveCaches();
  }

  @EventHandler
  public void onIMCEvent(@Nonnull IMCEvent event) {
    IMCRegistry.INSTANCE.handleEvent(event);
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
    return VERSION;
  }
}
