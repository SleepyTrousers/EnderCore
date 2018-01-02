package com.enderio.core.client;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.client.render.IconUtil;
import com.enderio.core.common.CommonProxy;
import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.Scheduler;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ClientProxy extends CommonProxy {

  @Override
  public @Nonnull Scheduler getScheduler() {
    if (scheduler != null) {
      return scheduler;
    }
    return scheduler = new Scheduler(false);
  }

  @Override
  public @Nonnull World getClientWorld() {
    return Minecraft.getMinecraft().world;
  }

  @Override
  public void throwModCompatibilityError(@Nonnull String... msgs) {
    EnderCoreModConflictException ex = new EnderCoreModConflictException(msgs);
    ReflectionHelper.setPrivateValue(FMLClientHandler.class, FMLClientHandler.instance(), ex, "customError");
//    throw ex;
  }

  @Override
  public void onPreInit(@Nonnull FMLPreInitializationEvent event) {
    IconUtil.instance.init();
  }

  @Override
  public CreativeTabs getCreativeTab(@Nonnull ItemStack stack) {
    return stack.getItem().getCreativeTab();
  }

  @SuppressWarnings("null")
  @Override
  public void getSubItems(@Nonnull Item itemIn, @Nullable CreativeTabs tab, @Nonnull NNList<ItemStack> subItems) {
    itemIn.getSubItems(itemIn, tab, subItems);
  }

}
