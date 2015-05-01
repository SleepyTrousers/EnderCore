package com.enderio.core.api.client.render;

import java.util.List;

import com.enderio.core.client.render.CustomRenderBlocks;
import com.enderio.core.common.vecmath.Vertex;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public interface IRenderFace {

  void renderFace(CustomRenderBlocks rb, ForgeDirection face, Block par1Block, double x, double y, double z, IIcon texture, List<Vertex> refVertices,
      boolean translateToXyz);

}
