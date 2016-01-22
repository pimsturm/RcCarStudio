package com.github.pimsturm.commandmessenger.Queue;

import com.github.pimsturm.commandmessenger.AsyncWorker;
import com.github.pimsturm.commandmessenger.IAsyncWorkerJob;

import java.util.ArrayList;

// Command queue base object.
public abstract class CommandQueue {
    private final AsyncWorker _worker;

    protected final ListQueue<CommandStrategy> Queue = new ListQueue<CommandStrategy>();   // Buffer for commands
    protected final ArrayList<GeneralStrategy> GeneralStrategies = new ArrayList<GeneralStrategy>(); // Buffer for command independent strategies

    private boolean IsRunning;
    public boolean getIsRunning() { return _worker.isRunning(); }
    private boolean IsSuspended;
    public boolean getIsSuspended() { return _worker.isSuspended(); }

    /// <summary>Gets count of records in queue. NOT THREAD-SAFE.</summary>
    private int Count;
    public int getCount()
    {
        return Queue.size();
    }

    /// <summary>Gets is queue is empty. NOT THREAD-SAFE.</summary>
    private boolean IsEmpty;
    public boolean getIsEmpty()
    {
        return Queue.size() == 0;
    }

    /// <summary> Clears the queue. </summary>
    public void Clear()
    {
        synchronized (Queue) {Queue.clear();}
    }

    protected CommandQueue()
    {
        set_processQueue();
        if (_processQueue == null) {throw new NullPointerException("processQueue may not be null.");}
        _worker = new AsyncWorker(_processQueue, "CommandQueue");
    }

    /// <summary> Adds a general strategy. This strategy is applied to all queued and dequeued commands.  </summary>
    /// <param name="generalStrategy"> The general strategy. </param>
    public void AddGeneralStrategy(GeneralStrategy generalStrategy)
    {
        // Give strategy access to queue
        generalStrategy.setCommandQueue(Queue);
        // Add to general strategy list
        GeneralStrategies.add(generalStrategy);
    }

    /// <summary>
    /// Queue the command wrapped in a command strategy.
    /// Call SignalWaiter method to continue processing of queue.
    /// </summary>
    /// <param name="commandStrategy"> The command strategy. </param>
    public abstract void QueueCommand(CommandStrategy commandStrategy);

    public void Dispose()
    {
        Dispose(true);
        //GC.SuppressFinalize(this);
    }

    public void Start()
    {
        _worker.start();
    }

    public void Stop()
    {
        _worker.stop();
        Clear();
    }

    public void Suspend()
    {
        _worker.suspend();
    }

    public void Resume()
    {
        _worker.resume();
    }

    protected void SignalWorker()
    {
        _worker.signal();
    }

    protected void Dispose(boolean disposing)
    {
        if (disposing)
        {
            Stop();
        }
    }

    /// <summary> Process the queue. </summary>
    protected IAsyncWorkerJob _processQueue;
    protected abstract void set_processQueue();
    public abstract class ProcessQueue implements IAsyncWorkerJob {}
}
