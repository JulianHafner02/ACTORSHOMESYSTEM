package at.fhv.sysarch.lab2.homeautomation.devices;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;
import at.fhv.sysarch.lab2.homeautomation.misc.Temperature;

import java.util.Optional;

public class AirCondition extends AbstractBehavior<AirCondition.AirConditionCommand> {
    public interface AirConditionCommand {}

    public static final class PowerAirCondition implements AirConditionCommand {
        final boolean value; // No need for Optional here

        public PowerAirCondition(boolean value) {
            this.value = value;
        }
    }

    public static final class ReceivedTemperature implements AirConditionCommand {
        private Temperature temperature;

        public ReceivedTemperature(double value, String unit) {
            this.temperature = new Temperature(value, unit);
        }
    }

    private final String groupId;
    private final String deviceId;
    private boolean active = false;
    private boolean poweredOn = true;

    public AirCondition(ActorContext<AirConditionCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("AirCondition started");
    }

    public static Behavior<AirConditionCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new AirCondition(context, groupId, deviceId));
    }

    @Override
    public Receive<AirConditionCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReceivedTemperature.class, this::onReadTemperature)
                .onMessage(PowerAirCondition.class, this::onPowerAirCondition)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<AirConditionCommand> onReadTemperature(ReceivedTemperature r) {
        getContext().getLog().info("Aircondition reading: Temp {} {}", String.format("%.2f", r.temperature.getValue()), r.temperature.getUnit());
        // Implement better control logic here
        active = r.temperature.getValue() >= 15;
        getContext().getLog().info("Aircondition {}", active ? "activated" : "deactivated");
        return this;
    }

    private Behavior<AirConditionCommand> onPowerAirCondition(PowerAirCondition r) {
        poweredOn = r.value;
        getContext().getLog().info("Aircondition powered {}", poweredOn ? "on" : "off");
        return this;
    }

    private AirCondition onPostStop() {
        getContext().getLog().info("AirCondition actor {}-{} stopped", groupId, deviceId);
        return this;
    }
}
