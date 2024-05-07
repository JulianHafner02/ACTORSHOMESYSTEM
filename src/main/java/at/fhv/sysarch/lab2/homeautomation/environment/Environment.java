package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.misc.Weather;

import java.time.Duration;
import java.util.Random;

public class Environment extends AbstractBehavior<Environment.EnvironmentCommand> {

    //TODO environments ufteila damit die daten manuell au abgeändert werden künnand, indem se die gleiche referenz denn hond
    public interface EnvironmentCommand {}

    public static final class TemperatureEnvironment implements EnvironmentCommand {
        private double changedTemperature;

        public TemperatureEnvironment(double changedTemperature) {
            this.changedTemperature = changedTemperature;
        }
    }

    public static final class WeatherEnvironment implements EnvironmentCommand {

        private Weather changedWeather;

        public WeatherEnvironment(Weather changedWeather) {
            this.changedWeather = changedWeather;
        }
    }

    private double currentTemperature = 15.0;
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
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureEnvironment(currentTemperature), Duration.ofSeconds(5));
        this.weatherTimeScheduler.startTimerAtFixedRate(new WeatherEnvironment(currentWeather), Duration.ofSeconds(10));
        this.tempSensor = tempSensor;
        this.weatherSensor = weatherSensor;
    }

    @Override
    public Receive<EnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureEnvironment.class, this::onChangeTemperature)
                .onMessage(WeatherEnvironment.class, this::onChangeWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<EnvironmentCommand> onChangeTemperature(TemperatureEnvironment t) {
        currentTemperature = t.changedTemperature;
        getContext().getLog().info("Temperature changed to {}", String.format("%.2f", currentTemperature));
        tempSensor.tell(new TemperatureSensor.ReadTemperature(currentTemperature));
        t.changedTemperature = t.changedTemperature + (Math.random() - 0.4) * 2;
        return this;
    }


    private Behavior<EnvironmentCommand> onChangeWeather(WeatherEnvironment w) {
        currentWeather =  w.changedWeather;
        getContext().getLog().info("Weather changed to {}", currentWeather);
        weatherSensor.tell(new WeatherSensor.ReadWeather(currentWeather));
        w.changedWeather = Weather.values()[new Random().nextInt(Weather.values().length)];
        return this;
    }

    private Environment onPostStop() {
        getContext().getLog().info("Environment actor stopped");
        return this;
    }
}
