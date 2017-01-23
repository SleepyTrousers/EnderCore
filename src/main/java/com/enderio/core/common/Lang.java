package com.enderio.core.common;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.enderio.core.common.util.NullHelper;
import com.google.common.collect.Lists;

import net.minecraft.util.text.translation.I18n;

@SuppressWarnings("deprecation")
public class Lang {

  private static final @Nonnull String REGEX = "\\" + '|';
  public static final char CHAR = '|';

  private final @Nonnull String prefix;

  public Lang(@Nonnull String locKey) {
    this.prefix = NullHelper.notnullJ(locKey.concat("."), "String.concat()");
  }

  /**
   * @return The prefix assigned to this Lang object.
   */
  public @Nonnull String getPrefix() {
    return this.prefix;
  }

  /**
   * Adds the stored prefix to this string, separating with a period. Using this method returns the string that is used for localizing if you passed this arg
   * into {@link #localize(String, Object...)}.
   *
   * @param suffix
   *          The suffix string
   * @return The full string
   */
  public @Nonnull String addPrefix(@Nonnull String suffix) {
    return NullHelper.notnullJ(prefix.concat(suffix), "String.concat()");
  }

  /**
   * Localizes the string passed, first appending the prefix stored in this instance of the class.
   *
   * @param unloc
   *          The unlocalized string.
   * @param args
   *          The args to format the localized text with.
   *
   * @return A localized string.
   */
  public @Nonnull String localize(@Nonnull String unloc, @Nonnull Object... args) {
    return localizeExact(addPrefix(unloc), args);
  }

  /**
   * Localizes the string passed, first appending the prefix stored in this instance of the class.
   *
   * @param unloc
   *          The unlocalized string.
   *
   * @return A localized string.
   */
  public @Nonnull String localize(@Nonnull String unloc) {
    return localizeExact(addPrefix(unloc));
  }

  /**
   * Ignores the prefix stored in this instance of the class and localizes the raw string passed.
   *
   * @param unloc
   *          The unlocalized string.
   * @param args
   *          The args to format the localized text with.
   *
   * @return A localized string.
   */
  public @Nonnull String localizeExact(@Nonnull String unloc, @Nonnull Object... args) {
    return I18n.translateToLocalFormatted(unloc, args);
  }

  /**
   * Ignores the prefix stored in this instance of the class and localizes the raw string passed.
   *
   * @param unloc
   *          The unlocalized string.
   *
   * @return A localized string.
   */
  public @Nonnull String localizeExact(@Nonnull String unloc) {
    return I18n.translateToLocal(unloc);
  }

  /**
   * Splits the localized text on "|" into a String[].
   *
   * @param unloc
   *          The unlocalized string.
   * @param args
   *          The args to format the localized text with.
   * @return A localized list of strings.
   */
  public @Nonnull String[] localizeList(@Nonnull String unloc, @Nonnull String... args) {
    return splitList(localize(unloc, (Object[]) args));
  }

  /**
   * Splits the localized text on "|" into a String[].
   *
   * @param unloc
   *          The unlocalized string.
   * @return A localized list of strings.
   */
  public @Nonnull String[] localizeList(@Nonnull String unloc) {
    return splitList(localize(unloc));
  }

  /**
   * Localizes all strings in a list, using the prefix.
   *
   * @param unloc
   *          The list of unlocalized strings.
   * @return A list of localized versions of the passed strings.
   */
  public @Nonnull List<String> localizeAll(@Nonnull List<String> unloc) {
    List<String> ret = Lists.newArrayList();
    for (String s : unloc) {
      final @Nullable String notnulliswear = s;
      ret.add(localize(notnulliswear == null ? "null" : notnulliswear));
    }
    return ret;
  }

  /**
   * Localizes all strings in an array, using the prefix.
   *
   * @param unloc
   *          The array of unlocalized strings.
   * @return An array of localized versions of the passed strings.
   */
  public @Nonnull String[] localizeAll(@Nonnull Lang lang, @Nonnull String... unloc) {
    String[] ret = new String[unloc.length];
    for (int i = 0; i < ret.length; i++) {
      final @Nullable String notnulliswear = unloc[i];
      ret[i] = lang.localize(notnulliswear == null ? "null" : notnulliswear);
    }
    return ret;
  }

  /**
   * Splits a list of strings based on {@value #CHAR}
   *
   * @param list
   *          The list of strings to split
   * @return An array of strings split on {@value #CHAR}
   */
  public @Nonnull String[] splitList(@Nonnull String list) {
    return NullHelper.notnullJ(list.split(REGEX), "String.split()");
  }

  public @Nonnull String[] splitList(@Nonnull String list, @Nonnull String split) {
    return NullHelper.notnullJ(list.split(split), "String.split()");
  }

  /**
   * Checks if the passed string (plus the prefix) has a localization mapped.
   *
   * @param unloc
   *          The unlocalized suffix
   * @return True if there is a localization mapped, false otherwise.
   */
  public boolean canLocalize(@Nonnull String unloc) {
    return canLocalizeExact(addPrefix(unloc));
  }

  /**
   * Checks if the passed string has a localization mapped. Does not use the prefix.
   *
   * @param unloc
   *          The unlocalized string
   * @return True if there is a localization mapped, false otherwise.
   */
  public boolean canLocalizeExact(@Nonnull String unloc) {
    return I18n.canTranslate(unloc);
  }
}
