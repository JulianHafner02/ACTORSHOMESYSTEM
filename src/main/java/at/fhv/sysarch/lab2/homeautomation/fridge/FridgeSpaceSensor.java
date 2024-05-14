package at.fhv.sysarch.lab2.homeautomation.fridge;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.PostStop;
import at.fhv.sysarch.lab2.homeautomation.misc.Product;

public class FridgeSpaceSensor extends AbstractBehavior<FridgeSpaceSensor.FridgeSpaceSensorCommand> {

    public interface FridgeSpaceSensorCommand {}

    public static final class AddProductSpace implements FridgeSpaceSensorCommand {
        private Product product;
        public AddProductSpace(Product product) {
            this.product = product;
        }
    }

    public static final class RemoveProductSpace implements FridgeSpaceSensorCommand {
        private Product product;
        public RemoveProductSpace(Product product) {
            this.product = product;
        }
    }

    public static final class GetAvailableSpace implements FridgeSpaceSensorCommand {
        private ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessor;
        public GetAvailableSpace(ActorRef<OrderProcessor.OrderProcessorCommand> orderProcessor) {
            this.orderProcessor = orderProcessor;
        }
    }

    private final String groupId;
    private final String deviceId;
    private final int maxSpace = 35;
    private int currentSpace = 0;

    public static Behavior<FridgeSpaceSensorCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new FridgeSpaceSensor(context, groupId, deviceId));
    }

    private FridgeSpaceSensor(ActorContext<FridgeSpaceSensorCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        getContext().getLog().info("FridgeSpaceSensor started");
    }

    @Override
    public Receive<FridgeSpaceSensorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddProductSpace.class, this::onAddProductSpace)
                .onMessage(RemoveProductSpace.class, this::onRemoveProductSpace)
                .onMessage(GetAvailableSpace.class, this::onGetAvailableSpace)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeSpaceSensorCommand> onAddProductSpace(AddProductSpace a) {
        currentSpace += a.product.getSpace();
        getContext().getLog().info("FridgeSpaceSensor received {} current space", currentSpace);
        return this;
    }

    private Behavior<FridgeSpaceSensorCommand> onRemoveProductSpace(RemoveProductSpace a) {
        currentSpace -= a.product.getSpace();
        getContext().getLog().info("FridgeSpaceSensor received {} current space", currentSpace);
        return this;
    }

    private Behavior<FridgeSpaceSensorCommand> onGetAvailableSpace(GetAvailableSpace a) {
        a.orderProcessor.tell(new OrderProcessor.AvailableSpace(maxSpace - currentSpace));
        return this;
    }

    private FridgeSpaceSensor onPostStop() {
        getContext().getLog().info("FridgeSpaceSensor actor {}-{} stopped", groupId, deviceId);
        return this;
    }

}
