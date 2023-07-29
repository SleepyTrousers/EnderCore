package com.enderio.core.common;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.autosave.Reader;
import com.enderio.core.common.autosave.Writer;
import com.enderio.core.common.autosave.annotations.Storable;
import com.enderio.core.common.autosave.annotations.Store.StoreFor;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketProgress;

@Storable
public abstract class TileEntityBase extends TileEntity implements ITickable {

    private final int checkOffset = (int) (Math.random() * 20);
    protected final boolean isProgressTile;

    protected int lastProgressScaled = -1;
    protected int ticksSinceLastProgressUpdate;

    public TileEntityBase() {
        isProgressTile = this instanceof IProgressTile;
    }

    @Override
    public final void update() {
        doUpdate();
        if (isProgressTile && !worldObj.isRemote) {
            int curScaled = getProgressScaled(16);
            if (++ticksSinceLastProgressUpdate >= getProgressUpdateFreq() || curScaled != lastProgressScaled) {
                sendTaskProgressPacket();
                lastProgressScaled = curScaled;
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
    Reader.read(StoreFor.SAVE, root, this);
  }

  @Override
  public final void writeToNBT(NBTTagCompound root) {
    super.writeToNBT(root);
    Writer.write(StoreFor.SAVE, root, this);
  }

  @Override
  public final Packet<?> getDescriptionPacket() {
    NBTTagCompound root = new NBTTagCompound();
    super.writeToNBT(root);
    Writer.write(StoreFor.CLIENT, root, this);
    return new S35PacketUpdateTileEntity(getPos(), 1, root);
  }

  @Override
  public final void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
    NBTTagCompound root = pkt.getNbtCompound();
    super.readFromNBT(root);
    Reader.read(StoreFor.CLIENT, root, this);
  }

  protected final void readItemNBT(NBTTagCompound root) {
    Reader.read(StoreFor.ITEM, root, this);
  }

  protected final void writeItemNBT(NBTTagCompound root) {
    Writer.write(StoreFor.ITEM, root, this);
  }

  public boolean canPlayerAccess(EntityPlayer player) {
    return !isInvalid() && player.getDistanceSqToCenter(getPos().add(0.5, 0.5, 0.5)) <= 64D;
  }

    protected void updateBlock() {
        if (worldObj != null) {
            worldObj.markBlockForUpdate(getPos());
        }
    }

    protected boolean isPoweredRedstone() {
        return worldObj.isBlockLoaded(getPos()) ? worldObj.getStrongPower(getPos()) > 0 : false;
    }

    /**
     * Called directly after the TE is constructed. This is the place to call non-final methods.
     *
     * Note: This will not be called when the TE is loaded from the save. Hook into the nbt methods for that.
     */
    public void init() {}

    /**
     * Call this with an interval (in ticks) to find out if the current tick is the one you want to do some work. This
     * is staggered so the work of different TEs is stretched out over time.
     *
     * @see #shouldDoWorkThisTick(int, int) If you need to offset work ticks
     */
    protected boolean shouldDoWorkThisTick(int interval) {
        return shouldDoWorkThisTick(interval, 0);
    }

    /**
     * Call this with an interval (in ticks) to find out if the current tick is the one you want to do some work. This
     * is staggered so the work of different TEs is stretched out over time.
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
     * Called server-side when a GhostSlot is changed. Check that the given slot
     * number really is a ghost slot before storing the given stack.
     * 
     * @param slot
     *          The slot number that was given to the ghost slot
     * @param stack
     *          The stack that should be placed, null to clear
     */
    public void setGhostSlotContents(int slot, ItemStack stack) {
    }
}
