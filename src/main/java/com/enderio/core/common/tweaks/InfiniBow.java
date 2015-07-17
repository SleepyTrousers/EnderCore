package com.enderio.core.common.tweaks;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

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
    EntityPlayer player = event.entityPlayer;
    ItemStack stack = player.getHeldItem();
    if (player.capabilities.isCreativeMode || player.inventory.hasItem(Items.arrow)
        || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) > 0) {
      player.setItemInUse(stack, stack.getItem().getMaxItemUseDuration(stack));
    }

    event.result = stack;
    event.setCanceled(true);
  }
}
