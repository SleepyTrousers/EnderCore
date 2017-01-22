package com.enderio.core.client.render;

import java.util.Collection;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.render.VertexTransform;
import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vertex;

public class VertexTransformComposite implements VertexTransform {

  public final @Nonnull VertexTransform[] xforms;

  public VertexTransformComposite(@Nonnull VertexTransform... xforms) {
    this.xforms = xforms;
  }

  VertexTransformComposite(@Nonnull Collection<VertexTransform> xformsIn) {
    xforms = new VertexTransform[xformsIn.size()];
    int i = 0;
    for (VertexTransform xform : xformsIn) {
      xforms[i] = xform;
      i++;
    }
  }

  @Override
  public void apply(@Nonnull Vertex vertex) {
    for (VertexTransform xform : xforms) {
      xform.apply(vertex);
    }
  }

  @Override
  public void apply(@Nonnull Vector3d vec) {
    for (VertexTransform xform : xforms) {
      xform.apply(vec);
    }
  }

  @Override
  public void applyToNormal(@Nonnull Vector3f vec) {
    for (VertexTransform xform : xforms) {
      xform.applyToNormal(vec);
    }
  }

}
