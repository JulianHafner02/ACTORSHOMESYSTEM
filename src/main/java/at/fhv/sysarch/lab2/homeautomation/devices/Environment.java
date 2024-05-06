package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Random;

public class Environment extends AbstractBehavior<Environment.EnvironmentCommand> {

    public interface EnvironmentCommand {}

    public static final class TemperatureChanger implements EnvironmentCommand {}

    public static final class WeatherChanger implements EnvironmentCommand {}

    public enum Weather {
        SUNNY, CLOUDY, RAINY, WINDY, STORMY
    }
    private double temperature = 15.0;
    private Weather currentWeather = Weather.SUNNY;
    private final TimerScheduler<EnvironmentCommand> temperatureTimeScheduler;
    private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;

    public static Behavior<EnvironmentCommand> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new Environment(context, timers, timers)));
    }

    private Environment(ActorContext<EnvironmentCommand> context, TimerScheduler<EnvironmentCommand> tempTimer, TimerScheduler<EnvironmentCommand> weatherTimer) {
        super(context);
        this.temperatureTimeScheduler = tempTimer;
        this.weatherTimeScheduler = weatherTimer;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureChanger(), Duration.ofSeconds(5));
        this.weatherTimeScheduler.startTimerAtFixedRate(new WeatherChanger(), Duration.ofSeconds(10));
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
        //TODO NOTIFY SENSOR
        return this;
    }


    private Behavior<EnvironmentCommand> onChangeWeather(WeatherChanger w) {
        currentWeather =  Weather.values()[new Random().nextInt(Weather.values().length)];
        getContext().getLog().info("Weather changed to {}", currentWeather);
        //TODO NOTIFY SENSOR
        return this;
    }

    private Environment onPostStop() {
        getContext().getLog().info("Environment actor stopped");
        return this;
    }
}
