package com.enderio.core.common.util;

import java.util.Random;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

@UtilityClass
public class EnderEntityUtils
{
    private static final Random rand = new Random();

    public void setEntityVelocity(Entity entity, double velX, double velY, double velZ)
    {
        entity.motionX = velX;
        entity.motionY = velY;
        entity.motionZ = velZ;
    }

    public EntityFireworkRocket getRandomFirework(World world)
    {
        return getRandomFirework(world, new BlockCoord(0, 0, 0));
    }

    public EntityFireworkRocket getRandomFirework(World world, BlockCoord pos)
    {
        ItemStack firework = new ItemStack(Items.fireworks);
        firework.stackTagCompound = new NBTTagCompound();
        NBTTagCompound expl = new NBTTagCompound();
        expl.setBoolean("Flicker", true);
        expl.setBoolean("Trail", true);

        int[] colors = new int[rand.nextInt(8) + 1];
        for (int i = 0; i < colors.length; i++)
        {
            colors[i] = ItemDye.field_150922_c[rand.nextInt(16)];
        }
        expl.setIntArray("Colors", colors);
        byte type = (byte) (rand.nextInt(3) + 1);
        type = type == 3 ? 4 : type;
        expl.setByte("Type", type);

        NBTTagList explosions = new NBTTagList();
        explosions.appendTag(expl);

        NBTTagCompound fireworkTag = new NBTTagCompound();
        fireworkTag.setTag("Explosions", explosions);
        fireworkTag.setByte("Flight", (byte) 1);
        firework.stackTagCompound.setTag("Fireworks", fireworkTag);

        EntityFireworkRocket e = new EntityFireworkRocket(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, firework);
        return e;
    }

    public void spawnFirework(BlockCoord block, int dimID)
    {
        spawnFirework(block, dimID, 0);
    }

    public void spawnFirework(BlockCoord block, int dimID, int range)
    {
        World world = DimensionManager.getWorld(dimID);

        BlockCoord pos = new BlockCoord(block.x, block.y, block.z);

        // don't bother if there's no randomness at all
        if (range > 0)
        {
            pos = new BlockCoord(moveRandomly(block.x, range), block.y, moveRandomly(block.z, range));

            int tries = -1;
            while (!world.isAirBlock(pos.x, pos.y, pos.z) && !world.getBlock(pos.x, pos.y, pos.z).isReplaceable(world, pos.x, pos.y, pos.z))
            {
                tries++;
                if (tries > 100)
                {
                    return;
                }
            }
        }

        world.spawnEntityInWorld(getRandomFirework(world, pos));
    }

    private double moveRandomly(double base, double range)
    {
        return base + 0.5 + rand.nextDouble() * range - (range / 2);
    }
}
