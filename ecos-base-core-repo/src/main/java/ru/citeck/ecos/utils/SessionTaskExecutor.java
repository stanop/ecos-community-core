package ru.citeck.ecos.utils;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionTaskExecutor {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
