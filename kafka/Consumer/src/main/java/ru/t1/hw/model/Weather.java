package ru.t1.hw.model;

public class Weather {

    private int temperature;
    private WeatherType weatherType;

    public Weather() {

    }

    public Weather(int temperature, WeatherType weatherType) {
        this.temperature = temperature;
        this.weatherType = weatherType;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public WeatherType getWeatherType() {
        return weatherType;
    }

    public void setWeatherType(WeatherType weatherType) {
        this.weatherType = weatherType;
    }
}
