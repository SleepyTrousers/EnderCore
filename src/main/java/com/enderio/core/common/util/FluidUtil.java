package com.enderio.core.common.util;

import java.util.HashMap;
import java.util.Map;

import com.enderio.core.api.common.util.ITankAccess;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.wrappers.FluidContainerItemWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidContainerRegistryWrapper;
import net.minecraftforge.fluids.capability.wrappers.FluidHandlerWrapper;

public class FluidUtil {

  @CapabilityInject(net.minecraftforge.fluids.capability.IFluidHandler.class)
  private static final Capability<net.minecraftforge.fluids.capability.IFluidHandler> FLUID_HANDLER = null;

  // TODO: 1.10 see if this is still needed once BC updates. Might work with
  // caps.
//  public static final List<IFluidReceptor> fluidReceptors = new ArrayList<IFluidReceptor>();
//
//  static {
//    try {
//      Class.forName("crazypants.util.BuildcraftUtil");
//    } catch (Exception e) {
//      if (Loader.isModLoaded("BuildCraft|Transport")) {
//        Log.warn("ItemUtil: Could not register Build Craft pipe handler. Fluid conduits will show connections to all Build Craft pipes.");
//      } //Don't log if BC isn't installed, but we still check in case another mod is using their API
//    }
//  }
  

  @SuppressWarnings("deprecation")
  public static IFluidHandler getFluidHandlerCapability(ICapabilityProvider provider, EnumFacing side) {
    if (provider.hasCapability(FLUID_HANDLER, side)) {
      return provider.getCapability(FLUID_HANDLER, side);
    }
    return getLegacyHandler(provider, side);
  }

  public static IFluidHandler getFluidHandlerCapability(ItemStack stack) {
    return getFluidHandlerCapability(stack, null);
  }

  @Deprecated
  private static IFluidHandler getLegacyHandler(ICapabilityProvider provider, EnumFacing side) {
    if (provider instanceof ItemStack) {
      ItemStack stack = (ItemStack) provider;
      if (stack.getItem() instanceof IFluidContainerItem) {        
        return new FluidContainerItemWrapper((IFluidContainerItem) stack.getItem(), stack);
      }      
      if(FluidContainerRegistry.isContainer(stack)) {
        return new FluidContainerRegistryWrapper(stack); 
      }
    }
    if (provider instanceof net.minecraftforge.fluids.IFluidHandler) {      
      return new FluidHandlerWrapper((net.minecraftforge.fluids.IFluidHandler) provider, side);
    }
    return null;
  }

  public static Map<EnumFacing, IFluidHandler> getNeighbouringFluidHandlers(World worldObj, BlockPos location) {
    Map<EnumFacing, IFluidHandler> res = new HashMap<EnumFacing, IFluidHandler>();
    for (EnumFacing dir : EnumFacing.VALUES) {
      IFluidHandler fh = getFluidHandler(worldObj, location.offset(dir), dir.getOpposite());
      if (fh != null) {
        res.put(dir, fh);
      }
    }
    return res;
  }

  private static IFluidHandler getFluidHandler(World worldObj, BlockPos location, EnumFacing side) {
    return getFluidHandlerCapability(worldObj.getTileEntity(location), side);
  }
  
  public static FluidStack getFluidTypeFromItem(ItemStack stack) {
    if (stack == null) {
      return null;
    }

    stack = stack.copy();
    stack.stackSize = 1;
    IFluidHandler handler = getFluidHandlerCapability(stack);
    if (handler != null) {
      return handler.drain(Fluid.BUCKET_VOLUME, false);
    }
    if (Block.getBlockFromItem(stack.getItem()) instanceof IFluidBlock) {
      Fluid fluid = ((IFluidBlock) Block.getBlockFromItem(stack.getItem())).getFluid();
      if (fluid != null) {
        return new FluidStack(fluid, 1000);
      }
    }
    return null;

  }

  public static boolean isFluidContainer(ItemStack stack) {
    return isFluidContainer(stack, null);
  }

  public static boolean isFluidContainer(ICapabilityProvider provider, EnumFacing side) {
    return getFluidHandlerCapability(provider, side) != null;
  }

  public static boolean hasEmptyCapacity(ItemStack stack) {
    net.minecraftforge.fluids.capability.IFluidHandler handler = getFluidHandlerCapability(stack);
    if (handler == null) {
      return false;
    }
    IFluidTankProperties[] props = handler.getTankProperties();
    if (props == null) {
      return false;
    }
    for (IFluidTankProperties tank : props) {
      int cap = tank.getCapacity();
      FluidStack contents = tank.getContents();
      if (cap >= 0 && (contents == null || contents.amount < cap)) {
        return true;
      }
    }
    return false;
  }

