package com.github.pimsturm.commandmessenger.Transport;

import com.github.pimsturm.commandmessenger.IEventHandler;

/**
 * Interface for transport layer.
 */
public interface ITransport {
    /**
     * connect transport
     */
    void startConnectionManager();

    /**
     * disconnect transport
     */
    void stopConnectionManager();

    /**
     * Returns connection status
     * @return true when connected
     */
    boolean isConnected();

    /**
     * write a string to the output stream
     * @param value The string to write
     */
    void write(String value);

    void setConnectionFound(IEventHandler eventHandler);

    void setProgress(IEventHandler eventHandler);

}
