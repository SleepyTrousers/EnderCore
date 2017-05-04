package com.enderio.core.common.handlers;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.enchant.EnchantAutoSmelt;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Handler
public class AutoSmeltHandler {

  @SubscribeEvent
  public void handleBlockBreak(BlockEvent.HarvestDropsEvent event) {
    if (!event.getWorld().isRemote && event.getHarvester() != null && !event.isSilkTouching()) {

      ItemStack held = event.getHarvester().getHeldItemMainhand();
      if (held != null) {
        int level = EnchantmentHelper.getEnchantmentLevel(EnchantAutoSmelt.instance(), held);

        if (level > 0) {
          for (int i = 0; i < event.getDrops().size(); i++) {
            ItemStack stack = event.getDrops().get(i);
            if (stack != null && stack.getItem() != null) {
              final ItemStack smeltingResult = FurnaceRecipes.instance().getSmeltingResult(stack);
              if (smeltingResult != null) {
                ItemStack furnaceStack = smeltingResult.copy();

                event.getDrops().set(i, furnaceStack);

                // adapted vanilla code, see net.minecraft.inventory.SlotFurnaceOutput.onCrafting()

                furnaceStack.onCrafting(event.getWorld(), event.getHarvester(), furnaceStack.stackSize);

                if (!(event.getHarvester() instanceof FakePlayer)) {
                  int xp = furnaceStack.stackSize;
                  float f = FurnaceRecipes.instance().getSmeltingExperience(furnaceStack);

                  if (f == 0.0F) {
                    xp = 0;
                  } else if (f < 1.0F) {
                    int j = MathHelper.floor_float(xp * f);

                    if (j < MathHelper.ceiling_float_int(xp * f) && (float) Math.random() < xp * f - j) {
                      ++j;
                    }

                    xp = j;
                  }

                  while (xp > 0) {
                    int k = EntityXPOrb.getXPSplit(xp);
                    xp -= k;
                    event.getWorld()
                        .spawnEntityInWorld(new EntityXPOrb(event.getWorld(), event.getPos().getX(), event.getPos().getY() + 0.5, event.getPos().getZ(), k));
                  }

                  if (furnaceStack.getItem() == Items.IRON_INGOT) {
                    event.getHarvester().addStat(AchievementList.ACQUIRE_IRON);
                  }

                  if (furnaceStack.getItem() == Items.COOKED_FISH) {
                    event.getHarvester().addStat(AchievementList.COOK_FISH);
                  }
                }

                FMLCommonHandler.instance().firePlayerSmeltedEvent(event.getHarvester(), furnaceStack);
              }
            }
          }
        }
      }
    }
  }

}
