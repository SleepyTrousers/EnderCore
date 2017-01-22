package com.enderio.core.client;

import java.lang.reflect.Field;

import com.enderio.core.common.util.Log;

import net.minecraft.client.particle.Particle;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientUtil {

  private static final Field X;
  private static final Field Y;
  private static final Field Z;

  static {
    Field x = null;
    Field y = null;
    Field z = null;
    try {
      x = ReflectionHelper.findField(Particle.class, "motionX", "field_187129_i");
      y = ReflectionHelper.findField(Particle.class, "motionY", "field_187130_j");
      z = ReflectionHelper.findField(Particle.class, "motionZ", "field_187131_k");
    } catch (Exception e) {
      Log.error("ClientUtil: Could not find motion fields for class Particle: " + e.getMessage());
    } finally {
      X = x;
      Y = y;
      Z = z;
    }
  }

  public static void setParticleVelocity(Particle p, double x, double y, double z) {
    if (p == null || X == null || Y == null || Z == null) {
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
    if (p == null || X == null || Y == null || Z == null) {
      return;
    }
    try {
      Y.set(p, y);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static double getParticleVelocityY(Particle p) {
    if (p == null || X == null || Y == null || Z == null) {
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
