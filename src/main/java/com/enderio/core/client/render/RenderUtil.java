package com.enderio.core.client.render;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.handlers.ClientHandler;
import com.enderio.core.common.util.Log;
import com.enderio.core.common.vecmath.Matrix4d;
import com.enderio.core.common.vecmath.VecmathUtil;
import com.enderio.core.common.vecmath.Vector2f;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vector4d;
import com.enderio.core.common.vecmath.Vector4f;
import com.enderio.core.common.vecmath.Vertex;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;
import static org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS;
import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_ZERO;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopAttrib;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushAttrib;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glShadeModel;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3i;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad.Builder;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class RenderUtil {

  public static final Vector4f DEFAULT_TEXT_SHADOW_COL = new Vector4f(0.33f, 0.33f, 0.33f, 0.33f);

  public static final Vector4f DEFAULT_TXT_COL = new Vector4f(1, 1, 1, 1);

  public static final Vector4f DEFAULT_TEXT_BG_COL = new Vector4f(0.275f, 0.08f, 0.4f, 0.75f);

  public static final Vector3d UP_V = new Vector3d(0, 1, 0);

  public static final Vector3d ZERO_V = new Vector3d(0, 0, 0);

  private static final FloatBuffer MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);

  public static final ResourceLocation BLOCK_TEX = TextureMap.locationBlocksTexture;

  public static final ResourceLocation GLINT_TEX = new ResourceLocation("textures/misc/enchanted_item_glint.png");

  public static int BRIGHTNESS_MAX = 15 << 20 | 15 << 4;

  public static void loadMatrix(Matrix4d mat) {
    MATRIX_BUFFER.rewind();
    MATRIX_BUFFER.put((float) mat.m00);
    MATRIX_BUFFER.put((float) mat.m01);
    MATRIX_BUFFER.put((float) mat.m02);
    MATRIX_BUFFER.put((float) mat.m03);
    MATRIX_BUFFER.put((float) mat.m10);
    MATRIX_BUFFER.put((float) mat.m11);
    MATRIX_BUFFER.put((float) mat.m12);
    MATRIX_BUFFER.put((float) mat.m13);
    MATRIX_BUFFER.put((float) mat.m20);
    MATRIX_BUFFER.put((float) mat.m21);
    MATRIX_BUFFER.put((float) mat.m22);
    MATRIX_BUFFER.put((float) mat.m23);
    MATRIX_BUFFER.put((float) mat.m30);
    MATRIX_BUFFER.put((float) mat.m31);
    MATRIX_BUFFER.put((float) mat.m32);
    MATRIX_BUFFER.put((float) mat.m33);
    MATRIX_BUFFER.rewind();
    GL11.glLoadMatrix(MATRIX_BUFFER);
  }

  private static Field timerField = initTimer();

  /**
   * Non-thread-safe holder for the current render pass. You must update this in
   * your block's canRenderInPass method for it to work properly!
   * 
   * DEPRECATED - Should use {@link ForgeHooksClient#getWorldRenderPass()}
   */
  @Deprecated
  public static volatile int theRenderPass = 0;

  private static Field initTimer() {
    Field f = null;
    try {
      f = ReflectionHelper.findField(Minecraft.class, "field_71428_T", "timer", "Q");
      f.setAccessible(true);
    } catch (Exception e) {
      Log.error("Failed to initialize timer reflection for IO config.");
      e.printStackTrace();
    }
    return f;
  }

  @Nullable
  public static Timer getTimer() {
    if (timerField == null) {
      return null;
    }
    try {
      return (Timer) timerField.get(Minecraft.getMinecraft());
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static TextureManager engine() {
    return Minecraft.getMinecraft().renderEngine;
  }

  public static void bindBlockTexture() {
    engine().bindTexture(BLOCK_TEX);
  }

  public static void bindGlintTexture() {
    engine().bindTexture(BLOCK_TEX);
  }

  public static void bindTexture(String string) {
    engine().bindTexture(new ResourceLocation(string));
  }

  public static void bindTexture(ResourceLocation tex) {
    engine().bindTexture(tex);
  }

  public static FontRenderer fontRenderer() {
    return Minecraft.getMinecraft().fontRendererObj;
  }

  public static float[] getDefaultPerSideBrightness() {
    float[] brightnessPerSide = new float[6];
    for (EnumFacing dir : EnumFacing.VALUES) {
      brightnessPerSide[dir.ordinal()] = RenderUtil.getColorMultiplierForFace(dir);
    }
    return brightnessPerSide;
  }

  public static float claculateTotalBrightnessForLocation(World worldObj, BlockPos pos) {
    return claculateTotalBrightnessForLocation(worldObj, pos.getX(), pos.getY(), pos.getZ());
  }

  public static float claculateTotalBrightnessForLocation(World worldObj, int xCoord, int yCoord, int zCoord) {
    int i = worldObj.getLightFromNeighborsFor(EnumSkyBlock.SKY, new BlockPos(xCoord, yCoord, zCoord));
    int j = i % 65536;
    int k = i / 65536;

    // 0.2 - 1
    float sunBrightness = worldObj.getSunBrightness(1);
    float percentRecievedFromSun = k / 255f;

    // Highest value recieved from a light
    float fromLights = j / 255f;

    // 0 - 1 for sun only, 0 - 0.6 for light only
    //float recievedPercent = worldObj.getLightBrightness(new BlockPos(xCoord, yCoord, zCoord));
    float highestValue = Math.max(fromLights, percentRecievedFromSun * sunBrightness);
    return Math.max(0.2f, highestValue);
  }

  public static float getColorMultiplierForFace(EnumFacing face) {
    if (face == EnumFacing.UP) {
      return 1;
    }
    if (face == EnumFacing.DOWN) {
      return 0.5f;
    }
    if (face.getFrontOffsetX() != 0) {
      return 0.6f;
    }
    return 0.8f; // z
  }

  public static void renderQuad2D(double x, double y, double z, double width, double height, int colorRGB) {

    GL11.glDisable(GL11.GL_TEXTURE_2D);
    Vector3f col = ColorUtil.toFloat(colorRGB);
    GL11.glColor3f(col.x, col.y, col.z);

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer tes = tessellator.getWorldRenderer();
    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    tes.pos(x, y + height, z).endVertex();
    tes.pos(x + width, y + height, z).endVertex();
    tes.pos(x + width, y, z).endVertex();
    tes.pos(x, y, z).endVertex();

    tessellator.draw();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
  }

  public static void renderQuad2D(double x, double y, double z, double width, double height, Vector4f colorRGBA) {
    GL11.glColor4f(colorRGBA.x, colorRGBA.y, colorRGBA.z, colorRGBA.w);

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer tes = tessellator.getWorldRenderer();
    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    tes.pos(x, y + height, z).endVertex();
    tes.pos(x + width, y + height, z).endVertex();
    tes.pos(x + width, y, z).endVertex();
    tes.pos(x, y, z).endVertex();
    tessellator.draw();
    GL11.glEnable(GL11.GL_TEXTURE_2D);
  }

  public static Matrix4d createBillboardMatrix(TileEntity te, EntityLivingBase entityPlayer) {
    BlockPos p = te.getPos();
    return createBillboardMatrix(new Vector3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5), entityPlayer);
  }

  public static Matrix4d createBillboardMatrix(Vector3d lookAt, EntityLivingBase entityPlayer) {
    Vector3d playerEye = new Vector3d(entityPlayer.posX, entityPlayer.posY + 1.62 - entityPlayer.getYOffset(), entityPlayer.posZ);
    Vector3d blockOrigin = new Vector3d(lookAt.x, lookAt.y, lookAt.z);
    Matrix4d lookMat = VecmathUtil.createMatrixAsLookAt(blockOrigin, playerEye, RenderUtil.UP_V);
    lookMat.setTranslation(new Vector3d());
    lookMat.invert();
    return lookMat;
  }

  public static void renderBillboard(Matrix4d lookMat, float minU, float maxU, float minV, float maxV, double size, int brightness) {
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer tes = tessellator.getWorldRenderer();
    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

    double s = size / 2;
    Vector3d v = new Vector3d();
    v.set(-s, s, 0);
    lookMat.transform(v);
    tes.pos(v.x, v.y, v.z).tex(minU, maxV).endVertex();
    v.set(s, s, 0);
    lookMat.transform(v);
    tes.pos(v.x, v.y, v.z).tex(maxU, maxV).endVertex();
    v.set(s, -s, 0);
    lookMat.transform(v);
    tes.pos(v.x, v.y, v.z).tex(maxU, minV).endVertex();
    v.set(-s, -s, 0);
    lookMat.transform(v);
    tes.pos(v.x, v.y, v.z).tex(minU, minV).endVertex();

    tessellator.draw();
  }

  /**
   * @return The edge directions for a face, in the order left, bottom, right,
   *         top.
   */
  public static List<EnumFacing> getEdgesForFace(EnumFacing face) {
    List<EnumFacing> result = new ArrayList<EnumFacing>(4);
    if (face.getFrontOffsetY() != 0) {
      result.add(NORTH);
      result.add(EAST);
      result.add(SOUTH);
      result.add(WEST);

    } else if (face.getFrontOffsetX() != 0) {
      result.add(DOWN);
      result.add(SOUTH);
      result.add(UP);
      result.add(NORTH);
    } else {
      result.add(DOWN);
      result.add(WEST);
      result.add(UP);
      result.add(EAST);
    }
    return result;
  }

  public static void addVerticesToTessellator(List<Vertex> vertices, VertexFormat format, boolean doBegin) {
    addVerticesToTessellator(vertices, null, format, doBegin);
  }

  public static void addVerticesToTessellator(List<Vertex> vertices, VertexTranslation xForm, VertexFormat format, boolean doBegin) {
    if (vertices == null || vertices.isEmpty()) {
      return;
    }

    if(xForm != null) {
      List<Vertex> newV = new ArrayList<Vertex>(vertices.size());
      for(Vertex v : vertices) {
        Vertex xv = new Vertex(v);
        xForm.apply(xv);
        newV.add(xv);
      }
      vertices = newV;
    }
    
    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer tes = tessellator.getWorldRenderer();
    if (doBegin) {
      tes.begin(GL11.GL_QUADS, format);
    }

    for (Vertex v : vertices) {      
      for(VertexFormatElement el : format.getElements()) {
        switch(el.getUsage()) {       
        case COLOR:
          if(el.getType() == EnumType.FLOAT) {
            tes.color(v.r(), v.g(), v.b(), v.a());
          }
          break;        
        case NORMAL:
          tes.normal(v.nx(), v.ny(), v.nz());
          break;        
        case POSITION:
          tes.pos(v.x(), v.y(), v.z());
          break;
        case UV:
          if(el.getType() == EnumType.FLOAT && v.uv != null) {
            tes.tex(v.u(), v.v());
          }
          break;
        case GENERIC:
          break;
        case PADDING:
          break;
        default:
          break;
        
        }
      }      
      tes.endVertex();
    }
  }

  public static void getUvForCorner(Vector2f uv, Vector3d corner, int x, int y, int z, EnumFacing face, TextureAtlasSprite icon) {
    if (icon == null) {
      return;
    }

    Vector3d p = new Vector3d(corner);
    p.x -= x;
    p.y -= y;
    p.z -= z;

    float uWidth = 1;
    float vWidth = 1;
    if (icon != null) {
      uWidth = icon.getMaxU() - icon.getMinU();
      vWidth = icon.getMaxV() - icon.getMinV();
    }

    uv.x = (float) VecmathUtil.distanceFromPointToPlane(getUPlaneForFace(face), p);
    uv.y = (float) VecmathUtil.distanceFromPointToPlane(getVPlaneForFace(face), p);

    if (icon != null) {
      uv.x = icon.getMinU() + (uv.x * uWidth);
      uv.y = icon.getMinV() + (uv.y * vWidth);
    }

  }

  public static Vector4d getVPlaneForFace(EnumFacing face) {
    switch (face) {
    case DOWN:
    case UP:
      return new Vector4d(0, 0, 1, 0);
    case EAST:
    case WEST:
    case NORTH:
    case SOUTH:
      return new Vector4d(0, -1, 0, 1);
    default:
      break;
    }
    return null;
  }

  public static Vector4d getUPlaneForFace(EnumFacing face) {
    switch (face) {
    case DOWN:
    case UP:
      return new Vector4d(1, 0, 0, 0);
    case EAST:
      return new Vector4d(0, 0, -1, 1);
    case WEST:
      return new Vector4d(0, 0, 1, 0);
    case NORTH:
      return new Vector4d(-1, 0, 0, 1);
    case SOUTH:
      return new Vector4d(1, 0, 0, 0);
    default:
      break;
    }
    return null;
  }

  public static EnumFacing getVDirForFace(EnumFacing face) {
    switch (face) {
    case DOWN:
    case UP:
      return SOUTH;
    case EAST:
    case WEST:
    case NORTH:
    case SOUTH:
      return EnumFacing.UP;
    default:
      break;
    }
    return null;
  }

  public static EnumFacing getUDirForFace(EnumFacing face) {
    switch (face) {
    case DOWN:
    case UP:
      return EnumFacing.EAST;
    case EAST:
      return NORTH;
    case WEST:
      return SOUTH;
    case NORTH:
      return WEST;
    case SOUTH:
      return EnumFacing.EAST;
    default:
      break;
    }
    return null;
  }

  public static TextureAtlasSprite getStillTexture(FluidStack fluid) {
    if (fluid == null || fluid.getFluid() == null) {
      return null;
    }
    return getStillTexture(fluid.getFluid());
  }

  public static TextureAtlasSprite getStillTexture(Fluid fluid) {
    ResourceLocation iconKey = fluid.getStill();
    if (iconKey == null) {
      return null;
    }
    return Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(iconKey.toString());
  }

  public static void renderGuiTank(FluidTank tank, double x, double y, double zLevel, double width, double height) {
    renderGuiTank(tank.getFluid(), tank.getCapacity(), tank.getFluidAmount(), x, y, zLevel, width, height);
  }

  public static void renderGuiTank(FluidStack fluid, int capacity, int amount, double x, double y, double zLevel, double width, double height) {
    if (fluid == null || fluid.getFluid() == null || fluid.amount <= 0) {
      return;
    }

    TextureAtlasSprite icon = getStillTexture(fluid);
    if (icon == null) {      
      return;
    }

    int renderAmount = (int) Math.max(Math.min(height, amount * height / capacity), 1);
    int posY = (int) (y + height - renderAmount);

    RenderUtil.bindBlockTexture();
    int color = fluid.getFluid().getColor(fluid);
    GL11.glColor3ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF));

    GL11.glEnable(GL11.GL_BLEND);
    for (int i = 0; i < width; i += 16) {
      for (int j = 0; j < renderAmount; j += 16) {
        int drawWidth = (int) Math.min(width - i, 16);
        int drawHeight = Math.min(renderAmount - j, 16);

        int drawX = (int) (x + i);
        int drawY = posY + j;

        double minU = icon.getMinU();
        double maxU = icon.getMaxU();
        double minV = icon.getMinV();
        double maxV = icon.getMaxV();

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer tes = tessellator.getWorldRenderer();
        tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        tes.pos(drawX, drawY + drawHeight, 0).tex(minU, minV + (maxV - minV) * drawHeight / 16F).endVertex();
        tes.pos(drawX + drawWidth, drawY + drawHeight, 0).tex(minU + (maxU - minU) * drawWidth / 16F, minV + (maxV - minV) * drawHeight / 16F).endVertex();
        tes.pos(drawX + drawWidth, drawY, 0).tex(minU + (maxU - minU) * drawWidth / 16F, minV).endVertex();
        tes.pos(drawX, drawY, 0).tex(minU, minV).endVertex();
        tessellator.draw();
      }
    }
    GL11.glDisable(GL11.GL_BLEND);
  }

  public static void drawBillboardedText(Vector3f pos, String text, float size) {
    drawBillboardedText(pos, text, size, DEFAULT_TXT_COL, true, DEFAULT_TEXT_SHADOW_COL, true, DEFAULT_TEXT_BG_COL);
  }

  public static void drawBillboardedText(Vector3f pos, String text, float size, Vector4f bgCol) {
    drawBillboardedText(pos, text, size, DEFAULT_TXT_COL, true, DEFAULT_TEXT_SHADOW_COL, true, bgCol);
  }

  public static void drawBillboardedText(Vector3f pos, String text, float size, Vector4f txtCol, boolean drawShadow, Vector4f shadowCol, boolean drawBackground,
      Vector4f bgCol) {

    GL11.glPushMatrix();
    GL11.glTranslatef(pos.x, pos.y, pos.z);
    GL11.glRotatef(180, 1, 0, 0);

    Minecraft mc = Minecraft.getMinecraft();
    FontRenderer fnt = mc.fontRendererObj;
    float scale = size / fnt.FONT_HEIGHT;
    GL11.glScalef(scale, scale, scale);
    GL11.glRotatef(mc.getRenderManager().playerViewY + 180, 0.0F, 1.0F, 0.0F);
    GL11.glRotatef(-mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

    GL11.glTranslatef(-fnt.getStringWidth(text) / 2, 0, 0);
    if (drawBackground) {
      renderBackground(fnt, text, bgCol);
    }
    fnt.drawString(text, 0, 0, ColorUtil.getRGBA(txtCol));
    if (drawShadow) {
      GL11.glTranslatef(0.5f, 0.5f, 0.1f);
      fnt.drawString(text, 0, 0, ColorUtil.getRGBA(shadowCol));
    }
    GL11.glPopMatrix();

    RenderUtil.bindBlockTexture();
  }

  public static void renderBackground(FontRenderer fnt, String toRender, Vector4f color) {
    glPushAttrib(GL_ALL_ATTRIB_BITS);
    glDisable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glShadeModel(GL_SMOOTH);
    glDisable(GL_ALPHA_TEST);
    glDisable(GL_CULL_FACE);
    glDepthMask(false);
    RenderHelper.disableStandardItemLighting();
    OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO); // stop
                                                                                     // random
                                                                                     // disappearing

    float width = fnt.getStringWidth(toRender);
    float height = fnt.FONT_HEIGHT;
    float padding = 2f;

    GlStateManager.color(color.x, color.y, color.z, color.w);

    WorldRenderer tes = Tessellator.getInstance().getWorldRenderer();
    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    tes.pos(-padding, -padding, 0).endVertex();
    tes.pos(-padding, height + padding, 0).endVertex();
    tes.pos(width + padding, height + padding, 0).endVertex();
    tes.pos(width + padding, -padding, 0).endVertex();
    Tessellator.getInstance().draw();

    glPopAttrib();
  }

  /**
   * Renders an item entity in 3D
   * 
   * @param item
   *          The item to render
   * @param rotate
   *          Whether to "spin" the item like it would if it were a real dropped
   *          entity
   */
  public static void render3DItem(EntityItem item, boolean rotate) {
    float rot = getRotation(1.0f);

    glPushMatrix();
    glDepthMask(true);
    rotate &= Minecraft.getMinecraft().gameSettings.fancyGraphics;

    if (rotate) {
      glRotatef(rot, 0, 1, 0);
    }

    item.hoverStart = 0.0F;
    Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(item, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);

    glPopMatrix();
  }

  public static float getRotation(float mult) {
    return ClientHandler.getTicksElapsed() * mult;
  }

  public static void renderBillboardQuad(float rot, double scale) {
    glPushMatrix();

    rotateToPlayer();

    glPushMatrix();

    glRotatef(rot, 0, 0, 1);
    glColor3f(1, 1, 1);

    Tessellator tessellator = Tessellator.getInstance();
    WorldRenderer tes = tessellator.getWorldRenderer();
    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    tes.pos(-scale, -scale, 0).tex(0, 0).endVertex();
    tes.pos(-scale, scale, 0).tex(0, 1).endVertex();
    tes.pos(scale, scale, 0).tex(1, 1).endVertex();
    tes.pos(scale, -scale, 0).tex(1, 0).endVertex();
    tessellator.draw();
    glPopMatrix();
    glPopMatrix();
  }

  public static void rotateToPlayer() {
    RenderManager rm = Minecraft.getMinecraft().getRenderManager();
    glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
    glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);
  }

  public static TextureAtlasSprite getTexture(IBlockState state) {
    return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
  }

  public static void renderBoundingBox(BoundingBox bb) {

    WorldRenderer tes = Tessellator.getInstance().getWorldRenderer();
    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
    List<Vector3f> corners;
    for (EnumFacing face : EnumFacing.VALUES) {
      corners = bb.getCornersForFace(face);
      for (Vector3f v : corners) {
        tes.pos(v.x, v.y, v.z).endVertex();
      }
    }
    Tessellator.getInstance().draw();
  }

  public static void renderBoundingBox(BoundingBox bb, IBlockState state) {
    renderBoundingBox(bb, getTexture(state));
  }

  public static void renderBoundingBox(BoundingBox bb, TextureAtlasSprite tex) {
    renderBoundingBox(bb, tex.getMinU(), tex.getMaxU(), tex.getMinV(), tex.getMaxV());
  }

  public static void renderBoundingBox(BoundingBox bb, float minU, float maxU, float minV, float maxV) {

    WorldRenderer tes = Tessellator.getInstance().getWorldRenderer();
    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
    List<Vertex> corners;
    for (EnumFacing face : EnumFacing.VALUES) {
      corners = bb.getCornersWithUvForFace(face, minU, maxU, minV, maxV);
      for (Vertex v : corners) {
        tes.pos(v.x(), v.y(), v.z()).tex(v.u(), v.v()).endVertex();
      }
    }
    Tessellator.getInstance().draw();

  }

  public static void registerReloadListener(IResourceManagerReloadListener obj) {
    ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(obj);
  }

  public static int getTesselatorBrightness(World world, BlockPos pos) {
    IBlockState bs = world.getBlockState(pos);
    Block block = bs.getBlock();
    int res = block.getMixedBrightnessForBlock(world, pos);
    return res;
  }
  
  public static void setupLightmapCoords(BlockPos pos, World world) {
    float f = world.getLight(pos);
    int l = RenderUtil.getLightBrightnessForSkyBlocks(world, pos, 0);
    int l1 = l % 65536;
    int l2 = l / 65536;
    GlStateManager.color(f, f, f);
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, l1, l2);
  }
  
  public static int getLightBrightnessForSkyBlocks(World world, BlockPos pos, int min) {
    int i1 = world.getLightFor(EnumSkyBlock.SKY, pos);
    int j1 = world.getLightFor(EnumSkyBlock.BLOCK, pos);
    if (j1 < min) {
      j1 = min;
    }
    return i1 << 20 | j1 << 4;
  }
  
  public static void renderBlockModel(World world, BlockPos pos, boolean translateToOrigin) {

    WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
    wr.begin(7, DefaultVertexFormats.BLOCK);

    if (translateToOrigin) {
      wr.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
    }

    for (int pass = 0; pass < 2; pass++) {
      RenderPassHelper.setBlockRenderPass(pass);
      RenderPassHelper.setBlockRenderPass(pass);

      IBlockState state = world.getBlockState(pos);
      BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
      IBakedModel ibakedmodel = blockrendererdispatcher.getModelFromBlockState(state, world, pos);
      blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, state, pos, Tessellator.getInstance().getWorldRenderer());

    }
    if (translateToOrigin) {
      wr.setTranslation(0, 0, 0);
    }
    Tessellator.getInstance().draw();

    RenderPassHelper.clearBlockRenderPass();

  }
  
  public static void addBakedQuads(BoundingBox bb, TextureAtlasSprite tex, List<BakedQuad> quads) {
    for (EnumFacing face : EnumFacing.VALUES) {
      addBakedQuadForFace(quads, bb, tex, face);
    }
  }

  public static void addBakedQuadForFace(List<BakedQuad> quads, BoundingBox bb, TextureAtlasSprite tex, EnumFacing face) {
    UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(Attributes.DEFAULT_BAKED_FORMAT);
    List<Vertex> corners = bb.getCornersWithUvForFace(face);
    builder.setQuadOrientation(face);
    builder.setQuadColored();      
    for (Vertex v : corners) {
      putVertexData(builder, v, face.getDirectionVec(), tex);
    }
    quads.add(builder.build());
  }

  private static void putVertexData(Builder builder, Vertex v, Vec3i normal, TextureAtlasSprite sprite) {
    if(sprite == null) {
      sprite = IconUtil.instance.errorTexture;
    }
    
    VertexFormat format = builder.getVertexFormat();
    for (int e = 0; e < format.getElementCount(); e++) {
      switch (format.getElement(e).getUsage()) {
      case POSITION:
        builder.put(e, (float) v.x(), (float) v.y(), (float) v.z(), 1);
        break;
      case COLOR:
        float d;
        if (v.normal != null) {
          d = LightUtil.diffuseLight(v.normal.x, v.normal.y, v.normal.z);
        } else {
          d = LightUtil.diffuseLight(normal.getX(), normal.getY(), normal.getZ());
        }

        if (v.color != null) {
          builder.put(e, d * v.color.x, d * v.color.y, d * v.color.z, v.color.w);
        } else {
          builder.put(e, d, d, d, 1);
        }
        break;
      case UV:        
          builder.put(e, sprite.getInterpolatedU(v.u() * 16),
              sprite.getInterpolatedV(v.v() * 16), 0, 1);
        
        break;
      case NORMAL:
        if(v.normal != null) {
          builder.put(e, v.nx(), v.ny(), v.nz(), 0);
        } else {
          builder.put(e, normal.getX(), normal.getY(), normal.getZ(), 0);
        }
        break;
      default:
        builder.put(e);
      }
    }
  }

}
