package com.enderio.core.common.handlers;

import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.util.ItemUtil;
import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

@Handler
public class RightClickCropHandler {

  public static interface IPlantInfo {
    @Nonnull
    ItemStack getSeed();

    @Nonnull
    IBlockState getGrownState();

    @Nonnull
    IBlockState getResetState();

    void init();
  }

  public static class LegacyPlantInfo implements IPlantInfo {
    public String seed;
    public String block;
    public int meta = 7;
    public int resetMeta = 0;

    private transient @Nonnull ItemStack seedStack = ItemStack.EMPTY;
    private transient @Nonnull IBlockState grownState = Blocks.AIR.getDefaultState();
    private transient @Nonnull IBlockState resetState = Blocks.AIR.getDefaultState();

    public LegacyPlantInfo(String seed, String block, int meta, int resetMeta) {
      this.seed = seed;
      this.block = block;
      this.meta = meta;
      this.resetMeta = resetMeta;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void init() {
      seedStack = ItemUtil.parseStringIntoItemStack(NullHelper.notnull(seed, "invalid item specifier received in IMC message from another mod"));
      String[] blockinfo = block.split(":");
      if (blockinfo.length != 2) {
        throw new RuntimeException("invalid block specifier '" + block + "' received in IMC message from another mod");
      }
      Block mcblock = ForgeRegistries.BLOCKS
          .getValue(new ResourceLocation(NullHelper.notnullJ(blockinfo[0], "String.split()"), NullHelper.notnullJ(blockinfo[1], "String.split()")));
      if (mcblock == null) {
        throw new RuntimeException("invalid block specifier '" + block + "' received in IMV message from another mod");
      }
      grownState = mcblock.getStateFromMeta(meta);
      resetState = mcblock.getStateFromMeta(resetMeta);
    }

    @Override
    @Nonnull
    public ItemStack getSeed() {
      return seedStack;
    }

    @Override
    @Nonnull
    public IBlockState getGrownState() {
      return grownState;
    }

    @Override
    @Nonnull
    public IBlockState getResetState() {
      return resetState;
    }
  }

  private List<IPlantInfo> plants = Lists.newArrayList();

  private IPlantInfo currentPlant = null;

  public static final RightClickCropHandler INSTANCE = new RightClickCropHandler();

  private RightClickCropHandler() {
  }

  public void addCrop(IPlantInfo info) {
    plants.add(info);
  }

  @SubscribeEvent
  public void handleCropRightClick(RightClickBlock event) {
    if (!ConfigHandler.allowCropRC) {
      return;
    }

    BlockPos pos = event.getPos();
    IBlockState blockState = event.getWorld().getBlockState(pos);
    if (event.getEntityPlayer().getHeldItemMainhand().isEmpty() || !event.getEntityPlayer().isSneaking()) {
      for (IPlantInfo info : plants) {
        if (info.getGrownState() == blockState) {
          if (event.getWorld().isRemote) {
            event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);
          } else {
            currentPlant = info;
            blockState.getBlock().dropBlockAsItem(NullHelper.notnullF(event.getWorld(), "RightClickBlock.getWorld()"), pos, blockState, 0);
            currentPlant = null;
            IBlockState newBS = info.getResetState();
            event.getWorld().setBlockState(pos, newBS, 3);
            event.setCanceled(true);
          }
          break;
        }
      }
    }
  }

  @SubscribeEvent
  public void onHarvestDrop(HarvestDropsEvent event) {
    if (currentPlant != null) {
      for (int i = 0; i < event.getDrops().size(); i++) {
        ItemStack stack = event.getDrops().get(i);
        if (stack.getItem() == currentPlant.getSeed().getItem()
            && (currentPlant.getSeed().getItemDamage() == OreDictionary.WILDCARD_VALUE || stack.getItemDamage() == currentPlant.getSeed().getItemDamage())) {
          event.getDrops().remove(i);
          break;
        }
      }
    }
  }
}
