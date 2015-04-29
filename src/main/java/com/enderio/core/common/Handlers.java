package com.enderio.core.common;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import net.minecraftforge.common.MinecraftForge;

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.IEnderMod;
import com.enderio.core.EnderCore;
import com.enderio.core.common.Handlers.Handler.HandlerType;
import com.enderio.core.common.Handlers.Handler.Inst;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import static com.enderio.core.common.Handlers.Handler.Inst.*;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.ModContainer;

@UtilityClass
public class Handlers
{
    /**
     * To be put on classes that are Forge/FML event handlers. If you are using this from another mod, be sure to implement {@link IEnderMod} on your
     * {@code @Mod} class, or call {@code Handlers.addPackage("your.base.package")} so that this class can search your classes
     * <p>
     * Class must have either:<br>
     * A public no args constructor (or lombok {@link NoArgsConstructor}) <b>OR</b><br>
     * A static singleton object with field name {@code INSTANCE} (public or private). <b>OR</b><br>
     * A static method with name <code>instance()</code> (public or private)
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Handler
    {
        public enum HandlerType
        {
            /**
             * Represents the {@link MinecraftForge#EVENT_BUS}
             */
            FORGE,

            /**
             * Represents the {@link FMLCommonHandler#instance()#bus()}
             */
            FML
        }

        public enum Inst
        {
            /**
             * The default, will try all three methods, in the order {@link Inst#CONSTRUCTOR}, {@link Inst#FIELD}, {@link Inst#METHOD}
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
            METHOD;

            boolean matches(Inst other)
            {
                return this == AUTO || other == AUTO || other == this;
            }
        }

        /**
         * Array of buses to register this handler to. Leave blank for all.
         */
        HandlerType[] value() default { HandlerType.FORGE, HandlerType.FML };

        /**
         * The method of getting your handler's instance. Defaults to {@link Inst#AUTO}
         */
        Inst getInstFrom() default AUTO;
    }

    private static final Set<String> packageSet = new HashSet<String>();

    public void findPackages()
    {
        for (ModContainer mod : Loader.instance().getActiveModList())
        {
            if (mod.getMod() instanceof IEnderMod)
            {
                addPackage(getEnclosingPackage(mod.getMod()));
            }
        }
    }

    private String getEnclosingPackage(Object obj)
    {
        Class<?> modClass = obj.getClass();

        while (modClass.getComponentType() != null)
        {
            modClass = modClass.getComponentType();
        }

        while (modClass.getEnclosingClass() != null)
        {
            modClass = modClass.getEnclosingClass();
        }

        String name = modClass.getName();
        int lastDot = name.lastIndexOf('.');

        return lastDot == -1 ? name : name.substring(0, lastDot);
    }

    /**
     * Registers a top level package to be searched for {@link Handler} classes. Not needed if your {@code @Mod} class implements {@link IEnderMod}
     * 
     * @param packageName
     */
    public void addPackage(String packageName)
    {
        if (Loader.instance().hasReachedState(LoaderState.INITIALIZATION))
        {
            throw new RuntimeException("This method must only be called in preinit");
        }

        EnderCore.logger.info("Adding package " + packageName + " to handler search.");
        packageSet.add(packageName);
    }

    private boolean registered = false;

    /**
     * For internal use only. Do not call. Callers will be sacked.
     */
    public void register()
    {
        if (registered)
        {
            throw new IllegalStateException("I warned you!");
        }

        ClassPath classpath;

        try
        {
            classpath = ClassPath.from(EnderCore.class.getClassLoader());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        for (String packageName : packageSet)
        {
            Set<ClassInfo> classes = classpath.getTopLevelClassesRecursive(packageName);

            for (ClassInfo info : classes)
            {
                // if not client handler, or we are on client, continue
                if (!info.getPackageName().contains("client") || FMLCommonHandler.instance().getEffectiveSide().isClient())
                {
                    try
                    {
                        Class<?> c = info.load();

                        Annotation a = c.getAnnotation(Handler.class);
                        if (a != null)
                        {
                            registerHandler(c, (Handler) a);
                        }
                    }
                    catch (Throwable t)
                    {
                        EnderCore.logger.error(String.format("[Handlers] %s threw an error on load, skipping...", info.getName()));
                        t.printStackTrace();
                    }
                }
                else
                {
                    EnderCore.logger.info(String.format("[Handlers] Skipping client class %s, we are on a dedicated server", info.getSimpleName()));
                }
            }
        }

        registered = true;
    }

    private void registerHandler(Class<?> c, Handler handler) throws InstantiationException, IllegalAccessException
    {
        EnderCore.logger.info(String.format("[Handlers] Registering handler %s to busses: %s", c.getSimpleName(), Arrays.deepToString(handler.value())));

        HandlerType[] types = handler.value();
        Object inst = tryInit(handler, c);

        if (ArrayUtils.contains(types, HandlerType.FORGE))
            MinecraftForge.EVENT_BUS.register(inst);

        if (ArrayUtils.contains(types, HandlerType.FML))
            FMLCommonHandler.instance().bus().register(inst);
    }

    private Object tryInit(Handler annot, Class<?> c)
    {
        Inst pref = annot.getInstFrom();

        // Silence all exceptions to trickle down to next if statement.
        // If all three fail, RuntimeException is thrown.
        if (pref.matches(CONSTRUCTOR))
        {
            try
            {
                return c.newInstance();
            }
            catch (Exception e)
            {}
        }

        if (pref.matches(FIELD))
        {
            try
            {
                Field inst = c.getDeclaredField("INSTANCE");
                inst.setAccessible(true);
                return inst.get(null);
            }
            catch (Exception e)
            {}
        }

        if (pref.matches(METHOD))
        {
            try
            {
                Method inst = c.getDeclaredMethod("instance");
                inst.setAccessible(true);
                return inst.invoke(null);
            }
            catch (Exception e)
            {}
        }

        throw new RuntimeException("Could not instantiate @Handler class " + c.getName() + " or access INSTANCE field or instance() method.");
    }
}
