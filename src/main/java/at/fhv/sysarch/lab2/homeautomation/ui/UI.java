package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.*;
import at.fhv.sysarch.lab2.homeautomation.environment.TemperatureEnvironment;
import at.fhv.sysarch.lab2.homeautomation.environment.WeatherEnvironment;
import at.fhv.sysarch.lab2.homeautomation.fridge.Fridge;
import at.fhv.sysarch.lab2.homeautomation.misc.ProductFactory;
import at.fhv.sysarch.lab2.homeautomation.misc.Weather;
import at.fhv.sysarch.lab2.homeautomation.misc.Product;

import java.time.Duration;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private final ActorRef<AirCondition.AirConditionCommand> airCondition;
    private final ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnvironment;
    private final ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnvironment;
    private final ActorRef<MediaStation.MediaStationCommand> mediaStation;
    private TemperatureEnvironment.TemperatureChanged temperatureChanged;
    private ActorRef<Fridge.FridgeCommand> fridge;

    public static Behavior<Void> create( ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnvironment, ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnvironment, ActorRef<MediaStation.MediaStationCommand> mediaStation, TemperatureEnvironment.TemperatureChanged temperatureChanged, ActorRef<Fridge.FridgeCommand> fridge) {
        return Behaviors.setup(context -> new UI(context, airCondition, weatherEnvironment, tempEnvironment, mediaStation, temperatureChanged, fridge));
    }

    private UI(ActorContext<Void> context, ActorRef<AirCondition.AirConditionCommand> airCondition, ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnvironment, ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnvironment, ActorRef<MediaStation.MediaStationCommand> mediaStation, TemperatureEnvironment.TemperatureChanged temperatureChanged, ActorRef<Fridge.FridgeCommand> fridge) {
        super(context);
        this.airCondition = airCondition;
        this.weatherEnvironment = weatherEnvironment;
        this.tempEnvironment = tempEnvironment;
        this.mediaStation = mediaStation;
        this.temperatureChanged = temperatureChanged;
        this.fridge = fridge;

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
                System.out.println("Enter command ('temperature <value>', 'aircon <true/false>', 'weather <type>', 'mediap <true/false>', 'mediamovie <true/false>', 'consume <product>', 'order <product>', 'fridgelist', 'orderhistory'): ");
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    String[] parts = input.split(" ");
                    try {
                        if (parts.length > 0) {
                            switch (parts[0].toLowerCase()) {
                                case "temperature":
                                    if (parts.length > 1) {
                                        double temperature = Double.parseDouble(parts[1]);
                                        this.temperatureChanged.setChangedTemperature(temperature);
                                        tempEnvironment.tell(this.temperatureChanged);
                                    } else {
                                        System.err.println("Temperature value is missing.");
                                    }
                                    break;
                                case "aircon":
                                    if (parts.length > 1) {
                                        boolean powerState = Boolean.parseBoolean(parts[1]);
                                        airCondition.tell(new AirCondition.PowerAirCondition(powerState));
                                    } else {
                                        System.err.println("Power state is missing.");
                                    }
                                    break;
                                case "weather":
                                    if (parts.length > 1) {
                                        Weather weather = Weather.valueOf(parts[1].toUpperCase());
                                        weatherEnvironment.tell(new WeatherEnvironment.WeatherChanged(weather));
                                    } else {
                                        System.err.println("Weather type is missing.");
                                    }
                                    break;
                                case "mediap":
                                    if (parts.length > 1) {
                                        boolean powerOn = Boolean.parseBoolean(parts[1]);
                                        mediaStation.tell(new MediaStation.PowerOn(powerOn));
                                    } else {
                                        System.err.println("Power state for media station is missing.");
                                    }
                                    break;
                                case "mediamovie":
                                    if (parts.length > 1) {
                                        boolean playMovie = Boolean.parseBoolean(parts[1]);
                                        mediaStation.tell(new MediaStation.PlayMovie(playMovie));
                                    } else {
                                        System.err.println("Play state for movie is missing.");
                                    }
                                    break;
                                case "consume":
                                    if (parts.length > 1) {
                                        String productName = parts[1];
                                        Product product = ProductFactory.createProduct(productName);
                                        fridge.tell(new Fridge.ConsumeProduct(product));
                                    } else {
                                        System.err.println("Product name is missing.");
                                    }
                                    break;
                                case "order":
                                    if (parts.length > 1) {
                                        String productName = parts[1];
                                        Product product = ProductFactory.createProduct(productName);
                                        fridge.tell(new Fridge.RequestOrder(product));
                                    } else {
                                        System.err.println("Product name is missing.");
                                    }
                                    break;
                                case "fridgelist":
                                    fridge.tell(new Fridge.ListProducts());
                                    break;
                                case "orderhistory":
                                    fridge.tell(new Fridge.ListOrders());
                                    break;
                                default:
                                    System.err.println("Invalid command.");
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid input format for numerical values.");
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid input for weather or product name.");
                    }
                }
            }
        }
    }
}
