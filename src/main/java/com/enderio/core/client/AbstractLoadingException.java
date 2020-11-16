package com.enderio.core.client;

import javax.annotation.Nonnull;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// TODO: Stop using in ClientProxy and make abstract.
@OnlyIn(Dist.CLIENT)
public class AbstractLoadingException extends RuntimeException {//ModLoadingException {

  private static final long serialVersionUID = 1L;

  private final @Nonnull String[] msgs;

  public AbstractLoadingException(@Nonnull String[] msgs) {
    super(msgs[0], new RuntimeException());
    this.msgs = msgs;
  }

//  @Override
//  public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
//  }
//
//
//
//  @Override
//  public void drawScreen(@Nullable GuiErrorScreen errorScreen, @Nullable FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
//    if (errorScreen == null || fontRenderer == null) {
//      return;
//    }
//    int y = errorScreen.height / 2 - msgs.length * 5;
//    for (String msg : msgs) {
//      if (msg != null) {
//        errorScreen.drawCenteredString(fontRenderer, msg, errorScreen.width / 2, y, 0xFFFFFF);
//        y += 10;
//      } else {
//        y += 5;
//      }
//    }
//  }
}
