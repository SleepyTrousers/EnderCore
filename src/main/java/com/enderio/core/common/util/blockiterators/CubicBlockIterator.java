package com.enderio.core.common.util.blockiterators;

import com.enderio.core.common.util.BlockCoord;

public class CubicBlockIterator extends AbstractBlockIterator {

    protected int radius;
    protected int minX, minY, minZ;
    protected int curX, curY, curZ;
    protected int maxX, maxY, maxZ;

    public CubicBlockIterator(BlockCoord base, int radius) {
        super(base);
        this.radius = radius;

        curX = minX = base.x - radius;
        curY = minY = base.y - radius;
        curZ = minZ = base.z - radius;

        maxX = base.x + radius;
        maxY = base.y + radius;
        maxZ = base.z + radius;
    }

    @Override
    public BlockCoord next() {
        BlockCoord ret = new BlockCoord(curX, curY, curZ);
        curX = curX == maxX ? minX : curX + 1;
        curY = curX == minX ? curY == maxY ? minY : curY + 1 : curY;
        curZ = curY == minY && curX == minX ? curZ + 1 : curZ;
        return ret;
    }

    @Override
    public boolean hasNext() {
        return curZ <= maxZ;
    }
}
