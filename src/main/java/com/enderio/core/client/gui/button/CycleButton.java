package com.enderio.core.client.gui.button;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.api.client.gui.IGuiScreen;
import com.enderio.core.api.client.render.IWidgetIcon;
import com.enderio.core.client.gui.button.CycleButton.ICycleEnum;
import com.enderio.core.common.util.NNList;

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

  private final @Nonnull NNList<T> modes;

  private @Nullable T mode;

  public CycleButton(@Nonnull IGuiScreen gui, int id, int x, int y, @Nonnull Class<T> enumClass) {
    super(gui, x, y, null);
    modes = NNList.of(enumClass);
  }

  @Override
  public void onGuiInit() {
    super.onGuiInit();
    if (mode == null) {
      setMode(modes.get(0));
    }
  }

  @Override
  public void onClick(double mouseX, double mouseY) {
    super.onClick(mouseX, mouseY);
    nextMode();
  }

  @Override
  public boolean buttonPressed(double mouseX, double mouseY, int button) {
    boolean result = button == 1 && super.clicked(mouseX, mouseY);
    if (result) {
      prevMode();
    }
    return result;
  }

  private void nextMode() {
    setMode(modes.next(getMode()));
  }

  private void prevMode() {
    setMode(modes.prev(getMode()));
  }

  public void setMode(@Nonnull T newMode) {
    if (mode == newMode) {
      return;
    }
    mode = newMode;
    List<String> tooltip = newMode.getTooltipLines();
    setToolTip(tooltip.toArray(new String[tooltip.size()]));

    icon = newMode.getIcon();
  }

  public @Nonnull T getMode() {
    return mode != null ? mode : modes.get(0);
  }
}
