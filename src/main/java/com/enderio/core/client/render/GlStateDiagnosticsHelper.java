package com.enderio.core.client.render;

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.enderio.core.common.util.Log;

public class GlStateDiagnosticsHelper {

  private final Set<String> before = getGLStates();
  private final String location;

  public GlStateDiagnosticsHelper(String location) {
    this.location = location;
  }

  static String[] glStates = { "GL_ALPHA_TEST", "GL_AUTO_NORMAL", "GL_BLEND", "GL_CLIP_PLANE0", "GL_CLIP_PLANE1", "GL_CLIP_PLANE2", "GL_CLIP_PLANE3",
      "GL_CLIP_PLANE4", "GL_CLIP_PLANE5", "GL_COLOR_ARRAY", "GL_COLOR_LOGIC_OP", "GL_COLOR_MATERIAL", "GL_CULL_FACE", "GL_DEPTH_TEST", "GL_DITHER", "GL_FOG",
      "GL_INDEX_ARRAY", "GL_INDEX_LOGIC_OP", "GL_LIGHT0", "GL_LIGHT1", "GL_LIGHT2", "GL_LIGHT3", "GL_LIGHT4", "GL_LIGHT5", "GL_LIGHT6", "GL_LIGHT7",
      "GL_LIGHTING", "GL_LINE_SMOOTH", "GL_LINE_STIPPLE", "GL_MAP1_COLOR_4", "GL_MAP1_INDEX", "GL_MAP1_NORMAL", "GL_MAP1_TEXTURE_COORD_1",
      "GL_MAP1_TEXTURE_COORD_2", "GL_MAP1_TEXTURE_COORD_3", "GL_MAP1_TEXTURE_COORD_4", "GL_MAP1_VERTEX_3", "GL_MAP1_VERTEX_4", "GL_MAP2_COLOR_4",
      "GL_MAP2_INDEX", "GL_MAP2_NORMAL", "GL_MAP2_TEXTURE_COORD_1", "GL_MAP2_TEXTURE_COORD_2", "GL_MAP2_TEXTURE_COORD_3", "GL_MAP2_TEXTURE_COORD_4",
      "GL_MAP2_VERTEX_3", "GL_MAP2_VERTEX_4", "GL_NORMAL_ARRAY", "GL_NORMALIZE", "GL_POINT_SMOOTH", "GL_POLYGON_OFFSET_FILL", "GL_POLYGON_OFFSET_LINE",
      "GL_POLYGON_OFFSET_POINT", "GL_POLYGON_SMOOTH", "GL_POLYGON_STIPPLE", "GL_SCISSOR_TEST", "GL_STENCIL_TEST", "GL_TEXTURE_1D", "GL_TEXTURE_2D",
      "GL_TEXTURE_COORD_ARRAY", "GL_TEXTURE_GEN_Q", "GL_TEXTURE_GEN_R", "GL_TEXTURE_GEN_S", "GL_TEXTURE_GEN_T", "GL_VERTEX_ARRAY" };

  public static int[] glCaps;

  private static void initGLStates() {
    glCaps = new int[glStates.length];

    for (int i = 0; i < glStates.length; i++) {
      glCaps[i] = -1;
      try {
        glCaps[i] = GL11.class.getField(glStates[i]).getInt(null);
      } catch (NoSuchFieldException ignored) {

      } catch (IllegalAccessException ignored) {

      }
    }

  }

  public static Set<String> getGLStates() {
    if (glCaps == null) {
      initGLStates();
    }

    Set<String> result = new HashSet<String>();
    for (int i = 0; i < glCaps.length; i++) {
      if (glCaps[i] != -1 && GL11.glIsEnabled(glCaps[i])) {
        result.add(glStates[i]);
      }
    }
    return result;
  }

  public void finish() {
    Set<String> after = getGLStates();
    if (!before.equals(after)) {
      Set<String> added = new HashSet<String>();
      Set<String> removed = new HashSet<String>();
      for (String string : before) {
        if (!after.contains(string)) {
          removed.add(string);
        }
      }
      for (String string : after) {
        if (!before.contains(string)) {
          added.add(string);
        }
      }
      Log.error("GL flag corruption at " + location + ": Lost " + removed + " gained " + added);
    }
  }

  public void dump() {
    Log.info("GL flags at " + location + ": " + before);
  }
}
