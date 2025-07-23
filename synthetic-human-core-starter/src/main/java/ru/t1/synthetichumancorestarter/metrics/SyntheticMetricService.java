package ru.t1.synthetichumancorestarter.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class SyntheticMetricService {
    private final MeterRegistry meterRegistry;
    private final AtomicLong currentAndroidConnectedValue = new AtomicLong();
    private final Map<String, Counter> completedTasksCounterMap = new HashMap<>();

    @PostConstruct
    public void registerMetrics() {
        Gauge.builder("active_android", currentAndroidConnectedValue::get)
                .description("Current Android Connected")
                .register(meterRegistry);
    }

    public void publishQueueSizeMetric(int queueSize) {
        currentAndroidConnectedValue.set(queueSize);
    }

    public void publishExecuteTaskMetric(String author) {
        completedTasksCounterMap.computeIfAbsent(author, counter ->
                        Counter.builder("current_android_execute_task")
                                .description("current android execute task by author")
                                .tag("author", author)
                                .register(meterRegistry))
                .increment();
    }
}
