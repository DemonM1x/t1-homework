package ru.t1.hw;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.t1.hw.model.Weather;
import ru.t1.hw.model.WeatherType;
import ru.t1.hw.util.WeatherSerializer;

import java.util.Properties;
import java.util.Random;

public class WeatherProducer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, WeatherSerializer.class.getName());
        try (KafkaProducer<String, Weather> producer = new KafkaProducer<>(props)) {
            for (int i = 0; i < 10; i++) {
                int randomTemp = new Random().nextInt(35);
                int randomHumidity = new Random().nextInt(WeatherType.class.getEnumConstants().length);
                Weather weather = new Weather(randomTemp, WeatherType.class.getEnumConstants()[randomHumidity]);
                producer.send(new ProducerRecord<>("weather-topic", "key-"+i, weather));
                Thread.sleep(2000);

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
