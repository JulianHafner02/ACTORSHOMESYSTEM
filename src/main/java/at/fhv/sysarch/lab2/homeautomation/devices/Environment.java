package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.misc.Weather;

import java.time.Duration;
import java.util.Random;

public class Environment extends AbstractBehavior<Environment.EnvironmentCommand> {

    public interface EnvironmentCommand {}

    public static final class TemperatureChanger implements EnvironmentCommand {}

    public static final class WeatherChanger implements EnvironmentCommand {}

    private double temperature = 15.0;
    private Weather currentWeather = Weather.SUNNY;
    private final TimerScheduler<EnvironmentCommand> temperatureTimeScheduler;
    private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;

    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;

    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor;

    public static Behavior<EnvironmentCommand> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Environment(context, timers, timers, tempSensor, weatherSensor)));
    }

    private Environment(ActorContext<EnvironmentCommand> context, TimerScheduler<EnvironmentCommand> tempTimer, TimerScheduler<EnvironmentCommand> weatherTimer, ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        super(context);
        this.temperatureTimeScheduler = tempTimer;
        this.weatherTimeScheduler = weatherTimer;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureChanger(), Duration.ofSeconds(5));
        this.weatherTimeScheduler.startTimerAtFixedRate(new WeatherChanger(), Duration.ofSeconds(10));
        this.tempSensor = tempSensor;
        this.weatherSensor = weatherSensor;
    }

    @Override
    public Receive<EnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureChanger.class, this::onChangeTemperature)
                .onMessage(WeatherChanger.class, this::onChangeWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<EnvironmentCommand> onChangeTemperature(TemperatureChanger t) {
        double change = (Math.random() - 0.3) * 2;
        temperature += change;
        getContext().getLog().info("Temperature changed to {}", String.format("%.2f", temperature));
        tempSensor.tell(new TemperatureSensor.ReadTemperature(temperature));
        return this;
    }


    private Behavior<EnvironmentCommand> onChangeWeather(WeatherChanger w) {
        currentWeather =  Weather.values()[new Random().nextInt(Weather.values().length)];
        getContext().getLog().info("Weather changed to {}", currentWeather);
        weatherSensor.tell(new WeatherSensor.ReadWeather(currentWeather));
        return this;
    }

    private Environment onPostStop() {
        getContext().getLog().info("Environment actor stopped");
        return this;
    }
}
