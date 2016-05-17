package com.enderio.core.client.handlers;

import java.util.Map;

import com.enderio.core.api.common.enchant.IAdvancedEnchant;
import com.enderio.core.common.Handlers.Handler;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Handler
public class EnchantTooltipHandler {
  
  @SubscribeEvent
  public void handleTooltip(ItemTooltipEvent event) {
    if (event.getItemStack().hasTagCompound()) {
      Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(event.getItemStack());

      for (Enchantment enchant : enchantments.keySet()) {        
        if (enchant instanceof IAdvancedEnchant) {
          for (int i = 0; i < event.getToolTip().size(); i++) {
            if (event.getToolTip().get(i).contains(I18n.translateToLocal(enchant.getName()))) {
              for (String s : ((IAdvancedEnchant) enchant).getTooltipDetails(event.getItemStack())) {
                event.getToolTip().add(i + 1, TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + "  - " + s);
                i++;
              }
            }
          }
        }
      }
    }
  }
}
