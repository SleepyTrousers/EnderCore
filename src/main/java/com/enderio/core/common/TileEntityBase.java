package com.enderio.core.common;

import javax.annotation.Nonnull;

import com.enderio.core.api.common.util.IProgressTile;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.network.EnderPacketHandler;
import com.enderio.core.common.network.PacketProgress;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public abstract class TileEntityBase extends TileEntity implements ITickable {

  private final int checkOffset = (int) (Math.random() * 20);
  protected final boolean isProgressTile;

  protected float lastProgressSent = -1;
  protected long lastProgressUpdate;
  private long lastUpdate = 0;

  public TileEntityBase() {
    isProgressTile = this instanceof IProgressTile;
  }

  @Override
  public final void update() {
    // Note: Commented out checks are done in World for 1.12
    if (/* !hasWorld() || isInvalid() || !world.isBlockLoaded(getPos()) || */ world.getTileEntity(getPos()) != this
        || world.getBlockState(pos).getBlock() != getBlockType()) {
      // we can get ticked after being removed from the world, ignore this
      return;
    }
    if (ConfigHandler.allowExternalTickSpeedup || world.getTotalWorldTime() != lastUpdate) {
      lastUpdate = world.getTotalWorldTime();
      doUpdate();
      sendProgressIf();
    }
  }

  public static int getProgressScaled(int scale, @Nonnull IProgressTile tile) {
    return (int) (tile.getProgress() * scale);
  }

  private final void sendProgressIf() {
    // this is only used for players that do not have the GUI open. They do not need a very fine resolution, as they only see the the machine being on or
    // off and get the sound restarted on progress==0
    if (isProgressTile && !world.isRemote) {
      float progress = ((IProgressTile) this).getProgress();
      boolean send = //
          progress < lastProgressSent // always send progress if it goes down, e.g. machine goes inactive or new task starts
              || (lastProgressSent <= 0 && progress > 0) // always send progress if machine goes active
              || (lastUpdate - lastProgressUpdate) > 60 * 20; // also update every 60 seconds to avoid stale client status

      if (send) {
        EnderPacketHandler.INSTANCE.sendToAllAround(((IProgressTile) this).getProgressPacket(), this);
        lastProgressSent = progress;
        lastProgressUpdate = lastUpdate;
      }
    }
  }

  protected void doUpdate() {

  }

  public @Nonnull IMessage getProgressPacket() {
    return new PacketProgress((IProgressTile) this);
  }

  /**
   * SERVER: Called when being written to the save file.
   */
  @Override
  public final @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound root) {
    super.writeToNBT(root);
    writeCustomNBT(NBTAction.SAVE, root);
    return root;
  }

  /**
   * SERVER: Called when being read from the save file.
   */
  @Override
  public final void readFromNBT(@Nonnull NBTTagCompound tag) {
    super.readFromNBT(tag);
    readCustomNBT(NBTAction.SAVE, tag);
  }

  /**
   * Called when the chunk data is sent (client receiving chunks from server). Must have x/y/z tags.
   */
  @Override
  public final @Nonnull NBTTagCompound getUpdateTag() {
    NBTTagCompound tag = super.getUpdateTag();
    writeCustomNBT(NBTAction.CLIENT, tag);
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      tag.setFloat("tileprogress", ((IProgressTile) this).getProgress());
    }
    return tag;
  }

  /**
   * CLIENT: Called when chunk data is received (client receiving chunks from server).
   */
  @Override
  public final void handleUpdateTag(@Nonnull NBTTagCompound tag) {
    super.handleUpdateTag(tag);
    readCustomNBT(NBTAction.CLIENT, tag);
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      ((IProgressTile) this).setProgress(tag.getFloat("tileprogress"));
    }
  }

  /**
   * SERVER: Called when block data is sent (client receiving blocks from server, via notifyBlockUpdate). No need for x/y/z tags.
   */
  @Override
  public final SPacketUpdateTileEntity getUpdatePacket() {
    NBTTagCompound tag = new NBTTagCompound();
    writeCustomNBT(NBTAction.CLIENT, tag);
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      tag.setFloat("tileprogress", ((IProgressTile) this).getProgress());
    }
    return new SPacketUpdateTileEntity(getPos(), 1, tag);
  }

  /**
   * CLIENT: Called when block data is received (client receiving blocks from server, via notifyBlockUpdate).
   */
  @Override
  public final void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
    readCustomNBT(NBTAction.CLIENT, pkt.getNbtCompound());
    if (isProgressTile) {
      // TODO: nicer way to do this? This is needed so players who enter a chunk get a correct progress.
      ((IProgressTile) this).setProgress(pkt.getNbtCompound().getFloat("tileprogress"));
    }
  }

  protected void writeCustomNBT(@Nonnull ItemStack stack) {
    final NBTTagCompound tag = new NBTTagCompound();
    writeCustomNBT(NBTAction.ITEM, tag);
    if (!tag.isEmpty()) {
      stack.setTagCompound(tag);
    }
  }

  @Deprecated
  protected abstract void writeCustomNBT(@Nonnull NBTAction action, @Nonnull NBTTagCompound root);

  protected void readCustomNBT(@Nonnull ItemStack stack) {
    if (stack.hasTagCompound()) {
      readCustomNBT(NBTAction.ITEM, NullHelper.notnullM(stack.getTagCompound(), "tag compound vanished"));
    }
  }

  @Deprecated
  protected abstract void readCustomNBT(@Nonnull NBTAction action, @Nonnull NBTTagCompound root);

  public boolean canPlayerAccess(EntityPlayer player) {
    return hasWorld() && !isInvalid() && player.getDistanceSqToCenter(getPos()) <= 64D;
  }

  protected void updateBlock() {
    if (hasWorld() && world.isBlockLoaded(getPos())) {
      IBlockState bs = world.getBlockState(getPos());
      world.notifyBlockUpdate(pos, bs, bs, 3);
    }
  }

  protected boolean isPoweredRedstone() {
    return hasWorld() && world.isBlockLoaded(getPos()) ? world.getRedstonePowerFromNeighbors(getPos()) > 0 : false;
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
    return (world.getTotalWorldTime() + checkOffset + offset) % interval == 0;
  }

  @Override
  public boolean shouldRefresh(@Nonnull World worldIn, @Nonnull BlockPos posIn, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
    return oldState.getBlock() != newState.getBlock();
  }

  /**
   * Called server-side when a GhostSlot is changed. Check that the given slot number really is a ghost slot before storing the given stack.
   *
   * @param slot
   *          The slot number that was given to the ghost slot
   * @param stack
   *          The stack that should be placed, null to clear
   * @param realsize
   */
  public void setGhostSlotContents(int slot, @Nonnull ItemStack stack, int realsize) {
  }

  @Override
  public void markDirty() {
    if (hasWorld() && world.isBlockLoaded(getPos())) { // we need the loaded check to make sure we don't trigger a chunk load while the chunk is loaded
      world.markChunkDirty(pos, this);
      IBlockState state = world.getBlockState(pos);
      if (state.hasComparatorInputOverride()) {
        world.updateComparatorOutputLevel(pos, state.getBlock());
      }
    }
  }

  /**
   * Sends an update packet to all players who have this TileEntity loaded. Needed because inventory changes are not synced in a timely manner unless the player
   * has the GUI open. And sometimes the rendering needs the inventory...
   */
  public void forceUpdatePlayers() {
    if (!(world instanceof WorldServer)) {
      return;
    }

    WorldServer worldServer = (WorldServer) world;
    PlayerChunkMap playerManager = worldServer.getPlayerChunkMap();
    SPacketUpdateTileEntity updatePacket = null;

    int chunkX = pos.getX() >> 4;
    int chunkZ = pos.getZ() >> 4;

    for (EntityPlayer playerObj : world.playerEntities) {
      if (playerObj instanceof EntityPlayerMP) {
        EntityPlayerMP player = (EntityPlayerMP) playerObj;

        if (playerManager.isPlayerWatchingChunk(player, chunkX, chunkZ)) {
          if (updatePacket == null) {
            updatePacket = getUpdatePacket();
            if (updatePacket == null) {
              return;
            }
          }
          try {
            player.connection.sendPacket(updatePacket);
          } catch (Exception e) {
          }
        }
      }
    }
  }

  @Override
  protected void setWorldCreate(@Nonnull World worldIn) {
    // Forge gives us our World earlier than vanilla. No idea why it doesn't get put into #world but is ignored by default.
    // Anyway, this is helpful while reading our nbt, so let's use it.
    setWorld(worldIn);
  }

}
