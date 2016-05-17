package com.enderio.core.common.handlers;

import java.util.List;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.util.ItemUtil;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
  public static class PlantInfo {
    public String seed;
    public String block;
    public int meta = 7;
    public int resetMeta = 0;

    private transient ItemStack seedStack;
    private transient Block blockInst;
    
    public PlantInfo() {
    }

    public PlantInfo(String seed, String block, int meta, int resetMeta) {
      this.seed = seed;
      this.block = block;
      this.meta = meta;
      this.resetMeta = resetMeta;
    }

    public void init() {
      seedStack = ItemUtil.parseStringIntoItemStack(seed);
      String[] blockinfo = block.split(":");      
      blockInst = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockinfo[0], blockinfo[1]));
    }
  }

  private List<PlantInfo> plants = Lists.newArrayList();

  private PlantInfo currentPlant = null;

  public static final RightClickCropHandler INSTANCE = new RightClickCropHandler();

  private RightClickCropHandler() {
  }

  public void addCrop(PlantInfo info) {
    plants.add(info);
  }

  @SubscribeEvent
  public void handleCropRightClick(RightClickBlock event) {
    BlockPos pos = event.getPos();
    if(pos == null) {
      return;
    }
    
    IBlockState blockState = event.getWorld().getBlockState(pos);
    Block block = blockState.getBlock();
    int meta = block.getMetaFromState(blockState);
    if (ConfigHandler.allowCropRC 
        && (event.getEntityPlayer().getHeldItemMainhand() == null || !event.getEntityPlayer().isSneaking())) {
      for (PlantInfo info : plants) {        
        if (info.blockInst == block && meta == info.meta) {
          if (event.getWorld().isRemote) {
            event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);            
          } else {
            currentPlant = info;
            block.dropBlockAsItem(event.getWorld(), pos, blockState, 0);
            currentPlant = null;
            IBlockState newBS = block.getStateFromMeta(info.resetMeta);
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
        if (stack.getItem() == currentPlant.seedStack.getItem()
            && (currentPlant.seedStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || stack.getItemDamage() == currentPlant.seedStack.getItemDamage())) {
          event.getDrops().remove(i);
          break;
        }
      }
    }
  }
}
