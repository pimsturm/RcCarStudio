package com.github.pimsturm.commandmessenger;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Utility class providing escaping functions
 */
public final class Escaping
{
    // Remove all occurrences of removeChar unless it is escaped by escapeChar

    private static char fieldSeparator = ',';	// The field separator
    private static char commandSeparator = ';';	// The command separator
    private static char escapeCharacter = '/';	// The escape character

    /**
     * Gets the escape character.
     * @return The escape character.
     */
    public static char getEscapeCharacter() {
        return escapeCharacter;
    }

    /**
     * Sets custom escape characters.
     * @param fieldSeparator The field separator.
     * @param commandSeparator The command separator.
     * @param escapeCharacter The escape character.
     */
    public static void setEscapeChars(char fieldSeparator, char commandSeparator, char escapeCharacter)
    {
        Escaping.fieldSeparator = fieldSeparator;
        Escaping.commandSeparator = commandSeparator;
        Escaping.escapeCharacter = escapeCharacter;
    }

    /**
     * Removes all occurences of a specific character unless escaped.
     * @param input The input.
     * @param removeChar The  character to remove.
     * @param escapeChar The escape character.
     * @return The string with all removeChars removed.
     */
    public static String remove(String input, char removeChar, char escapeChar)
    {
        String output = "";
        IsEscaped escaped = new IsEscaped();
        for (int i = 0; i < input.length(); i++)
        {
            char inputChar = input.charAt(i);
            boolean isEscaped = escaped.isEscapedChar(inputChar);
            if (inputChar != removeChar || isEscaped)
            {
                output += inputChar;
            }
        }
        return output;
    }

    // Split String on separator character unless it is escaped by escapeChar

    /**
     * Splits.
     * @param input The input
     * @param separator The Separator
     * @param escapeCharacter The escape character.
     * @param removeEmptyEntries Options for controlling the string split.
     * @return
     */
    public static String[] split(String input, char separator, char escapeCharacter,
                                 Boolean removeEmptyEntries)
    {
        String word = "";
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < input.length(); i++)
        {
            char t = input.charAt(i);
            if (t == separator)
            {
                result.add(word);
                word = "";
            }
            else
            {
                if (t == escapeCharacter)
                {
                    word += t;
                    if (i < input.length() - 1) t = input.charAt(++i);
                }
                word += t;
            }
        }
        result.add(word);
        if (removeEmptyEntries) {
            result.removeAll(Collections.singleton(""));
        }

        return (String[]) result.toArray();
    }

    /**
     *
     * @param input The unescaped input string.
     * @return Escaped output string.
     */
    public static String escape(String input)
    {
        String[] escapeChars = new String[]
                {
                        String.valueOf(escapeCharacter),
                        String.valueOf(fieldSeparator),
                        String.valueOf(commandSeparator),
                        "\0"
                };
//        input = escapeChars.Aggregate(input,
//                (current, escapeChar) =>
//        current.Replace(escapeChar, escapeCharacter + escapeChar));
        return input;
    }

    /**
     *  Unescapes the input string.
     * @param input The escaped input string.
     * @return The unescaped output string.
     */
    public static String unescape(String input)
    {
        String output = "";
        // Move unescaped characters right
        for (char fromChar = 0; fromChar < input.length(); fromChar++)
        {
            if (input.charAt(fromChar) == escapeCharacter)
            {
                fromChar++;
            }
            output += input.charAt(fromChar);
        }
        return output;
    }
}
