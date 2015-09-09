package com.enderio.core.common.util;

import java.io.File;
import java.util.List;

import lombok.SneakyThrows;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.FMLCommonHandler;

public class PermanentCache<I> extends WorldCache<I> {

  private static final List<PermanentCache<?>> allCaches = Lists.newArrayList();

  @SneakyThrows
  public PermanentCache(String ident) {
    super(ident);
    loadData(getSaveFile());
  }

  @Override
  protected File getSaveFile() {
    if (FMLCommonHandler.instance().getSide().isServer()) {
      return new File(ident + ".dat");
    }
    return new File("saves", ident + ".dat");
  }

  @Override
  protected void blockOldIDs() {
    if (!objToName.isEmpty()) {
      super.blockOldIDs();
    }
  }

  @Override
  protected void mergeNewIDs() {
    if (!objToName.isEmpty()) {
      super.mergeNewIDs();
    }
  }

  @Override
  public void addObject(I object, String name) {
    super.addObject(object, name);
    setID(name);
  }

  public static void saveCaches() {
    for (PermanentCache<?> c : allCaches) {
      c.saveData(c.getSaveFile());
    }
  }
}