  public static FluidAndStackResult tryFillContainer(ItemStack target, FluidStack source) {
    if (target == null || target.getItem() == null || source == null || source.getFluid() == null || source.amount <= 0) {
      return new FluidAndStackResult(null, null, target, source);
    }

    ItemStack filledStack = target.copy();
    filledStack.stackSize = 1;
    net.minecraftforge.fluids.capability.IFluidHandler handler = getFluidHandlerCapability(filledStack);
    if (handler == null) {
      return new FluidAndStackResult(null, null, target, source);
    }

    int filledAmount = handler.fill(source.copy(), true);
    if (filledAmount <= 0) {
      return new FluidAndStackResult(null, null, target, source);
    }
    FluidStack resultFluid = source.copy();
    resultFluid.amount = filledAmount;

    ItemStack remainderStack = target.copy();
    remainderStack.stackSize--;
    if (remainderStack.stackSize <= 0) {
      remainderStack = null;
    }
    FluidStack remainderFluid = source.copy();
    remainderFluid.amount -= filledAmount;
    if (remainderFluid.amount <= 0) {
      remainderFluid = null;
    }
    return new FluidAndStackResult(filledStack, resultFluid, remainderStack, remainderFluid);

  }

  public static FluidAndStackResult tryDrainContainer(ItemStack source, FluidStack target, int capacity) {
    if (source == null || source.getItem() == null) {
      return new FluidAndStackResult(null, null, source, target);
    }

    ItemStack emptiedStack = source.copy();
    emptiedStack.stackSize = 1;
    net.minecraftforge.fluids.capability.IFluidHandler handler = getFluidHandlerCapability(emptiedStack);
    if (handler == null) {
      return new FluidAndStackResult(null, null, target, source);
    }

    int maxDrain = capacity - (target != null ? target.amount : 0);
    FluidStack drained;
    if (target != null) {
      FluidStack available = target.copy();
      available.amount = maxDrain;
      drained = handler.drain(available, true);
    } else {
      drained = handler.drain(maxDrain, true);
    }

    if (drained == null || drained.amount <= 0) {
      return new FluidAndStackResult(null, null, source, target);
    }

    ItemStack remainderStack = source.copy();
    remainderStack.stackSize--;
    if (remainderStack.stackSize <= 0) {
      remainderStack = null;
    }
    FluidStack remainderFluid = target != null ? target.copy() : new FluidStack(drained, 0);
    remainderFluid.amount += drained.amount;

    if (emptiedStack.stackSize <= 0) {
      emptiedStack = null;
    }
    return new FluidAndStackResult(emptiedStack, drained, remainderStack, remainderFluid);
  }

  public static FluidAndStackResult tryDrainContainer(ItemStack source, ITankAccess tank) {
    FluidAndStackResult result = new FluidAndStackResult(null, null, null, source);
    if (source == null || source.getItem() == null || tank == null) {
      return result;
    }

    ItemStack emptiedStack = source.copy();
    emptiedStack.stackSize = 1;
    net.minecraftforge.fluids.capability.IFluidHandler handler = getFluidHandlerCapability(emptiedStack);
    if (handler == null) {
      return result;
    }
    FluidStack contentType = getFluidTypeFromItem(source);
    if (contentType == null) {
      return result;
    }
    FluidTank targetTank = tank.getInputTank(contentType);
    if (targetTank == null) {
      return result;
    }

    return tryDrainContainer(source, targetTank.getFluid(), targetTank.getCapacity());
  }

  public static boolean fillPlayerHandItemFromInternalTank(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, ITankAccess tank) {
    return fillPlayerHandItemFromInternalTank(world, pos.getX(), pos.getY(), pos.getZ(), entityPlayer, hand, tank);
  }

