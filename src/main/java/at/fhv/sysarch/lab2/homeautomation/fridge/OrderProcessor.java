package at.fhv.sysarch.lab2.homeautomation.fridge;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;
import at.fhv.sysarch.lab2.homeautomation.misc.Product;

public class OrderProcessor extends AbstractBehavior<OrderProcessor.OrderProcessorCommand> {

    public interface OrderProcessorCommand {}

    public static final class AvailableWeight implements OrderProcessorCommand {
        private int availableWeight;
        public AvailableWeight(int availableWeight) {
            this.availableWeight = availableWeight;
        }
    }

    public static final class AvailableSpace implements OrderProcessorCommand {
        private int availableSpace;
        public AvailableSpace(int availableSpace) {
            this.availableSpace = availableSpace;
        }
    }



    private Product product;
    private int availableWeight = -1;
    private int availableSpace = -1;
    private ActorRef<FridgeSpaceSensor.FridgeSpaceSensorCommand> fridgeSpaceSensor;
    private ActorRef<FridgeWeightSensor.FridgeWeightSensorCommand> fridgeWeightSensor;
    private ActorRef<Fridge.FridgeCommand> fridge;

    public static Behavior<OrderProcessorCommand> create( ActorRef<FridgeSpaceSensor.FridgeSpaceSensorCommand> fridgeSpaceSensor, ActorRef<FridgeWeightSensor.FridgeWeightSensorCommand> fridgeWeightSensor, ActorRef<Fridge.FridgeCommand> fridge, Product product) {
        return Behaviors.setup(context -> new OrderProcessor(context, fridgeSpaceSensor, fridgeWeightSensor, fridge, product));
    }

    private OrderProcessor(ActorContext<OrderProcessorCommand> context, ActorRef<FridgeSpaceSensor.FridgeSpaceSensorCommand> fridgeSpaceSensor, ActorRef<FridgeWeightSensor.FridgeWeightSensorCommand> fridgeWeightSensor, ActorRef<Fridge.FridgeCommand> fridge, Product product) {
        super(context);
        this.fridgeSpaceSensor = fridgeSpaceSensor;
        this.fridgeWeightSensor = fridgeWeightSensor;
        this.fridge = fridge;
        this.product = product;
        getContext().getLog().info("OrderProcessor started");
        this.fridgeSpaceSensor.tell(new FridgeSpaceSensor.GetAvailableSpace(getContext().getSelf()));
        this.fridgeWeightSensor.tell(new FridgeWeightSensor.GetAvailableWeight(getContext().getSelf()));
    }

    @Override
    public Receive<OrderProcessorCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AvailableWeight.class, this::onAvailableWeight)
                .onMessage(AvailableSpace.class, this::onAvailableSpace)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }


    private Behavior<OrderProcessorCommand> onAvailableWeight(AvailableWeight a) {
        availableWeight = a.availableWeight;
        getContext().getLog().info("OrderProcessor received {} availableWeight", availableWeight);
        permitOrder();
        return this;
    }

    private Behavior<OrderProcessorCommand> onAvailableSpace(AvailableSpace a) {
        availableSpace = a.availableSpace;
        getContext().getLog().info("OrderProcessor received {} availableSpace", availableSpace);
        permitOrder();
        return this;
    }

    private void permitOrder() {
        if (availableSpace > 0 && availableWeight > 0) {
            if (availableSpace >= product.getSpace() && availableWeight >= product.getWeight()) {
                getContext().getLog().info("Order permitted");
                getContext().getLog().info("Receipt of ordered product " +product.getName() + " price " + product.getPrice() + "€");
                fridge.tell(new Fridge.AddOrderedProduct(product));
            } else {
                getContext().getLog().info("Order not permitted");
            }
        }
        else {
            getContext().getLog().info("Available space or weight not received yet");
        }
    }


    private Behavior<OrderProcessorCommand> onPostStop() {
        getContext().getLog().info("OrderProcessor stopped");
        return this;
    }


}
