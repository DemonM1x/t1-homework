package ru.t1.synthetichumancorestarter.command.service.factory;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SyntheticThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumber = new AtomicInteger(1); // Счётчик для уникальных имён
    private final String namePrefix;

    public SyntheticThreadFactory(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    @Override
    public Thread newThread(@NonNull Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(namePrefix + threadNumber.getAndIncrement());
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in thread {}: {}",
                t.getName(),
                e.getMessage(),
                e));
        log.info("Created new thread: {}",
                thread.getName());
        return thread;
    }
}
