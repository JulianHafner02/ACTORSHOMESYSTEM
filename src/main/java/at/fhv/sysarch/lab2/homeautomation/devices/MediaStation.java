package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;

public class MediaStation extends AbstractBehavior<MediaStation.MediaStationCommand> {

    public interface MediaStationCommand {}

    public static final class PlayMovie implements MediaStationCommand {
        private boolean playMovie;

        public PlayMovie(boolean playMovie) {
            this.playMovie = playMovie;
        }
    }

    public static final class PowerOn implements MediaStationCommand {
        private boolean poweredOn;

        public PowerOn(boolean poweredOn) {
            this.poweredOn = poweredOn;
        }
    }

    public static Behavior<MediaStationCommand> create(ActorRef<Blinds.BlindsCommand> blinds, String groupId, String deviceId) {
        return Behaviors.setup(context -> new MediaStation(context, groupId, deviceId, blinds));
    }

    private boolean playMovie = false;
    private boolean poweredOn = false;
    private final String groupId;
    private final String deviceId;
    private final ActorRef<Blinds.BlindsCommand> blinds;


    public MediaStation(ActorContext<MediaStationCommand> context, String groupId, String deviceId, ActorRef<Blinds.BlindsCommand> blinds) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.blinds = blinds;
    }

    @Override
    public Receive<MediaStationCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(PowerOn.class, this::onPowerOn)
                .onMessage(PlayMovie.class, this::onPlayMovie)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<MediaStationCommand> onPlayMovie(PlayMovie p) {
        if(poweredOn && p.playMovie != playMovie) {
            playMovie = p.playMovie;
            blinds.tell(new Blinds.ReceiveMovie(playMovie));
            getContext().getLog().info("MediaStation is {}", playMovie ? "playing a movie" : "not playing a movie");
        }
        else if (!poweredOn){
            getContext().getLog().info("MediaStation is not powered on");
        }
        else {
            getContext().getLog().info("MediaStation is already {}", playMovie ? "playing a movie" : "not playing a movie");
        }
        return this;
    }

    private Behavior<MediaStationCommand> onPowerOn(PowerOn p) {
        poweredOn = p.poweredOn;
        getContext().getLog().info("MediaStation is {}", poweredOn ? "on" : "off");
        return this;
    }

    private Behavior<MediaStationCommand> onPostStop() {
        getContext().getLog().info("MediaStation actor {}-{} stopped", groupId, deviceId);
        return this;
    }

}

