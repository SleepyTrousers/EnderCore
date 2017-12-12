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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockFluidEnder extends BlockFluidClassic {

  private float fogColorRed = 1f;
  private float fogColorGreen = 1f;
  private float fogColorBlue = 1f;

  protected BlockFluidEnder(Fluid fluid, Material material, int fogColor) {
    super(fluid, new MaterialLiquid(material.getMaterialMapColor()) {
      // new Material for each liquid so neighboring different liquids render correctly and don't bleed into each other
      @Override
      public boolean blocksMovement() {
        return true; // so our liquids are not replaced by water
      }
    });

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
    setUnlocalizedName(NullHelper.notnullF(fluid.getUnlocalizedName(), "encountered fluid without a name"));
    setRegistryName("block_fluid_" + fluid.getName().toLowerCase(Locale.ENGLISH));
  }

  public void init() {
    ForgeRegistries.BLOCKS.register(this); // FIXME
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
    if (materialIn == Material.WATER || materialIn == this.blockMaterial) {
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

  /////////////////////////////////////////////////////////////////////////
  // Molten Metal
  /////////////////////////////////////////////////////////////////////////

  public static class MoltenMetal extends BlockFluidEnder {

    public static MoltenMetal create(Fluid fluid, Material material, int fogColor) {
      MoltenMetal res = new MoltenMetal(fluid, material, fogColor);
      res.init();
      fluid.setBlock(res);
      return res;
    }

    protected MoltenMetal(Fluid fluid, Material material, int fogColor) {
      super(fluid, material, fogColor);
    }

    @Override
    public void onEntityCollidedWithBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entity) {
      if (!world.isRemote && !entity.isImmuneToFire()) {
        entity.attackEntityFrom(DamageSource.LAVA, 4.0F);
        entity.setFire(15);
      }
      super.onEntityCollidedWithBlock(world, pos, state, entity);
    }

    @Override
    public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos blockpos, @Nonnull IBlockState iblockstate, @Nonnull Entity entity,
        double yToTest, @Nonnull Material materialIn, boolean testingHead) {
      if (materialIn == Material.LAVA || materialIn == this.blockMaterial) {
        return Boolean.TRUE;
      }
      // Note: There's no callback for Entity.isInLava(), so just pretend we're also WATER. It has some drawbacks, but we don't really expect people to go
      // swimming in molten metals, do we?
      return super.isEntityInsideMaterial(world, blockpos, iblockstate, entity, yToTest, materialIn, testingHead);
    }

  }

}
