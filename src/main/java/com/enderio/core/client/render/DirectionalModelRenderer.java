package com.enderio.core.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import org.lwjgl.opengl.GL11;

import com.enderio.core.api.client.model.IEnderModel;
import com.enderio.core.common.util.ItemUtil;

/**
 * Renders a model with directional placement
 * 
 * @author Garrett Spicer-Davis
 */
public class DirectionalModelRenderer<T extends TileEntity> extends TileEntitySpecialRenderer implements IItemRenderer {

    private IModelCustom model;
    private ResourceLocation texture;
    private IEnderModel modelSMT;

    public DirectionalModelRenderer(ResourceLocation model, ResourceLocation texture) {
        this.model = AdvancedModelLoader.loadModel(model);
        this.texture = texture;
    }

    public DirectionalModelRenderer(IEnderModel model, ResourceLocation texture) {
        this.modelSMT = model;
        this.texture = texture;
    }

    private void renderDirectionalTileEntityAt(T tile, double x, double y, double z, int metaOverride) {
        int meta = getMetadata(tile, metaOverride);
        setup(x, y, z, metaOverride);
        rotate(getRotation(tile, metaOverride));
        renderModel(tile, meta);
    }

    protected final int getMetadata(T tile, int metaOverride) {
        return metaOverride >= 0 ? metaOverride : tile.getBlockMetadata();
    }

    protected void setup(double x, double y, double z, int metaOverride) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5f, (float) y - (metaOverride >= 0 ? 0.1f : 0), (float) z + 0.5f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
    }

    protected int getRotation(T tile, int metaOverride) {
        return getMetadata(tile, metaOverride);
    }

    protected void rotate(int rotation) {
        switch (rotation) {
            case 1:
                GL11.glRotatef(180f, 0, 0, 1);
                GL11.glTranslatef(0, -1f, 0);
                break;
            case 2:
                GL11.glRotatef(90f, 1f, 0, 0);
                GL11.glTranslatef(0, -0.5f, -0.5f);
                break;
            case 3:
                GL11.glRotatef(90f, -1f, 0, 0);
                GL11.glTranslatef(0, -0.5f, 0.5f);
                break;
            case 4:
                GL11.glRotatef(90f, 0, 0, -1f);
                GL11.glTranslatef(-0.5f, -0.5f, 0);
                break;
            case 5:
                GL11.glRotatef(90f, 0, 0, 1f);
                GL11.glTranslatef(0.5f, -0.5f, 0);
                break;
        }
    }

    protected void renderModel(T tile, int meta) {
        if (model != null) {
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            model.renderAll();
        } else {
            GL11.glTranslated(0, -0.5, 0);
            modelSMT.render(0.0625f);
        }

        GL11.glPopMatrix();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float yaw) {
        renderDirectionalTileEntityAt((T) tile, x, y, z, -1);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return helper == ItemRendererHelper.INVENTORY_BLOCK || helper == ItemRendererHelper.ENTITY_BOBBING
                || helper == ItemRendererHelper.ENTITY_ROTATION;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();

        switch (type) {
            case ENTITY:
                GL11.glTranslatef(-0.4f, 0.1f, -0.4f);
                GL11.glScalef(0.75f, 0.75f, 0.75f);
                break;
            case EQUIPPED:
                GL11.glScalef(0.75f, 0.75f, 0.75f);
                GL11.glTranslatef(-0.1f, 0.2f, 0.5f);
                GL11.glRotatef(-40, 1, 0, 0);
                GL11.glRotatef(45, 0, 1, 0);
                GL11.glRotatef(20, 0, 0, 1);
                break;
            case EQUIPPED_FIRST_PERSON:
                GL11.glScalef(0.75f, 0.75f, 0.75f);
                break;
            case FIRST_PERSON_MAP:
                break;
            case INVENTORY:
                break;
        }

        renderDirectionalTileEntityAt(null, 0, 0, 0, ItemUtil.getNBTTag(item).getInteger("storedMetaData"));

        GL11.glPopMatrix();
    }
}
