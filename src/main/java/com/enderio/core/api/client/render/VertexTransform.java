package com.enderio.core.api.client.render;

import com.enderio.core.common.vecmath.Vector3d;
import com.enderio.core.common.vecmath.Vector3f;
import com.enderio.core.common.vecmath.Vertex;

public interface VertexTransform {

    void apply(Vertex vertex);

    void apply(Vector3d vec);

    void applyToNormal(Vector3f vec);

}
