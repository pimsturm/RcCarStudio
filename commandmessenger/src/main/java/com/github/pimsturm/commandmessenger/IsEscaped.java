package com.github.pimsturm.commandmessenger;

/**
 * Class for bookkeeping which characters in the stream are escaped.
 */
public class IsEscaped {
    private char lastChar = '\0';  // The last character

    /**
     * Returns if the character is escaped
     * Note create new instance for every independent string
     * @param currChar The current character.
     * @return true if the character is escaped, false if not.
     */
    public boolean isEscapedChar(char currChar)
    {
        boolean escaped = (lastChar == Escaping.getEscapeCharacter());
        lastChar = currChar;

        // special case: the escape char has been escaped:
        if (lastChar == Escaping.getEscapeCharacter() && escaped)
        {
            lastChar = '\0';
        }
        return escaped;
    }
}

