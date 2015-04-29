package com.enderio.core.common.util;

import lombok.experimental.UtilityClass;

import org.lwjgl.opengl.GL11;

@UtilityClass
public class EnderColorUtils
{
    /**
     * Turns an int into a glColor4f function
     * 
     * @author Buildcraft team
     */
    public void setGLColorFromInt(int color)
    {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glColor4f(red, green, blue, 1.0F);
    }

    public int toHex(int r, int g, int b)
    {
        int hex = 0;
        hex = hex | ((r) << 16);
        hex = hex | ((g) << 8);
        hex = hex | ((b));
        return hex;
    }
}
