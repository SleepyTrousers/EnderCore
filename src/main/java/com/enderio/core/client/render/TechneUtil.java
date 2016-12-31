/*

Copyright © 2014 RainWarrior

Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:

   1. The origin of this software must not be misrepresented; you must not
   claim that you wrote the original software.

   2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

   3. Altered source versions must be plainly marked as such, and must not be
   misrepresented as being the original software.

   4. This notice may not be removed or altered from any source
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package com.enderio.core.client.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.obj.Face;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.TextureCoordinate;
import net.minecraftforge.client.model.obj.Vertex;
import net.minecraftforge.client.model.obj.WavefrontObject;
import net.minecraftforge.client.model.techne.TechneModel;
import net.minecraftforge.client.model.techne.TechneModelLoader;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.enderio.core.api.client.render.VertexTransform;
import com.enderio.core.common.vecmath.Vector3d;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

/**
 * Slightly modified to fit the EnderIO source.
 * 
 * @author Based on RainWarrior's code <a
 *         href=https://gist.github.com/RainWarrior
 *         /39794c3548de9dcc7303>here.</a>
 */
public class TechneUtil {

  public static VertexTransform vt = null;

  private static final TechneModelLoader modelLoader = new TechneModelLoader();

  private static final Tessellator tes = Tessellator.instance;

  public static List<GroupObject> bakeModel(ModelRenderer model, TechneModel supermodel) {
    return bakeModel(model, supermodel, 1);
  }

  public static List<GroupObject> bakeModel(ModelRenderer model, TechneModel supermodel, float scale) {
    return bakeModel(model, supermodel, scale, new Matrix4f());
  }

  public static List<GroupObject> bakeModel(ModelRenderer model, TechneModel supermodel, float scale, Matrix4f matrix) {
    return bakeModel(model, supermodel, scale, matrix, false);
  }

  /**
   * Convert ModelRenderer to a list of GroupObjects, for ease of use in ISBRH
   * and other static contexts.
   * 
   * @param scale
   *          the scale factor, usually last argument to rendering methods
   * @param matrix
   *          initial transformation matrix (replaces calling
   *          glTranslate/glRotate/e.t.c. before rendering)
   * @param rotateYFirst
   *          true, of the order of rotations be like in
   *          ModelRenderer.renderWithRotation, false if like in
   *          ModelRenderer.render
   */
  @SuppressWarnings("unchecked")
  public static List<GroupObject> bakeModel(ModelRenderer model, TechneModel supermodel, float scale, Matrix4f matrix, boolean rotateYFirst) {

    // Correction for the fact that TechneModel sets the texture size on the
    // ModelRenderer after the texture positions have been calculated. It also
    // never sets the size on the TechneModel, so we can use that value (which
    // is taken by the ModelRenderer for the calculations) to correct the
    // positions.

    float uCorrection = ((int) model.textureWidth) / supermodel.textureWidth;
    float vCorrection = ((int) model.textureHeight) / supermodel.textureHeight;

    Matrix4f m = new Matrix4f(matrix);

    m.translate(new Vector3f(model.offsetX + model.rotationPointX * scale, model.offsetY + model.rotationPointY * scale, model.offsetZ + model.rotationPointZ
        * scale));

    if (!rotateYFirst) {
      m.rotate(model.rotateAngleZ, new Vector3f(0, 0, 1));
    }
    m.rotate(model.rotateAngleY, new Vector3f(0, 1, 0));
    m.rotate(model.rotateAngleX, new Vector3f(1, 0, 0));

    if (rotateYFirst) {
      m.rotate(model.rotateAngleZ, new Vector3f(0, 0, 1));
    }

    Vector4f vec = new Vector4f();
    List<GroupObject> res = new ArrayList<GroupObject>();

    for (ModelBox box : (List<ModelBox>) model.cubeList) {
      GroupObject obj = new GroupObject("", GL11.GL_QUADS);
      TexturedQuad[] quads = (TexturedQuad[]) ObfuscationReflectionHelper.getPrivateValue(ModelBox.class, box, "quadList", "field_78254_i");
      for (int i = 0; i < quads.length; i++) {
        Face face = new Face();
        face.vertices = new Vertex[4];
        face.textureCoordinates = new TextureCoordinate[4];
        float minU = -1, minV = -1;
        for (int j = 0; j < 4; j++) {
          PositionTextureVertex pv = quads[i].vertexPositions[j];
          if (minU < 0 || minU > pv.texturePositionX) {
            minU = pv.texturePositionX;
          }
          if (minV < 0 || minV > pv.texturePositionY) {
            minV = pv.texturePositionY;
          }
        }
        for (int j = 0; j < 4; j++) {
          PositionTextureVertex pv = quads[i].vertexPositions[j];

          vec.x = (float) pv.vector3D.xCoord * scale;
          vec.y = (float) pv.vector3D.yCoord * scale;
          vec.z = (float) pv.vector3D.zCoord * scale;
          vec.w = 1;

          Matrix4f.transform(m, vec, vec);

          face.vertices[j] = new Vertex(vec.x / vec.w, vec.y / vec.w, vec.z / vec.w);

          // The small offset will reduce the amount of visible errors (white
          // pixels) at the edges of textures a lot. The introduced visible
          // offset is very small, about 1 screen pixel between a 1/16 block
          // wide element and a 1 block wide element when standing "on touch".
          float u;
          float v;
          if (pv.texturePositionX == minU) {
            u = pv.texturePositionX / uCorrection + 0.0001f;
          } else {
            u = pv.texturePositionX / uCorrection - 0.0001f;
          }
          if (pv.texturePositionY == minV) {
            v = pv.texturePositionY / vCorrection + 0.0001f;
          } else {
            v = pv.texturePositionY / vCorrection - 0.0001f;
          }
          face.textureCoordinates[j] = new TextureCoordinate(u, v);
        }
        face.faceNormal = face.calculateFaceNormal();
        obj.faces.add(face);
      }
      res.add(obj);
    }

    return res;
  }

