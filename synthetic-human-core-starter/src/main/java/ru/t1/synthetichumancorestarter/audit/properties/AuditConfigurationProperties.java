package ru.t1.synthetichumancorestarter.audit.properties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ValidAuditProperties
@ConfigurationProperties(prefix = "synth.core.audit")
@Data
public class AuditConfigurationProperties {

    @NotNull
    private AuditMode mode = AuditMode.CONSOLE;

    private String topic;

    public enum AuditMode {
        KAFKA,
        CONSOLE
    }

}
