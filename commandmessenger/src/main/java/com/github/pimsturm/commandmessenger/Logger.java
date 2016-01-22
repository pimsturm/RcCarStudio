package com.github.pimsturm.commandmessenger;

import java.io.FileWriter;
import java.io.IOException;

public class Logger {
    //private static final Encoding StringEncoder = Encoding.GetEncoding("ISO-8859-1");	// The string encoder
    private static FileWriter fileWriter;

    public Logger()
    {
        logFileName = null;
        isEnabled = true;
    }

    static private boolean isEnabled;
    static public boolean getIsEnabled() {return isEnabled;}
    static public void setIsEnabled(boolean isEnabled) {
        Logger.isEnabled = isEnabled;}

    static private boolean isOpen;
    static public boolean getIsOpen() {return isOpen;}
    static private boolean directFlush;
    static public boolean getDirectFlush() {return directFlush;}
    static void setDirectFlush(boolean directFlush) {
        Logger.directFlush = directFlush;}

    static private String logFileName;

    /**
     * Gets the log file name.
     * @return
     */
    static public String getLogFileName() {return logFileName;}


    static public boolean open()
    {
        return open(logFileName);
    }

    static public boolean open(String logFileName)
    {
        if (isOpen && Logger.logFileName == logFileName) return true;

        Logger.logFileName = logFileName;
        if (isOpen)
        {
            try
            {
                fileWriter.close();
            }
            catch (IOException e) { }
            isOpen = false;
        }

        try
        {
            fileWriter = new FileWriter(logFileName);
        }
        catch (IOException e)
        {
            return false;
        }
        isOpen = true;
        return true;
    }

    static public void close()
    {
        if (!isOpen) return;
        try
        {
            fileWriter.close();
        }
        catch (IOException e) { }
        isOpen = false;
    }

    static public void log(String logString)
    {
        if (!isEnabled || !isOpen) return;
        try {
            fileWriter.write(logString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (directFlush) try {
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void logLine(String logString)
    {
        log(logString + "\n");
    }
}