  public static Map<String, GroupObject> bakeModel(TechneModel model) {
    return bakeModel(model, 1);
  }

  public static Map<String, GroupObject> bakeModel(TechneModel model, float scale) {
    return bakeModel(model, scale, new Matrix4f());
  }

  public static Map<String, GroupObject> bakeModel(TechneModel model, float scale, Matrix4f m) {
    return bakeModel(model, scale, m, false);
  }

  /**
   * Use this to convert TechneModel to it's static representation
   */
  @SuppressWarnings("unchecked")
  public static Map<String, GroupObject> bakeModel(TechneModel model, float scale, Matrix4f m, boolean rotateYFirst) {
    Map<String, ModelRenderer> parts = (Map<String, ModelRenderer>) ObfuscationReflectionHelper.getPrivateValue(TechneModel.class, model, "parts");
    Map<String, GroupObject> res = Maps.newHashMap();

    for (Map.Entry<String, ModelRenderer> e : parts.entrySet()) {
      GroupObject obj = bakeModel(e.getValue(), model, scale, m, rotateYFirst).get(0);
      res.put(e.getKey(), obj);
    }

    return res;
  }

  public static Map<String, GroupObject> getModel(String modid, String modelPath) {
    TechneModel tm = (TechneModel) modelLoader.loadInstance(new ResourceLocation(modid.toLowerCase(Locale.US), modelPath + ".tcn"));
    return TechneUtil.bakeModel(tm, 1f / 16, new Matrix4f().scale(new Vector3f(-1, -1, 1)));
  }

  public static Collection<GroupObject> getModelAll(String modid, String modelPath) {
    TechneModel tm = (TechneModel) modelLoader.loadInstance(new ResourceLocation(modid.toLowerCase(Locale.US), modelPath + ".tcn"));
    return TechneUtil.bakeModel(tm, 1f / 16, new Matrix4f().scale(new Vector3f(-1, -1, 1))).values();
  }

  public static void renderWithIcon(WavefrontObject model, IIcon icon, Tessellator tes) {
    renderWithIcon(model.groupObjects, icon, null, tes);
  }

