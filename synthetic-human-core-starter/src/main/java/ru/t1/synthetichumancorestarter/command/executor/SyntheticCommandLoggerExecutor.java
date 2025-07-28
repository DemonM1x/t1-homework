package ru.t1.synthetichumancorestarter.command.executor;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.t1.synthetichumancorestarter.command.model.SyntheticCommand;
import ru.t1.synthetichumancorestarter.metrics.SyntheticMetricService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyntheticCommandLoggerExecutor implements SyntheticCommandExecutor{

    private final SyntheticMetricService metricService;

    @Override
    @SneakyThrows
    public void execute(SyntheticCommand command) {
        Thread.sleep(1_000);
        log.info("""
            Start executing command...
            \tdescription - {}
            \tpriority - {}
            \tcommand author - {}
            \tregistered at - {}
            End executing command...""",
                command.getDescription(),
                command.getPriority(),
                command.getAuthor(),
                command.getTime());
        metricService.publishExecuteTaskMetric(command.getAuthor());
    }
}
