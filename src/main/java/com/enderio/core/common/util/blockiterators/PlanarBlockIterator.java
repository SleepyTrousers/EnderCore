package com.enderio.core.common.util.blockiterators;

import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.BlockCoord;

public class PlanarBlockIterator extends CubicBlockIterator {

    public static enum Orientation {

        EAST_WEST,
        NORTH_SOUTH,
        HORIZONTAL;

        public static Orientation perpendicular(ForgeDirection dir) {
            switch (dir) {
                case DOWN:
                case UP:
                    return HORIZONTAL;
                case NORTH:
                case SOUTH:
                    return EAST_WEST;
                case EAST:
                case WEST:
                    return NORTH_SOUTH;
                default:
                    return null;
            }
        }
    }

    private Orientation orientation;

    public PlanarBlockIterator(BlockCoord base, Orientation orientation, int radius) {
        super(base, radius);

        this.orientation = orientation;
    }

    @Override
    public BlockCoord next() {
        BlockCoord coord = new BlockCoord(curX, curY, curZ);
        switch (orientation) {
            case EAST_WEST:
                curY = curY == maxY ? minY : curY + 1;
                curX = curY == minY ? curX + 1 : curX;
            case NORTH_SOUTH:
                curY = curY == maxY ? minY : curY + 1;
                curZ = curY == minY ? curZ + 1 : curZ;
            case HORIZONTAL:
                curX = curX == maxX ? minX : curX + 1;
                curZ = curX == minX ? curZ + 1 : curZ;
        }
        return coord;
    }

    @Override
    public boolean hasNext() {
        return curX <= maxX && curY <= maxY && curZ <= maxZ;
    }
}
