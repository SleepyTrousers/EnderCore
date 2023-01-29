package com.enderio.core.common.event;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * ItemEvent is fired when an event involving any ItemStack occurs.<br>
 * If a method utilizes this {@link Event} as its parameter, the method will receive every child event of this
 * class.<br>
 * <br>
 * {@link #item} contains the ItemStack that caused this event to occur.<br>
 * <br>
 * All children of this event are fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 **/
public class ItemStackEvent extends Event {

    public final ItemStack item;

    public ItemStackEvent(ItemStack item) {
        this.item = item;
    }

    /**
     * ItemEnchantabilityEvent is fired when calculating an Item's enchantability. <br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * <br>
     * {@link #enchantability} will be the final value returned for these calculations, and is provided with the base
     * enchantability when the event is first fired. <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     **/
    public static class ItemEnchantabilityEvent extends ItemStackEvent {

        public int enchantability;

        public ItemEnchantabilityEvent(ItemStack itemStack, int baseEnchantability) {
            super(itemStack);
            enchantability = baseEnchantability;
        }
    }

    /**
     * ItemRarityEvent is fired when calculating an Item's Rarity. (Text color for the item name in tooltips) <br>
     * This event is fired whenever vanilla Minecraft determines that an entity <br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * <br>
     * {@link #rarity} will be the final value returned for these calculations, and is provided with the normal rarity
     * from Item.getRarity() when the event is first fired. <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     **/
    public static class ItemRarityEvent extends ItemStackEvent {

        public EnumRarity rarity;

        public ItemRarityEvent(ItemStack itemStack, EnumRarity baseRarity) {
            super(itemStack);
            rarity = baseRarity;
        }
    }
}
