package ru.citeck.ecos.counter;

public interface LockExecutorCode<T> {
    public T execute() throws EnumerationException;
}
