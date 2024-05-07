package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.*;
import at.fhv.sysarch.lab2.homeautomation.environment.Environment;
import at.fhv.sysarch.lab2.homeautomation.misc.Weather;

import java.time.Duration;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<Environment.EnvironmentCommand> environment;
    private final ActorRef<MediaStation.MediaStationCommand> mediaStation;

    public static Behavior<Void> create( ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<Environment.EnvironmentCommand> environment, ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        return Behaviors.setup(context -> new UI(context, airCondition, environment, mediaStation));
    }

    private UI(ActorContext<Void> context, ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<Environment.EnvironmentCommand> environment, ActorRef<MediaStation.MediaStationCommand> mediaStation) {
        super(context);
        this.airCondition = airCondition;
        this.environment = environment;
        this.mediaStation = mediaStation;
        getContext().getSystem().scheduler().scheduleOnce(Duration.ofSeconds(1), this::runCommandLine, getContext().getSystem().executionContext());
        getContext().getLog().info("UI started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private UI onPostStop() {
        getContext().getLog().info("UI stopped");
        return this;
    }

    public void runCommandLine() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Enter command ('t <temperature>' or 'a <true/false>'): ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    String[] parts = input.split(" ");
                    try {
                        if (parts[0].equalsIgnoreCase("t") && parts.length > 1) {
                            double temperature = Double.parseDouble(parts[1]);
                            environment.tell(new Environment.TemperatureEnvironment(temperature));
                        } else if (parts[0].equalsIgnoreCase("a") && parts.length > 1) {
                            boolean powerState = Boolean.parseBoolean(parts[1]);
                            airCondition.tell(new AirCondition.PowerAirCondition(powerState));
                        } else if (parts[0].equalsIgnoreCase("w") && parts.length > 1) {
                            Weather weather = Weather.valueOf(parts[1].toUpperCase());
                            environment.tell(new Environment.WeatherEnvironment(weather));
                        }
                        else if (parts[0].equalsIgnoreCase("mp") && parts.length > 1) {
                            boolean powerOn = Boolean.parseBoolean(parts[1]);
                            mediaStation.tell(new MediaStation.PowerOn(powerOn));
                        }
                        else if (parts[0].equalsIgnoreCase("mm") && parts.length > 1) {
                            boolean playMovie = Boolean.parseBoolean(parts[1]);
                            mediaStation.tell(new MediaStation.PlayMovie(playMovie));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid input format.");
                    }
                }
            }
        }
    }
}
