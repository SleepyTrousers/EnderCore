package com.enderio.core.common;

import static com.enderio.core.common.Handlers.Handler.Inst.AUTO;
import static com.enderio.core.common.Handlers.Handler.Inst.CONSTRUCTOR;
import static com.enderio.core.common.Handlers.Handler.Inst.FIELD;
import static com.enderio.core.common.Handlers.Handler.Inst.METHOD;
import static com.enderio.core.common.Handlers.Handler.Inst.SCALA_OBJECT;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.MinecraftForge;

import com.enderio.core.EnderCore;
import com.enderio.core.IEnderMod;
import com.enderio.core.common.Handlers.Handler.HandlerSide;
import com.enderio.core.common.Handlers.Handler.Inst;
import com.google.common.base.Throwables;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;
import cpw.mods.fml.common.discovery.asm.ModAnnotation.EnumHolder;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;

public class Handlers {

    /**
     * New enum to represent handler types. Possible to use to figure out what type a method is without direct info.
     * 
     * The order of the constants is the order they are tried. This is VERY important!
     */
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

        private HandlerType(String eventIdentifier, EventBus bus) {
            this.eventIdentifier = eventIdentifier;
            this.bus = bus;
        }

        public final String eventIdentifier;
        public final EventBus bus;
    }

    /**
     * To be put on classes that are Forge/FML event handlers. EnderCore will automatically figure out what busses it
     * needs to be registered to.
     * <p>
     * It can get the instance of the handler in a few ways: <br>
     * A public no args constructor (or lombok {@link NoArgsConstructor}) <b>OR</b><br>
     * A static singleton object with field name {@code INSTANCE} (public or private). <b>OR</b><br>
     * A static method with name <code>instance()</code> (public or private)
     * <p>
     * This can also be explicitly set using {@link #getInstFrom()} to avoid invoking code that may cause issues.
     */
    // Explicit names to work around https://bugs.openjdk.org/browse/JDK-8185328
    @java.lang.annotation.Target(ElementType.TYPE)
    @java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
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
             * The default, will try all three methods, in the order {@link Inst#CONSTRUCTOR}, {@link Inst#FIELD},
             * {@link Inst#METHOD}
             */
            AUTO,

            /**
             * The handler should be constructed from the default constructor of the class
             */
            CONSTRUCTOR,

            /**
             * The handler should be grabbed from the static {@code INSTANCE} field of the class
             */
            FIELD,

            /**
             * The handler should be grabbed from the static {@code instance()} method of the class
             */
            METHOD,

            /**
             * Added for scala compat, not necessary to set explicitly ({@link #AUTO} will find this).
             */
            SCALA_OBJECT;

            boolean matches(Inst other) {
                return this == AUTO || other == AUTO || other == this;
            }
        }

        public enum HandlerSide {

            /**
             * Whether to load this handler or not will be determined by the package name. If it contains "client" it
             * will only load clientside. The default value.
             */
            AUTO,

            /**
             * Will load on both sides.
             */
            COMMON,

            /**
             * Will load only clientside.
             */
            CLIENT,

            /**
             * Will load only serverside.
             */
            SERVER;

            public boolean equals(Side other) {
                if (this == COMMON) {
                    return true;
                } else {
                    return this == CLIENT ? other == Side.CLIENT : other == Side.SERVER;
                }
            }
        }

        /**
         * Array of buses to register this handler to. Leave blank for all.
         */
        @Deprecated
        HandlerType[] value() default { HandlerType.FORGE, HandlerType.FML };

        /**
         * The method of getting your handler's instance. Defaults to {@link Inst#AUTO}
         */
        Inst getInstFrom() default AUTO;

        /**
         * The side to load this handler on. Defaults to {@link HandlerSide#AUTO}
         */
        HandlerSide side() default HandlerSide.AUTO;
    }

    private static final Set<String> packageSet = new HashSet<String>();

    private static Set<ASMData> annotations;

    public static void preInit(FMLPreInitializationEvent event) {
        annotations = event.getAsmData().getAll(Handler.class.getName());
    }

    // private String getEnclosingPackage(Object obj) {
    // Class<?> modClass = obj.getClass();
    //
    // while (modClass.getComponentType() != null) {
    // modClass = modClass.getComponentType();
    // }
    //
    // while (modClass.getEnclosingClass() != null) {
    // modClass = modClass.getEnclosingClass();
    // }
    //
    // String name = modClass.getName();
    // int lastDot = name.lastIndexOf('.');
    //
    // return lastDot == -1 ? name : name.substring(0, lastDot);
    // }

    /**
     * Registers a top level package to be searched for {@link Handler} classes. Not needed if your {@code @Mod} class
     * implements {@link IEnderMod}
     * 
     * @param packageName
     * @deprecated This is not needed, period.
     */
    @Deprecated
    public static void addPackage(String packageName) {
        if (Loader.instance().hasReachedState(LoaderState.INITIALIZATION)) {
            throw new RuntimeException("This method must only be called in preinit");
        }

        EnderCore.logger.info("Adding package " + packageName + " to handler search.");
        packageSet.add(packageName);
    }

    private static boolean registered = false;

    /**
     * For internal use only. Do not call. Callers will be sacked.
     * 
     * @param event
     */
    public static void register(FMLInitializationEvent event) {
        if (registered) {
            throw new IllegalStateException("I warned you!");
        }

        for (ASMData data : annotations) {
            String className = data.getClassName();
            // if not client handler, or we are on client, continue
            if (shouldLoad(data)) {
                try {
                    Class<?> c = Class.forName(className);
                    Annotation a = c.getAnnotation(Handler.class);
                    if (a != null) {
                        registerHandler(c, data, (Handler) a);
                    }
                } catch (Throwable t) {
                    EnderCore.logger
                            .error(String.format("[Handlers] %s threw an error on load, skipping...", className));
                    t.printStackTrace();
                }
            } else {
                EnderCore.logger
                        .info(String.format("[Handlers] Skipping class %s, it is not loaded on this side.", className));
            }
        }

        registered = true;
    }

    private static final Field _value = ReflectionHelper.findField(EnumHolder.class, "value");

    private static boolean shouldLoad(ASMData data) {
        EnumHolder holder = (EnumHolder) data.getAnnotationInfo().get("side");
        HandlerSide side;
        try {
            if (holder == null) {
                side = HandlerSide.AUTO;
            } else {
                side = HandlerSide.valueOf((String) _value.get(holder));
            }
            Side currentSide = FMLCommonHandler.instance().getEffectiveSide();

            if (side == null || side == HandlerSide.AUTO) {
                return !data.getClassName().contains("client") || currentSide.isClient();
            } else {
                return side.equals(currentSide);
            }
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return false;
    }

    private static void registerHandler(Class<?> c, ASMData data, Handler handler)
            throws InstantiationException, IllegalAccessException {
        EnderCore.logger.info(
                String.format(
                        "[Handlers] Registering handler %s to busses: %s",
                        c.getSimpleName(),
                        Arrays.deepToString(handler.value())));

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

    private static Object tryInit(Handler annot, Class<?> c) {
        Inst pref = annot.getInstFrom();

        // Silence all exceptions to trickle down to next if statement.
        // If all three fail, RuntimeException is thrown.
        if (pref.matches(CONSTRUCTOR)) {
            try {
                return c.newInstance();
            } catch (Exception e) {}
        }

        if (pref.matches(FIELD)) {
            try {
                Field inst = c.getDeclaredField("INSTANCE");
                inst.setAccessible(true);
                return inst.get(null);
            } catch (Exception e) {}
        }

        if (pref.matches(METHOD)) {
            try {
                Method inst = c.getDeclaredMethod("instance");
                inst.setAccessible(true);
                return inst.invoke(null);
            } catch (Exception e) {}
        }

        if (pref.matches(SCALA_OBJECT)) {
            try {
                Field inst = Class.forName(c.getName() + "$").getDeclaredField("MODULE$");
                inst.setAccessible(true);
                return inst.get(null);
            } catch (Exception e) {}
        }

        throw new RuntimeException(
                "Could not instantiate @Handler class " + c.getName()
                        + " or access INSTANCE field or instance() method.");
    }
}
