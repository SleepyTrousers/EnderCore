package com.enderio.core.common;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NNList;
import com.enderio.core.common.util.Scheduler;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
  protected Scheduler scheduler;

  /**
   * Returns a scheduler for the current side
   * <p>
   * For internal use only, please call {@link Scheduler#instance()} to obtain an {@link Scheduler} instance.
   */
  public @Nonnull Scheduler getScheduler() {
    if (scheduler != null) {
      return scheduler;
    }
    return scheduler = new Scheduler(true);
  }

  public World getClientWorld() {
    return null;
  }

  public void throwModCompatibilityError(@Nonnull String... msgs) {
    StringBuilder sb = new StringBuilder();
    for (String msg : msgs) {
      if (sb.length() > 0) {
        sb.append("\n");
      }
      sb.append(msg);
    }
    throw new RuntimeException(sb.toString());
  }

  public void onPreInit(@Nonnull FMLPreInitializationEvent event) {
  }

  /**
   * Proxy for Item.getCreativeTab(), which is client-only.
   */
  public CreativeTabs getCreativeTab(@Nonnull ItemStack stack) {
    return null;
  }

  /**
   * Proxy for Item.getSubItems(), which is client-only.
   */
  public void getSubItems(@Nonnull Item itemIn, @Nullable CreativeTabs tab, @Nonnull NNList<ItemStack> subItems) {
    subItems.add(new ItemStack(itemIn));
  }

}
