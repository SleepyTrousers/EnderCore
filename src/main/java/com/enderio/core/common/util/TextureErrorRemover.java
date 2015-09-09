package com.enderio.core.common.util;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.client.renderer.texture.TextureMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.Message;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.ConfigHandler;

import cpw.mods.fml.relauncher.ReflectionHelper;

public class TextureErrorRemover extends Logger {
  private static TextureErrorRemover INSTANCE;
  private int removed = 0;

  private TextureErrorRemover(Logger other) {
    super(other.getContext(), other.getName(), other.getMessageFactory());
  }

  @Override
  public void log(Marker marker, String fqcn, Level level, Message data, Throwable t) {
    if (ConfigHandler.textureErrorRemover != 0) {
      if (t instanceof FileNotFoundException) {
        if (ConfigHandler.textureErrorRemover == 1) {
          super.log(marker, fqcn, level, data, null);
        }
        removed++;
      } else {
        super.log(marker, fqcn, level, data, t);
      }
      if (data.getFormat().startsWith("Created:")) {
        EnderCore.logger.info(ConfigHandler.textureErrorRemover == 1 ? new FormattedMessage("Removed %d missing texture stacktraces. Tada!", removed)
            : new FormattedMessage("There were %d missing texture errors here. They're gone now.", removed));
        removed = 0;
      }
      return;
    }
    super.log(marker, fqcn, level, data, t);
  }

  public static void beginIntercepting() {
    EnderCore.logger.info("Attempting to initialize texture error message interceptor.");
    try {
      Field f = ReflectionHelper.findField(TextureMap.class, "logger", "field_147635_d", "d");
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);
      modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
      f.setAccessible(true);
      INSTANCE = new TextureErrorRemover((Logger) f.get(null));
      f.set(null, INSTANCE);
    } catch (Exception e) {
      EnderCore.logger.error("Failed to initialize texture error interceptor!", e);
    }
  }
}
