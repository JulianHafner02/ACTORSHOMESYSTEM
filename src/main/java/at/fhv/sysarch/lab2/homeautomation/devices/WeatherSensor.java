package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.misc.Weather;

public class WeatherSensor extends AbstractBehavior<WeatherSensor.WeatherCommand> {

    public interface WeatherCommand {}

    public static final class ReadWeather implements WeatherCommand {

        private Weather weather;

        public ReadWeather(Weather weather) {
            this.weather = weather;
        }
    }

    public static Behavior<WeatherCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new WeatherSensor(context, groupId, deviceId));
    }


    private final String groupId;
    private final String deviceId;


    public WeatherSensor(ActorContext<WeatherCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        getContext().getLog().info("WeatherSensor started");
    }

    @Override
    public Receive<WeatherCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadWeather.class, this::onReadWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<WeatherCommand> onReadWeather(ReadWeather r) {
        getContext().getLog().info("WeatherSensor received {}", r.weather);
        return this;
    }

    private WeatherSensor onPostStop() {
        getContext().getLog().info("WeatherSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
