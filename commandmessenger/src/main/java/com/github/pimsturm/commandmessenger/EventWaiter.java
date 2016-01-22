package com.github.pimsturm.commandmessenger;

/**
 * Functionality comparable to AutoResetEvent (http://www.albahari.com/threading/part2.aspx#_AutoResetEvent)
 * but implemented using the monitor class: not inter process, but ought to be more efficient.
 * http://stackoverflow.com/questions/1091973/javas-equivalent-to-nets-autoresetevent
 */
public class EventWaiter {
    private final Object monitor = new Object();
    private volatile boolean blocked;

    /**
     * start blocked (waiting for signal)
     */
    public EventWaiter()
    {
        blocked = true;
    }

    /**
     * start blocked or signalled.
     * @param set If true, first Wait will directly continue
     */
    public EventWaiter(boolean set)
    {
        blocked = !set;
    }

    /**
     * Wait function. Blocks until signal is set or time-out
     * @param timeOut time-out in ms, zero = infinity
     * @return a waitstate indicating if the wait ended normally or because of a timeout
     * @throws InterruptedException
     */
    public WaitState waitOne(long timeOut) throws InterruptedException
    {
        synchronized (monitor)
        {
            // Check if signal has already been raised before the wait function is entered
            if (!blocked)
            {
                // If so, reset event for next time and exit wait loop
                blocked = true;
                return WaitState.Normal;
            }

            // Wait under conditions
            long t = System.currentTimeMillis();
            boolean noTimeOut = true;
            while ( noTimeOut && blocked) {
                monitor.wait(timeOut);
                // Check for timeout
                if (timeOut > 0 && System.currentTimeMillis() - t >= timeOut)
                    noTimeOut = false;
            }
            // Block Wait for next entry
            blocked = true;

            // Return whether the Wait function was quit because of a set event or timeout
            return noTimeOut ? WaitState.Normal : WaitState.TimeOut;
        }
    }

    /**
     * Sets signal, will unblock thread in Wait function
     */
    public void set()
    {
        synchronized (monitor)
        {
            blocked = false;
            monitor.notify();
        }
    }

    /**
     * Resets signal, will block threads entering Wait function
     */
    public void reset()
    {
        blocked = true;
    }
}
