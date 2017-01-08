package com.enderio.core.common;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketProgress;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class TileEntityBase extends TileEntity implements ITickable {

  private final int checkOffset = (int) (Math.random() * 20);
  protected final boolean isProgressTile;

  protected int lastProgressScaled = -1;
  protected int ticksSinceLastProgressUpdate;
  private long lastUpdate = 0;

  public TileEntityBase() {
    isProgressTile = this instanceof IProgressTile;
  }

  @Override
  public final void update() {
    if (worldObj.getTileEntity(getPos()) != this || isInvalid()) {
      // we can get ticked after being removed from the world, ignore this
      return;
    }
    if (ConfigHandler.allowExternalTickSpeedup || worldObj.getTotalWorldTime() != lastUpdate) {
      lastUpdate = worldObj.getTotalWorldTime();
      doUpdate();
      if (isProgressTile && !worldObj.isRemote) {
        int curScaled = getProgressScaled(16);
        if (++ticksSinceLastProgressUpdate >= getProgressUpdateFreq() || curScaled != lastProgressScaled) {
          sendTaskProgressPacket();
          lastProgressScaled = curScaled;
        }
      }
    }
  }

  public static int getProgressScaled(int scale, IProgressTile tile) {
    return (int) (tile.getProgress() * scale);
  }

  public final int getProgressScaled(int scale) {
    if (isProgressTile) {
      return getProgressScaled(scale, (IProgressTile) this);
    }
    return 0;
  }

  protected void doUpdate() {

  }

  protected void sendTaskProgressPacket() {
    if (isProgressTile) {
      EnderPacketHandler.sendToAllAround(new PacketProgress((IProgressTile) this), this);
    }
    ticksSinceLastProgressUpdate = 0;
  }

  /**
   * Controls how often progress updates. Has no effect if your TE is not {@link IProgressTile}.
   */
  protected int getProgressUpdateFreq() {
    return 20;
  }

  @Override
  public final void readFromNBT(NBTTagCompound root) {
    super.readFromNBT(root);
    readCustomNBT(root);
  }

  @Override
  public final NBTTagCompound writeToNBT(NBTTagCompound root) {
    super.writeToNBT(root);
    writeCustomNBT(root);
    return root;
  }

  @Override
  public NBTTagCompound getUpdateTag() {
    NBTTagCompound tag = new NBTTagCompound();
    writeCustomNBT(tag);
    return tag;
  }

  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {
    NBTTagCompound tag = new NBTTagCompound();
    writeCustomNBT(tag);
    return new SPacketUpdateTileEntity(getPos(), 1, tag);
  }

  @Override
  public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
    readCustomNBT(pkt.getNbtCompound());
  }

  public boolean canPlayerAccess(EntityPlayer player) {
    return !isInvalid() && player.getDistanceSqToCenter(getPos().add(0.5, 0.5, 0.5)) <= 64D;
  }

  protected abstract void writeCustomNBT(NBTTagCompound root);

  protected abstract void readCustomNBT(NBTTagCompound root);

  protected void updateBlock() {
    if (worldObj != null) {
      IBlockState bs = worldObj.getBlockState(getPos());
      worldObj.notifyBlockUpdate(pos, bs, bs, 3);
    }
  }

  protected boolean isPoweredRedstone() {
    return worldObj.isBlockLoaded(getPos()) ? worldObj.isBlockIndirectlyGettingPowered(getPos()) > 0 : false;
  }

  /**
   * Called directly after the TE is constructed. This is the place to call non-final methods.
   *
   * Note: This will not be called when the TE is loaded from the save. Hook into the nbt methods for that.
   */
  public void init() {
  }

  /**
   * Call this with an interval (in ticks) to find out if the current tick is the one you want to do some work. This is staggered so the work of different TEs
   * is stretched out over time.
   *
   * @see #shouldDoWorkThisTick(int, int) If you need to offset work ticks
   */
  protected boolean shouldDoWorkThisTick(int interval) {
    return shouldDoWorkThisTick(interval, 0);
  }

  /**
   * Call this with an interval (in ticks) to find out if the current tick is the one you want to do some work. This is staggered so the work of different TEs
   * is stretched out over time.
   *
   * If you have different work items in your TE, use this variant to stagger your work.
   */
  protected boolean shouldDoWorkThisTick(int interval, int offset) {
    return (worldObj.getTotalWorldTime() + checkOffset + offset) % interval == 0;
  }

  @Override
  public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
    return oldState.getBlock() != newSate.getBlock();
  }

  /**
   * Called server-side when a GhostSlot is changed. Check that the given slot number really is a ghost slot before storing the given stack.
   * 
   * @param slot
   *          The slot number that was given to the ghost slot
   * @param stack
   *          The stack that should be placed, null to clear
   */
  public void setGhostSlotContents(int slot, ItemStack stack) {
  }

  @Override
  public void markDirty() {
    if (worldObj != null) {
      worldObj.markChunkDirty(pos, this);
      IBlockState state = worldObj.getBlockState(pos);
      if (state.hasComparatorInputOverride()) {
        worldObj.updateComparatorOutputLevel(pos, state.getBlock());
      }
    }
  }

}
