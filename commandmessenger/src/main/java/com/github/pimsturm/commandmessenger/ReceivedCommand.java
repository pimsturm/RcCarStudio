package com.github.pimsturm.commandmessenger;

import java.util.Arrays;

/**
 * A command received from CmdMessenger
 */
public class ReceivedCommand extends Command {
    private int parameter = -1;    // The parameter
    private boolean dumped = true;  // true if parameter has been dumped

    private String rawString;

    /**
     * Gets the command input.
     * @return The raw string.
     */
    public String getRawString() {return rawString;}
    public void setRawString(String rawString) {
        this.rawString = rawString;}

    public ReceivedCommand() {
        super();
    }

    /**
     * Constructor.
     * @param rawArguments All command arguments, first one is command ID
     */
    public ReceivedCommand(String[] rawArguments)
    {
        super();
        setCmdId(rawArguments != null && rawArguments.length !=0 ? Integer.getInteger(rawArguments[0], -1): -1);
        if (getCmdId() < 0) return;
        if (rawArguments.length > 1)
        {
            String[] array = new String[rawArguments.length - 1];
            System.arraycopy(rawArguments, 1, array, 0, array.length);
            cmdArgs.addAll(Arrays.asList(array));
        }
    }

    /**
     * Fetches the next argument.
     * @return true if it succeeds, false if it fails.
     */
    public boolean next()
    {
        // If this parameter has already been read, see if there is another one
        if (dumped)
        {
            if (parameter < cmdArgs.size()-1)
            {
                parameter++;
                dumped = false;
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * returns if a next command is available
     * @return true if it succeeds, false if it fails.
     */
    public boolean available()
    {
        return next();
    }

    // ***** String based **** /

    /**
     * Reads the current argument as short value.
     * @return The short value.
     */
    public short readInt16Arg() //Int16
    {
        if (next())
        {
            try {
                short current = Short.parseShort(cmdArgs.get(parameter));
                dumped = true;
                return current;
            } catch (NumberFormatException e){}
        }
        return 0;
    }

    /**
     * Reads the current argument as unsigned short value.
     * @return The unsigned short value.
     */
    public UInt16 readUInt16Arg()
    {
        if (next())
        {
            try {
                UInt16 current = new UInt16(cmdArgs.get(parameter));
                dumped = true;
                return current;
            } catch (NumberFormatException e) {}
        }
        return new UInt16(0);
    }

    /**
     * Reads the current argument as boolean value.
     * @return The boolean value.
     */
    public boolean readBoolArg()
    {
        return (readInt32Arg() != 0);
    }

    /**
     * Reads the current argument as int value.
     * @return The int value.
     */
    public int readInt32Arg()     //Int32
    {
        if (next())
        {
            try {
                int current = Integer.parseInt(cmdArgs.get(parameter));
                dumped = true;
                return current;
            } catch (NumberFormatException e){}
        }
        return 0;
    }

    /**
     * Reads the current argument as unsigned int value.
     * @return The unsigned int value.
     */
    public UInt32 readUInt32Arg()
    {
        if (next())
        {
            try {
                UInt32 current = new UInt32(cmdArgs.get(parameter));
                dumped = true;
                return current;
            } catch (NumberFormatException e) {}

        }
        return new UInt32(0);
    }

    /**
     * Reads the current argument as a float value.
     * @return The float value.
     */
    public float readFloatArg()
    {
        if (next())
        {
            try {
                float current = Float.parseFloat(cmdArgs.get(parameter));
                dumped = true;
                return current;
            } catch (NumberFormatException e){}
        }
        return 0;
    }

    /**
     * Reads the current argument as a double value.
     * @return The unsigned double value.
     */
    public double readDoubleArg()
    {
        if (next())
        {
            if (settings.getBoardType() == BoardType.Bit16)
            {
                try {
                    float current = Float.parseFloat(cmdArgs.get(parameter));
                    dumped = true;
                    return (double)current;
                } catch (NumberFormatException e){}

            }
            else
            {
                try {
                    double current = Double.parseDouble(cmdArgs.get(parameter));
                    dumped = true;
                    return current;
                } catch (NumberFormatException e){}

            }
        }
        return 0;
    }

    /**
     * Reads the current argument as a string value.
     * @return The string value.
     */
    public String readStringArg()
    {
        if (next())
        {
            if (cmdArgs.get(parameter) != null)
            {
                dumped = true;
                return cmdArgs.get(parameter);
            }
        }
        return "";
    }

    // ***** Binary **** /

    /**
     * Reads the current binary argument as a float value.
     * @return The float value.
     */
    public float readBinFloatArg()
    {
        if (next())
        {
            Float current = BinaryConverter.toFloat(cmdArgs.get(parameter));
            if (current != null)
            {
                dumped = true;
                return (float) current;
            }
        }
        return 0;
    }

    /**
     * Reads the current binary argument as a double value.
     * @return The double value.
     */
    public double readBinDoubleArg()
    {
        if (next())
        {
            if (settings.getBoardType() == BoardType.Bit16)
            {
                Float current = BinaryConverter.toFloat(cmdArgs.get(parameter));
                if (current != null)
                {
                    dumped = true;
                    return (double) current;
                }
            }
            else
            {
                Double current = BinaryConverter.toDouble(cmdArgs.get(parameter));
                if (current != null)
                {
                    dumped = true;
                    return current;
                }
            }
        }
        return 0;
    }

    /**
     * Reads the current binary argument as a short value.
     * @return The short value.
     */
    public short readBinInt16Arg() //Int16
    {
        if (next())
        {
            Short current = BinaryConverter.toInt16(cmdArgs.get(parameter));
            if (current != null)
            {
                dumped = true;
                return current;
            }
        }
        return 0;
    }

    /**
     * Reads the current binary argument as a unsigned short value.
     * @return The unsigned short value.
     */
    public UInt16 readBinUInt16Arg()
    {
        if (next())
        {
            UInt16 current = BinaryConverter.toUInt16(cmdArgs.get(parameter));
            if (current != null)
            {
                dumped = true;
                return current;
            }
        }
        return new UInt16(0);
    }

    /**
     * Reads the current binary argument as an int value.
     * @return The int32 value.
     */
    public int readBinInt32Arg()      //Int32
    {
        if (next())
        {
            Integer current = BinaryConverter.toInt32(cmdArgs.get(parameter));
            if (current != null)
            {
                dumped = true;
                return current;
            }
        }
        return 0;
    }

    /**
     * Reads the current binary argument as an unsigned int value.
     * @return The unsigned int value.
     */
    public UInt32 readBinUInt32Arg()
    {
        if (next())
        {
            UInt32 current = BinaryConverter.toUInt32(cmdArgs.get(parameter));
            if (current != null)
            {
                dumped = true;
                return (UInt32) current;
            }
        }
        return new UInt32(0);
    }

    /**
     * Reads the current binary argument as a string value.
     * @return The string value.
     */
    public String readBinStringArg()
    {
        if (next())
        {
            if (cmdArgs.get(parameter) != null)
            {
                dumped = true;
                return Escaping.unescape(cmdArgs.get(parameter));
            }
        }
        return "";
    }

    /**
     * Reads the current binary argument as a boolean value.
     * @return The boolean value.
     */
    public boolean readBinBoolArg()
    {
        if (next())
        {
            Byte current = BinaryConverter.toByte(cmdArgs.get(parameter));
            if (current != null)
            {
                dumped = true;
                return (current != 0);
            }
        }
        return false;
    }
}
