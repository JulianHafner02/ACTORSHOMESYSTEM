package at.fhv.sysarch.lab2.homeautomation.ui;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.devices.AirCondition;
import at.fhv.sysarch.lab2.homeautomation.devices.TemperatureSensor;

import java.time.Duration;
import java.util.Scanner;

public class UI extends AbstractBehavior<Void> {

    private ActorRef<TemperatureSensor.TemperatureCommand> tempSensor;
    private ActorRef<AirCondition.AirConditionCommand> airCondition;

    public static Behavior<Void> create(ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, ActorRef<AirCondition.AirConditionCommand> airCondition) {
        return Behaviors.setup(context -> new UI(context, tempSensor, airCondition));
    }

    private UI(ActorContext<Void> context, ActorRef<TemperatureSensor.TemperatureCommand> tempSensor, ActorRef<AirCondition.AirConditionCommand> airCondition) {
        super(context);
        this.tempSensor = tempSensor;
        this.airCondition = airCondition;
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
                            tempSensor.tell(new TemperatureSensor.ReadTemperature(temperature));
                        } else if (parts[0].equalsIgnoreCase("a") && parts.length > 1) {
                            boolean powerState = Boolean.parseBoolean(parts[1]);
                            airCondition.tell(new AirCondition.PowerAirCondition(powerState));
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid input format.");
                    }
                }
            }
        }
    }
}
