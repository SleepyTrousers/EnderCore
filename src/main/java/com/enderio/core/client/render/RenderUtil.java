package com.enderio.core.client.render;

import javax.annotation.Nonnull;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.AtlasTexture;

import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.texture.TextureManager;

import net.minecraft.util.ResourceLocation;

// TODO: As we're porting EIO, we'll need to start tidying and bringing some of this stuff back.
public class RenderUtil {

//  public static final @Nonnull Vector4f DEFAULT_TEXT_SHADOW_COL = new Vector4f(0.33f, 0.33f, 0.33f, 0.33f);
//
//  public static final @Nonnull Vector4f DEFAULT_TXT_COL = new Vector4f(1, 1, 1, 1);
//
//  public static final @Nonnull Vector4f DEFAULT_TEXT_BG_COL = new Vector4f(0.275f, 0.08f, 0.4f, 0.75f);
//
//  public static final @Nonnull Vector3d UP_V = new Vector3d(0, 1, 0);
//
//  public static final @Nonnull Vector3d ZERO_V = new Vector3d(0, 0, 0);
//
//  private static final @Nonnull FloatBuffer MATRIX_BUFFER = GLAllocation.createDirectFloatBuffer(16);
//
  public static final @Nonnull ResourceLocation BLOCK_TEX = AtlasTexture.LOCATION_BLOCKS_TEXTURE;

  public static final @Nonnull ResourceLocation GLINT_TEX = new ResourceLocation("textures/misc/enchanted_item_glint.png");

//  public static int BRIGHTNESS_MAX = 15 << 20 | 15 << 4;

//  public static void loadMatrix(@Nonnull Matrix4d mat) {
//    MATRIX_BUFFER.rewind();
//    MATRIX_BUFFER.put((float) mat.m00);
//    MATRIX_BUFFER.put((float) mat.m01);
//    MATRIX_BUFFER.put((float) mat.m02);
//    MATRIX_BUFFER.put((float) mat.m03);
//    MATRIX_BUFFER.put((float) mat.m10);
//    MATRIX_BUFFER.put((float) mat.m11);
//    MATRIX_BUFFER.put((float) mat.m12);
//    MATRIX_BUFFER.put((float) mat.m13);
//    MATRIX_BUFFER.put((float) mat.m20);
//    MATRIX_BUFFER.put((float) mat.m21);
//    MATRIX_BUFFER.put((float) mat.m22);
//    MATRIX_BUFFER.put((float) mat.m23);
//    MATRIX_BUFFER.put((float) mat.m30);
//    MATRIX_BUFFER.put((float) mat.m31);
//    MATRIX_BUFFER.put((float) mat.m32);
//    MATRIX_BUFFER.put((float) mat.m33);
//    MATRIX_BUFFER.rewind();
//    GL11.glLoadMatrix(MATRIX_BUFFER);
//  }
//
  public static @Nonnull TextureManager getTextureManager() {
    return Minecraft.getInstance().getTextureManager();
  }

  public static void bindBlockTexture() {
    getTextureManager().bindTexture(BLOCK_TEX);
  }

  public static void bindGlintTexture() {
    getTextureManager().bindTexture(GLINT_TEX);
  }

  public static void bindTexture(@Nonnull String string) {
    getTextureManager().bindTexture(new ResourceLocation(string));
  }

  public static void bindTexture(@Nonnull ResourceLocation tex) {
    getTextureManager().bindTexture(tex);
  }

