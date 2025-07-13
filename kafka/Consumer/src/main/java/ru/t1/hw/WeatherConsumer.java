package ru.t1.hw;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import ru.t1.hw.model.Weather;
import ru.t1.hw.util.WeatherDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class WeatherConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "weather-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, WeatherDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        try {
            KafkaConsumer<String, Weather> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singleton("weather-topic"));
            while (true) {
                ConsumerRecords<String, Weather> records =
                        consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, Weather> record : records) {
                    String key = record.key();
                    Weather weather = record.value();
                    System.out.println("key = " + key + "\n" + "value = " + weather.getTemperature() + " " + weather.getWeatherType());
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
