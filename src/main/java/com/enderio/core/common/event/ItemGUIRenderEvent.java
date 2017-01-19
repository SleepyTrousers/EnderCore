package com.enderio.core.common.event;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when an item is rendered into the GUI
 *
 */
public class ItemGUIRenderEvent extends Event {

  private final @Nonnull ItemStack stack;
  private final int xPosition, yPosition;

  public ItemGUIRenderEvent(@Nonnull ItemStack stack, int xPosition, int yPosition) {
    super();
    this.stack = stack;
    this.xPosition = xPosition;
    this.yPosition = yPosition;
  }

  public @Nonnull ItemStack getStack() {
    return stack;
  }

  public int getxPosition() {
    return xPosition;
  }

  public int getyPosition() {
    return yPosition;
  }

  /**
   * Fired before the item is rendered
   *
   */
  public static class Pre extends ItemGUIRenderEvent {

    public Pre(@Nonnull ItemStack stack, int xPosition, int yPosition) {
      super(stack, xPosition, yPosition);
    }

  }

  /**
   * Fired after the item and either the damage bar or the stack size are rendered but before the cooldown overlay is rendered
   *
   */
  public static class Post extends ItemGUIRenderEvent {

    public Post(@Nonnull ItemStack stack, int xPosition, int yPosition) {
      super(stack, xPosition, yPosition);
    }

  }

}
