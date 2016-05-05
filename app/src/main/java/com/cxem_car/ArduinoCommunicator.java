package com.cxem_car;

import android.util.Log;
import android.widget.Toast;

import com.github.pimsturm.commandmessenger.CmdMessenger;
import com.github.pimsturm.commandmessenger.CommandEventArgs;
import com.github.pimsturm.commandmessenger.ConnectionManagerProgressEventArgs;
import com.github.pimsturm.commandmessenger.IEventHandler;
import com.github.pimsturm.commandmessenger.IMessengerCallbackFunction;
import com.github.pimsturm.commandmessenger.ReceivedCommand;
import com.github.pimsturm.commandmessenger.SendCommand;
import com.github.pimsturm.commandmessenger.EventHandler;

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
    private static ArduinoCommunicator uniqueInstance;
    private static final String TAG = "ArduinoCommunicator";
    // Most of the time you want to be sure you are connecting with the correct device.
    private static final String COMMUNICATION_IDENTIFIER = "BFAF4176-766E-436A-ADF2-96133C02B03C";

    // Maximum speed interval of the Arduino motor shield
    private static final int PWM_MOTOR_LEFT = 255;
    private static final int PWM_MOTOR_RIGHT = 255;

    private CmdMessenger cmdMessenger;

    private ArduinoCommunicator() {
    }

    public static synchronized ArduinoCommunicator getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new ArduinoCommunicator();
        }
        return uniqueInstance;
    }

    public void SetupChannel() {
        Log.d(TAG, "Setup channel");

        cmdMessenger = new CmdMessenger();
        cmdMessenger.getSettings().setUniqueDeviceId(COMMUNICATION_IDENTIFIER);
        // Enable watchdog functionality.
        cmdMessenger.getSettings().setWatchdogEnabled(true);
        cmdMessenger.getSettings().setDeviceScanEnabled(false);

        // Attach the callbacks to the Command Messenger
        AttachCommandCallBacks();

        // Attach to NewLinesReceived for logging purposes
        cmdMessenger.newLineReceived = new NewLineReceived();

        // Attach to newLineSent for logging purposes
        cmdMessenger.newLineSent = new NewLineSent();

        // Show all connection progress in the output window
        cmdMessenger.getConnectionManager().setProgress(new EventHandler<ConnectionManagerProgressEventArgs>() {
            @Override
            public void invokeEvent(Object sender, ConnectionManagerProgressEventArgs eventArgs) {
                if (eventArgs.getLevel() <= 3) {
                    Log.d(TAG, eventArgs.getDescription());
                    Toast.makeText(ApplicationContextProvider.getContext(), eventArgs.getLevel() + " " + eventArgs.getDescription(), Toast.LENGTH_LONG).show();
                }
            }

        });

        cmdMessenger.getConnectionManager().setConnectionFound(new EventHandler<CommandEventArgs>() {
            @Override
            public void invokeEvent(Object sender, CommandEventArgs eventArgs) {
                //TODO: enable the control buttons on the form

                Log.d(TAG, "Connection found");
                Toast.makeText(ApplicationContextProvider.getContext(), "Connection found", Toast.LENGTH_LONG).show();

                // Get the settings currently stored in the Arduino EEPROM
                SendCommand command = new SendCommand(Command.kSettings.ordinal());
                cmdMessenger.sendCommand(command);

            }
        });

        // Finally - activate connection manager
        cmdMessenger.getConnectionManager().startConnectionManager();

    }

    /**
     * Send Command to the Arduino to switch on the light
     */
    public void SwitchLightOn() {
        SendCommand command = new SendCommand(Command.kLight.ordinal());
        command.addArgument(1);
        cmdMessenger.sendCommand(command);
    }

    /**
     * Send Command to the Arduino to switch off the light
     */
    public void SwitchLightOff() {
        SendCommand command = new SendCommand(Command.kLight.ordinal());
        command.addArgument(0);
        cmdMessenger.sendCommand(command);
    }

    /**
     * Send commands to make the car move straight ahead
     */
    public void MotorForward() {
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
    public void MotorBackward() {
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
    public void MotorStop() {
        SendCommand command = new SendCommand(Command.kStopMotor.ordinal());
        cmdMessenger.sendCommand(command);
    }

    /**
     * Send commands to make the car move to the left
     */
    public void MotorToLeft() {
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
    public void MotorToRight() {
        SendCommand command = new SendCommand(Command.kLeftMotor.ordinal());
        command.addArgument(PWM_MOTOR_LEFT);
        cmdMessenger.sendCommand(command);

        command = new SendCommand(Command.kRightMotor.ordinal());
        command.addArgument(-PWM_MOTOR_RIGHT);
        cmdMessenger.sendCommand(command);

    }

    /// Attach command call backs.
    private void AttachCommandCallBacks() {
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

            int timeOut = arguments.readInt16Arg();

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
    public void Exit() {
        if (cmdMessenger.getConnectionManager() != null)
            cmdMessenger.getConnectionManager().stopConnectionManager();

        // dispose Command Messenger
        cmdMessenger = null;


        Log.d(TAG, "Exit from Arduino Communicator");
    }

}