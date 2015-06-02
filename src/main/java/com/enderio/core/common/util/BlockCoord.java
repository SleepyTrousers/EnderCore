package com.enderio.core.common.util;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.Wither;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.base.Strings;

@AllArgsConstructor
@EqualsAndHashCode
public class BlockCoord {
  @Wither
  public final int x, y, z;

  public BlockCoord() {
    this(0, 0, 0);
  }

  public BlockCoord(double x, double y, double z) {
    this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
  }

  public BlockCoord(TileEntity tile) {
    this(tile.xCoord, tile.yCoord, tile.zCoord);
  }

  public BlockCoord(Entity e) {
    this(e.posX, e.posY, e.posZ);
  }

  public BlockCoord(BlockCoord bc) {
    this(bc.x, bc.y, bc.z);
  }

  public BlockCoord getLocation(ForgeDirection dir) {
    return new BlockCoord(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
  }

  public BlockCoord(String x, String y, String z) {
    this(Strings.isNullOrEmpty(x) ? 0 : Integer.parseInt(x), Strings.isNullOrEmpty(y) ? 0 : Integer.parseInt(y), Strings.isNullOrEmpty(z) ? 0 : Integer
        .parseInt(z));
  }

  public BlockCoord(MovingObjectPosition mop) {
    this(mop.blockX, mop.blockY, mop.blockZ);
  }
  
  public Block getBlock(IBlockAccess world) {
    return world.getBlock(x, y, z);
  }

  public int getMetadata(IBlockAccess world) {
    return world.getBlockMetadata(x, y, z);
  }

  public TileEntity getTileEntity(IBlockAccess world) {
    return world.getTileEntity(x, y, z);
  }

  public int getDistSq(BlockCoord other) {
    int xDiff = x - other.x;
    int yDiff = y - other.y;
    int zDiff = z - other.z;
    return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff;
  }

  public int getDistSq(TileEntity other) {
    return getDistSq(new BlockCoord(other));
  }

  public int getDist(BlockCoord other) {
    double dsq = getDistSq(other);
    return (int) Math.ceil(Math.sqrt(dsq));
  }

  public int getDist(TileEntity other) {
    return getDist(new BlockCoord(other));
  }
  
  public void writeToBuf(ByteBuf buf) {
    buf.writeInt(x);
    buf.writeInt(y);
    buf.writeInt(z);
  }

  public static BlockCoord readFromBuf(ByteBuf buf) {
    return new BlockCoord(buf.readInt(), buf.readInt(), buf.readInt());
  }

  public void writeToNBT(NBTTagCompound tag) {
    tag.setInteger("bc:x", x);
    tag.setInteger("bc:y", y);
    tag.setInteger("bc:z", z);
  }

  public static BlockCoord readFromNBT(NBTTagCompound tag) {
    return new BlockCoord(tag.getInteger("bc:x"), tag.getInteger("bc:y"), tag.getInteger("bc:z"));
  }

  public String chatString() {
    return chatString(EnumChatFormatting.WHITE);
  }

  public String chatString(EnumChatFormatting defaultColor) {
    return String.format(
        "x%s%d%s y%s%d%s z%s%d",
        EnumChatFormatting.GREEN, x, defaultColor,
        EnumChatFormatting.GREEN, y, defaultColor,
        EnumChatFormatting.GREEN, z
        );
  }
  
  public boolean equals(int x, int y, int z) {
    return equals(new BlockCoord(x, y, z));
  }

  @Override
  public String toString() {
    return "X: " + x + "  Y: " + y + "  Z: " + z;
  }
}
