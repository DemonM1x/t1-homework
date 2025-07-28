package ru.t1.synthetichumancorestarter.command.service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.t1.synthetichumancorestarter.command.executor.SyntheticCommandExecutor;
import ru.t1.synthetichumancorestarter.command.model.SyntheticCommand;
import ru.t1.synthetichumancorestarter.command.properties.CommandConfigurationProperties;
import ru.t1.synthetichumancorestarter.command.service.factory.SyntheticThreadFactory;
import ru.t1.synthetichumancorestarter.command.validation.SyntheticValidator;
import ru.t1.synthetichumancorestarter.errorhandling.exception.QueueIsFullException;
import ru.t1.synthetichumancorestarter.metrics.SyntheticMetricService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ThreadPoolCommandService implements CommandService {

    private final ThreadPoolExecutor threadPoolExecutor;
    private final SyntheticValidator syntheticValidator;
    private final SyntheticCommandExecutor syntheticCommandExecutor;
    private final CommandConfigurationProperties commandConfigurationProperties;
    private final SyntheticMetricService metricService;

    public ThreadPoolCommandService(
            SyntheticValidator syntheticValidator,
            SyntheticCommandExecutor syntheticCommandExecutor,
            CommandConfigurationProperties commandConfigurationProperties,
            SyntheticMetricService metricService
    ) {
        this.syntheticValidator = syntheticValidator;
        this.syntheticCommandExecutor = syntheticCommandExecutor;
        this.commandConfigurationProperties = commandConfigurationProperties;
        this.metricService = metricService;
        final var poolProperties = commandConfigurationProperties.getPoolProperties();
        this.threadPoolExecutor = new ThreadPoolExecutor(
                poolProperties.getMinSize(),
                poolProperties.getMaxSize(),
                poolProperties.getIdleThreadKeepAliveTime(),
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(poolProperties.getQueueCapacity()),
                new SyntheticThreadFactory("bishop-task-"),
                new ThreadPoolExecutor.AbortPolicy());
    }

    @Scheduled(
            initialDelayString = "#{syntheticProperties.getBusyness().getInitialDelay()}",
            fixedDelayString = "#{syntheticProperties.getBusyness().getFixedDelay()}")
    public void publishQueueSizeMetric() {
        metricService.publishQueueSizeMetric(threadPoolExecutor.getQueue().size());
    }

    @Override
    public void processCommand(SyntheticCommand command) throws QueueIsFullException {
        syntheticValidator.validate(command);
        switch (command.getPriority()) {
            case COMMON -> {
                try {
                    threadPoolExecutor.execute(() -> syntheticCommandExecutor.execute(command));
                } catch (RejectedExecutionException e) {
                    throw new QueueIsFullException("Stop executing command: " + command + ". queue is full", e);
                }
            }
            case CRITICAL -> syntheticCommandExecutor.execute(command);
        }
    }

    @PreDestroy
    public void preDestroy() {
        threadPoolExecutor.shutdown();
        try {
            if (!threadPoolExecutor.awaitTermination(commandConfigurationProperties.getPoolProperties().getTerminationTimeout().toNanos(),
                    TimeUnit.NANOSECONDS)) {

                threadPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPoolExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
