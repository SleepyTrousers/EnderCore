package com.enderio.core.common.util;

import java.util.EnumMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.util.NNList.Callback;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class FluidUtil {

  @CapabilityInject(IFluidHandler.class)
  private static final Capability<IFluidHandler> FLUID_HANDLER = null;
  
  @CapabilityInject(IFluidHandlerItem.class)
  private static final Capability<IFluidHandlerItem> FLUID_ITEM_HANDLER = null;

  // TODO: Mod BC see if this is still needed once BC updates. Might work with
  // caps.
  // public static final List<IFluidReceptor> fluidReceptors = new ArrayList<IFluidReceptor>();
  //
  // static {
  // try {
  // Class.forName("crazypants.util.BuildcraftUtil");
  // } catch (Exception e) {
  // if (Loader.isModLoaded("BuildCraft|Transport")) {
  // Log.warn("ItemUtil: Could not register Build Craft pipe handler. Fluid conduits will show connections to all Build Craft pipes.");
  // } //Don't log if BC isn't installed, but we still check in case another mod is using their API
  // }
  // }

  // TODO: Add canFill and canDrain helpers?

  public static @Nonnull Capability<IFluidHandler> getFluidCapability() {
    return NullHelper.notnullF(FLUID_HANDLER, "IFluidHandler capability is missing");
  }
  
  public static @Nonnull Capability<IFluidHandlerItem> getFluidItemCapability() {
    return NullHelper.notnullF(FLUID_ITEM_HANDLER, "IFluidHandlerItem capability is missing");
  }

  public static IFluidHandler getFluidHandlerCapability(@Nullable ICapabilityProvider provider, @Nullable Direction side) {
    if (provider != null) {
      return provider.getCapability(getFluidCapability(), side).resolve().orElse(null);
    }
    return null;
  }

  public static IFluidHandlerItem getFluidHandlerCapability(@Nonnull ItemStack stack) {
    return stack.getCapability(getFluidItemCapability()).resolve().orElse(null);
  }

  public static EnumMap<Direction, IFluidHandler> getNeighbouringFluidHandlers(@Nonnull final World worldObj, @Nonnull final BlockPos location) {
    final EnumMap<Direction, IFluidHandler> res = new EnumMap<Direction, IFluidHandler>(Direction.class);
    NNList.FACING.apply(dir -> {
      IFluidHandler fh = getFluidHandler(worldObj, location.offset(dir), dir.getOpposite());
      res.put(dir, fh);
    });
    return res;
  }

  static IFluidHandler getFluidHandler(@Nonnull World worldObj, @Nonnull BlockPos location, @Nullable Direction side) {
    return getFluidHandlerCapability(worldObj.getTileEntity(location), side);
  }

  public static FluidStack getFluidTypeFromItem(@Nonnull ItemStack stack) {
    if (stack.isEmpty()) {
      return null;
    }

    ItemStack copy = stack.copy();
    copy.setCount(1);
    IFluidHandlerItem handler = getFluidHandlerCapability(copy);
    if (handler != null) {
      return handler.drain(FluidAttributes.BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
    }
    if (Block.getBlockFromItem(copy.getItem()) instanceof IFluidBlock) {
      Fluid fluid = ((IFluidBlock) Block.getBlockFromItem(copy.getItem())).getFluid();
      if (fluid != null) {
        return new FluidStack(fluid, 1000);
      }
    }
    return null;
  }

  public static boolean isFluidContainer(@Nonnull ItemStack stack) {
    return getFluidHandlerCapability(stack) != null;
  }

  public static boolean isFluidContainer(@Nonnull ICapabilityProvider provider, @Nullable Direction side) {
    if (provider instanceof ItemStack) {
      Log.warn("isFluidContainer(ICapabilityProvider, EnumFacing) is not for ItemStacks");
      return isFluidContainer((ItemStack) provider);
    }
    return getFluidHandlerCapability(provider, side) != null;
  }

  public static boolean hasEmptyCapacity(@Nonnull ItemStack stack) {
    IFluidHandlerItem handler = getFluidHandlerCapability(stack);
    if (handler == null) {
      return false;
    }
    int tanks = handler.getTanks();
    for (int i = 0; i < tanks; i++) {
      int cap = handler.getTankCapacity(i);
      FluidStack contents = handler.getFluidInTank(i);
      if (cap >= 0 && (contents == null || contents.getAmount() < cap)) {
        return true;
      }
    }
    return false;
  }

  public static @Nonnull FluidAndStackResult tryFillContainer(@Nonnull ItemStack target, @Nullable FluidStack source) {
    if (target.isEmpty() || source == null || source.getFluid() == null || source.getAmount() <= 0) {
      return new FluidAndStackResult(ItemStack.EMPTY, null, target, source);
    }

    ItemStack filledStack = target.copy();
    filledStack.setCount(1);
    IFluidHandlerItem handler = getFluidHandlerCapability(filledStack);
    if (handler == null) {
      return new FluidAndStackResult(ItemStack.EMPTY, null, target, source);
    }

    int filledAmount = handler.fill(source.copy(), IFluidHandler.FluidAction.EXECUTE);
    if (filledAmount <= 0 || filledAmount > source.getAmount()) {
      return new FluidAndStackResult(ItemStack.EMPTY, null, target, source);
    }

    filledStack = handler.getContainer();

    FluidStack resultFluid = source.copy();
    resultFluid.setAmount(filledAmount);

    ItemStack remainderStack = target.copy();
    remainderStack.shrink(1);

    FluidStack remainderFluid = source.copy();
    remainderFluid.setAmount(remainderFluid.getAmount() - filledAmount);
    if (remainderFluid.getAmount() <= 0) {
      remainderFluid = null;
    }

    return new FluidAndStackResult(filledStack, resultFluid, remainderStack, remainderFluid);

  }

  public static @Nonnull FluidAndStackResult tryDrainContainer(@Nonnull ItemStack source, @Nullable FluidStack target, int capacity) {
    if (source.isEmpty()) {
      return new FluidAndStackResult(ItemStack.EMPTY, null, source, target);
    }

    ItemStack emptiedStack = source.copy();
    emptiedStack.setCount(1);
    IFluidHandlerItem handler = getFluidHandlerCapability(emptiedStack);
    if (handler == null) {
      return new FluidAndStackResult(null, ItemStack.EMPTY, target, source);
    }

    int maxDrain = capacity - (target != null ? target.getAmount() : 0);
    FluidStack drained;
    if (target != null) {
      FluidStack available = target.copy();
      available.setAmount(maxDrain);
      drained = handler.drain(available, IFluidHandler.FluidAction.EXECUTE);
    } else {
      drained = handler.drain(maxDrain, IFluidHandler.FluidAction.EXECUTE);
    }

    if (drained == null || drained.getAmount() <= 0 || drained.getAmount() > maxDrain) {
      return new FluidAndStackResult(ItemStack.EMPTY, null, source, target);
    }

    emptiedStack = handler.getContainer();

    ItemStack remainderStack = source.copy();
    remainderStack.shrink(1);

    FluidStack remainderFluid = target != null ? target.copy() : new FluidStack(drained, 0);
    remainderFluid.setAmount(remainderFluid.getAmount() + drained.getAmount());

    return new FluidAndStackResult(emptiedStack, drained, remainderStack, remainderFluid);
  }

  public static @Nonnull FluidAndStackResult tryDrainContainer(@Nonnull ItemStack source, @Nonnull ITankAccess tank) {
    FluidAndStackResult result = new FluidAndStackResult(null, ItemStack.EMPTY, null, source);

    if (source.isEmpty()) {
      return result;
    }
    IFluidHandlerItem handler = getFluidHandlerCapability(source);
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

  /**
   * If the currently held item of the given player can be filled with the liquid in the given tank's output tank, do so and put the resultant filled container
   * item where it can go. This will also drain the tank and set it to dirty.
   *
   * <p>
   * Cases handled for the the filled container:
   *
   * <ul>
   * <li>If the stacksize of the held item is one, then it will be replaced by the filled container unless the player in in creative.
   * <li>If the filled container is stackable and the player already has a non-maxed stack in the inventory, it is put there.
   * <li>If the player has space in his inventory, it is put there.
   * <li>Otherwise it will be dropped on the ground between the position given as parameter and the player's position.
   * </ul>
   *
   * @param world
   * @param pos
   * @param entityPlayer
   * @param hand
   * @param tank
   * @return true if a container was filled, false otherwise
   */
  public static boolean fillPlayerHandItemFromInternalTank(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity entityPlayer,
                                                           @Nonnull Hand hand, @Nonnull ITankAccess tank) {

    ItemStack heldItem = entityPlayer.getHeldItem(hand);
    boolean doFill = !(entityPlayer.isCreative() && heldItem.getItem() == Items.BUCKET);

    for (FluidTank subTank : tank.getOutputTanks()) {
      FluidAndStackResult fill = tryFillContainer(entityPlayer.getHeldItem(hand), subTank.getFluid());
      if (fill.result.fluidStack != null) {

        subTank.setFluid(fill.remainder.fluidStack);
        tank.setTanksDirty();
        if (doFill) {
          if (fill.remainder.itemStack.isEmpty()) {
            // The input item was used up. We can simply replace it with the output item and be done.
            entityPlayer.setHeldItem(hand, fill.result.itemStack);
            return true;
          } else {
            entityPlayer.setHeldItem(hand, fill.remainder.itemStack);
            if (fill.result.itemStack.isEmpty()) {
              // We got no result item. Weird case, but whatever... (maybe an item that takes liquid XP and puts it into the player's XP bar and is used up?)
              return true;
            }
          }

          if (entityPlayer.inventory.addItemStackToInventory(fill.result.itemStack)) {
            // will change the itemstack's count on partial inserts
            return true;
          }

          if (!world.isRemote) {
            double x0 = (pos.getX() + 0.5D + entityPlayer.getPosX()) / 2.0D;
            double y0 = (pos.getY() + 0.5D + entityPlayer.getPosY() + 0.5D) / 2.0D;
            double z0 = (pos.getZ() + 0.5D + entityPlayer.getPosZ()) / 2.0D;
            Util.dropItems(world, fill.result.itemStack, x0, y0, z0, true);
          }
        }

        return true;
      }
    }
    return false;
  }

  public static boolean fillInternalTankFromPlayerHandItem(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity entityPlayer,
      @Nonnull Hand hand, @Nonnull ITankAccess tank) {
    FluidAndStackResult fill = tryDrainContainer(entityPlayer.getHeldItem(hand), tank);
    if (fill.result.fluidStack == null) {
      return false;
    }

    final FluidTank inputTank = tank.getInputTank(fill.result.fluidStack);
    if (inputTank == null) {
      return false;
    }
    inputTank.setFluid(fill.remainder.fluidStack);
    tank.setTanksDirty();

    if (!entityPlayer.isCreative()) {
      if (fill.remainder.itemStack.isEmpty()) {
        entityPlayer.setHeldItem(hand, fill.result.itemStack);
        return true;
      } else {
        entityPlayer.setHeldItem(hand, fill.remainder.itemStack);
      }

      if (fill.result.itemStack.isEmpty()) {
        return true;
      }

      if (entityPlayer.inventory.addItemStackToInventory(fill.result.itemStack)) {
        // will change the itemstack's count on partial inserts
        return true;
      }

      if (!world.isRemote) {
        double x0 = (pos.getX() + 0.5D + entityPlayer.getPosX()) / 2.0D;
        double y0 = (pos.getY() + 0.5D + entityPlayer.getPosY() + 0.5D) / 2.0D;
        double z0 = (pos.getZ() + 0.5D + entityPlayer.getPosZ()) / 2.0D;
        Util.dropItems(world, fill.result.itemStack, x0, y0, z0, true);
      }
    }

    return true;
  }

  public static class FluidAndStack {
    public final @Nullable FluidStack fluidStack;
    public final @Nonnull ItemStack itemStack;

    public FluidAndStack(@Nullable FluidStack fluidStack, @Nonnull ItemStack itemStack) {
      this.fluidStack = fluidStack;
      this.itemStack = itemStack;
    }

    public FluidAndStack(@Nonnull ItemStack itemStack, @Nullable FluidStack fluidStack) {
      this.fluidStack = fluidStack;
      this.itemStack = itemStack;
    }
  }

  public static class FluidAndStackResult {
    public final @Nonnull FluidAndStack result;
    public final @Nonnull FluidAndStack remainder;

    public FluidAndStackResult(@Nonnull FluidAndStack result, @Nonnull FluidAndStack remainder) {
      this.result = result;
      this.remainder = remainder;
    }

    public FluidAndStackResult(FluidStack fluidStackResult, @Nonnull ItemStack itemStackResult, FluidStack fluidStackRemainder,
        @Nonnull ItemStack itemStackRemainder) {
      this.result = new FluidAndStack(fluidStackResult, itemStackResult);
      this.remainder = new FluidAndStack(fluidStackRemainder, itemStackRemainder);
    }

    public FluidAndStackResult(@Nonnull ItemStack itemStackResult, FluidStack fluidStackResult, @Nonnull ItemStack itemStackRemainder,
        FluidStack fluidStackRemainder) {
      this.result = new FluidAndStack(fluidStackResult, itemStackResult);
      this.remainder = new FluidAndStack(fluidStackRemainder, itemStackRemainder);
    }
  }

  public static boolean areFluidsTheSame(@Nullable Fluid fluid, @Nullable Fluid fluid2) {
    if (fluid == null) {
      return fluid2 == null;
    }
    if (fluid2 == null) {
      return false;
    }
    return fluid == fluid2 || fluid.getAttributes().getTranslationKey().equals(fluid2.getAttributes().getTranslationKey());
  }

}
