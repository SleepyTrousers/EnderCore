package com.enderio.core.common.util;

import java.io.FileNotFoundException;

import net.minecraft.client.renderer.texture.TextureMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.FormattedMessage;
import org.apache.logging.log4j.message.Message;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.ConfigHandler;
import com.gtnewhorizon.gtnhlib.reflect.Fields;

import cpw.mods.fml.common.Loader;

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
                EnderCore.logger.info(
                        ConfigHandler.textureErrorRemover == 1
                                ? new FormattedMessage("Removed %d missing texture stacktraces. Tada!", removed)
                                : new FormattedMessage(
                                        "There were %d missing texture errors here. They're gone now.",
                                        removed));
                removed = 0;
            }
            return;
        }
        super.log(marker, fqcn, level, data, t);
    }

    public static void beginIntercepting() {
        if (!Loader.isModLoaded("gtnhlib")) return;
        EnderCore.logger.info("Attempting to initialize texture error message interceptor.");
        try {
            Fields.ClassFields<TextureMap>.Field<org.apache.logging.log4j.Logger> f = Fields.ofClass(TextureMap.class)
                    .getField(Fields.LookupType.DECLARED, "logger", org.apache.logging.log4j.Logger.class);
            if (f == null) {
                f = Fields.ofClass(TextureMap.class)
                        .getField(Fields.LookupType.DECLARED, "field_147635_d", org.apache.logging.log4j.Logger.class);
            }
            INSTANCE = new TextureErrorRemover((Logger) f.getValue(null));
            f.setValue(null, INSTANCE);
        } catch (Exception e) {
            EnderCore.logger.error("Failed to initialize texture error interceptor!", e);
        }
    }
}