  /**
   * If the currently held item of the given player can be filled with the
   * liquid in the given tank's output tank, do so and put the resultant filled
   * container item where it can go. This will also drain the tank and set it to
   * dirty.
   * 
   * <p>
   * Cases handled for the the filled container:
   * 
   * <ul>
   * <li>If the stacksize of the held item is one, then it will be replaced by
   * the filled container unless the player in in creative.
   * <li>If the filled container is stackable and the player already has a
   * non-maxed stack in the inventory, it is put there.
   * <li>If the player has space in his inventory, it is put there.
   * <li>Otherwise it will be dropped on the ground between the position given
   * as parameter and the player's position.
   * </ul>
   * 
   * @param world
   * @param x
   * @param y
   * @param z
   * @param entityPlayer
   * @param hand
   * @param tank
   * @return true if a container was filled, false otherwise
   */
  public static boolean fillPlayerHandItemFromInternalTank(World world, int x, int y, int z, EntityPlayer entityPlayer, EnumHand hand, ITankAccess tank) {

    for (FluidTank subTank : tank.getOutputTanks()) {
      FluidAndStackResult fill = tryFillContainer(entityPlayer.getHeldItem(hand), subTank.getFluid());
      if (fill.result.fluidStack != null) {

        subTank.setFluid(fill.remainder.fluidStack);
        tank.setTanksDirty();
        if (!entityPlayer.capabilities.isCreativeMode) {
          if (fill.remainder.itemStack == null) {
            // entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem,
            // fill.result.itemStack);
            entityPlayer.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, fill.result.itemStack);
            return true;
          } else {
            // entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem,
            // fill.remainder.itemStack);
            entityPlayer.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, fill.remainder.itemStack);
          }

          if (fill.result.itemStack.isStackable()) {
            for (int i = 0; i < entityPlayer.inventory.mainInventory.length; i++) {
              ItemStack inventoryItem = entityPlayer.inventory.mainInventory[i];
              if (ItemUtil.areStackMergable(inventoryItem, fill.result.itemStack) && inventoryItem.stackSize < inventoryItem.getMaxStackSize()) {
                fill.result.itemStack.stackSize += inventoryItem.stackSize;
                entityPlayer.inventory.setInventorySlotContents(i, fill.result.itemStack);
                return true;
              }
            }
          }

          for (int i = 0; i < entityPlayer.inventory.mainInventory.length; i++) {
            if (entityPlayer.inventory.mainInventory[i] == null) {
              entityPlayer.inventory.setInventorySlotContents(i, fill.result.itemStack);
              return true;
            }
          }

          if (!world.isRemote) {
            double x0 = (x + entityPlayer.posX) / 2.0D;
            double y0 = (y + entityPlayer.posY) / 2.0D + 0.5D;
            double z0 = (z + entityPlayer.posZ) / 2.0D;
            Util.dropItems(world, fill.result.itemStack, x0, y0, z0, true);
          }
        }

        return true;
      }
    }
    return false;
  }

  public static boolean fillInternalTankFromPlayerHandItem(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, ITankAccess tank) {
    return fillInternalTankFromPlayerHandItem(world, pos.getX(), pos.getY(), pos.getZ(), entityPlayer, hand, tank);
  }

  public static boolean fillInternalTankFromPlayerHandItem(World world, int x, int y, int z, EntityPlayer entityPlayer, EnumHand hand, ITankAccess tank) {
    FluidAndStackResult fill = tryDrainContainer(entityPlayer.getHeldItem(hand), tank);
    if (fill.result.fluidStack == null) {
      return false;
    }

    tank.getInputTank(fill.result.fluidStack).setFluid(fill.remainder.fluidStack);
    tank.setTanksDirty();

    if (!entityPlayer.capabilities.isCreativeMode) {
      if (fill.remainder.itemStack == null) {
        entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, fill.result.itemStack);
        return true;
      } else {
        entityPlayer.inventory.setInventorySlotContents(entityPlayer.inventory.currentItem, fill.remainder.itemStack);
      }

      if (fill.result.itemStack == null) {
        return true;
      }

      if (fill.result.itemStack.isStackable()) {
        for (int i = 0; i < entityPlayer.inventory.mainInventory.length; i++) {
          ItemStack inventoryItem = entityPlayer.inventory.mainInventory[i];
          if (ItemUtil.areStackMergable(inventoryItem, fill.result.itemStack) && inventoryItem.stackSize < inventoryItem.getMaxStackSize()) {
            fill.result.itemStack.stackSize += inventoryItem.stackSize;
            entityPlayer.inventory.setInventorySlotContents(i, fill.result.itemStack);
            return true;
          }
        }
      }

      for (int i = 0; i < entityPlayer.inventory.mainInventory.length; i++) {
        if (entityPlayer.inventory.mainInventory[i] == null) {
          entityPlayer.inventory.setInventorySlotContents(i, fill.result.itemStack);
          return true;
        }
      }

      if (!world.isRemote) {
        double x0 = (x + entityPlayer.posX) / 2.0D;
        double y0 = (y + entityPlayer.posY) / 2.0D + 0.5D;
        double z0 = (z + entityPlayer.posZ) / 2.0D;
        Util.dropItems(world, fill.result.itemStack, x0, y0, z0, true);
      }
    }

    return true;
  }

  public static class FluidAndStack {
    public final FluidStack fluidStack;
    public final ItemStack itemStack;

    public FluidAndStack(FluidStack fluidStack, ItemStack itemStack) {
      this.fluidStack = fluidStack;
      this.itemStack = itemStack;
    }

    public FluidAndStack(ItemStack itemStack, FluidStack fluidStack) {
      this.fluidStack = fluidStack;
      this.itemStack = itemStack;
    }
  }

  public static class FluidAndStackResult {
    public final FluidAndStack result;
    public final FluidAndStack remainder;

    public FluidAndStackResult(FluidAndStack result, FluidAndStack remainder) {
      this.result = result;
      this.remainder = remainder;
    }

    public FluidAndStackResult(FluidStack fluidStackResult, ItemStack itemStackResult, FluidStack fluidStackRemainder, ItemStack itemStackRemainder) {
      this.result = new FluidAndStack(fluidStackResult, itemStackResult);
      this.remainder = new FluidAndStack(fluidStackRemainder, itemStackRemainder);
    }

    public FluidAndStackResult(ItemStack itemStackResult, FluidStack fluidStackResult, ItemStack itemStackRemainder, FluidStack fluidStackRemainder) {
      this.result = new FluidAndStack(fluidStackResult, itemStackResult);
      this.remainder = new FluidAndStack(fluidStackRemainder, itemStackRemainder);
    }
  }

  public static boolean areFluidsTheSame(Fluid fluid, Fluid fluid2) {
    if (fluid == null) {
      return fluid2 == null;
    }
    if (fluid2 == null) {
      return false;
    }
    return fluid == fluid2 || fluid.getName().equals(fluid2.getName());
  }

}
