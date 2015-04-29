package com.enderio.core.common.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.Wither;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@AllArgsConstructor
@EqualsAndHashCode
public class BlockCoord
{
    @Wither
    public int x, y, z;

    public BlockCoord(Entity e)
    {
        this(MathHelper.floor_double(e.posX), MathHelper.floor_double(e.posY), MathHelper.floor_double(e.posZ));
    }

    public BlockCoord(TileEntity te)
    {
        this(te.xCoord, te.yCoord, te.zCoord);
    }

    public BlockCoord(BlockCoord other)
    {
        this(other.x, other.y, other.z);
    }

    public Block getBlock(World world)
    {
        return world.getBlock(x, y, z);
    }

    public int getMetadata(World world)
    {
        return world.getBlockMetadata(x, y, z);
    }

    public TileEntity getTileEntity(World world)
    {
        return world.getTileEntity(x, y, z);
    }

    public double getDistSq(BlockCoord other)
    {
        double xDiff = x + 0.5D - other.x;
        double yDiff = y + 0.5D - other.y;
        double zDiff = z + 0.5D - other.z;
        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
    }

    public double getDistSq(TileEntity other)
    {
        return other.getDistanceFrom(x + 0.5, y + 0.5, z + 0.5);
    }

    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setInteger("blockCoordx", x);
        tag.setInteger("blockCoordy", y);
        tag.setInteger("blockCoordz", z);
    }

    public static BlockCoord readFromNBT(NBTTagCompound tag)
    {
        int x = tag.getInteger("blockCoordx");
        int y = tag.getInteger("blockCoordy");
        int z = tag.getInteger("blockCoordz");

        return new BlockCoord(x, y, z);
    }

    public void setPosition(double x, double y, double z)
    {
        this.x = MathHelper.floor_double(x);
        this.y = MathHelper.floor_double(y);
        this.z = MathHelper.floor_double(z);
    }

    @Override
    public String toString()
    {
        return "X: " + x + "  Y: " + y + "  Z: " + z;
    }
}
