package com.enderio.core.common.tweaks;

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
    //TODO: 1.9 
//    EntityPlayer player = event.getEntityPlayer();
//    ItemStack stack = player.getHeldItem();
//    if (player.capabilities.isCreativeMode || player.inventory.hasItem(Items.arrow)
//        || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) > 0) {
//      player.setItemInUse(stack, stack.getItem().getMaxItemUseDuration(stack));
//    }
//
//    event.setResult(stack);
//    event.setCanceled(true);
  }
}
