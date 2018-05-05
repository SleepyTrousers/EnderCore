package com.enderio.core.common.handlers;

import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.util.NullHelper;
import com.enderio.core.common.util.stackable.Things;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBeetroot;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
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

    boolean init(@Nonnull String source);
  }

  public static class LegacyPlantInfo implements IPlantInfo {
    public String seed;
    public String block;
    public int meta = 7;
    public int resetMeta = 0;
    public boolean optional;

    private transient @Nonnull Things seedStack = new Things();
    private transient @Nonnull IBlockState grownState = Blocks.AIR.getDefaultState();
    private transient @Nonnull IBlockState resetState = Blocks.AIR.getDefaultState();

    public LegacyPlantInfo() { // for json de-serialization
    }

    public LegacyPlantInfo(String seed, String block, int meta, int resetMeta) {
      this.seed = seed;
      this.block = block;
      this.meta = meta;
      this.resetMeta = resetMeta;
    }

    @Override
    public boolean init(@Nonnull String source) {
      seedStack.add(seed);
      if (!seedStack.isValid()) {
        // some blocks and items share the same id but you cannot make an itemstack from the block.
        // if that is the case here, we can rescue this with a bit of bad code
        try {
          seedStack.add("item:" + seed);
        } catch (Exception e) {
        }
        if (!seedStack.isValid()) {
          if (optional) {
            return false;
          } else {
            throw new RuntimeException("invalid item specifier '" + seed + "' " + source);
          }
        }
      }
      String[] blockinfo = block.split(":");
      if (blockinfo.length != 2) {
        throw new RuntimeException("invalid block specifier '" + block + "' " + source);
      }
      Block mcblock = ForgeRegistries.BLOCKS
          .getValue(new ResourceLocation(NullHelper.notnullJ(blockinfo[0], "String.split()"), NullHelper.notnullJ(blockinfo[1], "String.split()")));
      if (mcblock == null) {
        if (optional) {
          return false;
        } else {
          throw new RuntimeException("invalid block specifier '" + block + "' " + source);
        }
      }
      if (mcblock instanceof BlockBeetroot) { // BlockBeetroot extends BlockCrops, so it needs to be checked first
        meta = 3;
        resetMeta = 0;
        grownState = mcblock.getDefaultState().withProperty(BlockBeetroot.BEETROOT_AGE, 3);
        resetState = mcblock.getDefaultState().withProperty(BlockBeetroot.BEETROOT_AGE, 0);
      } else if (mcblock instanceof BlockCrops) {
        meta = ((BlockCrops) mcblock).getMaxAge();
        resetMeta = 0;
        grownState = mcblock.getDefaultState().withProperty(BlockCrops.AGE, ((BlockCrops) mcblock).getMaxAge());
        resetState = mcblock.getDefaultState().withProperty(BlockCrops.AGE, 0);
      } else if (mcblock instanceof BlockNetherWart) {
        meta = 3;
        resetMeta = 0;
        grownState = mcblock.getDefaultState().withProperty(BlockNetherWart.AGE, 3);
        resetState = mcblock.getDefaultState().withProperty(BlockNetherWart.AGE, 0);
      } else {
        grownState = mcblock.getStateFromMeta(meta);
        resetState = mcblock.getStateFromMeta(resetMeta);
      }
      return true;
    }

    @Override
    @Nonnull
    public ItemStack getSeed() {
      return seedStack.getItemStack();
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

    if (event.getEntityPlayer().getHeldItemMainhand().isEmpty() || !event.getEntityPlayer().isSneaking()) {
      BlockPos pos = event.getPos();
      IBlockState blockState = event.getWorld().getBlockState(pos);
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
          stack.shrink(1);
          if (stack.isEmpty()) {
            event.getDrops().remove(i);
          }
          break;
        }
      }
    }
  }
}
