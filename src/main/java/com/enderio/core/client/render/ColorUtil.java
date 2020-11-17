package com.enderio.core.client.render;

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vector4f;
import com.mojang.blaze3d.platform.GlStateManager;


public final class ColorUtil {

  public static @Nonnull Vector4f toFloat(@Nonnull Color color) {
    float[] rgba = color.getComponents(null);
    return new Vector4f(rgba[0], rgba[1], rgba[2], rgba[3]);
  }

  public static @Nonnull Vector3f toFloat(int rgb) {
    int r = rgb >> 16 & 255;
    int g = rgb >> 8 & 255;
    int b = rgb & 255;
    return new Vector3f(r / 255F, g / 255F, b / 255F);
  }

  public static @Nonnull Vector4f toFloat4(int rgb) {
    int r = rgb >> 16 & 255;
    int g = rgb >> 8 & 255;
    int b = rgb & 255;
    return new Vector4f(r / 255F, g / 255F, b / 255F, 1);
  }

  public static int getRGB(@Nullable Color color) {
    // Note: Constants in java.awt.Color are not @Nonnull-annotated
    return color == null ? 0 : getRGB(color.getRed(), color.getGreen(), color.getBlue());
  }

  public static int getRGBA(@Nullable Color color) {
    return color == null ? 0 : getRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
  }

  public static int getARGB(@Nullable Color color) {
    return color == null ? 0 : getRGBA(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
  }

  public static int getRGB(@Nonnull Vector3f rgb) {
    return getRGB(rgb.x, rgb.y, rgb.z);
  }

  public static int getRGB(float r, float g, float b) {
    return getRGB((int) (r * 255), (int) (g * 255), (int) (b * 255));
  }

  public static int getRGBA(@Nonnull Vector4f col) {
    return getRGBA(col.x, col.y, col.z, col.w);
  }

  public static int getRGBA(float r, float g, float b, float a) {
    return getRGBA((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
  }

  public static int getARGB(float r, float g, float b, float a) {
    return getARGB((int) (a * 255), (int) (r * 255), (int) (g * 255), (int) (b * 255));
  }

  public static int getRGB(int r, int g, int b) {
    return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
  }

  public static int getARGB(int r, int g, int b, int a) {
    return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
  }

  public static int getRGBA(int r, int g, int b, int a) {
    return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
  }

  /**
   * Turns an int into a glColor4f function
   *
   * @author Buildcraft team
   */
  public static void setGLColorFromInt(int color) {
    float red = (color >> 16 & 255) / 255.0F;
    float green = (color >> 8 & 255) / 255.0F;
    float blue = (color & 255) / 255.0F;
    GlStateManager.color4f(red, green, blue, 1.0F);
  }

  public static int toHex(int r, int g, int b) {
    int hex = 0;
    hex = hex | ((r) << 16);
    hex = hex | ((g) << 8);
    hex = hex | ((b));
    return hex;
  }

  private ColorUtil() {
  }

}
