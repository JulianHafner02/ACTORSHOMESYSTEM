package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.misc.Weather;

public class Blinds extends AbstractBehavior<Blinds.BlindsCommand> {

    public interface BlindsCommand {}

    public static final class ReceiveWeather implements BlindsCommand {

        private Weather weather;

        public ReceiveWeather(Weather weather) {
            this.weather = weather;
        }
    }
    public static final class ReceiveMovie implements BlindsCommand {

        private boolean playMovie;

        public ReceiveMovie(boolean playMovie) {
            this.playMovie = playMovie;
        }
    }

    public static Behavior<BlindsCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Blinds(context, groupId, deviceId));
    }

    private Weather weather;
    private boolean playMovie;
    private boolean open = true;
    private final String groupId;
    private final String deviceId;

    public Blinds(ActorContext<BlindsCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("Blinds started");
    }


    @Override
    public Receive<BlindsCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceiveMovie.class, this::onReceiveMovie)
                .onMessage(ReceiveWeather.class, this::onReceiveWeather)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<BlindsCommand> onReceiveMovie(ReceiveMovie r) {
        playMovie = r.playMovie;
        getContext().getLog().info("Blinds received playing a movie: {}", playMovie ? "yes" : "no");
        changeBlinds();
        return this;
    }

    private Behavior<BlindsCommand> onReceiveWeather(ReceiveWeather r) {
        weather = r.weather;
        getContext().getLog().info("Blinds received weather: {}", weather);
        changeBlinds();
        return this;
    }

    private void changeBlinds() {
        open = weather != Weather.SUNNY && weather != Weather.STORMY && !playMovie;
        getContext().getLog().info("Blinds are {}", open? "open":"closed");
    }

    private Blinds onPostStop() {
        getContext().getLog().info("Blinds actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