  public static void renderWithIcon(Collection<GroupObject> model, IIcon icon, IIcon override, Tessellator tes) {
    renderWithIcon(model, icon, override, tes, null);
  }

  public static void renderWithIcon(Collection<GroupObject> model, IIcon icon, IIcon override, Tessellator tes, VertexTransform vt) {
    renderWithIcon(model, icon, override, tes, null, 0, 0, 0, vt);
  }

  public static void renderWithIcon(Collection<GroupObject> model, IIcon icon, IIcon override, Tessellator tes, IBlockAccess world, int x, int y, int z) {
    renderWithIcon(model, icon, override, tes, world, x, y, z, null);
  }

  public static void renderWithIcon(Collection<GroupObject> model, IIcon icon, IIcon override, Tessellator tes, IBlockAccess world, int x, int y, int z,
      VertexTransform vt) {
    renderWithIcon(model, icon, override, tes, world, x, y, z, vt, true);
  }

  public static void renderWithIcon(Collection<GroupObject> model, IIcon icon, IIcon override, Tessellator tes, IBlockAccess world, int x, int y, int z,
      VertexTransform vt, boolean isbrh) {
    for (GroupObject go : model) {
      renderWithIcon(go, icon, override, tes, world, x, y, z, vt, isbrh);
    }
  }

  public static void renderWithIcon(GroupObject go, IIcon icon, IIcon override, Tessellator tes, IBlockAccess world, int x, int y, int z, VertexTransform vt,
      boolean isbrh) {
    for (Face f : go.faces) {
      Vertex n = f.faceNormal;
      tes.setNormal(n.x, n.y, n.z);
      ForgeDirection normal = getNormalFor(n);
      ForgeDirection right = normal.getRotation(ForgeDirection.DOWN);
      if (normal == right) {
        right = ForgeDirection.EAST;
      }
      ForgeDirection down = normal.getRotation(right.getOpposite());

      if (isbrh && world != null && world.getBlock(x, y, z).getLightOpacity() > 0) {
        int bx = x + normal.offsetX;
        int by = y + normal.offsetY;
        int bz = z + normal.offsetZ;
        tes.setBrightness(world.getBlock(bx, by, bz).getMixedBrightnessForBlock(world, bx, by, bz));
      }

      if (vt != null) {
        // TODO BLECH
        if (vt instanceof VertexRotationFacing) {
          normal = ((VertexRotationFacing) vt).rotate(normal);
        } else if (vt instanceof VertexTransformComposite) {
          for (VertexTransform xform : ((VertexTransformComposite) vt).xforms) {
            if (xform instanceof VertexRotationFacing) {
              normal = ((VertexRotationFacing) xform).rotate(normal);
            }
          }
        }
      }

      if (isbrh) {
        int c = (int) (0xFF * RenderUtil.getColorMultiplierForFace(normal));
        tes.setColorOpaque(c, c, c);
      }

      for (int i = 0; i < f.vertices.length; i++) {
        Vertex vert = f.vertices[i];
        Vector3d v = new Vector3d(vert);
        Vector3d tv = new Vector3d(v);
        tv.add(0.5, 0, 0.5);
        if (vt != null) {
          vt.apply(v);
        }

        if (override != null) {

          double interpX = Math.abs(tv.x * right.offsetX + tv.y * right.offsetY + tv.z * right.offsetZ);
          double interpY = Math.abs(tv.x * down.offsetX + tv.y * down.offsetY + tv.z * down.offsetZ);

          // Handles verts outside block bounds. Modulo fails at 1.0.
          while (interpX > 1) {
            interpX--;
          }
          while (interpY > 1) {
            interpY--;
          }

          if (normal == ForgeDirection.SOUTH || normal == ForgeDirection.WEST) {
            interpX = 1 - interpX;
          }
          if (normal != ForgeDirection.UP && normal != ForgeDirection.DOWN) {
            interpY = 1 - interpY;
          }
          tes.addVertexWithUV(v.x, v.y, v.z, override.getInterpolatedU(interpX * 16), override.getInterpolatedV(interpY * 16));
        } else {
          TextureCoordinate t = f.textureCoordinates[i];
          tes.addVertexWithUV(v.x, v.y, v.z, getInterpolatedU(icon, t.u), getInterpolatedV(icon, t.v));
        }
      }
    }
  }

