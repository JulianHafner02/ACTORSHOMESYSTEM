package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.Optional;

public class Environment extends AbstractBehavior<Environment.EnvironmentCommand> {

    public interface EnvironmentCommand {}

    public static final class TemperatureChanger implements EnvironmentCommand {

    }

    public static final class WeatherConditionsChanger implements EnvironmentCommand {
        final Optional<Boolean> isSunny;

        public WeatherConditionsChanger(Optional<Boolean> isSunny) {
            this.isSunny = isSunny;
        }
    }

    private double temperature = 15.0;
    private boolean isSunny = false;

    private final TimerScheduler<EnvironmentCommand> temperatureTimeScheduler;
    private final TimerScheduler<EnvironmentCommand> weatherTimeScheduler;

    // TODO: Provide the means for manually setting the temperature
    // TODO: Provide the means for manually setting the weather

    public static Behavior<EnvironmentCommand> create(){
        return Behaviors.setup(context ->  Behaviors.withTimers(timers -> new Environment(context, timers, timers)));
    }

    private Environment(ActorContext<EnvironmentCommand> context,TimerScheduler<EnvironmentCommand> tempTimer, TimerScheduler<EnvironmentCommand> weatherTimer) {
        super(context);
        this.temperatureTimeScheduler = tempTimer;
        this.weatherTimeScheduler = weatherTimer;
        this.temperatureTimeScheduler.startTimerAtFixedRate(new TemperatureChanger(), Duration.ofSeconds(5));
        this.weatherTimeScheduler.startTimerAtFixedRate(new WeatherConditionsChanger(Optional.of(isSunny)), Duration.ofSeconds(35));
    }

    @Override
    public Receive<EnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureChanger.class, this::onChangeTemperature)
                .onMessage(WeatherConditionsChanger.class, this::onChangeWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<EnvironmentCommand> onChangeTemperature(TemperatureChanger t) {
        // TODO: Implement behavior for random changes to temperature
        this.temperature += 0.1;
        getContext().getLog().info("Environment received {}", temperature);
        // TODO: Handling of temperature change. Are sensors notified or do they read the temperature?
        return this;
    }

    private Behavior<EnvironmentCommand> onChangeWeather(WeatherConditionsChanger w) {
        getContext().getLog().info("Environment Change Sun to {}", !isSunny);
        // TODO: Implement behavior for random changes to weather. Include more than just sunny and not sunny
        isSunny = !isSunny;
        // TODO: Handling of weather change. Are sensors notified or do they read the weather information?
        return this;
    }


    private Environment onPostStop(){
        getContext().getLog().info("Environment actor stopped");
        return this;
    }
}


