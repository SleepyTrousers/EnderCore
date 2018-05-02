package com.enderio.core.common.tweaks;

import com.enderio.core.common.config.AbstractConfigHandler.RestartReqs;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InfiniBow extends Tweak {

  public InfiniBow() {
    super("infinibow", "Makes bows with Infinity enchant able to be fired with no arrows in the inventory.", RestartReqs.NONE);
  }

  @Override
  public void load() {
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  @Override
  public void unload() {
    MinecraftForge.EVENT_BUS.unregister(this);
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onArrowNock(ArrowNockEvent event) {
    EntityPlayer player = event.getEntityPlayer();
    ItemStack stack = event.getBow();
    if (player.capabilities.isCreativeMode || player.inventory.hasItemStack(new ItemStack(Items.ARROW))
        || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0) {
      event.getEntityPlayer().setActiveHand(NullHelper.notnullF(event.getHand(), "null hand in ArrowNockEvent event"));
      event.setAction(new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack));
    }
  }
}
