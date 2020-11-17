package com.enderio.core.api.client.gui;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IAdvancedTooltipProvider {

  @OnlyIn(Dist.CLIENT)
  default void addCommonEntries(@Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer, @Nonnull List<ITextComponent> list, boolean flag) {
  }

  @OnlyIn(Dist.CLIENT)
  default void addBasicEntries(@Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer, @Nonnull List<ITextComponent> list, boolean flag) {
  }

  @OnlyIn(Dist.CLIENT)
  default void addDetailedEntries(@Nonnull ItemStack itemstack, @Nullable PlayerEntity entityplayer, @Nonnull List<ITextComponent> list, boolean flag) {
  }

}
