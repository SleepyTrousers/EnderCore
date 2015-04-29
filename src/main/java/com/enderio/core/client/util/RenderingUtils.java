package com.enderio.core.client.util;

import com.enderio.core.client.handlers.ClientHandler;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.model.obj.Face;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.Vertex;
import net.minecraftforge.client.model.obj.WavefrontObject;
import static org.lwjgl.opengl.GL11.*;

@UtilityClass
public class RenderingUtils
{
    public void renderWithIcon(WavefrontObject model, IIcon icon, Tessellator tes)
    {
        for (GroupObject go : model.groupObjects)
        {
            for (Face f : go.faces)
            {
                Vertex n = f.faceNormal;
                tes.setNormal(n.x, n.y, n.z);
                for (int i = 0; i < f.vertices.length; i++)
                {
                    Vertex v = f.vertices[i];
                    TextureCoordinate t = f.textureCoordinates[i];
                    tes.addVertexWithUV(v.x, v.y, v.z, icon.getInterpolatedU(t.u * 16), icon.getInterpolatedV(t.v * 16));
                }
            }
        }
    }

    /**
     * Renders an item entity in 3D
     * 
     * @param item
     *            The item to render
     * @param rotate
     *            Whether to "spin" the item like it would if it were a real dropped entity
     */
    public void render3DItem(EntityItem item, boolean rotate)
    {
        float rot = getRotation(1.0f);

        glPushMatrix();
        glDepthMask(true);
        rotate &= Minecraft.getMinecraft().gameSettings.fancyGraphics;

        if (rotate)
        {
            glRotatef(rot, 0, 1, 0);
        }

        item.hoverStart = 0.0F;
        RenderManager.instance.renderEntityWithPosYaw(item, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);

        glPopMatrix();
    }

    public float getRotation(float mult)
    {
        return ClientHandler.getTicksElapsed() * mult;
    }

    public void renderBillboardQuad(float rot, double scale)
    {
        glPushMatrix();

        glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);

        glPushMatrix();

        glRotatef(rot, 0, 0, 1);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        glColor3f(1, 1, 1);
        tessellator.setColorRGBA(255, 255, 255, 255);
        tessellator.addVertexWithUV(-scale, -scale, 0, 0, 0);
        tessellator.addVertexWithUV(-scale, scale, 0, 0, 1);
        tessellator.addVertexWithUV(scale, scale, 0, 1, 1);
        tessellator.addVertexWithUV(scale, -scale, 0, 1, 0);
        tessellator.draw();
        glPopMatrix();
        glPopMatrix();
    }

    public void rotateToPlayer()
    {
        glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
        glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
    }
}
