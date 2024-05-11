package at.fhv.sysarch.lab2.homeautomation;

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
import at.fhv.sysarch.lab2.homeautomation.ui.UI;

public class HomeAutomationController extends AbstractBehavior<Void>{
    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<WeatherSensor.WeatherCommand> weatherSensor;
    private ActorRef<AirCondition.AirConditionCommand> airCondition;
    private ActorRef<TemperatureEnvironment.TemperatureEnvironmentCommand> tempEnvironment;
    private TemperatureEnvironment.TemperatureChanged temperatureChanged;
    private ActorRef<WeatherEnvironment.WeatherEnvironmentCommand> weatherEnvironment;
    private ActorRef<Blinds.BlindsCommand> blinds;
    private ActorRef<MediaStation.MediaStationCommand> mediaStation;

    public static Behavior<Void> create() {
        return Behaviors.setup(HomeAutomationController::new);
    }

    private  HomeAutomationController(ActorContext<Void> context) {
        super(context);
        // TODO: consider guardians and hierarchies. Who should create and communicate with which Actors?
        this.airCondition = getContext().spawn(AirCondition.create("2", "1"), "AirCondition");
        this.blinds = getContext().spawn(Blinds.create("2", "2"), "Blinds");
        this.mediaStation = getContext().spawn(MediaStation.create(this.blinds, "2", "3"), "MediaStation");
        this.tempSensor = getContext().spawn(TemperatureSensor.create(this.airCondition, "1", "1"), "TemperatureSensor");
        this.weatherSensor = getContext().spawn(WeatherSensor.create(this.blinds,"1", "2"), "WeatherSensor");
        this.temperatureChanged = new TemperatureEnvironment.TemperatureChanged(15.0);
        this.tempEnvironment = getContext().spawn(TemperatureEnvironment.create(this.tempSensor, this.temperatureChanged), "TemperatureEnvironment");
        this.weatherEnvironment = getContext().spawn(WeatherEnvironment.create(this.weatherSensor), "WeatherEnvironment");

        ActorRef<Void> ui = getContext().spawn(UI.create(this.airCondition, this.weatherEnvironment, this.tempEnvironment, this.mediaStation, this.temperatureChanged), "UI");
        getContext().getLog().info("HomeAutomation Application started");
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private HomeAutomationController onPostStop() {
        getContext().getLog().info("HomeAutomation Application stopped");
        return this;
    }
}