  // reducing texture positioning error by working in double and not multiplying
  // and dividing by 16.
  public static double getInterpolatedV(IIcon icon, double offset) {
    return icon.getMinV() + (icon.getMaxV() - icon.getMinV()) * offset;
  }

  public static double getInterpolatedU(IIcon icon, double offset) {
    return icon.getMinU() + (icon.getMaxU() - icon.getMinU()) * offset;
  }

  private static ForgeDirection getNormalFor(Vertex n) {
    if (n.x != 0) {
      return n.x > 0 ? ForgeDirection.EAST : ForgeDirection.WEST;
    } else if (n.y != 0) {
      return n.y > 0 ? ForgeDirection.UP : ForgeDirection.DOWN;
    } else {
      return n.z > 0 ? ForgeDirection.SOUTH : ForgeDirection.NORTH;
    }
  }

  public static void renderInventoryBlock(Collection<GroupObject> model, Block block, int metadata, RenderBlocks rb) {
    renderInventoryBlock(model, getIconFor(block, metadata), block, metadata, rb);
  }

  public static void renderInventoryBlock(Collection<GroupObject> model, IIcon icon, Block block, int metadata, RenderBlocks rb) {
    tes.startDrawingQuads();
    tes.setColorOpaque_F(1, 1, 1);
    tes.addTranslation(0, -0.47f, 0);
    renderWithIcon(model, icon, rb.overrideBlockTexture, tes, vt);
    tes.addTranslation(0, 0.47f, 0);
    tes.draw();
    resetVT();
  }

  public static void renderInventoryBlock(GroupObject model, Block block, int metadata, RenderBlocks rb) {
    IIcon icon = getIconFor(block, metadata);
    tes.startDrawingQuads();
    tes.setColorOpaque_F(1, 1, 1);
    tes.addTranslation(0, -0.47f, 0);
    renderWithIcon(model, icon, rb.overrideBlockTexture, tes, null, 0, 0, 0, vt, true);
    tes.addTranslation(0, 0.47f, 0);
    tes.draw();
    resetVT();
  }

  public static boolean renderWorldBlock(Collection<GroupObject> model, IBlockAccess world, int x, int y, int z, Block block, RenderBlocks rb) {
    IIcon icon = getIconFor(block, world, x, y, z);
    return renderWorldBlock(model, icon, world, x, y, z, block, rb);
  }

  public static boolean renderWorldBlock(Collection<GroupObject> model, IIcon icon, IBlockAccess world, int x, int y, int z, Block block, RenderBlocks rb) {
    if (icon == null) {
      return false;
    }
    tes.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
    tes.setColorOpaque_F(1, 1, 1);
    tes.addTranslation(x + .5F, y + 0.0375f, z + .5F);
    renderWithIcon(model, icon, rb.overrideBlockTexture, tes, world, x, y, z, vt);
    tes.addTranslation(-x - .5F, -y - 0.0375f, -z - .5F);
    resetVT();
    return true;
  }

  public static boolean renderWorldBlock(GroupObject model, IBlockAccess world, int x, int y, int z, Block block, RenderBlocks rb) {
    IIcon icon = getIconFor(block, world, x, y, z);
    tes.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
    tes.setColorOpaque_F(1, 1, 1);
    tes.addTranslation(x + .5F, y + 0.0375f, z + .5F);
    renderWithIcon(model, icon, rb.overrideBlockTexture, tes, world, x, y, z, vt, true);
    tes.addTranslation(-x - .5F, -y - 0.0375f, -z - .5F);
    resetVT();
    return true;
  }

  private static void resetVT() {
    vt = null;
  }

  private static IIcon getIconFor(Block block, IBlockAccess world, int x, int y, int z) {
    return block.getIcon(world, x, y, z, 0);
  }

  private static IIcon getIconFor(Block block, int metadata) {
    return block.getIcon(0, metadata);
  }
}
