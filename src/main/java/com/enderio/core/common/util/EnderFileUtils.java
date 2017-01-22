package com.enderio.core.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.ConfigHandler;

public class EnderFileUtils {
  public static final FileFilter pngFilter = FileFilterUtils.suffixFileFilter(".png");
  public static final FileFilter langFilter = FileFilterUtils.suffixFileFilter(".lang");

  /**
   * @param jarClass
   *          - A class from the jar in question
   * @param filename
   *          - Name of the file to copy, automatically prepended with "/assets/"
   * @param to
   *          - File to copy to
   */
  public static void copyFromJar(Class<?> jarClass, String filename, File to) {
    EnderCore.logger.info("Copying file " + filename + " from jar");
    URL url = jarClass.getResource("/assets/" + filename);

    try {
      FileUtils.copyURLToFile(url, to);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @author Ilias Tsagklis
   *         <p>
   *         From <a href= "http://examples.javacodegeeks.com/core-java/util/zip/extract-zip-file-with-subdirectories/" > this site.</a>
   *
   * @param zip
   *          - The zip file to extract
   *
   * @return The folder extracted to
   */
  @Nonnull
  public static File extractZip(File zip) {
    String zipPath = zip.getParent() + "/extracted";
    final @Nonnull File temp = new File(zipPath);
    temp.mkdir();

    ZipFile zipFile = null;

    try {
      zipFile = new ZipFile(zip);

      // get an enumeration of the ZIP file entries
      Enumeration<? extends ZipEntry> e = zipFile.entries();

      while (e.hasMoreElements()) {
        ZipEntry entry = e.nextElement();

        File destinationPath = new File(zipPath, entry.getName());

        // create parent directories
        destinationPath.getParentFile().mkdirs();

        // if the entry is a file extract it
        if (entry.isDirectory()) {
          continue;
        } else {
          EnderCore.logger.info("Extracting file: " + destinationPath);

          BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

          int b;
          byte buffer[] = new byte[1024];

          FileOutputStream fos = new FileOutputStream(destinationPath);

          BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

          while ((b = bis.read(buffer, 0, 1024)) != -1) {
            bos.write(buffer, 0, b);
          }

          bos.close();
          bis.close();
        }
      }
    } catch (IOException e) {
      EnderCore.logger.error("Error opening zip file" + e);
    } finally {
      try {
        if (zipFile != null) {
          zipFile.close();
        }
      } catch (IOException e) {
        EnderCore.logger.error("Error while closing zip file" + e);
      }
    }

    return NullHelper.notnullJ(temp, "Neither '@Nonnull', 'final', nor the single assignment from 'new' stopped this variable from going null. Whow!");
  }

  /**
   * @author McDowell - http://stackoverflow.com/questions/1399126/java-util-zip -recreating-directory-structure
   *
   * @param directory
   *          The directory to zip the contents of. Content structure will be preserved.
   * @param zipfile
   *          The zip file to output to.
   * @throws IOException
   */
  @SuppressWarnings("resource")
  public static void zipFolderContents(File directory, File zipfile) throws IOException {
    URI base = directory.toURI();
    Deque<File> queue = new LinkedList<File>();
    queue.push(directory);
    OutputStream out = new FileOutputStream(zipfile);
    Closeable res = out;
    try {
      ZipOutputStream zout = new ZipOutputStream(out);
      res = zout;
      while (!queue.isEmpty()) {
        File dir = queue.pop();
        for (File child : dir.listFiles()) {
          String name = base.relativize(child.toURI()).getPath();
          if (child.isDirectory()) {
            queue.push(child);
            name = name.endsWith("/") ? name : name + "/";
            zout.putNextEntry(new ZipEntry(name));
          } else {
            zout.putNextEntry(new ZipEntry(name));
            copy(child, zout);
            zout.closeEntry();
          }
        }
      }
    } finally {
      res.close();
    }
  }

  /** @see #zipFolderContents(File, File) */
  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    while (true) {
      int readCount = in.read(buffer);
      if (readCount < 0) {
        break;
      }
      out.write(buffer, 0, readCount);
    }
  }

  /** @see #zipFolderContents(File, File) */
  private static void copy(File file, OutputStream out) throws IOException {
    InputStream in = new FileInputStream(file);
    try {
      copy(in, out);
    } finally {
      in.close();
    }
  }

  @Nonnull
  public static File writeToFile(String filepath, String json) {
    File file = new File(filepath);

    try {
      file.createNewFile();
      FileWriter fw = new FileWriter(file);
      fw.write(json);
      fw.flush();
      fw.close();
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void safeDelete(File file) {
    try {
      file.delete();
    } catch (Exception e) {
      EnderCore.logger.error("Deleting file " + file.getAbsolutePath() + " failed.");
    }
  }

  public static void safeDeleteDirectory(File file) {
    try {
      FileUtils.deleteDirectory(file);
    } catch (Exception e) {
      EnderCore.logger.error("Deleting directory " + file.getAbsolutePath() + " failed.");
    }
  }

  /**
   * I don't think this ever worked.
   *
   * @param directory
   */
  @Deprecated
  public static void loadLangFiles(File directory) {
    // NO-OP
  }

  /**
   * Same as <code>manuallyChangeConfigValue(String, String, String)</code>, but with an additional parameter for <i>what</i> config file to edit
   *
   * @param filePathFromConfigFolder
   *          - the full path to the files, including extensions, from inside config/
   * @param prefix
   *          - The prefix of the config option (anything before '='), must match exactly.
   * @param from
   *          - The setting to change it from
   * @param to
   *          - The setting to change it to
   * @return whether anything changed
   */
  public static boolean manuallyChangeConfigValue(String filePathFromConfigFolder, String prefix, String from, String to) {
    File config = new File(ConfigHandler.configFolder.getAbsolutePath() + "/" + filePathFromConfigFolder);
    boolean found = false;

    try {
      FileReader fr1 = new FileReader(config);
      BufferedReader read = new BufferedReader(fr1);

      ArrayList<String> strings = new ArrayList<String>();

      while (read.ready()) {
        strings.add(read.readLine());
      }

      fr1.close();
      read.close();

      FileWriter fw = new FileWriter(config);
      BufferedWriter bw = new BufferedWriter(fw);

      for (String s : strings) {
        if (!found && s.contains(prefix + "=" + from) && !s.contains("=" + to)) {
          s = s.replace(prefix + "=" + from, prefix + "=" + to);
          EnderCore.logger.info("Successfully changed config value " + prefix + " from " + from + " to " + to);
          found = true;
        }

        fw.write(s + "\n");
      }

      bw.flush();
      bw.close();
    } catch (Throwable t) {
      t.printStackTrace();
    }

    return found;
  }

  /**
   * Finds the config value in the file specified (path starting after config/), and for the key specified
   *
   * @param filePathFromConfigFolder
   *          - The path to the file, everything up to config/ is calculated for you
   * @param key
   *          - A key to find the value by, does not need to match exactly
   * @return A parseable string that can be transformed into any of the types of config values, for instance using <code>Boolean.parseBoolean(String)</code>
   */
  public static String manuallyGetConfigValue(String filePathFromConfigFolder, String key) {
    File config = new File(ConfigHandler.configFolder.getAbsolutePath() + "/" + filePathFromConfigFolder);
    Scanner scan = null;

    try {
      scan = new Scanner(config);
    } catch (FileNotFoundException e) {
      return "";
    }

    while (scan.hasNext()) {
      String s = scan.next();

      if (s.contains(key)) {
        scan.close();
        return s.substring(s.indexOf("=") + 1, s.length());
      }
    }

    scan.close();
    return "";
  }
}
