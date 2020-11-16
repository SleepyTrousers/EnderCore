package com.enderio.core.client.handlers;

import java.util.Map;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.enchantment.IAdvancedEnchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class EnchantTooltipHandler {

  @SubscribeEvent
  public static void handleTooltip(ItemTooltipEvent event) {
    if (event.getItemStack().getTag() != null) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(event.getItemStack());

      for (Enchantment enchant : enchantments.keySet()) {
        if (enchant instanceof IAdvancedEnchantment) {
          for (int i = 0; i < event.getToolTip().size(); i++) {
            if (event.getToolTip().get(i).getString().contains(EnderCore.lang.localizeExact(enchant.getName()))) {
              for (String s : ((IAdvancedEnchantment) enchant).getTooltipDetails(event.getItemStack())) {
                event.getToolTip().add(i + 1, new StringTextComponent(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + "  - " + s));
                i++;
              }
            }
          }
        }
      }
    }
  }

}
