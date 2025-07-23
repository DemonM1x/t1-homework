package ru.t1.synthetichumancorestarter.audit.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.t1.synthetichumancorestarter.audit.model.AuditEvent;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AuditConfigurationProperties.class)
@ConditionalOnProperty(prefix = "synth.core.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuditAutoConfiguration {
    @Bean
    @ConditionalOnProperty(name = "synth.core.audit.mode", havingValue = "KAFKA")
    public KafkaTemplate<String, AuditEvent> kafkaTemplate(AuditConfigurationProperties props) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put("bootstrap.servers", "localhost:9092");
        configProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        configProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        ProducerFactory<String, AuditEvent> producerFactory = new DefaultKafkaProducerFactory<>(configProps);
        KafkaTemplate<String, AuditEvent> template = new KafkaTemplate<>(producerFactory);
        template.setDefaultTopic(props.getTopic());
        return template;
    }

}