  public static @Nonnull FontRenderer getFontRenderer() {
    return Minecraft.getInstance().fontRenderer;
  }

//  public static float claculateTotalBrightnessForLocation(@Nonnull World worldObj, @Nonnull BlockPos pos) {
//    int i = worldObj.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
//    int j = i % 65536;
//    int k = i / 65536;
//
//    // 0.2 - 1
//    float sunBrightness = worldObj.getSunBrightness(1);
//    float percentRecievedFromSun = k / 255f;
//
//    // Highest value received from a light
//    float fromLights = j / 255f;
//
//    // 0 - 1 for sun only, 0 - 0.6 for light only
//    // float recievedPercent = worldObj.getLightBrightness(new BlockPos(xCoord,
//    // yCoord, zCoord));
//    float highestValue = Math.max(fromLights, percentRecievedFromSun * sunBrightness);
//    return Math.max(0.2f, highestValue);
//  }
//
//  public static float getColorMultiplierForFace(@Nonnull Direction face) {
//    if (face == Direction.UP) {
//      return 1;
//    }
//    if (face == Direction.DOWN) {
//      return 0.5f;
//    }
//    if (face.getXOffset() != 0) {
//      return 0.6f;
//    }
//    return 0.8f; // z
//  }
//
//  public static void renderQuad2D(double x, double y, double z, double width, double height, int colorRGB) {
//
//    GlStateManager.disableTexture2D();
//
//    Vector3f col = ColorUtil.toFloat(colorRGB);
//    GlStateManager.color4f(col.x, col.y, col.z);
//
//    Tessellator tessellator = Tessellator.getInstance();
//    BufferBuilder tes = tessellator.getBuffer();
//    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
//    tes.pos(x, y + height, z).endVertex();
//    tes.pos(x + width, y + height, z).endVertex();
//    tes.pos(x + width, y, z).endVertex();
//    tes.pos(x, y, z).endVertex();
//
//    tessellator.draw();
//    GlStateManager.enableTexture2D();
//  }
//
//  public static void renderQuad2D(double x, double y, double z, double width, double height, @Nonnull Vector4f colorRGBA) {
//    GlStateManager.color4f(colorRGBA.x, colorRGBA.y, colorRGBA.z, colorRGBA.w);
//    GlStateManager.disableTexture2D();
//
//    Tessellator tessellator = Tessellator.getInstance();
//    BufferBuilder tes = tessellator.getBuffer();
//    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
//    tes.pos(x, y + height, z).endVertex();
//    tes.pos(x + width, y + height, z).endVertex();
//    tes.pos(x + width, y, z).endVertex();
//    tes.pos(x, y, z).endVertex();
//    tessellator.draw();
//    GlStateManager.enableTexture2D();
//  }
//
//  public static Matrix4d createBillboardMatrix(@Nonnull TileEntity te, @Nonnull LivingEntity entityPlayer) {
//    BlockPos p = te.getPos();
//    return createBillboardMatrix(new Vector3d(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5), entityPlayer);
//  }
//
//  public static Matrix4d createBillboardMatrix(@Nonnull Vector3d lookAt, @Nonnull LivingEntity entityPlayer) {
//    Vector3d playerEye = new Vector3d(entityPlayer.getPosX(), entityPlayer.getPosY() + 1.62 - entityPlayer.getYOffset(), entityPlayer.getPosZ());
//    Vector3d blockOrigin = new Vector3d(lookAt.x, lookAt.y, lookAt.z);
//    Matrix4d lookMat = VecmathUtil.createMatrixAsLookAt(blockOrigin, playerEye, RenderUtil.UP_V);
//    lookMat.setTranslation(new Vector3d());
//    lookMat.invert();
//    return lookMat;
//  }
//
//  public static void renderBillboard(@Nonnull Matrix4d lookMat, float minU, float maxU, float minV, float maxV, double size, int brightness) {
//    Tessellator tessellator = Tessellator.getInstance();
//    BufferBuilder tes = tessellator.getBuffer();
//    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//
//    double s = size / 2;
//    Vector3d v = new Vector3d();
//    v.set(-s, s, 0);
//    lookMat.transform(v);
//    tes.pos(v.x, v.y, v.z).tex(minU, maxV).endVertex();
//    v.set(s, s, 0);
//    lookMat.transform(v);
//    tes.pos(v.x, v.y, v.z).tex(maxU, maxV).endVertex();
//    v.set(s, -s, 0);
//    lookMat.transform(v);
//    tes.pos(v.x, v.y, v.z).tex(maxU, minV).endVertex();
//    v.set(-s, -s, 0);
//    lookMat.transform(v);
//    tes.pos(v.x, v.y, v.z).tex(minU, minV).endVertex();
//
//    tessellator.draw();
//  }
//
//  /**
//   * @return The edge directions for a face, in the order left, bottom, right, top.
//   */
//  public static List<Direction> getEdgesForFace(@Nonnull Direction face) {
//    List<Direction> result = new ArrayList<Direction>(4);
//    if (face.getFrontOffsetY() != 0) {
//      result.add(NORTH);
//      result.add(EAST);
//      result.add(SOUTH);
//      result.add(WEST);
//
//    } else if (face.getFrontOffsetX() != 0) {
//      result.add(DOWN);
//      result.add(SOUTH);
//      result.add(UP);
//      result.add(NORTH);
//    } else {
//      result.add(DOWN);
//      result.add(WEST);
//      result.add(UP);
//      result.add(EAST);
//    }
//    return result;
//  }
//
//  public static void addVerticesToTessellator(@Nullable List<Vertex> vertices, @Nonnull VertexFormat format, boolean doBegin) {
//    addVerticesToTessellator(vertices, null, format, doBegin);
//  }
//
//  public static void addVerticesToTessellator(@Nullable List<Vertex> vertices, VertexTranslation xForm, @Nonnull VertexFormat format, boolean doBegin) {
//    if (vertices == null || vertices.isEmpty()) {
//      return;
//    }
//
//    List<Vertex> newV;
//    if (xForm != null) {
//      newV = new ArrayList<Vertex>(vertices.size());
//      for (Vertex v : vertices) {
//        Vertex xv = new Vertex(v);
//        xForm.apply(xv);
//        newV.add(xv);
//      }
//    } else {
//      newV = vertices;
//    }
//
//    Tessellator tessellator = Tessellator.getInstance();
//    BufferBuilder tes = tessellator.getBuffer();
//    if (doBegin) {
//      tes.begin(GL11.GL_QUADS, format);
//    }
//
//    for (Vertex v : vertices) {
//      for (VertexFormatElement el : format.getElements()) {
//        switch (el.getUsage()) {
//        case COLOR:
//          if (el.getType() == EnumType.FLOAT) {
//            tes.color(v.r(), v.g(), v.b(), v.a());
//          }
//          break;
//        case NORMAL:
//          tes.normal(v.nx(), v.ny(), v.nz());
//          break;
//        case POSITION:
//          tes.pos(v.x(), v.y(), v.z());
//          break;
//        case UV:
//          if (el.getType() == EnumType.FLOAT && v.uv != null) {
//            tes.tex(v.u(), v.v());
//          }
//          break;
//        case GENERIC:
//          break;
//        case PADDING:
//          break;
//        default:
//          break;
//
//        }
//      }
//      tes.endVertex();
//    }
//  }
//
//  public static void getUvForCorner(@Nonnull Vector2f uv, @Nonnull Vector3d corner, int x, int y, int z, @Nonnull Direction face,
//      @Nonnull TextureAtlasSprite icon) {
//    Vector3d p = new Vector3d(corner);
//    p.x -= x;
//    p.y -= y;
//    p.z -= z;
//
//    float uWidth = 1;
//    float vWidth = 1;
//    uWidth = icon.getMaxU() - icon.getMinU();
//    vWidth = icon.getMaxV() - icon.getMinV();
//
//    uv.x = (float) VecmathUtil.distanceFromPointToPlane(getUPlaneForFace(face), p);
//    uv.y = (float) VecmathUtil.distanceFromPointToPlane(getVPlaneForFace(face), p);
//
//    uv.x = icon.getMinU() + (uv.x * uWidth);
//    uv.y = icon.getMinV() + (uv.y * vWidth);
//  }
//
//  public static @Nonnull Vector4d getVPlaneForFace(@Nonnull Direction face) {
//    switch (face) {
//    case DOWN:
//    case UP:
//      return new Vector4d(0, 0, 1, 0);
//    default:
//      return new Vector4d(0, -1, 0, 1);
//    }
//  }
//
//  public static @Nonnull Vector4d getUPlaneForFace(@Nonnull Direction face) {
//    switch (face) {
//    case EAST:
//      return new Vector4d(0, 0, -1, 1);
//    case WEST:
//      return new Vector4d(0, 0, 1, 0);
//    case NORTH:
//      return new Vector4d(-1, 0, 0, 1);
//    case SOUTH:
//      return new Vector4d(1, 0, 0, 0);
//    default:
//      return new Vector4d(1, 0, 0, 0);
//    }
//  }
//
//  public static @Nonnull Direction getVDirForFace(@Nonnull Direction face) {
//    switch (face) {
//    case DOWN:
//    case UP:
//      return SOUTH;
//    default:
//      return Direction.UP;
//    }
//  }
//
//  public static @Nonnull Direction getUDirForFace(@Nonnull Direction face) {
//    switch (face) {
//    case EAST:
//      return NORTH;
//    case WEST:
//      return SOUTH;
//    case NORTH:
//      return WEST;
//    case SOUTH:
//      return Direction.EAST;
//    default:
//      return Direction.EAST;
//    }
//  }
//
//  public static @Nonnull TextureAtlasSprite getStillTexture(@Nonnull FluidStack fluidstack) {
//    final Fluid fluid = fluidstack.getFluid();
//    if (fluid == null) {
//      return getMissingSprite();
//    }
//    return getStillTexture(fluid);
//  }
//
//  public static @Nonnull TextureAtlasSprite getStillTexture(@Nonnull Fluid fluid) {
//    ResourceLocation iconKey = fluid.getStill();
//    final TextureAtlasSprite textureExtry = Minecraft.getInstance().getTextureMapBlocks().getTextureExtry(iconKey.toString());
//    return textureExtry != null ? textureExtry : getMissingSprite();
//  }
//
//  public static @Nonnull TextureAtlasSprite getMissingSprite() {
//    return Minecraft.getInstance().getTextureMapBlocks().getMissingSprite();
//  }
//
//  public static void renderGuiTank(@Nonnull FluidTank tank, double x, double y, double zLevel, double width, double height) {
//    renderGuiTank(tank.getFluid(), tank.getCapacity(), tank.getFluidAmount(), x, y, zLevel, width, height);
//  }
//
//  public static void renderGuiTank(@Nullable FluidStack fluid, int capacity, int amount, double x, double y, double zLevel, double width, double height) {
//    if (fluid == null || fluid.getFluid() == null || amount <= 0) {
//      return;
//    }
//
//    TextureAtlasSprite icon = getStillTexture(fluid);
//
//    int renderAmount = (int) Math.max(Math.min(height, amount * height / capacity), 1);
//    int posY = (int) (y + height - renderAmount);
//
//    RenderUtil.bindBlockTexture();
//    int color = fluid.getFluid().getColor(fluid);
//    GlStateManager.color4f((color >> 16 & 0xFF) / 255f, (color >> 8 & 0xFF) / 255f, (color & 0xFF) / 255f);
//
//    GlStateManager.enableBlend();
//    for (int i = 0; i < width; i += 16) {
//      for (int j = 0; j < renderAmount; j += 16) {
//        int drawWidth = (int) Math.min(width - i, 16);
//        int drawHeight = Math.min(renderAmount - j, 16);
//
//        int drawX = (int) (x + i);
//        int drawY = posY + j;
//
//        double minU = icon.getMinU();
//        double maxU = icon.getMaxU();
//        double minV = icon.getMinV();
//        double maxV = icon.getMaxV();
//
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder tes = tessellator.getBuffer();
//        tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//        tes.pos(drawX, drawY + drawHeight, 0).tex(minU, minV + (maxV - minV) * drawHeight / 16F).endVertex();
//        tes.pos(drawX + drawWidth, drawY + drawHeight, 0).tex(minU + (maxU - minU) * drawWidth / 16F, minV + (maxV - minV) * drawHeight / 16F).endVertex();
//        tes.pos(drawX + drawWidth, drawY, 0).tex(minU + (maxU - minU) * drawWidth / 16F, minV).endVertex();
//        tes.pos(drawX, drawY, 0).tex(minU, minV).endVertex();
//        tessellator.draw();
//      }
//    }
//    GlStateManager.disableBlend();
//    GlStateManager.color4f(1, 1, 1);
//  }
//
//  public static void drawBillboardedText(@Nonnull Vector3f pos, @Nonnull String text, float size) {
//    drawBillboardedText(pos, text, size, DEFAULT_TXT_COL, true, DEFAULT_TEXT_SHADOW_COL, true, DEFAULT_TEXT_BG_COL);
//  }
//
//  public static void drawBillboardedText(@Nonnull Vector3f pos, @Nonnull String text, float size, @Nonnull Vector4f bgCol) {
//    drawBillboardedText(pos, text, size, DEFAULT_TXT_COL, true, DEFAULT_TEXT_SHADOW_COL, true, bgCol);
//  }
//
//  public static void drawBillboardedText(@Nonnull Vector3f pos, @Nonnull String text, float size, @Nonnull Vector4f txtCol, boolean drawShadow,
//      @Nullable Vector4f shadowCol, boolean drawBackground, @Nullable Vector4f bgCol) {
//
//    GlStateManager.pushMatrix();
//    GlStateManager.translate(pos.x, pos.y, pos.z);
//    GlStateManager.rotate(180, 1, 0, 0);
//
//    Minecraft mc = Minecraft.getInstance();
//    FontRenderer fnt = mc.fontRenderer;
//    float scale = size / fnt.FONT_HEIGHT;
//    GlStateManager.scale(scale, scale, scale);
//    GlStateManager.rotate(mc.getRenderManager().playerViewY + 180, 0.0F, 1.0F, 0.0F);
//    GlStateManager.rotate(-mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
//
//    GlStateManager.translate(-fnt.getStringWidth(text) / 2, 0, 0);
//    if (drawBackground && bgCol != null) {
//      renderBackground(fnt, text, bgCol);
//    }
//    fnt.drawString(text, 0, 0, ColorUtil.getRGBA(txtCol));
//    if (drawShadow && shadowCol != null) {
//      GlStateManager.translate(0.5f, 0.5f, 0.1f);
//      fnt.drawString(text, 0, 0, ColorUtil.getRGBA(shadowCol));
//    }
//    GlStateManager.enableAlpha();
//    GlStateManager.popMatrix();
//
//    RenderUtil.bindBlockTexture();
//  }
//
//  public static void renderBackground(@Nonnull FontRenderer fnt, @Nonnull String toRender, @Nonnull Vector4f color) {
//
//    GlStateManager.enableBlend(); // blend comes in as on or off depending on the player's view vector
//
//    GlStateManager.disableTexture2D();
//    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//    GlStateManager.shadeModel(GL_SMOOTH);
//    GlStateManager.disableAlpha();
//    GlStateManager.disableCull();
//    GlStateManager.depthMask(false);
//
//    RenderHelper.disableStandardItemLighting();
//
//    float width = fnt.getStringWidth(toRender);
//    float height = fnt.FONT_HEIGHT;
//    float padding = 2f;
//
//    GlStateManager.color4f(color.x, color.y, color.z, color.w);
//
//    BufferBuilder tes = Tessellator.getInstance().getBuffer();
//    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
//    tes.pos(-padding, -padding, 0).endVertex();
//    tes.pos(-padding, height + padding, 0).endVertex();
//    tes.pos(width + padding, height + padding, 0).endVertex();
//    tes.pos(width + padding, -padding, 0).endVertex();
//    Tessellator.getInstance().draw();
//
//    GlStateManager.enableTexture2D();
//    GlStateManager.enableCull();
//    GlStateManager.enableAlpha();
//    RenderHelper.enableStandardItemLighting();
//    GlStateManager.disableLighting();
//  }
//
//  /**
//   * Renders an item entity in 3D
//   *
//   * @param item
//   *          The item to render
//   * @param rotate
//   *          Whether to "spin" the item like it would if it were a real dropped entity
//   */
//  public static void render3DItem(@Nonnull ItemEntity item, boolean rotate) {
//    float rot = getRotation(1.0f);
//
//    glPushMatrix();
//    glDepthMask(true);
//
//    if (rotate && Minecraft.getInstance().gameSettings.fancyGraphics) {
//      glRotatef(rot, 0, 1, 0);
//    }
//
//    item.hoverStart = 0.0F;
//    Minecraft.getInstance().getRenderManager().renderEntity(item, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, false);
//
//    glPopMatrix();
//  }
//
//  public static float getRotation(float mult) {
//    return ClientHandler.getTicksElapsed() * mult;
//  }
//
//  public static void renderBillboardQuad(float rot, double scale) {
//    glPushMatrix();
//
//    rotateToPlayer();
//
//    glPushMatrix();
//
//    glRotatef(rot, 0, 0, 1);
//    glColor3f(1, 1, 1);
//
//    Tessellator tessellator = Tessellator.getInstance();
//    BufferBuilder tes = tessellator.getBuffer();
//    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//    tes.pos(-scale, -scale, 0).tex(0, 0).endVertex();
//    tes.pos(-scale, scale, 0).tex(0, 1).endVertex();
//    tes.pos(scale, scale, 0).tex(1, 1).endVertex();
//    tes.pos(scale, -scale, 0).tex(1, 0).endVertex();
//    tessellator.draw();
//    glPopMatrix();
//    glPopMatrix();
//  }
//
//  public static void rotateToPlayer() {
//    EntityRendererManager rm = Minecraft.getInstance().getRenderManager();
//    glRotatef(-rm.playerViewY, 0.0F, 1.0F, 0.0F);
//    glRotatef(rm.playerViewX, 1.0F, 0.0F, 0.0F);
//  }
//
//  public static @Nonnull TextureAtlasSprite getTexture(@Nonnull BlockState state) {
//    return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
//  }
//
//  public static void renderBoundingBox(@Nonnull final BoundingBox bb) {
//    final BufferBuilder tes = Tessellator.getInstance().getBuffer();
//    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
//    NNList.FACING.apply(new Callback<Direction>() {
//      @Override
//      public void apply(@Nonnull Direction e) {
//        for (Vector3f v : bb.getCornersForFace(e)) {
//          tes.pos(v.x, v.y, v.z).endVertex();
//        }
//      }
//    });
//    Tessellator.getInstance().draw();
//  }
//
//  public static void renderBoundingBox(@Nonnull BoundingBox bb, @Nonnull BlockState state) {
//    renderBoundingBox(bb, getTexture(state));
//  }
//
//  public static void renderBoundingBox(@Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex) {
//    renderBoundingBox(bb, tex.getMinU(), tex.getMaxU(), tex.getMinV(), tex.getMaxV());
//  }
//
//  public static void renderBoundingBox(@Nonnull final BoundingBox bb, final float minU, final float maxU, final float minV, final float maxV) {
//
//    final BufferBuilder tes = Tessellator.getInstance().getBuffer();
//    tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
//    NNList.FACING.apply(new Callback<Direction>() {
//      @Override
//      public void apply(@Nonnull Direction e) {
//        for (Vertex v : bb.getCornersWithUvForFace(e, minU, maxU, minV, maxV)) {
//          tes.pos(v.x(), v.y(), v.z()).tex(v.u(), v.v()).endVertex();
//        }
//      }
//    });
//    Tessellator.getInstance().draw();
//  }
//
//  public static void registerReloadListener(@Nonnull IResourceManagerReloadListener obj) {
//    ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(obj);
//  }
//
//  public static void setupLightmapCoords(@Nonnull BlockPos pos, @Nonnull World world) {
//    float f = world.getLight(pos);
//    int l = RenderUtil.getLightBrightnessForSkyBlocks(world, pos, 0);
//    int l1 = l % 65536;
//    int l2 = l / 65536;
//    GlStateManager.color4f(f, f, f);
//    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, l1, l2);
//  }
//
//  public static int getLightBrightnessForSkyBlocks(@Nonnull World world, @Nonnull BlockPos pos, int min) {
//    int i1 = world.getLightFor(EnumSkyBlock.SKY, pos);
//    int j1 = world.getLightFor(EnumSkyBlock.BLOCK, pos);
//    if (j1 < min) {
//      j1 = min;
//    }
//    return i1 << 20 | j1 << 4;
//  }
//
//  public static void renderBlockModel(@Nonnull final World world, @Nonnull final BlockPos pos, boolean translateToOrigin) {
//    final RenderType oldRenderLayer = MinecraftForgeClient.getRenderLayer();
//    final BlockState state = world.getBlockState(pos);
//    final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//    final IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(state);
//    final Tessellator tesselator = Tessellator.getInstance();
//    final BufferBuilder wr = tesselator.getBuffer();
//    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//    if (translateToOrigin) {
//      wr.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
//    }
//    NNList.RENDER_LAYER.apply(new Callback<RenderType>() {
//      @Override
//      public void apply(@Nonnull RenderType layer) {
//        ForgeHooksClient.setRenderLayer(layer);
//        // TODO: Need to setup GL state correctly for each layer
//        blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, state, pos, wr, false);
//      }
//    });
//    if (translateToOrigin) {
//      wr.setTranslation(0, 0, 0);
//    }
//    tesselator.draw();
//    ForgeHooksClient.setRenderLayer(oldRenderLayer);
//  }
//
//  public static void renderBlockModelAsItem(@Nonnull World world, @Nonnull ItemStack stack, @Nonnull BlockState state) {
//    BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//    IBakedModel model = blockrendererdispatcher.getBlockModelShapes().getModelForState(state);
//    Minecraft.getInstance().getRenderItem().renderItem(stack, model);
//  }
//
//  @Nonnull
//  private static final Vector4f FULL_UVS = new Vector4f(0, 0, 1, 1);
//
//  public static void addBakedQuads(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex) {
//    addBakedQuads(quads, bb, FULL_UVS, tex);
//  }
//
//  public static void addBakedQuads(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull Vector4f uvs, @Nonnull TextureAtlasSprite tex) {
//    addBakedQuads(quads, bb, uvs, tex, null);
//  }
//
//  public static void addBakedQuads(@Nonnull final List<BakedQuad> quads, @Nonnull final BoundingBox bb, @Nonnull final TextureAtlasSprite tex,
//      final Vector4f color) {
//    addBakedQuads(quads, bb, FULL_UVS, tex, color);
//  }
//
//  public static void addBakedQuads(@Nonnull final List<BakedQuad> quads, @Nonnull final BoundingBox bb, @Nonnull Vector4f uvs, @Nonnull final TextureAtlasSprite tex,
//      final Vector4f color) {
//    NNList.FACING.apply(new Callback<Direction>() {
//      @Override
//      public void apply(@Nonnull Direction face) {
//        addBakedQuadForFace(quads, bb, tex, face, uvs, null, false, false, true, color);
//      }
//    });
//  }
//
//  public static void addBakedQuadForFace(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex, @Nonnull Direction face) {
//    addBakedQuadForFace(quads, bb, tex, face, FULL_UVS);
//  }
//
//  public static void addBakedQuadForFace(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex, @Nonnull Direction face,
//      @Nonnull Vector4f uvs) {
//    addBakedQuadForFace(quads, bb, tex, face, uvs, false, false);
//  }
//
//  public static void addBakedQuadForFace(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex, @Nonnull Direction face,
//      boolean rotateUV, boolean flipU) {
//    addBakedQuadForFace(quads, bb, tex, face, FULL_UVS, rotateUV, flipU);
//  }
//
//  public static void addBakedQuadForFace(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex, @Nonnull Direction face,
//      @Nonnull Vector4f uvs, boolean rotateUV, boolean flipU) {
//    addBakedQuadForFace(quads, bb, tex, face, null, rotateUV, flipU, true, null);
//  }
//
//  public static void addBakedQuadForFace(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex, @Nonnull Direction face,
//      @Nullable VertexTransform xform, boolean rotateUV, boolean flipU, boolean recolor, @Nullable Vector4f color) {
//    addBakedQuadForFace(quads, bb, tex, face, FULL_UVS, null, rotateUV, flipU, recolor, color);
//  }
//
//  public static void addBakedQuadForFace(@Nonnull List<BakedQuad> quads, @Nonnull BoundingBox bb, @Nonnull TextureAtlasSprite tex, @Nonnull Direction face,
//      @Nonnull Vector4f uvs, @Nullable VertexTransform xform, boolean rotateUV, boolean flipU, boolean recolor, @Nullable Vector4f color) {
//    UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(DefaultVertexFormats.ITEM);
//    List<Vertex> corners = bb.getCornersWithUvForFace(face, uvs.x, uvs.z, uvs.y, uvs.w);
//    builder.setQuadOrientation(face);
//    builder.setTexture(tex);
//    if (rotateUV) {
//      Vector2f vec = corners.get(corners.size() - 1).uv;
//      for (int i = corners.size() - 2; i >= 0; i--) {
//        Vertex vert = corners.get(i);
//        Vector2f temp = vert.uv;
//        vert.uv = vec;
//        vec = temp;
//      }
//      corners.get(corners.size() - 1).uv = vec;
//    }
//    for (Vertex v : corners) {
//      if (v != null) {
//        if (xform != null) {
//          xform.apply(v);
//        }
//        if (recolor) {
//          v.color = color;
//        }
//        if (flipU) {
//          v.uv.x = uvs.z - v.uv.x;
//        }
//        putVertexData(builder, v, face.getDirectionVec(), tex);
//      }
//    }
//    quads.add(builder.build());
//  }
//
//  public static void addBakedQuads(@Nonnull List<BakedQuad> quads, @Nonnull Collection<Vertex> vertices, @Nonnull TextureAtlasSprite tex,
//      @Nullable Vector4f color) {
//    Iterator<Vertex> it = vertices.iterator();
//    while (it.hasNext()) {
//      Direction face = Direction.DOWN;
//      UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(Attributes.DEFAULT_BAKED_FORMAT);
//      for (int i = 0; i < 4; i++) {
//        Vertex v = it.next();
//        if (i == 0) {
//          face = Direction.getFacingFromVector(v.nx(), v.ny(), v.nz());
//          builder.setQuadOrientation(face);
//          builder.setTexture(tex);
//        }
//        v.color = color;
//        putVertexData(builder, v, face.getDirectionVec(), tex);
//      }
//      quads.add(builder.build());
//    }
//  }
//
//  private static void putVertexData(@Nonnull Builder builder, @Nonnull Vertex v, @Nonnull Vec3i normal, @Nonnull TextureAtlasSprite sprite) {
//    VertexFormat format = builder.getVertexFormat();
//    for (int e = 0; e < format.getElementCount(); e++) {
//      switch (format.getElement(e).getUsage()) {
//      case POSITION:
//        builder.put(e, (float) v.x(), (float) v.y(), (float) v.z(), 1);
//        break;
//      case COLOR:
//        float d;
//        if (v.normal != null) {
//          d = LightUtil.diffuseLight(v.normal.x, v.normal.y, v.normal.z);
//        } else {
//          d = LightUtil.diffuseLight(normal.getX(), normal.getY(), normal.getZ());
//        }
//
//        if (v.color != null) {
//          builder.put(e, d * v.color.x, d * v.color.y, d * v.color.z, v.color.w);
//        } else {
//          builder.put(e, d, d, d, 1);
//        }
//        break;
//      case UV:
//        builder.put(e, sprite.getInterpolatedU(v.u() * 16), sprite.getInterpolatedV(v.v() * 16), 0, 1);
//
//        break;
//      case NORMAL:
//        if (v.normal != null) {
//          builder.put(e, v.nx(), v.ny(), v.nz(), 0);
//        } else {
//          builder.put(e, normal.getX(), normal.getY(), normal.getZ(), 0);
//        }
//        break;
//      default:
//        builder.put(e);
//      }
//    }
//  }

}
