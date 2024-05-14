package at.fhv.sysarch.lab2.homeautomation.fridge;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.misc.Product;
import at.fhv.sysarch.lab2.homeautomation.misc.ProductFactory;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    public interface FridgeCommand {}

    public static final class RequestOrder implements FridgeCommand {
        private Product product;

        public RequestOrder(Product product) {
            this.product = product;
        }
    }

    public static final class AddOrderedProduct implements FridgeCommand {
        private Product product;
        public AddOrderedProduct(Product product) {
            this.product = product;
        }
    }

    public static final class ConsumeProduct implements FridgeCommand {
        private Product product;
        public ConsumeProduct(Product product) {
            this.product = product;
        }
        public Product getProduct() {
            return this.product;
        }
    }

    public static final class ListProducts implements FridgeCommand {}
    public static final class ListOrders implements FridgeCommand {}

    private final String groupId;
    private final String deviceId;
    private ArrayList<Product> products = new ArrayList<>();
    private HashMap<Product, LocalDateTime> orders = new HashMap<>();
    private final ActorRef<FridgeWeightSensor.FridgeWeightSensorCommand> weightSensor;
    private final ActorRef<FridgeSpaceSensor.FridgeSpaceSensorCommand> spaceSensor;


    public static Behavior<FridgeCommand> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Fridge(context, groupId, deviceId));
    }

    private Fridge(ActorContext<FridgeCommand> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;
        this.weightSensor = context.spawn(FridgeWeightSensor.create(groupId, deviceId), "weightSensor");
        this.spaceSensor = context.spawn(FridgeSpaceSensor.create(groupId, deviceId), "spaceSensor");
        fillFridge();
        getContext().getLog().info("Fridge started");
    }

    @Override
    public Receive<FridgeCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddOrderedProduct.class, this::onAddOrderedProduct)
                .onMessage(ConsumeProduct.class, this::onConsumeProduct)
                .onMessage(ListProducts.class, this::onListProducts)
                .onMessage(RequestOrder.class, this::onRequestOrder)
                .onMessage(ListOrders.class, this::onListOrders)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<FridgeCommand> onAddOrderedProduct(AddOrderedProduct a) {
        orders.put(a.product, LocalDateTime.now());
        weightSensor.tell(new FridgeWeightSensor.AddProductWeight(a.product));
        spaceSensor.tell(new FridgeSpaceSensor.AddProductSpace(a.product));
        products.add(a.product);
        getContext().getLog().info("Product {} added to fridge", a.product.getName());
        return this;
    }

    private Behavior<FridgeCommand> onConsumeProduct(ConsumeProduct r) {
        String productName = r.product.getName();
        int productCount = 0;
        int removeIndex = 0;
        for (Product product : products) {
            if (product.getName().equals(productName)) {
                productCount++;
                removeIndex = products.indexOf(product);
            }
        }
        if(productCount > 0) {
            products.remove(removeIndex);
            productCount--;
            getContext().getLog().info("Product {} consumed from fridge", productName);
        }
        if (productCount == 0) {
            getContext().getLog().info("No product {} left/found in fridge", productName);
            getContext().getSelf().tell(new RequestOrder(r.product));
        }
        return this;
    }

    private Behavior<FridgeCommand> onListProducts(ListProducts l) {
        getContext().getLog().info("Fridge contains the following products:");
        products.forEach(product -> getContext().getLog().info("Product: {}", product.getName()));
        return this;
    }

    private Behavior<FridgeCommand> onListOrders(ListOrders l) {
        getContext().getLog().info("Fridge contains the following orders:");
        orders.forEach((key, value) -> getContext().getLog().info("Product: {} ordered at {}", key.getName(), value));
        return this;
    }

    private Behavior<FridgeCommand> onRequestOrder(RequestOrder r) {
        getContext().getLog().info("Fridge received order for {}", r.product.getName());
        getContext().spawn(OrderProcessor.create( spaceSensor, weightSensor, getContext().getSelf(), r.product), "OrderProcessor" + UUID.randomUUID());
        return this;
    }


    private void fillFridge(){
        ArrayList<Product> initialProducts = new ArrayList<>();
        initialProducts.add(ProductFactory.createProduct("Apple"));
        initialProducts.add(ProductFactory.createProduct("Beer"));
        initialProducts.add(ProductFactory.createProduct("Chicken"));
        initialProducts.add(ProductFactory.createProduct("Laugen"));
        initialProducts.add(ProductFactory.createProduct("Donut"));

        for(Product p : initialProducts){
            weightSensor.tell(new FridgeWeightSensor.AddProductWeight(p));
            spaceSensor.tell(new FridgeSpaceSensor.AddProductSpace(p));
            products.add(p);
            getContext().getLog().info("Product {} added to fridge", p.getName());
        }

    }
    private Fridge onPostStop() {
        getContext().getLog().info("Fridge actor {}-{} stopped", groupId, deviceId);
        return this;
    }

}