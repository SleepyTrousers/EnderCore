package com.enderio.core.common.handlers;

import java.util.Map;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.config.ConfigHandler;
import com.enderio.core.common.enchant.EnchantAutoSmelt;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Handler
public class AutoSmeltHandler {

  @SubscribeEvent
  public void handleBlockBreak(BlockEvent.HarvestDropsEvent event) {
    if (!event.getWorld().isRemote && event.getHarvester() != null) {
      
      ItemStack held = event.getHarvester().getHeldItemMainhand();
      if (held != null) {
        int level = getAutoSmeltLevel(held);
        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.fortune, held);

        if (level >= 0) {
          for (int i = 0; i < event.getDrops().size(); i++) {
            {
              ItemStack stack = event.getDrops().get(i);
              if (stack != null && !event.isSilkTouching()) {
                if (FurnaceRecipes.instance().getSmeltingResult(stack) != null) {
                  ItemStack furnaceStack = FurnaceRecipes.instance().getSmeltingResult(stack).copy();
                  //Fortune stuffs
                  if (fortune > 0 && ConfigHandler.allowAutoSmeltWithFortune)
                    furnaceStack.stackSize *= (event.getWorld().rand.nextInt(fortune + 1) + 1);

                  event.getDrops().set(i, furnaceStack);

                  //XP (adapted vanilla code)
                  int xp = furnaceStack.stackSize;
                  float f = FurnaceRecipes.instance().getSmeltingExperience(furnaceStack);
                  int j;

                  if (f == 0.0F) {
                    xp = 0;
                  } else if (f < 1.0F) {
                    j = MathHelper.floor_float((float) xp * f);

                    if (j < MathHelper.ceiling_float_int((float) xp * f) && (float) Math.random() < (float) xp * f - (float) j) {
                      ++j;
                    }

                    xp = j;
                  }

                  while (xp > 0) {
                    j = EntityXPOrb.getXPSplit(xp);
                    xp -= j;
                    event.getWorld().spawnEntityInWorld(new EntityXPOrb(event.getWorld(), event.getPos().getX(), event.getPos().getY() + 0.5, event.getPos().getZ(), j));
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private int getAutoSmeltLevel(ItemStack tool) {
    if (tool == null) {
      return -1;
    }

    Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(tool);
    for (Enchantment enchant : enchants.keySet()) {      
      if (enchant == EnchantAutoSmelt.instance()) {
        return enchants.get(enchant);
      }
    }
    return -1;
  }
}
