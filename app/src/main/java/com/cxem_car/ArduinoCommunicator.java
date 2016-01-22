package com.cxem_car;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.github.pimsturm.commandmessenger.BoardType;
import com.github.pimsturm.commandmessenger.CmdMessenger;
import com.github.pimsturm.commandmessenger.CommandEventArgs;
import com.github.pimsturm.commandmessenger.ConnectionManager;
import com.github.pimsturm.commandmessenger.ConnectionManagerProgressEventArgs;
import com.github.pimsturm.commandmessenger.IEventHandler;
import com.github.pimsturm.commandmessenger.IMessengerCallbackFunction;
import com.github.pimsturm.commandmessenger.ReceivedCommand;
import com.github.pimsturm.commandmessenger.SendCommand;
import com.github.pimsturm.commandmessenger.EventHandler;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothConnectionManager;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothConnectionStorer;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothTransport;
import com.github.pimsturm.commandmessenger.Transport.Bluetooth.BluetoothUtils;
import com.github.pimsturm.commandmessenger.Transport.ITransport;

enum Command {
    kLeftMotor,
    kRightMotor,
    kLight,
    kSettings,
    kStatus,              // Command to report status
    kIdentify,            // Command to identify device
    kStopMotor,
}


public final class ArduinoCommunicator {
    private static final String TAG = "ArduinoCommunicator";
    // Most of the time you want to be sure you are connecting with the correct device.
    private static final String COMMUNICATION_IDENTIFIER = "BFAF4176-766E-436A-ADF2-96133C02B03C";

    // Maximum speed interval of the Arduino motor shield
    private static final int PWM_MOTOR_LEFT = 255;
    private static final int PWM_MOTOR_RIGHT = 255;

    private static ITransport transport;
    private static CmdMessenger cmdMessenger;
    private static ConnectionManager connectionManager;

    private Context context;

    /**
     * Gets or sets the number of seconds after which the car stops.
     */
    private static int timeOut;

    public static int getTimeOut() {
        return timeOut;
    }

    public static void setTimeOut(int newTimeOut) {
        timeOut = newTimeOut;
    }

    private ArduinoCommunicator(Context context) {
        this.context = context;

    }

    public static void SetupChannel() {
        Log.d(TAG, "Setup channel");
        Toast.makeText(ApplicationContextProvider.getContext(), "Setup channel", Toast.LENGTH_SHORT).show();
        // Now let us set the transport layer
        transport = GetTransport();

        // Initialize the command messenger with the chosen transport layer
        // set if it is communicating with a 16- or 32-bit Arduino board
        cmdMessenger = new CmdMessenger(transport, BoardType.Bit16);
        cmdMessenger.setPrintLfCr(false);

        // Attach the callbacks to the Command Messenger
        AttachCommandCallBacks();

        // Attach to NewLinesReceived for logging purposes
        cmdMessenger.newLineReceived = new NewLineReceived();

        // Attach to newLineSent for logging purposes
        cmdMessenger.newLineSent = new NewLineSent();

        connectionManager = GetConnectionManager();

        // Show all connection progress in the output window
        connectionManager.progress = new EventHandler <ConnectionManagerProgressEventArgs> () {
            @Override
            public void invokeEvent(Object sender, ConnectionManagerProgressEventArgs eventArgs) {
                if (eventArgs.getLevel() <= 3) {
                    Log.d(TAG, eventArgs.getDescription());
                    //Toast.makeText(context.getApplicationContext(), eventArgs.getDescription(), Toast.LENGTH_SHORT);
                }
            }

        };

        connectionManager.connectionFound = new EventHandler <CommandEventArgs>() {
            @Override
            public void invokeEvent(Object sender, CommandEventArgs eventArgs) {
                //TODO: enable the control buttons on the form

                Log.d(TAG, "Connection found");
                Toast.makeText(ApplicationContextProvider.getContext(), "Connection found", Toast.LENGTH_SHORT).show();

                // Get the settings currently stored in the Arduino EEPROM
                SendCommand command = new SendCommand(Command.kSettings.ordinal());
                cmdMessenger.sendCommand(command);

            }
        };

        // Finally - activate connection manager
        connectionManager.startConnectionManager();

    }

    private static ConnectionManager GetConnectionManager() {
        // The Connection manager is capable of storing connection settings, in order to reconnect more quickly
        // the next time the application is run. You can determine yourself where and how to store the settings
        // by supplying a class, that implements ISerialConnectionStorer. For convenience, CmdMessenger provides
        //  simple binary file storage functionality
        BluetoothConnectionStorer bluetoothConnectionStorer = new BluetoothConnectionStorer("BluetoothConnectionManagerSettings.cfg");

        // It is easier to let the BluetoothConnectionManager connection for you.
        // It will:
        //  - Auto discover Bluetooth devices
        //  - If not yet paired, try to pair using the default Bluetooth passwords
        //  - See if the device responds with the correct CommunicationIdentifier
        ConnectionManager connectionManager = new BluetoothConnectionManager(
                (BluetoothTransport) transport,
                cmdMessenger,
                Command.kIdentify.ordinal(),
                COMMUNICATION_IDENTIFIER,
                null);

        // Enable watchdog functionality.
        connectionManager.setWatchdogEnabled(true);
        connectionManager.setDeviceScanEnabled(false);

        return connectionManager;

    }

