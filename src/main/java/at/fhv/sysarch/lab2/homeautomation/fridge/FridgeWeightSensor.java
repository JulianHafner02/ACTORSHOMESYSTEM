package at.fhv.sysarch.lab2.homeautomation.fridge;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.PostStop;
import at.fhv.sysarch.lab2.homeautomation.misc.Product;

public class FridgeWeightSensor extends AbstractBehavior<FridgeWeightSensor.FridgeWeightSensorCommand> {

    public interface FridgeWeightSensorCommand {}

    public static final class AddProductWeight implements FridgeWeightSensorCommand {
        private Product product;
        public AddProductWeight(Product product) {
            this.product = product;
        }
    }

    public static final class RemoveProductWeight implements FridgeWeightSensorCommand {
        private Product product;
        public RemoveProductWeight(Product product) {
            this.product = product;
        }
    }

    private final String groupId;
    private final String deviceId;
    private final int maxWeight = 3500;
    private int currentWeight = 0;

    public static Behavior<FridgeWeightSensorCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new FridgeWeightSensor(context, groupId, deviceId));
    }

    private FridgeWeightSensor(ActorContext<FridgeWeightSensorCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("FridgeWeightSensor started");
    }

    @Override
    public Receive<FridgeWeightSensorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddProductWeight.class, this::onAddProductWeight)
                .onMessage(RemoveProductWeight.class, this::onRemoveProductWeight)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeWeightSensorCommand> onAddProductWeight(AddProductWeight a) {
        currentWeight += a.product.getWeight();
        getContext().getLog().info("FridgeWeightSensor received {}", currentWeight);
        return this;
    }

    private Behavior<FridgeWeightSensorCommand> onRemoveProductWeight(RemoveProductWeight a) {
        currentWeight -= a.product.getWeight();
        getContext().getLog().info("FridgeWeightSensor received {}", currentWeight);
        return this;
    }

    private FridgeWeightSensor onPostStop() {
        getContext().getLog().info("FridgeWeightSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }



}
