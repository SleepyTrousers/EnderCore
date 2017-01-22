package com.enderio.core.common.compat;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Generated;

import org.apache.commons.lang3.ArrayUtils;

import com.enderio.core.EnderCore;
import com.enderio.core.common.util.RegisterTime;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLStateEvent;

public enum CompatRegistry {
  INSTANCE;

  private static class Registration {
    final String[] modids;
    final RegisterTime[] times;

    Registration(RegisterTime time, String... modids) {
      this.modids = modids;
      this.times = new RegisterTime[] { time };
    }

    Registration(RegisterTime[] times, String... modids) {
      this.modids = modids;
      this.times = times;
    }

    public String[] getModids() {
      return this.modids;
    }

    public RegisterTime[] getTimes() {
      return this.times;
    }

    @Override
    @Generated("lombok")
    public boolean equals(final java.lang.Object o) {
      if (o == this)
        return true;
      if (!(o instanceof CompatRegistry.Registration))
        return false;
      final Registration other = (Registration) o;
      if (!java.util.Arrays.deepEquals(this.getModids(), other.getModids()))
        return false;
      if (!java.util.Arrays.deepEquals(this.getTimes(), other.getTimes()))
        return false;
      return true;
    }

    @Override
    @Generated("lombok")
    public int hashCode() {
      final int PRIME = 59;
      int result = 1;
      result = result * PRIME + java.util.Arrays.deepHashCode(this.getModids());
      result = result * PRIME + java.util.Arrays.deepHashCode(this.getTimes());
      return result;
    }

    @Override
    @Generated("lombok")
    public java.lang.String toString() {
      return "CompatRegistry.Registration(modids=" + java.util.Arrays.deepToString(this.getModids()) + ", times="
          + java.util.Arrays.deepToString(this.getTimes()) + ")";
    }
  }

  private Map<Registration, String> compatMap = new HashMap<Registration, String>();

  private RegisterTime state = null;

  private CompatRegistry() {
  }

  public RegisterTime getState() {
    return state;
  }

  public void registerCompat(RegisterTime time, String clazz, String... modids) {
    compatMap.put(new Registration(time, modids), clazz);
  }

  public void registerCompat(RegisterTime[] times, String clazz, String... modids) {
    compatMap.put(new Registration(times, modids), clazz);
  }

  public void handle(FMLStateEvent event) {
    RegisterTime time = RegisterTime.timeFor(event);
    state = time;
    for (Registration r : compatMap.keySet()) {
      if (ArrayUtils.contains(r.times, time) && allModsLoaded(r.modids)) {
        doLoad(compatMap.get(r));
      }
    }
  }

  private static boolean allModsLoaded(String[] modids) {
    for (String s : modids) {
      if (!Loader.isModLoaded(s)) {
        return false;
      }
    }
    return true;
  }

  public void forceLoad(String clazz) {
    Iterator<Registration> iter = compatMap.keySet().iterator();
    while (iter.hasNext()) {
      Registration r = iter.next();
      String s = compatMap.get(r);
      if (s.equals(clazz)) {
        doLoad(s);
      }
    }
  }

  private static void doLoad(String clazz) {
    try {
      EnderCore.logger.info("[Compat] Loading compatability class " + clazz);
      Class<?> compat = Class.forName(clazz);
      compat.getDeclaredMethod(ICompat.METHOD_NAME).invoke(null);
    } catch (NoSuchMethodException e) {
      EnderCore.logger.error("[Compat] ICompatability class {} did not contain static method {}!", clazz, ICompat.METHOD_NAME);
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      throw new RuntimeException("Error in compatability class " + clazz, e.getTargetException());
    } catch (Throwable e) {
      EnderCore.logger.error("[Compat] An unknown error was thrown loading class {}.", clazz);
      e.printStackTrace();
    }
  }
}
