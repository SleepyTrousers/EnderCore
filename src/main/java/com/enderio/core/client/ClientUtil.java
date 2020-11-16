package com.enderio.core.client;

import java.lang.reflect.Field;

import com.enderio.core.common.util.Log;

import net.minecraft.client.particle.Particle;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@OnlyIn(Dist.CLIENT)
public class ClientUtil {
  private static final Field X;
  private static final Field Y;
  private static final Field Z;

  static {
    Field x = null;
    Field y = null;
    Field z = null;
    try {
      x = ObfuscationReflectionHelper.findField(Particle.class, "motionX");
      y = ObfuscationReflectionHelper.findField(Particle.class, "motionY");
      z = ObfuscationReflectionHelper.findField(Particle.class, "motionZ");
    } catch (Exception e) {
      Log.error("ClientUtil: Could not find motion fields for class Particle: " + e.getMessage());
    } finally {
      X = x;
      Y = y;
      Z = z;
    }
  }

  public static void setParticleVelocity(Particle p, double x, double y, double z) {
    if (p == null) {
      return;
    }

    try {
      X.set(p, x);
      Y.set(p, y);
      Z.set(p, z);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void setParticleVelocityY(Particle p, double y) {
    if (p == null) {
      return;
    }

    try {
      Y.set(p, y);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static double getParticleVelocityY(Particle p) {
    if (p == null) {
      return 0;
    }

    try {
      Object val = Y.get(p);
      return ((Double) val).doubleValue();
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }
}
