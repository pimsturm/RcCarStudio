package com.github.pimsturm.commandmessenger;

public interface IEventHandler<T> {
    void invokeEvent(Object sender, T e);
}
