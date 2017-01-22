package com.enderio.core.client.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.enderio.core.EnderCore;
import com.enderio.core.api.common.config.IConfigHandler;
import com.enderio.core.common.config.AbstractConfigHandler.Section;
import com.enderio.core.common.config.ConfigHandler;
import com.google.common.base.Throwables;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class BaseConfigGui extends GuiConfig {

  public BaseConfigGui(GuiScreen parentScreen) {
    // dummy super so we can call instance methods
    super(parentScreen, new ArrayList<IConfigElement>(), null, false, false, null);

    try {
      // pffft final, what a wimpy modifier
      Field modIDField = GuiConfig.class.getDeclaredField("modID");
      Field configElementsField = GuiConfig.class.getDeclaredField("configElements");

      modIDField.setAccessible(true);
      configElementsField.setAccessible(true);

      modIDField.set(this, getConfigHandler().getModID());
      configElementsField.set(this, getConfigElements());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    this.title = getTitle();
  }

  /**
   * The <b>localized</b> title of this config screen
   */
  protected String getTitle() {
    return EnderCore.lang.localize("config.title");
  }

  /**
   * The {@link IConfigHandler} to refer to when generating this config screen
   */
  protected IConfigHandler getConfigHandler() {
    return ConfigHandler.instance();
  }

  /**
   * The lang prefix to use before your section lang keys. Default is "config.".
   */
  protected String getLangPrefix() {
    return "config.";
  }

  private List<IConfigElement> getConfigElements() {
    List<IConfigElement> list = new ArrayList<IConfigElement>();
    String prefix = getLangPrefix();
    IConfigHandler config = getConfigHandler();

    prefix = prefix.endsWith(".") ? prefix : prefix + ".";

    for (Section s : config.getSections()) {
      list.add(new ConfigSection(s, prefix));
    }

    return list;
  }

  private class ConfigSection extends ConfigElement {
    private Section section;
    private String prefix;

    ConfigSection(Section s, String prefix) {
      super(BaseConfigGui.this.getConfigHandler().getCategory(s.lc()).setLanguageKey(prefix + s.lang));
      this.section = s;
      this.prefix = prefix;
    }

    @Override
    public List<IConfigElement> getChildElements() {
      List<IConfigElement> temp = super.getChildElements();
      List<IConfigElement> ret = new ArrayList<IConfigElement>(temp.size());
      for (IConfigElement e : temp) {
        if (e.isProperty()) {
          ret.add(new ConfigElementExtended(e));
        } else {
          ret.add(new ConfigSection(section, prefix));
        }
      }
      return ret;
    }
  }

  private static class ConfigElementExtended extends ConfigElement {

    private static final Field _prop;
    static {
      try {
        _prop = ConfigElement.class.getDeclaredField("prop");
        _prop.setAccessible(true);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    ConfigElementExtended(IConfigElement other) {
      super(getProp(other));
    }

    private static Property getProp(IConfigElement other) {
      try {
        return (Property) _prop.get(other);
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }

    @Override
    public String getComment() {
      String comment = super.getComment();
      String range = "[range:";
      String def = "[default:";
      if (comment.contains(range)) {
        comment = comment.substring(0, comment.indexOf(range) - 1);
      } else if (comment.contains(def)) {
        comment = comment.substring(0, comment.indexOf(def) - 1);
      }
      return comment;
    }
  }
}
