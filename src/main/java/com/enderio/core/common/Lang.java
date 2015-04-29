package com.enderio.core.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.util.StatCollector;

@AllArgsConstructor
@Getter
public class Lang
{
    private static final String REGEX = "\\" + '|';
    public static final char CHAR = '|';

    private String locKey;

    /**
     * Adds the stored prefix to this string, separating with a period. Using this method returns the string that is used for localizing if you passed
     * this arg into {@link #localize(String, Object...)}.
     * 
     * @param suffix
     *            The suffix string
     * @return The full string
     */
    public String addPrefix(String suffix)
    {
        return locKey + "." + suffix;
    }

    /**
     * Localizes the string passed, first appending the prefix stored in this instance of the class.
     * 
     * @param unloc
     *            The unlocalized string.
     * @param args
     *            The args to format the localized text withi.
     * 
     * @return A localized string.
     */
    public String localize(String unloc, Object... args)
    {
        return localizeExact(addPrefix(unloc), args);
    }

    /**
     * Ignores the prefix stored in this instance of the class and localizes the raw string passed.
     * 
     * @param unloc
     *            The unlocalized string.
     * @param args
     *            The args to format the localized text withi.
     * 
     * @return A localized string.
     */
    public String localizeExact(String unloc, Object... args)
    {
        return StatCollector.translateToLocalFormatted(unloc, args);
    }

    /**
     * Splits the localized text on "|" into a String[].
     * 
     * @param unloc
     *            The unlocalized string.
     * @param args
     *            The args to format the localized text withi.
     * @return A localized list of strings.
     */
    public String[] localizeList(String unloc, String... args)
    {
        return splitList(localize(unloc, true, args));
    }

    /**
     * Splits a list of strings based on {@value #CHAR}
     * 
     * @param list
     *            The list of strings to split
     * @return An array of strings split on {@value #CHAR}
     */
    public String[] splitList(String list)
    {
        return list.split(REGEX);
    }

    /**
     * Checks if the passed string (plus the prefix) has a localization mapped.
     * 
     * @param unloc
     *            The unlocalized suffix
     * @return True if there is a localization mapped, false otherwise.
     */
    public boolean canLocalize(String unloc)
    {
        return canLocalizeExact(addPrefix(unloc));
    }

    /**
     * Checks if the passed string has a localization mapped. Does not use the prefix.
     * 
     * @param unloc
     *            The unlocalized string
     * @return True if there is a localization mapped, false otherwise.
     */
    public boolean canLocalizeExact(String unloc)
    {
        return StatCollector.canTranslate(unloc);
    }
}
