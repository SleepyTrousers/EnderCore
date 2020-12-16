package com.enderio.core.client.gui.button;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Interface for {@link GuiButton}s to allow them to receive mouse button events for all mouse buttons.
 *
 */
public interface IButtonAwareButton {

  /**
   * Override this to handle mouse clicks with other buttons than the left
   *
   * @param mc
   *          The MC instance
   * @param mouseX
   *          X coordinate of mouse click
   * @param mouseY
   *          Y coordinate of mouse click
   * @param button
   *          the mouse button - only called for button {@literal >}= 1
   * @return <code>true</code> if the mouse click is handled
   */
  boolean mousePressedButton(@Nonnull Minecraft mc, int mouseX, int mouseY, int button);

  /**
   * Checks if the mouse click is in the button area. Should just call <code>super.mousePressed()</code>.
   */
  boolean checkMousePress(@Nonnull Minecraft mc, int mouseX, int mouseY);

}