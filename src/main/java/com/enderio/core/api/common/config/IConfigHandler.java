package com.enderio.core.api.common.config;

import java.io.File;
import java.util.List;

import com.enderio.core.common.config.AbstractConfigHandler.Section;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public interface IConfigHandler {
  void initialize(File cfg);

  List<Section> getSections();

  ConfigCategory getCategory(String name);

  String getModID();

  /**
   * A hook for the {@link FMLInitializationEvent}.
   */
  void initHook();

  /**
   * A hook for the {@link FMLPostInitializationEvent}.
   */
  void postInitHook();
}
