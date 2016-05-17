package com.enderio.core.common.util;

import javax.annotation.Generated;

import com.google.common.base.Strings;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;

public class BlockCoord {

  public final int x, y, z;

  public BlockCoord() {
    this(0, 0, 0);
  }
  
  public BlockCoord(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public BlockCoord(double x, double y, double z) {
    this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
  }

  public BlockCoord(TileEntity tile) {
    this(tile.getPos());
  }

  public BlockCoord(Entity e) {
    this(e.posX, e.posY, e.posZ);
  }

  public BlockCoord(BlockCoord bc) {
    this(bc.x, bc.y, bc.z);
  }
  
  public BlockCoord(BlockPos bc) {
    this(bc.getX(), bc.getY(), bc.getZ());
  }

  public BlockCoord getLocation(EnumFacing dir) {
    return new BlockCoord(x + dir.getFrontOffsetX(), y + dir.getFrontOffsetY(), z + dir.getFrontOffsetZ());
  }

  public BlockCoord(String x, String y, String z) {
    this(Strings.isNullOrEmpty(x) ? 0 : Integer.parseInt(x), Strings.isNullOrEmpty(y) ? 0 : Integer.parseInt(y), Strings.isNullOrEmpty(z) ? 0 : Integer
        .parseInt(z));
  }

  public BlockCoord(RayTraceResult mop) {
    this(mop.getBlockPos());
  }

  public IBlockState getBlockState(IBlockAccess world) {
    return world.getBlockState(getBlockPos());
  }
  
  public Block getBlock(IBlockAccess world) {
    return world.getBlockState(getBlockPos()).getBlock();
  }

  public int getMetadata(IBlockAccess world) {
    IBlockState bs = world.getBlockState(getBlockPos());
    return bs.getBlock().getMetaFromState(bs);
  }

  public TileEntity getTileEntity(IBlockAccess world) {
    return world.getTileEntity(getBlockPos());
  }
  
  public BlockPos getBlockPos() {
    return new BlockPos(x,y,z);
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
    return chatString(TextFormatting.WHITE);
  }

  public String chatString(TextFormatting defaultColor) {
    return String.format("x%s%d%s y%s%d%s z%s%d", TextFormatting.GREEN, x, defaultColor, TextFormatting.GREEN, y, defaultColor,
        TextFormatting.GREEN, z);
  }

  public boolean equals(int x, int y, int z) {
    return equals(new BlockCoord(x, y, z));
  }

  @Override
  public String toString() {
    return "X: " + x + "  Y: " + y + "  Z: " + z;
  }
  
  @Override
  @Generated("lombok")
  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof BlockCoord)) return false;
    final BlockCoord other = (BlockCoord)o;
    if (!other.canEqual((Object)this)) return false;
    if (this.x != other.x) return false;
    if (this.y != other.y) return false;
    if (this.z != other.z) return false;
    return true;
  }

  @Generated("lombok")
  protected boolean canEqual(final Object other) {
    return other instanceof BlockCoord;
  }

  @Override
  @Generated("lombok")
  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.x;
    result = result * PRIME + this.y;
    result = result * PRIME + this.z;
    return result;
  }

  @Generated("lombok")
  public BlockCoord withX(final int x) {
    return this.x == x ? this : new BlockCoord(x, this.y, this.z);
  }

  @Generated("lombok")
  public BlockCoord withY(final int y) {
    return this.y == y ? this : new BlockCoord(this.x, y, this.z);
  }

  @Generated("lombok")
  public BlockCoord withZ(final int z) {
    return this.z == z ? this : new BlockCoord(this.x, this.y, z);
  }
}
