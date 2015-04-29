package com.enderio.core.common.compat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.EnderCore;
import com.enderio.core.common.util.RegisterTime;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLStateEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompatabilityRegistry
{
    @Value
    private static class Registration
    {
        String[] modids;
        RegisterTime[] times;

        private Registration(RegisterTime time, String... modids)
        {
            this.modids = modids;
            this.times = new RegisterTime[] { time };
        }

        private Registration(RegisterTime[] times, String... modids)
        {
            this.modids = modids;
            this.times = times;
        }
    }

    public static final CompatabilityRegistry INSTANCE = new CompatabilityRegistry();

    private Map<Registration, String> compatMap = new HashMap<Registration, String>();

    @Getter
    private RegisterTime state = null;

    public void registerCompat(RegisterTime time, String clazz, String... modids)
    {
        compatMap.put(new Registration(time, modids), clazz);
    }

    public void registerCompat(RegisterTime[] times, String clazz, String... modids)
    {
        compatMap.put(new Registration(times, modids), clazz);
    }

    public void handle(FMLStateEvent event)
    {
        RegisterTime time = RegisterTime.timeFor(event);
        state = time;
        for (Registration r : compatMap.keySet())
        {
            if (ArrayUtils.contains(r.times, time) && allModsLoaded(r.modids))
            {
                doLoad(compatMap.get(r));
            }
        }
    }

    private boolean allModsLoaded(String[] modids)
    {
        for (String s : modids)
        {
            if (!Loader.isModLoaded(s))
            {
                return false;
            }
        }
        return true;
    }

    public void forceLoad(String clazz)
    {
        Iterator<Registration> iter = compatMap.keySet().iterator();
        while (iter.hasNext())
        {
            Registration r = iter.next();
            String s = compatMap.get(r);
            if (s.equals(clazz))
            {
                doLoad(s);
            }
        }
    }

    private void doLoad(String clazz)
    {
        try
        {
            EnderCore.logger.info("[Compat] Loading compatability class " + clazz);
            Class<?> compat = Class.forName(clazz);
            compat.getDeclaredMethod(ICompatability.METHOD_NAME).invoke(null);
        }
        catch (NoSuchMethodException e)
        {
            EnderCore.logger.error("[Compat] ICompatability class {} did not contain static method {}!", clazz, ICompatability.METHOD_NAME);
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException("Error in compatability class " + clazz, e.getTargetException());
        }
        catch (Exception e)
        {
            EnderCore.logger.error("[Compat] An unknown error was thrown loading class {}.", clazz);
            e.printStackTrace();
        }
    }
}
