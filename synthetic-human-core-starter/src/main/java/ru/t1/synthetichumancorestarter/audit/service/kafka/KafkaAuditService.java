package ru.t1.synthetichumancorestarter.audit.service.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.synthetichumancorestarter.audit.model.AuditEvent;
import ru.t1.synthetichumancorestarter.audit.properties.AuditConfigurationProperties;
import ru.t1.synthetichumancorestarter.audit.service.AuditService;

@Service
@ConditionalOnProperty(name = "synth.core.audit.mode", havingValue = "KAFKA")
@RequiredArgsConstructor
public class KafkaAuditService implements AuditService {

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;
    private final AuditConfigurationProperties auditConfigurationProperties;

    @Override
    public void audit(AuditEvent event) {
        kafkaTemplate.send(auditConfigurationProperties.getTopic(), event);
    }
}
