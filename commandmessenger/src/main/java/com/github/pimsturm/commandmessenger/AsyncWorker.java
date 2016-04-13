package com.github.pimsturm.commandmessenger;

import android.util.Log;

public class AsyncWorker {

    private static final String TAG = "AsyncWorker";
    private boolean isFaulted;

    private volatile WorkerState workerState = WorkerState.Stopped;
    private volatile WorkerState requestedState = WorkerState.Stopped;

    private final EventWaiter eventWaiter = new EventWaiter();

    private final IAsyncWorkerJob workerJob;

    private Thread workerTask;

    private String name;
    public String getName() {
        return name;
    }

    private WorkerState state;
    public WorkerState getState() {
        return state;
    }

    public boolean isRunning() {
        return workerState == WorkerState.Running;
    }
    public boolean isSuspended() {
        return workerState == WorkerState.Suspended;
    }

    public AsyncWorker(IAsyncWorkerJob workerJob, String workerName)
    {
        if (workerJob == null) throw new NullPointerException("workerJob");
        this.workerJob = workerJob;
        name = workerName;
    }

    public AsyncWorker(IAsyncWorkerJob workerJob) {
        this(workerJob, null);
    }

    public void start() {
        synchronized (this) {
            if (workerState == WorkerState.Stopped) {
                requestedState = workerState = WorkerState.Running;
                eventWaiter.reset();

                workerTask = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (workerState == WorkerState.Stopped) break;

                            boolean haveMoreWork = false;
                            if (workerState == WorkerState.Running) {
                                try {
                                    haveMoreWork = workerJob.execute();
                                } catch (Exception e)
                                {
                                    requestedState = workerState = WorkerState.Stopped;
                                    isFaulted = true;
                                    Log.d(TAG,"WorkerJob interrupted");
                                    //throw new InterruptedException("WorkerJob interrupted");
                                }

                                // Check if state has been changed in workerJob thread.
                                if (requestedState != workerState && requestedState == WorkerState.Stopped) {
                                    workerState = requestedState;
                                    break;
                                }
                            }

                            try {
                                // 0 = infinity
                                if (!haveMoreWork || workerState == WorkerState.Suspended)
                                    eventWaiter.waitOne(0);
                                workerState = requestedState;
                            } catch (InterruptedException e) {
                                Log.d(TAG, e.getMessage());
                            }
                        }

                    }
                });
                workerTask.setName(name);
                workerTask.setDaemon(true);   // IsBackground

                workerTask.start();
//                SpinWait.SpinUntil(() -> workerTask.isAlive());
                while(!workerTask.isAlive()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    Log.d(TAG, e.getMessage());}
                }
            } else {
                throw new IllegalStateException("The worker is already started.");
            }
        }
    }

    public void stop()
    {
        synchronized(this)
        {
            if (workerState == WorkerState.Running || workerState == WorkerState.Suspended)
            {
                requestedState = WorkerState.Stopped;

                // Prevent deadlock by checking is we stopping from worker task or not.
                if (Thread.currentThread().getId() != workerTask.getId()) //.ManagedThreadId
                {
                    eventWaiter.set();
                    try {
                        workerTask.join();
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            else if (!isFaulted)
            {
                // Probably not needed, added as a precaution.
                throw new IllegalStateException("The worker is already stopped.");
            }
        }
    }

    public void suspend()
    {
        synchronized(this)
        {
            if (workerState == WorkerState.Running)
            {
                requestedState = WorkerState.Suspended;
                eventWaiter.set();
                //SpinWait.SpinUntil(() -> requestedState == workerState);
                while(requestedState == workerState) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            else
            {
                // Probably not needed, added as a precaution.
                throw new IllegalStateException("The worker is not running.");
            }
        }
    }

    public void resume()
    {
        synchronized(this)
        {
            if (workerState == WorkerState.Suspended)
            {
                requestedState = WorkerState.Running;
                eventWaiter.set();
                //SpinWait.SpinUntil(() -> requestedState == workerState);
                while(requestedState == workerState) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            }
            else
            {
                // Probably not needed, added as a precaution.
                throw new IllegalStateException("The worker is not in suspended state.");
            }
        }
    }

    /**
     * signal worker to continue processing.
     */
    public void signal()
    {
        if (isRunning()) eventWaiter.set();
    }
}
