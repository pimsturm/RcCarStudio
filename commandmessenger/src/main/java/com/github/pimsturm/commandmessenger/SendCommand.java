package com.github.pimsturm.commandmessenger;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * A command to be sent by CmdMessenger
 */
public class SendCommand extends Command {
    private static final String TAG = "Command";
    private final ArrayList<Callable<Object>> lazyArguments = new ArrayList<Callable<Object>>();
    private Settings settings;

    private boolean reqAc;

    /**
     * Indicates if we want to wait for an acknowledge command.
     * @return true if request acknowledge, false if not.
     */
    public boolean getReqAc() {return reqAc; }
    public void setReqAc(boolean reqAc) {
        this.reqAc = reqAc; }

    /// <summary>  </summary>
    /// <value> the acknowledge command ID. </value>
    private int ackCmdId;

    /**
     * Sets the acknowledge command ID.
     * @param ackCmdId The command ID
     */
    public void setAckCmdId(int ackCmdId) { this.ackCmdId = ackCmdId; }

    /**
     * Gets the acknowledge command ID.
     * @return The command ID
     */
    public int getAckCmdId() {return ackCmdId; }

    /// <summary>  </summary>
    /// <value> The timeout on waiting for an acknowledge</value>
    private int timeout;

    /**
     * Sets the time we want to wait for the acknowledge command.
     * @param timeOut The time we want to wait in milliseconds
     */
    public void setTimeout(int timeOut) {
        timeout = timeOut; }

    /**
     * Gets the time we want to wait for the acknowledge command.
     * @return The time we want to wait in milliseconds
     */
    public int getTimeout() {return timeout;}

    /**
     * Constructor.
     * @param cmdId The command ID.
     */
    public SendCommand(int cmdId)
    {
        init(cmdId, false, 0, 0);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, String argument)
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param arguments The arguments.
     */
    public SendCommand(int cmdId, String[] arguments)
    {
        init(cmdId, false, 0, 0);
        addArguments(arguments);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, float argument)
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, double argument)
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, UInt16 argument)
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, short argument) //Int16 argument
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, UInt32 argument)
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, int argument)   //Int32 argument
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     */
    public SendCommand(int cmdId, boolean argument)
    {
        init(cmdId, false, 0, 0);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param ackCmdId Acknowledge command ID.
     * @param timeout he timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, int ackCmdId, int timeout)
    {
        init(cmdId, true, ackCmdId, timeout);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, String argument, int ackCmdId, int timeout)
    {
        init(cmdId, true, ackCmdId, timeout);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param arguments The arguments.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, String[] arguments, int ackCmdId, int timeout)
    {
        init(cmdId, true, ackCmdId, timeout);
        addArguments(arguments);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, float argument, int ackCmdId, int timeout)
    {
        init(cmdId, true, ackCmdId, timeout);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, double argument, int ackCmdId, int timeout)
    {
        init(cmdId, true, ackCmdId, timeout);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, short argument, int ackCmdId, int timeout) //Int16 argument
    {
        init(cmdId, true, ackCmdId, timeout);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, UInt16 argument, int ackCmdId, int timeout)
    {
        init(cmdId, true, ackCmdId, timeout);
        addArgument(argument);
    }

    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
/*    public sendCommand(int cmdId, int argument, int ackCmdId, int timeout)    //Int32 argument
    {
        init(cmdId, true, ackCmdId, timeout);
        addArgument(argument);
    }
*/
    /**
     * Constructor.
     * @param cmdId Command ID
     * @param argument The argument.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    public SendCommand(int cmdId, UInt32 argument, int ackCmdId, int timeout)
    {
        init(cmdId, true, ackCmdId, timeout);
        addArgument(argument);
    }

    /**
     * Initializes this object.
     * @param cmdId Command ID
     * @param reqAc true to request ac.
     * @param ackCmdId Acknowledge command ID.
     * @param timeout The timeout on waiting for an acknowledge
     */
    private void init(int cmdId, boolean reqAc, int ackCmdId, int timeout)
    {
        this.reqAc = reqAc;
        setCmdId(cmdId);
        this.ackCmdId = ackCmdId;
        this.timeout = timeout;
        this.settings = Settings.getInstance();
    }

    // ***** String based **** /

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(final String argument)
    {
        if (argument != null)
            lazyArguments.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    return cmdArgs.add(argument);
                }
            });
    }

    /**
     * Adds command arguments.
     * @param arguments The arguments.
     */
    public void addArguments(final String[] arguments)
    {
        if (arguments != null)
            lazyArguments.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (String argument: arguments
                         ) {
                        cmdArgs.add(argument);
                    }
                    return cmdArgs;
                }
            });
    }

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(final float argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(String.valueOf(argument));
            }
        });
    }

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(final Double argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (settings.getBoardType() == BoardType.Bit16) {
                    // Not completely sure if this is needed for plain text sending.
                    float floatArg = argument.floatValue();
                    cmdArgs.add(String.valueOf(floatArg));
                } else {
                    cmdArgs.add(argument.toString());
                }
                return cmdArgs;
            }
        });


    }

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(final short argument) //Int16
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(String.valueOf(String.valueOf(argument)));
            }
        });

    }

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(final UInt16 argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(String.valueOf(argument));    //CultureInfo.InvariantCulture
            }
        });

    }

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(final int argument)     //Int32 argument
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(String.valueOf(argument));    //CultureInfo.InvariantCulture
            }
        });

    }

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(final UInt32 argument)
    {
        // Make sure the other side can read this: on a 16 processor, read as Long
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(String.valueOf(argument));    //CultureInfo.InvariantCulture
            }
        });

    }

    /**
     * Adds a command argument.
     * @param argument The argument.
     */
    public void addArgument(boolean argument)
    {
        addArgument((int) (argument ? 1 : 0));
    }

    // ***** Binary **** /

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final String argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(Escaping.escape(argument));
            }
        });
    }

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final float argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(BinaryConverter.toString(argument));
            }
        });
    }

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final Double argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(settings.getBoardType() == BoardType.Bit16
                        ? BinaryConverter.toString(argument.floatValue())
                        : BinaryConverter.toString(argument));
            }
        });

    }

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final short argument)      //Int16 argument
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(BinaryConverter.toString(argument));
            }
        });
    }

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final UInt16 argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(BinaryConverter.toString(argument));
            }
        });
    }

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final int argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(BinaryConverter.toString(argument));
            }
        });
    }

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final UInt32 argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(BinaryConverter.toString(argument));
            }
        });
    }

    /**
     * Adds a binary command argument.
     * @param argument The argument.
     */
    public void addBinArgument(final boolean argument)
    {
        lazyArguments.add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return cmdArgs.add(BinaryConverter.toString(argument ? (byte) 1 : (byte) 0));
            }
        });
    }

    public void initArguments()
    {
        cmdArgs.clear();
        for (Callable<Object> action : lazyArguments)
        {
            try {
                action.call();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }
}
