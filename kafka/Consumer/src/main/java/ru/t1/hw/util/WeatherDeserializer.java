package ru.t1.hw.util;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Deserializer;
import ru.t1.hw.model.Weather;

public class WeatherDeserializer implements Deserializer<Weather> {
    private final Gson gson = new Gson();
    @Override
    public Weather deserialize(String topic, byte[] data) {
        if (data == null) return null;
        return gson.fromJson(new String(data), Weather.class);
    }
}
