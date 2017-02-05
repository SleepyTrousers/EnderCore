package com.enderio.core.common.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import com.enderio.core.EnderCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.FileResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A class that can be used to inject resources from files/folders outside your mod resources. Useful for loading textures and other assets from the config dir
 * or elsewhere.
 * <p>
 * To use, first construct an instance of this class, then add all your assets using {@link #addIcon(File)}, {@link #addLang(File)}, and
 * {@link #addCustomFile(String, File)}.
 * <p>
 * Once all files have been added, {@link #assemble()} Will create a zip of all the files in the {@link File directory} passed into the constructor.
 * <p>
 * Finally, {@link #inject()} will insert this resource pack into the game.
 * <p>
 * Also, {@link #setHasPackPng(Class)} allows your resource pack to have a logo.
 */
public class ResourcePackAssembler {
  private class CustomFile {
    String ext;
    File file;

    CustomFile(String ext, File file) {
      this.ext = ext;
      this.file = file;
    }
  }

  public enum ModelType {
    BLOCK("models/block"),
    ITEM("models/item"),
    BLOCKSTATE("blockstates");

    private final String path;

    private ModelType(String path) {
      this.path = path;
    }

    String getPath() {
      return path;
    }
  }

  private List<CustomFile> files = new ArrayList<CustomFile>();

  private static List<IResourcePack> defaultResourcePacks;

  private static final String MC_META_BASE = "{\"pack\":{\"pack_format\":1,\"description\":\"%s\"}}";

  private File dir;
  private File zip;
  private String name;
  private String mcmeta;
  private String modid;
  private String assetsPath;
  private boolean hasPackPng = false;
  private Class<?> jarClass;

  /**
   * @param directory
   *          The directory to assemble the resource pack in. The name of the zip created will be the same as this folder, and it will be created on the same
   *          level as the folder. This folder will be <strong>WIPED</strong> on every call of {@link #assemble()} .
   * @param packName
   *          The name of the resource pack.
   * @param modid
   *          Your mod's mod ID.
   */
  public ResourcePackAssembler(File directory, String packName, String modid) {
    this.dir = directory;
    this.zip = new File(dir.getAbsolutePath() + ".zip");
    this.name = packName;
    this.modid = modid.toLowerCase(Locale.US);
    this.mcmeta = String.format(MC_META_BASE, this.name);
    this.assetsPath = "/assets/" + modid + "/";
  }

  /**
   * Enables the use of a pack.png.
   * <p>
   * Will cause your mod's jar to be searched for a resource pack logo at assets/[modid]/pack.png.
   *
   * @param jarClass
   *          A class in your jar file.
   * @return The {@link ResourcePackAssembler} instance.
   */
  public ResourcePackAssembler setHasPackPng(Class<?> jarClass) {
    this.jarClass = jarClass;
    hasPackPng = true;
    return this;
  }

  /**
   * Adds an icon file. This file will be inserted into both the block and item texture folders.
   *
   * @param icon
   *          The icon file.
   */
  public void addIcon(File icon) {
    files.add(new CustomFile(assetsPath + "textures/items/", icon));
    files.add(new CustomFile(assetsPath + "textures/blocks/", icon));
  }

  /**
   * Adds a language file. This file will be inserted into the lang dir only.
   *
   * @param lang
   *          A language file (e.g. en_US.lang)
   */
  public void addLang(File lang) {
    files.add(new CustomFile(assetsPath + "lang/", lang));
  }

  /**
   * Adds a model json file. This file will be inserted into the models dir only.
   */
  public void addModel(File model, ModelType type) {
    String path = assetsPath + type.getPath() + "/";
    files.add(new CustomFile(path, model));
  }

  /**
   * Adds a custom file to the pack. This can be added into any folder in the pack you desire. Useful for one-off files such as sounds.json.
   *
   * @param path
   *          The path inside the resource pack to this file.
   * @param file
   *          The file to add.
   */
  public void addCustomFile(String path, File file) {
    files.add(new CustomFile(path, file));
  }

  /**
   * Adds the custom file at the base directory.
   *
   * @see #addCustomFile(String, File)
   *
   * @param file
   *          The file to add.
   */
  public void addCustomFile(File file) {
    addCustomFile(null, file);
  }

  /**
   * Assembles the resource pack. This creates a zip file with the name of the {@link File directory} that was passed into the constructor on the same level as
   * that folder.
   *
   * @return The {@link ResourcePackAssembler} instance.
   */
  public ResourcePackAssembler assemble() {
    EnderFileUtils.safeDeleteDirectory(dir);
    dir.mkdirs();

    String pathToDir = dir.getAbsolutePath();
    File metaFile = new File(pathToDir + "/pack.mcmeta");

    try {
      writeNewFile(metaFile, mcmeta);

      if (hasPackPng) {
        EnderFileUtils.copyFromJar(jarClass, modid + "/" + "pack.png", new File(dir.getAbsolutePath() + "/pack.png"));
      }

      for (CustomFile custom : files) {
        File directory = new File(pathToDir + (custom.ext != null ? "/" + custom.ext : ""));
        directory.mkdirs();
        FileUtils.copyFile(custom.file, new File(directory.getAbsolutePath() + "/" + custom.file.getName()));
      }

      EnderFileUtils.zipFolderContents(dir, zip);
      EnderFileUtils.safeDeleteDirectory(dir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return this;
  }

  /**
   * Inserts the resource pack into the game. Enabling the resource pack will not be required, it will load automatically.
   * <p>
   * A cache of the pack zip will be kept in "resourcepack/[pack name].zip" where "resourcepack" is a folder at the same level as the directory passed into the
   * constructor.
   */
  @SuppressWarnings("resource")
  public void inject() {
    if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
      try {
        if (defaultResourcePacks == null) {
          defaultResourcePacks = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao", "ap");
        }

        File dest = new File(dir.getParent() + "/resourcepack/" + zip.getName());
        EnderFileUtils.safeDelete(dest);
        FileUtils.copyFile(zip, dest);
        EnderFileUtils.safeDelete(zip);
        writeNewFile(new File(dest.getParent() + "/readme.txt"),
            EnderCore.lang.localize("resourcepack.readme") + "\n\n" + EnderCore.lang.localize("resourcepack.readme2"));
        defaultResourcePacks.add(new FileResourcePack(dest));
      } catch (Exception e) {
        EnderCore.logger.error("Failed to inject resource pack for mod {}", modid, e);
      }
    } else {
      EnderCore.logger.info("Skipping resource pack, we are on a dedicated server.");
    }
  }

  private static void writeNewFile(File file, String defaultText) throws IOException {
    EnderFileUtils.safeDelete(file);
    file.delete();
    file.getParentFile().mkdirs();
    file.createNewFile();

    FileWriter fw = new FileWriter(file);
    fw.write(defaultText);
    fw.flush();
    fw.close();
  }
}
