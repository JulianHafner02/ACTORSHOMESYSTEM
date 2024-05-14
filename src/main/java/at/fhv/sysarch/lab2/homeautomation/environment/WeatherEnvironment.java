package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.devices.WeatherSensor;
import at.fhv.sysarch.lab2.homeautomation.misc.Weather;

import java.time.Duration;
import java.util.Random;

public class WeatherEnvironment extends AbstractBehavior<WeatherEnvironment.WeatherEnvironmentCommand> {


    public interface WeatherEnvironmentCommand {
    }

    public static final class WeatherChanged implements WeatherEnvironmentCommand {
        private Weather changedWeather;

        public WeatherChanged(Weather changedWeather) {
            this.changedWeather = changedWeather;
        }

    }

    private Weather currentWeather = Weather.SUNNY;

    private final TimerScheduler<WeatherEnvironmentCommand> weatherTimerScheduler;

    private final ActorRef<WeatherSensor.WeatherCommand> weatherSensor;

    public static Behavior<WeatherEnvironmentCommand> create(ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new WeatherEnvironment(context, timers, weatherSensor)));
    }

    private WeatherEnvironment(ActorContext<WeatherEnvironmentCommand> context, TimerScheduler<WeatherEnvironmentCommand> weatherTimerScheduler, ActorRef<WeatherSensor.WeatherCommand> weatherSensor) {
        super(context);
        this.weatherTimerScheduler = weatherTimerScheduler;
        this.weatherTimerScheduler.startTimerAtFixedRate(new WeatherChanged(currentWeather), Duration.ofSeconds(20));
        this.weatherSensor = weatherSensor;
    }

    @Override
    public Receive<WeatherEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(WeatherChanged.class, this::onChangeWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private WeatherEnvironment onChangeWeather(WeatherChanged w) {
        this.currentWeather = w.changedWeather;
        getContext().getLog().info("Weather changed to {}", currentWeather);
        weatherSensor.tell(new WeatherSensor.ReadWeather(currentWeather));
        w.changedWeather = Weather.values()[new Random().nextInt(Weather.values().length)];
        return this;
    }

    private Behavior<WeatherEnvironmentCommand> onPostStop() {
        getContext().getLog().info("WeatherEnvironment actor stopped");
        return this;
    }
}
