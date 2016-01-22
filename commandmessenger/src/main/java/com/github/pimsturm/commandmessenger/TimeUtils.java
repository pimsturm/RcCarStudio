package com.github.pimsturm.commandmessenger;

import java.util.Date;

/**
 * class to get a timestamp
 */
public final class TimeUtils {

    public static long millis = new Date().getTime();
    private static long prevTime;

    public void setPrevTime(long prevTime) {
        TimeUtils.prevTime = prevTime;
    }


    // Returns if it has been more than interval (in ms) ago. Used for periodic actions
    public static boolean hasExpired(long interval)
    {
        if (millis - prevTime > interval)
        {
            prevTime = millis;
            return true;
        }
        return false;
    }
}
