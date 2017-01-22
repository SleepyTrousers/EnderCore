package com.enderio.core.api.common.imc;

public class IMC {

  /**
   * Use an IMC event with this string to add a right click crop as compatible. Syntax is a single string, with '|' separating the values, in the order seed,
   * block, meta, resetMeta. A valid entry would look like this:
   * <p>
   * {@literal "minecraft:wheat_seeds|minecraft:wheat|7|0"}
   * <p>
   * The "reset meta" is the metadata that the plant will be reset to when it is harvested. Regular meta is the meta that must match the plant in world for the
   * harvest to occur.
   */
  public static final String ADD_RIGHT_CLICK_CROP = "addRightClickCrop";

}
