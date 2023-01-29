package com.enderio.core.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.enderio.core.common.vecmath.Vector3d;

public class EntityUtil {

    private static final Random rand = new Random();

    public static void setEntityVelocity(Entity entity, double velX, double velY, double velZ) {
        entity.motionX = velX;
        entity.motionY = velY;
        entity.motionZ = velZ;
    }

    public static EntityFireworkRocket getRandomFirework(World world) {
        return getRandomFirework(world, new BlockCoord(0, 0, 0));
    }

    public static EntityFireworkRocket getRandomFirework(World world, BlockCoord pos) {
        ItemStack firework = new ItemStack(Items.fireworks);
        firework.stackTagCompound = new NBTTagCompound();
        NBTTagCompound expl = new NBTTagCompound();
        expl.setBoolean("Flicker", true);
        expl.setBoolean("Trail", true);

        int[] colors = new int[rand.nextInt(8) + 1];
        for (int i = 0; i < colors.length; i++) {
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

    public static void spawnFirework(BlockCoord block, int dimID) {
        spawnFirework(block, dimID, 0);
    }

    public static void spawnFirework(BlockCoord block, int dimID, int range) {
        World world = DimensionManager.getWorld(dimID);

        BlockCoord pos = new BlockCoord(block.x, block.y, block.z);

        // don't bother if there's no randomness at all
        if (range > 0) {
            pos = new BlockCoord(moveRandomly(block.x, range), block.y, moveRandomly(block.z, range));

            int tries = -1;
            while (!world.isAirBlock(pos.x, pos.y, pos.z)
                    && !world.getBlock(pos.x, pos.y, pos.z).isReplaceable(world, pos.x, pos.y, pos.z)) {
                tries++;
                if (tries > 100) {
                    return;
                }
            }
        }

        world.spawnEntityInWorld(getRandomFirework(world, pos));
    }

    private static double moveRandomly(double base, double range) {
        return base + 0.5 + rand.nextDouble() * range - (range / 2);
    }

    public static String getDisplayNameForEntity(String mobName) {
        return StatCollector.translateToLocal("entity." + mobName + ".name");
    }

    public static List<String> getAllRegisteredMobNames(boolean excludeBosses) {
        List<String> result = new ArrayList<String>();
        Set<Map.Entry<Class, String>> entries = EntityList.classToStringMapping.entrySet();
        for (Map.Entry<Class, String> entry : entries) {
            if (EntityLiving.class.isAssignableFrom(entry.getKey())) {
                if (!excludeBosses || !IBossDisplayData.class.isAssignableFrom(entry.getKey())) {
                    result.add(entry.getValue());
                }
            }
        }
        return result;
    }

    private EntityUtil() {}

    public static Vector3d getEntityPosition(Entity ent) {
        return new Vector3d(ent.posX, ent.posY, ent.posZ);
    }

    public static List<AxisAlignedBB> getCollidingBlockGeometry(World world, Entity entity) {
        AxisAlignedBB entityBounds = entity.boundingBox;
        ArrayList collidingBoundingBoxes = new ArrayList();
        int minX = MathHelper.floor_double(entityBounds.minX);
        int minY = MathHelper.floor_double(entityBounds.minY);
        int minZ = MathHelper.floor_double(entityBounds.minZ);
        int maxX = MathHelper.floor_double(entityBounds.maxX + 1.0D);
        int maxY = MathHelper.floor_double(entityBounds.maxY + 1.0D);
        int maxZ = MathHelper.floor_double(entityBounds.maxZ + 1.0D);
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != null) {
                        block.addCollisionBoxesToList(world, x, y, z, entityBounds, collidingBoundingBoxes, entity);
                    }
                }
            }
        }
        return collidingBoundingBoxes;
    }

    public static void spawnItemInWorldWithRandomMotion(World world, ItemStack item, int x, int y, int z) {
        if (item != null) {
            spawnItemInWorldWithRandomMotion(world, item, x + 0.5, y + 0.5, z + 0.5);
        }
    }

    public static void spawnItemInWorldWithRandomMotion(World world, ItemStack item, double x, double y, double z) {
        if (item != null) {
            spawnItemInWorldWithRandomMotion(new EntityItem(world, x, y, z, item));
        }
    }

    public static void spawnItemInWorldWithRandomMotion(EntityItem entity) {
        entity.delayBeforeCanPickup = 10;

        float f = (entity.worldObj.rand.nextFloat() * 0.1f) - 0.05f;
        float f1 = (entity.worldObj.rand.nextFloat() * 0.1f) - 0.05f;
        float f2 = (entity.worldObj.rand.nextFloat() * 0.1f) - 0.05f;

        entity.motionX += f;
        entity.motionY += f1;
        entity.motionZ += f2;

        entity.worldObj.spawnEntityInWorld(entity);
    }
}
