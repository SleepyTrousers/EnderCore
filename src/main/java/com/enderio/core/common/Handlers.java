package com.enderio.core.common;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraftforge.common.MinecraftForge;

import com.enderio.core.EnderCore;
import com.enderio.core.IEnderMod;
import com.enderio.core.common.Handlers.Handler.Inst;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import static com.enderio.core.common.Handlers.Handler.Inst.*;

@UtilityClass
public class Handlers {

  /**
   * New enum to represent handler types. Possible to use to figure out what
   * type a method is without direct info.
   * 
   * The order of the constants is the order they are tried. This is VERY
   * important!
   */
  @RequiredArgsConstructor
  public enum HandlerType {

    /**
     * Represents the {@link MinecraftForge#ORE_GEN_BUS}
     */
    FORGE_OREGEN("net.minecraftforge.event.terraingen.OreGenEvent", MinecraftForge.ORE_GEN_BUS),

    /**
     * Represents the {@link MinecraftForge#TERRAIN_GEN_BUS}
     */
    FORGE_TERRAIN("net.minecraftforge.event.terraingen", MinecraftForge.TERRAIN_GEN_BUS),

    /**
     * Represents the {@link MinecraftForge#EVENT_BUS}
     */
    FORGE("net.minecraftforge", MinecraftForge.EVENT_BUS),

    /**
     * Represents the {@link FMLCommonHandler#bus()}
     */
    FML("cpw.mods.fml", FMLCommonHandler.instance().bus());

    public final String eventIdentifier;
    public final EventBus bus;
  }

  /**
   * To be put on classes that are Forge/FML event handlers. If you are using
   * this from another mod, be sure to implement {@link IEnderMod} on your
   * {@code @Mod} class, or call
   * {@code Handlers.addPackage("your.base.package")} so that this class can
   * search your classes
   * <p>
   * Class must have either:<br>
   * A public no args constructor (or lombok {@link NoArgsConstructor})
   * <b>OR</b><br>
   * A static singleton object with field name {@code INSTANCE} (public or
   * private). <b>OR</b><br>
   * A static method with name <code>instance()</code> (public or private)
   */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Handler {
    @Deprecated
    public enum HandlerType {
      /**
       * Represents the {@link MinecraftForge#EVENT_BUS}
       */
      FORGE,

      /**
       * Represents the {@link FMLCommonHandler#bus()}
       */
      FML
    }

    public enum Inst {
      /**
       * The default, will try all three methods, in the order
       * {@link Inst#CONSTRUCTOR}, {@link Inst#FIELD}, {@link Inst#METHOD}
       */
      AUTO,

      /**
       * The handler should be constructed from the default constructor of the
       * class
       */
      CONSTRUCTOR,

      /**
       * The handler should be grabbed from the static {@code INSTANCE} field of
       * the class
       */
      FIELD,

      /**
       * The handler should be grabbed from the static {@code instance()} method
       * of the class
       */
      METHOD;

      boolean matches(Inst other) {
        return this == AUTO || other == AUTO || other == this;
      }
    }

    /**
     * Array of buses to register this handler to. Leave blank for all.
     */
    @Deprecated
    HandlerType[] value() default { HandlerType.FORGE, HandlerType.FML };

    /**
     * The method of getting your handler's instance. Defaults to
     * {@link Inst#AUTO}
     */
    Inst getInstFrom() default AUTO;
  }

  private static final Set<String> packageSet = new HashSet<String>();

  private static Set<ASMData> annotations;

  public static void preInit(FMLPreInitializationEvent event) {
    annotations = event.getAsmData().getAll(Handler.class.getName());
  }

  //  private String getEnclosingPackage(Object obj) {
  //    Class<?> modClass = obj.getClass();
  //
  //    while (modClass.getComponentType() != null) {
  //      modClass = modClass.getComponentType();
  //    }
  //
  //    while (modClass.getEnclosingClass() != null) {
  //      modClass = modClass.getEnclosingClass();
  //    }
  //
  //    String name = modClass.getName();
  //    int lastDot = name.lastIndexOf('.');
  //
  //    return lastDot == -1 ? name : name.substring(0, lastDot);
  //  }

  /**
   * Registers a top level package to be searched for {@link Handler} classes.
   * Not needed if your {@code @Mod} class implements {@link IEnderMod}
   * 
   * @param packageName
   */
  @Deprecated
  public void addPackage(String packageName) {
    if (Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
      throw new RuntimeException("This method must only be called in preinit");
    }

    EnderCore.logger.info("Adding package " + packageName + " to handler search.");
    packageSet.add(packageName);
  }

  private boolean registered = false;

  /**
   * For internal use only. Do not call. Callers will be sacked.
   * 
   * @param event
   */
  public void register(FMLInitializationEvent event) {
    if (registered) {
      throw new IllegalStateException("I warned you!");
    }

    for (ASMData data : annotations) {
      String className = data.getClassName();
      // if not client handler, or we are on client, continue
      if (!className.contains("client") || FMLCommonHandler.instance().getEffectiveSide().isClient()) {
        try {
          Class<?> c = Class.forName(className);
          Annotation a = c.getAnnotation(Handler.class);
          if (a != null) {
            registerHandler(c, data, (Handler) a);
          }
        } catch (Throwable t) {
          EnderCore.logger.error(String.format("[Handlers] %s threw an error on load, skipping...", className));
          t.printStackTrace();
        }
      } else {
        EnderCore.logger.info(String.format("[Handlers] Skipping client class %s, we are on a dedicated server", className));
      }
    }

    registered = true;
  }

  private void registerHandler(Class<?> c, ASMData data, Handler handler) throws InstantiationException, IllegalAccessException {
    EnderCore.logger.info(String.format("[Handlers] Registering handler %s to busses: %s", c.getSimpleName(), Arrays.deepToString(handler.value())));

    Object inst = tryInit(handler, c);

    Method[] methods = c.getDeclaredMethods();
    EnumSet<HandlerType> types = EnumSet.noneOf(HandlerType.class);

    // Iterate over all methods to find those with @SubscribeEvent, then figure out what type that method is
    for (Method m : methods) {
      if (m.isAnnotationPresent(SubscribeEvent.class)) {
        Class<?>[] params = m.getParameterTypes();
        if (params.length >= 1) {
          Class<?> param = params[0];
          l: while (param != Event.class && param != null && param != Object.class) {
            for (HandlerType type : HandlerType.values()) {
              if (param.getName().contains(type.eventIdentifier)) {
                types.add(type);
                break l;
              }
            }
            param = param.getSuperclass();
          }
        }
      }
    }

    for (HandlerType type : types) {
      type.bus.register(inst);
    }
  }

  private Object tryInit(Handler annot, Class<?> c) {
    Inst pref = annot.getInstFrom();

    // Silence all exceptions to trickle down to next if statement.
    // If all three fail, RuntimeException is thrown.
    if (pref.matches(CONSTRUCTOR)) {
      try {
        return c.newInstance();
      } catch (Exception e) {
      }
    }

    if (pref.matches(FIELD)) {
      try {
        Field inst = c.getDeclaredField("INSTANCE");
        inst.setAccessible(true);
        return inst.get(null);
      } catch (Exception e) {
      }
    }

    if (pref.matches(METHOD)) {
      try {
        Method inst = c.getDeclaredMethod("instance");
        inst.setAccessible(true);
        return inst.invoke(null);
      } catch (Exception e) {
      }
    }

    throw new RuntimeException("Could not instantiate @Handler class " + c.getName() + " or access INSTANCE field or instance() method.");
  }
}
