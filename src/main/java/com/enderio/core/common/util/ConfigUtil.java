package com.enderio.core.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

import com.enderio.core.EnderCore;
import com.enderio.core.common.config.ConfigHandler;

public class ConfigUtil {

  /**
   * Same as <code>manuallyChangeConfigValue(String, String, String)</code>, but
   * with an additional parameter for <i>what</i> config file to edit
   * 
   * @param filePathFromConfigFolder
   *          - the full path to the files, including extensions, from inside
   *          config/
   * @param prefix
   *          - The prefix of the config option (anything before '='), must
   *          match exactly.
   * @param from
   *          - The setting to change it from
   * @param to
   *          - The setting to change it to
   * @return whether anything changed
   */
  public boolean manuallyChangeConfigValue(String filePathFromConfigFolder, String prefix, String from, String to) {
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
   * Finds the config value in the file specified (path starting after config/),
   * and for the key specified
   * 
   * @param filePathFromConfigFolder
   *          - The path to the file, everything up to config/ is calculated for
   *          you
   * @param key
   *          - A key to find the value by, does not need to match exactly
   * @return A parseable string that can be transformed into any of the types of
   *         config values, for instance using
   *         <code>Boolean.parseBoolean(String)</code>
   */
  public String manuallyGetConfigValue(String filePathFromConfigFolder, String key) {
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
