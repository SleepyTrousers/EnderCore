package com.enderio.core.common;

import java.util.List;

import net.minecraft.util.StatCollector;

import com.google.common.collect.Lists;

public class Lang {

    private static final String REGEX = "\\" + '|';
    public static final char CHAR = '|';

    private final String prefix;

    public Lang(String locKey) {
        this.prefix = locKey.concat(".");
    }

    /**
     * @return The prefix assigned to this Lang object.
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Adds the stored prefix to this string, separating with a period. Using this method returns the string that is
     * used for localizing if you passed this arg into {@link #localize(String, Object...)}.
     * 
     * @param suffix The suffix string
     * @return The full string
     */
    public String addPrefix(String suffix) {
        return prefix.concat(suffix);
    }

    /**
     * Localizes the string passed, first appending the prefix stored in this instance of the class.
     * 
     * @param unloc The unlocalized string.
     * @param args  The args to format the localized text with.
     * 
     * @return A localized string.
     */
    public String localize(String unloc, Object... args) {
        return localizeExact(addPrefix(unloc), args);
    }

    /**
     * Localizes the string passed, first appending the prefix stored in this instance of the class.
     *
     * @param unloc The unlocalized string.
     *
     * @return A localized string.
     */
    public String localize(String unloc) {
        return localizeExact(addPrefix(unloc));
    }

    /**
     * Ignores the prefix stored in this instance of the class and localizes the raw string passed.
     * 
     * @param unloc The unlocalized string.
     * @param args  The args to format the localized text with.
     * 
     * @return A localized string.
     */
    public String localizeExact(String unloc, Object... args) {
        return StatCollector.translateToLocalFormatted(unloc, args);
    }

    /**
     * Ignores the prefix stored in this instance of the class and localizes the raw string passed.
     *
     * @param unloc The unlocalized string.
     *
     * @return A localized string.
     */
    public String localizeExact(String unloc) {
        return StatCollector.translateToLocal(unloc);
    }

    /**
     * Splits the localized text on "|" into a String[].
     * 
     * @param unloc The unlocalized string.
     * @param args  The args to format the localized text with.
     * @return A localized list of strings.
     */
    public String[] localizeList(String unloc, String... args) {
        return splitList(localize(unloc, (Object[]) args));
    }

    /**
     * Splits the localized text on "|" into a String[].
     *
     * @param unloc The unlocalized string.
     * @return A localized list of strings.
     */
    public String[] localizeList(String unloc) {
        return splitList(localize(unloc));
    }

    /**
     * Localizes all strings in a list, using the prefix.
     * 
     * @param unloc The list of unlocalized strings.
     * @return A list of localized versions of the passed strings.
     */
    public List<String> localizeAll(List<String> unloc) {
        List<String> ret = Lists.newArrayList();
        for (String s : unloc) {
            ret.add(localize(s));
        }
        return ret;
    }

    /**
     * Localizes all strings in an array, using the prefix.
     * 
     * @param unloc The array of unlocalized strings.
     * @return An array of localized versions of the passed strings.
     */
    public String[] localizeAll(Lang lang, String... unloc) {
        String[] ret = new String[unloc.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = lang.localize(unloc[i]);
        }
        return ret;
    }

    /**
     * Splits a list of strings based on {@value #CHAR}
     * 
     * @param list The list of strings to split
     * @return An array of strings split on {@value #CHAR}
     */
    public String[] splitList(String list) {
        return list.split(REGEX);
    }

    public String[] splitList(String list, String split) {
        return list.split(split);
    }

    /**
     * Checks if the passed string (plus the prefix) has a localization mapped.
     * 
     * @param unloc The unlocalized suffix
     * @return True if there is a localization mapped, false otherwise.
     */
    public boolean canLocalize(String unloc) {
        return canLocalizeExact(addPrefix(unloc));
    }

    /**
     * Checks if the passed string has a localization mapped. Does not use the prefix.
     * 
     * @param unloc The unlocalized string
     * @return True if there is a localization mapped, false otherwise.
     */
    public boolean canLocalizeExact(String unloc) {
        return StatCollector.canTranslate(unloc);
    }
}
