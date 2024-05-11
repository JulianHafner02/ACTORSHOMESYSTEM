package at.fhv.sysarch.lab2.homeautomation.environment;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;

import java.time.Duration;

public class TemperatureEnvironment extends AbstractBehavior<TemperatureEnvironment.TemperatureEnvironmentCommand> {

    public interface TemperatureEnvironmentCommand {}

    public static final class TemperatureChanged implements TemperatureEnvironmentCommand {
        private double changedTemperature;

        public TemperatureChanged(double changedTemperature) {
            this.changedTemperature = changedTemperature;
        }

        public void setChangedTemperature(double changedTemperature) {
            this.changedTemperature = changedTemperature;
        }
    }

    private double currentTemperature;
    private final TimerScheduler<TemperatureEnvironmentCommand> temperatureTimerScheduler;
    private TemperatureEnvironment.TemperatureChanged temperatureChanged;
    private final ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;

    public static Behavior<TemperatureEnvironmentCommand> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, TemperatureEnvironment.TemperatureChanged temperatureChanged) {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new TemperatureEnvironment(context, timers, tempSensor, temperatureChanged)));
    }

    private TemperatureEnvironment(ActorContext<TemperatureEnvironmentCommand> context, TimerScheduler<TemperatureEnvironmentCommand> temperatureTimerScheduler, ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, TemperatureEnvironment.TemperatureChanged temperatureChanged) {
        super(context);
        this.temperatureTimerScheduler = temperatureTimerScheduler;
        this.temperatureChanged = temperatureChanged;
        this.temperatureTimerScheduler.startTimerAtFixedRate(this.temperatureChanged, Duration.ofSeconds(5));
        this.tempSensor = tempSensor;
    }

    @Override
    public Receive<TemperatureEnvironmentCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(TemperatureChanged.class, this::onChangeTemperature)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private TemperatureEnvironment onChangeTemperature(TemperatureChanged t) {
        this.currentTemperature = t.changedTemperature;
        getContext().getLog().info("Temperature changed to {}", String.format("%.2f", currentTemperature));
        tempSensor.tell(new TemperatureSensor.ReadTemperature(currentTemperature));
        t.changedTemperature = t.changedTemperature + (Math.random() - 0.5) * 2;
        return this;
    }

    private Behavior<TemperatureEnvironmentCommand> onPostStop() {
        getContext().getLog().info("TemperatureEnvironment actor stopped");
        return this;
    }


}
