package com.enderio.core.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SoundUtil {

  @SideOnly(Side.CLIENT)
  public static void playClientSoundFX(SoundEvent name, TileEntity te) {
    World world = Minecraft.getMinecraft().thePlayer.worldObj;
    Minecraft.getMinecraft().theWorld.playSound(te.getPos().getX() + 0.5, te.getPos().getY()+ 0.5, te.getPos().getZ()+ 0.5, name, SoundCategory.BLOCKS, 0.1F,
        0.5F * ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.8F), true);
  }

}
