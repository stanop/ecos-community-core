package ru.citeck.ecos.locks;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface LockUtils {
    void doWithLock(String lockId, Runnable job);
    <T> T doWithLock(String lockId, Callable<T> job);
    void doWithLock(String lockId, long timeToLive, TimeUnit unit, Runnable job);
    <T> T doWithLock(String lockId, long timeToLive, TimeUnit unit, Callable<T> job);
}
