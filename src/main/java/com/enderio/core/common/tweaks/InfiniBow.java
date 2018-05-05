package com.enderio.core.common.tweaks;

import com.enderio.core.common.util.NullHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InfiniBow extends Tweak {

  public InfiniBow() {
    super("infinibow", "Makes bows with Infinity enchant able to be fired with no arrows in the inventory.");
  }

  @Override
  public void load() {
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onArrowNock(ArrowNockEvent event) {
    ItemStack stack = event.getBow();
    if (EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0) {
      event.getEntityPlayer().setActiveHand(NullHelper.notnullF(event.getHand(), "null hand in ArrowNockEvent event"));
      event.setAction(new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack));
    }
  }
}
