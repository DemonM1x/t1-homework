package ru.t1.authservice.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvPostProcessor implements EnvironmentPostProcessor, PriorityOrdered {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        Map<String, Object> dotenvEnvironments = new HashMap<>();
        for (DotenvEntry entry : dotenv.entries()) {
            dotenvEnvironments.put(entry.getKey(), entry.getValue());
        }

        MapPropertySource dotenvPropertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, dotenvEnvironments);
        environment.getPropertySources().addLast(dotenvPropertySource);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
