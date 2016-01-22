package com.github.pimsturm.commandmessenger.Transport;

import com.github.pimsturm.commandmessenger.IEventHandler;

/**
 * Interface for transport layer.
 */
public interface ITransport {
    /**
     * connect transport
     * @return
     */
    boolean connect();

    /**
     * disconnect transport
     * @return
     */
    boolean disconnect();

    /**
     * Returns connection status
     * @return true when connected
     */
    boolean isConnected();

    /**
     * Bytes read over transport
     * @return
     */
    byte[] read();

    /**
     * write bytes over transport
     * @param buffer
     */
    void write(byte[] buffer);

    /**
     * Bytes have been received event.
     * @param dataReceived
     */
    void setDataReceived (IEventHandler dataReceived);
}
