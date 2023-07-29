package com.enderio.core.common.fluid;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NullHelper;

import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockFluidEnder extends BlockFluidClassic {

  private float fogColorRed = 1f;
  private float fogColorGreen = 1f;
  private float fogColorBlue = 1f;
  private final @Nonnull Material fluidMaterial;

  protected BlockFluidEnder(@Nonnull Fluid fluid, @Nonnull Material fluidMaterial, int fogColor) {
    super(fluid, new MaterialLiquid(fluidMaterial.getMaterialMapColor()) {
      // new Material for each liquid so neighboring different liquids render correctly and don't bleed into each other
      @Override
      public boolean blocksMovement() {
        return true; // so our liquids are not replaced by water
      }
    });
    this.fluidMaterial = fluidMaterial;

    // darken fog color to fit the fog rendering
    float dim = 1;
    while (getFogColorRed() > .2f || getFogColorGreen() > .2f || getFogColorBlue() > .2f) {
      setFogColorRed((fogColor >> 16 & 255) / 255f * dim);
      setFogColorGreen((fogColor >> 8 & 255) / 255f * dim);
      setFogColorBlue((fogColor & 255) / 255f * dim);
      dim *= .9f;
    }

    setNames(fluid);
  }

  protected void setNames(Fluid fluid) {
    setTranslationKey(NullHelper.notnullF(fluid.getUnlocalizedName(), "encountered fluid without a name"));
    setRegistryName("block_fluid_" + fluid.getName().toLowerCase(Locale.ENGLISH));
  }

  public float getFogColorRed() {
    return fogColorRed;
  }

  public void setFogColorRed(float fogColorRed) {
    this.fogColorRed = fogColorRed;
  }

  public float getFogColorGreen() {
    return fogColorGreen;
  }

  public void setFogColorGreen(float fogColorGreen) {
    this.fogColorGreen = fogColorGreen;
  }

  public float getFogColorBlue() {
    return fogColorBlue;
  }

  public void setFogColorBlue(float fogColorBlue) {
    this.fogColorBlue = fogColorBlue;
  }

  @Override
  public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos blockpos, @Nonnull IBlockState iblockstate, @Nonnull Entity entity,
      double yToTest, @Nonnull Material materialIn, boolean testingHead) {
    if (materialIn == fluidMaterial || materialIn == this.material) {
      return Boolean.TRUE;
    }
    return super.isEntityInsideMaterial(world, blockpos, iblockstate, entity, yToTest, materialIn, testingHead);
  }

  @Override
  public boolean canDisplace(IBlockAccess world, BlockPos pos) {
    IBlockState bs = NullHelper.notnullF(world, "canDisplace() called without world")
        .getBlockState(NullHelper.notnullF(pos, "canDisplace() called without pos"));
    if (bs.getMaterial().isLiquid()) {
      return false;
    }
    return super.canDisplace(world, pos);
  }

  @Override
  public boolean displaceIfPossible(World world, BlockPos pos) {
    IBlockState bs = NullHelper.notnullF(world, "displaceIfPossible() called without world")
        .getBlockState(NullHelper.notnullF(pos, "displaceIfPossible() called without pos"));
    if (bs.getMaterial().isLiquid()) {
      return false;
    }
    return super.displaceIfPossible(world, pos);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(@Nullable CreativeTabs tab, @Nonnull NonNullList<ItemStack> list) {
    if (tab != null) {
      super.getSubBlocks(tab, list);
    }
  }

}
