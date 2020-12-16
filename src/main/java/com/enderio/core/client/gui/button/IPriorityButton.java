package com.enderio.core.client.gui.button;

/**
 * Interface for buttons that have a state in which they may overlap other elements and need to be on top.
 *
 */
public interface IPriorityButton {

  /**
   * If this returns <code>true</code>, mousePressed() (or mousePressedButton()) will be called additionally early in the mouse button handling. This allows the
   * button to make sure that it receives the click and not another element that's behind the button.
   */
  boolean isTopmost();

}
