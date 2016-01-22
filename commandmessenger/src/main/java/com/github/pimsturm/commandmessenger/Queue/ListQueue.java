package com.github.pimsturm.commandmessenger.Queue;

import java.util.ArrayList;

/// <summary> Queue class.  </summary>
/// <typeparam name="T"> Type of object to queue. </typeparam>
public class ListQueue<T> extends ArrayList<T> {
    /// <summary> Adds item to front of queue. </summary>
    /// <param name="item"> The item to queue. </param>
    public final void EnqueueFront(T item)
    {
        add(size(), item);
    }

    /// <summary> Adds item to back of queue. </summary>
    /// <param name="item"> The item to queue. </param>
    public final void Enqueue(T item)
    {
        add(item);
    }

    /// <summary> fetches item from front of queue. </summary>
    /// <returns> The item to dequeue. </returns>
    public final T Dequeue()
    {
        T t = this.get(0);
        remove(0);
        return t;
    }

    /// <summary> look at item at front of queue without removing it from the queue. </summary>
    /// <returns> The item to peek at. </returns>
    public final T Peek()
    {
        return this.get(0);
    }
}
