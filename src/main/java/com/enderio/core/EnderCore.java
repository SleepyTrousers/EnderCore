package com.enderio.core;

import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import com.enderio.core.client.ClientProxy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.Lang;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.stackable.Things;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraftforge.fml.common.Mod;

@Mod(EnderCore.MODID) public class EnderCore implements IEnderMod {

  public static final @Nonnull String MODID = "endercore";
  public static final @Nonnull String DOMAIN = MODID.toLowerCase(Locale.US);
  public static final @Nonnull String NAME = "EnderCore";
  public static final @Nonnull String BASE_PACKAGE = "com.enderio";
  public static final @Nonnull String VERSION = "@VERSION@";

  public static final @Nonnull Logger logger = NullHelper.notnull(LogManager.getLogger(NAME), "failed to aquire logger");
  public static final @Nonnull Lang lang = new Lang(MODID);

  public static EnderCore instance;

  public static CommonProxy proxy;

  //  public final @Nonnull List<IConfigHandler> configs = Lists.newArrayList();

  public EnderCore() {
    instance = this;

    proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
  }

  @SubscribeEvent public void setup(@Nonnull FMLCommonSetupEvent event) {
    proxy.setup(event);

    Things.init(event);

    EnderPacketHandler.init();
  }

  @SubscribeEvent public void loadComplete(@Nonnull FMLLoadCompleteEvent event) {
    Things.init(event);

    //    ThreadPoolExecutor fixedChunkExecutor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
    //        new ThreadFactory() {
    //          private AtomicInteger count = new AtomicInteger(1);
    //
    //          @Override
    //          public Thread newThread(Runnable r) {
    //            Thread thread = new Thread(r, "Chunk I/O Executor Thread-" + count.getAndIncrement());
    //            thread.setDaemon(true);
    //            return thread;
    //          }
    //        }) {
    //
    //      @Override
    //      @SuppressWarnings({ "unchecked", "rawtypes" })
    //      protected void afterExecute(Runnable r, Throwable t) {
    //        if (t != null) {
    //          try {
    //            FMLLog.log.error("Unhandled exception loading chunk:", t);
    //            Object queuedChunk = ReflectionHelper.getPrivateValue((Class) r.getClass(), (Object) r, "chunkInfo");
    //            Class cls = queuedChunk.getClass();
    //            FMLLog.log.error(queuedChunk);
    //            int x = (Integer) ReflectionHelper.getPrivateValue(cls, queuedChunk, "x");
    //            int z = (Integer) ReflectionHelper.getPrivateValue(cls, queuedChunk, "z");
    //            FMLLog.log.error(CrashReportCategory.getCoordinateInfo(x << 4, 64, z << 4));
    //          } catch (Throwable t2) {
    //            FMLLog.log.error(t2);
    //          }
    //        }
    //      }
    //    };

    //    try {
    //      EnumHelper.setFailsafeFieldValue(ReflectionHelper.findField(ChunkIOExecutor.class, "pool"), null, fixedChunkExecutor);
    //    } catch (Exception e) {
    //      throw new RuntimeException(e);
    //    }
  }

  @Override public @Nonnull String modid() {
    return MODID;
  }

  @Override public @Nonnull String name() {
    return NAME;
  }

  @Override public @Nonnull String version() {
    return VERSION;
  }
}