    private static ITransport GetTransport() {
        BluetoothTransport bluetoothTransport = new BluetoothTransport();
        // If you know your bluetooth device and you have already paired
        // you can directly connect to your Bluetooth Device by address.
        // Under windows you can find the address at:
        //    Control Panel >> All Control Panel Items >> Devices and Printers
        //    Right-click on device >> properties >> Unique id
        bluetoothTransport.setCurrentBluetoothDeviceInfo(BluetoothUtils.deviceByAddress("30:15:01:07:11:28"));
            return bluetoothTransport;

    }

    /**
     * Send Command to the Arduino to switch on the light
     */
    public static void SwitchLightOn() {
        SendCommand command = new SendCommand(Command.kLight.ordinal());
        command.addArgument(1);
        cmdMessenger.sendCommand(command);
    }

    /**
     * Send Command to the Arduino to switch off the light
     */
    public static void SwitchLightOff() {
        SendCommand command = new SendCommand(Command.kLight.ordinal());
        command.addArgument(0);
        cmdMessenger.sendCommand(command);
    }

    /**
     * Send commands to make the car move straight ahead
     */
    public static void MotorForward() {
        SendCommand command = new SendCommand(Command.kLeftMotor.ordinal());
        command.addArgument(PWM_MOTOR_LEFT);
        cmdMessenger.sendCommand(command);

        command = new SendCommand(Command.kRightMotor.ordinal());
        command.addArgument(PWM_MOTOR_RIGHT);
        cmdMessenger.sendCommand(command);

    }

    /**
     * Send commands to make the car move backward
     */
    public static void MotorBackward() {
        SendCommand command = new SendCommand(Command.kLeftMotor.ordinal());
        command.addArgument(-PWM_MOTOR_LEFT);
        cmdMessenger.sendCommand(command);

        command = new SendCommand(Command.kRightMotor.ordinal());
        command.addArgument(-PWM_MOTOR_RIGHT);
        cmdMessenger.sendCommand(command);

    }

    /**
     * Send a command to stop the car
     */
    public static void MotorStop() {
        SendCommand command = new SendCommand(Command.kStopMotor.ordinal());
        cmdMessenger.sendCommand(command);
    }

    /**
     * Send commands to make the car move to the left
     */
    public static void MotorToLeft() {
        SendCommand command = new SendCommand(Command.kLeftMotor.ordinal());
        command.addArgument(-PWM_MOTOR_LEFT);
        cmdMessenger.sendCommand(command);

        command = new SendCommand(Command.kRightMotor.ordinal());
        command.addArgument(PWM_MOTOR_RIGHT);
        cmdMessenger.sendCommand(command);

    }

    /**
     * Send commands to make the car move to the right
     */
    public static void MotorToRight() {
        SendCommand command = new SendCommand(Command.kLeftMotor.ordinal());
        command.addArgument(PWM_MOTOR_LEFT);
        cmdMessenger.sendCommand(command);

        command = new SendCommand(Command.kRightMotor.ordinal());
        command.addArgument(-PWM_MOTOR_RIGHT);
        cmdMessenger.sendCommand(command);

    }

    /// Attach command call backs.
    private static void AttachCommandCallBacks() {
        cmdMessenger.attach(new OnUnknownCommand());
        cmdMessenger.attach(Command.kStatus.ordinal(), new OnAcknowledge());
        cmdMessenger.attach(Command.kSettings.ordinal(), new OnSettingsReceived());
    }

    // ------------------  C A L L B A C K S ---------------------

    public static class OnUnknownCommand implements IMessengerCallbackFunction {
        // Called when a received command has no attached function.
        @Override
        public void handleMessage(ReceivedCommand arguments) {
            Log.d(TAG, "Command without attached callback received");
        }
    }

    public static class OnAcknowledge implements IMessengerCallbackFunction {
        // Callback function that prints that the Arduino has acknowledged
        @Override
        public void handleMessage(ReceivedCommand arguments) {
            Log.d(TAG, " Arduino is ready");
        }
    }

    public static class OnSettingsReceived implements IMessengerCallbackFunction {
        @Override
        public void handleMessage(ReceivedCommand arguments) {
            Log.d(TAG, "Settings received from Arduino.");

            timeOut = arguments.readInt16Arg();

            Log.d(TAG, "Timeout: " + timeOut);
        }
    }

    // Log received line to console
    public static class NewLineReceived implements IEventHandler<CommandEventArgs> {

        @Override
        public void invokeEvent(Object sender, CommandEventArgs e) {
            Log.d(TAG, "Received > " + e.getCommand().commandString());

        }
    }



    // Log sent line to console
    public static class NewLineSent implements IEventHandler<CommandEventArgs> {

        @Override
        public void invokeEvent(Object sender, CommandEventArgs e) {
            Log.d(TAG, "Sent > " + e.getCommand().commandString());
        }
    }

    /**
     * Exit function
      */
    public static void Exit() {
        if (connectionManager != null)
            connectionManager.stopConnectionManager();

        // Stop listening
        cmdMessenger.disconnect();

        // dispose Command Messenger
        cmdMessenger.dispose();

        // dispose Serial Port object
        //transport.dispose();
        transport = null;

        Log.d(TAG, "Exit from Arduino Communicator");
    }

}
