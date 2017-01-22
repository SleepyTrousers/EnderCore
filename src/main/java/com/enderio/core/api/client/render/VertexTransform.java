package com.enderio.core.api.client.render;

import javax.annotation.Nonnull;

import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vertex;

public interface VertexTransform {

  void apply(@Nonnull Vertex vertex);

  void apply(@Nonnull Vector3d vec);

  void applyToNormal(@Nonnull Vector3f vec);

}
