package com.enderio.core.common;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.enderio.core.api.common.util.ITankAccess;
import com.enderio.core.common.interfaces.IComparatorOutput;
import com.enderio.core.common.util.FluidUtil;
import com.enderio.core.common.util.Log;
import com.google.common.collect.Lists;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockEnder extends Block {

    protected final @Nullable Class<? extends TileEntityEnder> teClass;
    protected final String name;
    protected final boolean hasComparatorInputOverride;

    protected BlockEnder(String name, Class<? extends TileEntityEnder> teClass) {
        this(name, teClass, new Material(MapColor.ironColor));
    }

    protected BlockEnder(String name, Class<? extends TileEntityEnder> teClass, Material mat) {
        super(mat);
        this.teClass = teClass;
        this.name = name;
        setHardness(0.5F);
        setBlockName(name);
        setStepSound(Block.soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
        hasComparatorInputOverride = teClass != null && IComparatorOutput.class.isAssignableFrom(teClass);
    }

    protected void init() {
        GameRegistry.registerBlock(this, name);
        if (teClass != null) {
            GameRegistry.registerTileEntity(teClass, name + "TileEntity");
        }
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return teClass != null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        if (teClass != null) {
            try {
                TileEntityEnder te = teClass.newInstance();
                te.init();
                return te;
            } catch (Exception e) {
                Log.error("Could not create tile entity for block " + name + " for class " + teClass);
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iIconRegister) {
        blockIcon = iIconRegister.registerIcon("enderio:" + name);
    }

    /* Subclass Helpers */

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int side, float par7,
            float par8, float par9) {

        TileEntity te = world.getTileEntity(x, y, z);

        if (entityPlayer.isSneaking()) {
            return false;
        }

        if (te instanceof ITankAccess) {
            if (FluidUtil.fillInternalTankFromPlayerHandItem(world, x, y, z, entityPlayer, (ITankAccess) te)) {
                return true;
            }
            if (FluidUtil.fillPlayerHandItemFromInternalTank(world, x, y, z, entityPlayer, (ITankAccess) te)) {
                return true;
            }
        }

        return openGui(world, x, y, z, entityPlayer, side);
    }

    protected boolean shouldWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
        return true;
    }

    protected boolean openGui(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
        return false;
    }

    public boolean doNormalDrops(World world, int x, int y, int z) {
        return true;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (willHarvest) {
            return true;
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
        super.harvestBlock(world, player, x, y, z, meta);
        world.setBlockToAir(x, y, z);
    }

    @Override
    public final ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        if (doNormalDrops(world, x, y, z)) {
            return super.getDrops(world, x, y, z, metadata, fortune);
        }
        return Lists.newArrayList(getNBTDrop(world, x, y, z, (TileEntityEnder) world.getTileEntity(x, y, z)));
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        return getPickBlock(target, world, x, y, z, null);
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z,
            @Nullable EntityPlayer player) {
        return getNBTDrop(world, x, y, z, teClass != null ? getTileEntityEio(world, x, y, z) : null);
    }

    public ItemStack getNBTDrop(World world, int x, int y, int z, @Nullable TileEntityEnder te) {
        int meta = te != null ? damageDropped(te.getBlockMetadata()) : world.getBlockMetadata(x, y, z);
        ItemStack itemStack = new ItemStack(this, 1, meta);
        processDrop(world, x, y, z, te, itemStack);
        return itemStack;
    }

    protected void processDrop(World world, int x, int y, int z, @Nullable TileEntityEnder te, ItemStack drop) {}

    @SuppressWarnings("null")
    protected @Nullable TileEntityEnder getTileEntityEio(IBlockAccess world, int x, int y, int z) {
        if (teClass != null) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (teClass.isInstance(te)) { // no need to null-check teClass here, it was checked 2 lines above and is
                                          // *final*
                return (TileEntityEnder) te;
            }
        }
        return null;
    }

    protected boolean shouldDoWorkThisTick(World world, int x, int y, int z, int interval) {
        TileEntityEnder te = getTileEntityEio(world, x, y, z);
        if (te == null) {
            return world.getTotalWorldTime() % interval == 0;
        } else {
            return te.shouldDoWorkThisTick(interval);
        }
    }

    protected boolean shouldDoWorkThisTick(World world, int x, int y, int z, int interval, int offset) {
        TileEntityEnder te = getTileEntityEio(world, x, y, z);
        if (te == null) {
            return (world.getTotalWorldTime() + offset) % interval == 0;
        } else {
            return te.shouldDoWorkThisTick(interval, offset);
        }
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return hasComparatorInputOverride;
    }

    @Override
    public int getComparatorInputOverride(World w, int x, int y, int z, int side) {
        if (hasComparatorInputOverride) {
            TileEntityEnder te = getTileEntityEio(w, x, y, z);
            if (te != null) {
                return ((IComparatorOutput) te).getComparatorOutput();
            }
        }
        return 0;
    }

    // Because the vanilla method takes floats...
    public void setBlockBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public void setBlockBounds(AxisAlignedBB bb) {
        setBlockBounds(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }
}
