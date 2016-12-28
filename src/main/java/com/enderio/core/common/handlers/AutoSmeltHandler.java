package com.enderio.core.common.handlers;

import javax.annotation.Nonnull;

import com.enderio.core.common.Handlers.Handler;
import com.enderio.core.common.enchant.EnchantAutoSmelt;
import com.enderio.core.common.util.NullHelper;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Handler
public class AutoSmeltHandler {

  @SubscribeEvent
  public void handleBlockBreak(BlockEvent.HarvestDropsEvent event) {
    final @Nonnull World world = NullHelper.notnullF(event.getWorld(), "BlockEvent.HarvestDropsEvent.getWorld()");
    final EntityPlayer harvester = event.getHarvester();
    final EnchantAutoSmelt enchantment = EnchantAutoSmelt.instance();
    if (!world.isRemote && harvester != null && !event.isSilkTouching() && enchantment != null) {

      @Nonnull
      ItemStack held = harvester.getHeldItemMainhand();
      if (!held.isEmpty()) {
        int level = EnchantmentHelper.getEnchantmentLevel(enchantment, held);

        if (level > 0) {
          for (int i = 0; i < event.getDrops().size(); i++) {
            @Nonnull
            ItemStack stack = NullHelper.notnullF(event.getDrops().get(i), "BlockEvent.HarvestDropsEvent.getDrops()");
            if (!stack.isEmpty()) {
              final @Nonnull ItemStack smeltingResult = FurnaceRecipes.instance().getSmeltingResult(stack);
              if (!smeltingResult.isEmpty()) {
                @Nonnull
                ItemStack furnaceStack = smeltingResult.copy();

                event.getDrops().set(i, furnaceStack);

                // adapted vanilla code, see net.minecraft.inventory.SlotFurnaceOutput.onCrafting()

                furnaceStack.onCrafting(world, harvester, furnaceStack.getCount());

                int xp = furnaceStack.getCount();
                float f = FurnaceRecipes.instance().getSmeltingExperience(furnaceStack);

                if (f == 0.0F) {
                  xp = 0;
                } else if (f < 1.0F) {
                  int j = MathHelper.floor(xp * f);

                  if (j < MathHelper.ceil(xp * f) && (float) Math.random() < xp * f - j) {
                    ++j;
                  }

                  xp = j;
                }

                while (xp > 0) {
                  int k = EntityXPOrb.getXPSplit(xp);
                  xp -= k;
                  world.spawnEntity(new EntityXPOrb(world, event.getPos().getX(), event.getPos().getY() + 0.5, event.getPos().getZ(), k));
                }

                FMLCommonHandler.instance().firePlayerSmeltedEvent(harvester, furnaceStack);

                if (furnaceStack.getItem() == Items.IRON_INGOT) {
                  harvester.addStat(AchievementList.ACQUIRE_IRON);
                }

                if (furnaceStack.getItem() == Items.COOKED_FISH) {
                  harvester.addStat(AchievementList.COOK_FISH);
                }
              }
            }
          }
        }
      }
    }
  }

}
