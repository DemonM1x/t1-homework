package ru.t1.hw.util;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;
import ru.t1.hw.model.Weather;

public class WeatherSerializer implements Serializer<Weather> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, Weather data) {
        if (data == null) return null;
        return gson.toJson(data).getBytes();
    }
}
