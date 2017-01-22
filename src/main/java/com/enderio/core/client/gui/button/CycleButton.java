package com.enderio.core.client.gui.button;

import java.util.List;

import javax.annotation.Nonnull;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.button.CycleButton.ICycleEnum;

import net.minecraft.client.Minecraft;

/**
 * A button which automatically parses enum constants and cycles between them when clicked.
 *
 * @param <T>
 *          The enum type for this button.
 */
public class CycleButton<T extends Enum<T> & ICycleEnum> extends IconButton {

  public interface ICycleEnum {

    /**
     * @return The icon to display when the button has selected this mode.
     */
    @Nonnull
    IWidgetIcon getIcon();

    /**
     * @return Localized tooltip lines.
     */
    @Nonnull
    List<String> getTooltipLines();
  }

  private final @Nonnull T[] modes;

  private @Nonnull T mode;

  @SuppressWarnings("null")
  public CycleButton(@Nonnull IGuiScreen gui, int id, int x, int y, @Nonnull Class<T> enumClass) {
    super(gui, id, x, y, null);
    modes = enumClass.getEnumConstants();
    setMode(mode = modes[0]);
  }

  @Override
  public boolean mousePressed(@Nonnull Minecraft par1Minecraft, int par2, int par3) {
    boolean result = super.mousePressed(par1Minecraft, par2, par3);
    if (result) {
      nextMode();
    }
    return result;
  }

  @Override
  public boolean mousePressedButton(@Nonnull Minecraft mc, int x, int y, int button) {
    boolean result = button == 1 && super.checkMousePress(mc, x, y);
    if (result) {
      prevMode();
    }
    return result;
  }

  private void nextMode() {
    setMode(modes[(mode.ordinal() + 1) % modes.length]);
  }

  private void prevMode() {
    int ord = mode.ordinal() - 1;
    if (ord < 0) {
      ord = modes.length - 1;
    }
    setMode(modes[ord]);
  }

  public void setMode(T newMode) {
    if (mode == newMode || newMode == null) {
      return;
    }
    mode = newMode;
    List<String> tooltip = mode.getTooltipLines();
    setToolTip(tooltip.toArray(new String[tooltip.size()]));

    icon = mode.getIcon();
  }

  public @Nonnull T getMode() {
    return mode;
  }
}
