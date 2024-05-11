package at.fhv.sysarch.lab2.homeautomation.fridge;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import at.fhv.sysarch.lab2.homeautomation.misc.Product;
import at.fhv.sysarch.lab2.homeautomation.misc.ProductFactory;

import java.util.ArrayList;
import java.util.Iterator;

public class Fridge extends AbstractBehavior<Fridge.FridgeCommand> {

    public interface FridgeCommand {}


    public static final class AddProduct implements FridgeCommand {
        private Product product;
        public AddProduct(Product product) {
            this.product = product;
        }
    }

    public static final class RemoveProduct implements FridgeCommand {
        private Product product;
        public RemoveProduct(Product product) {
            this.product = product;
        }
        public Product getProduct() {
            return this.product;
        }
    }

    public static final class ListProducts implements FridgeCommand {}

    private final String groupId;
    private final String deviceId;
    private ArrayList<Product> products = new ArrayList<>();
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
                .onMessage(AddProduct.class, this::onAddProduct)
                .onMessage(RemoveProduct.class, this::onRemoveProduct)
                .onMessage(ListProducts.class, this::onListProducts)
                .build();
    }

    private Behavior<FridgeCommand> onAddProduct(AddProduct a) {



        return this;
    }

    private Behavior<FridgeCommand> onRemoveProduct(RemoveProduct r) {
        String productName = r.product.getName();
        boolean found = false;
        Iterator<Product> iterator = products.iterator();
        while (iterator.hasNext()) {
            Product product = iterator.next();
            if (product.getName().equals(productName)) {
                found = true;
                iterator.remove();
                weightSensor.tell(new FridgeWeightSensor.RemoveProductWeight(product));
                spaceSensor.tell(new FridgeSpaceSensor.RemoveProductSpace(product));
                getContext().getLog().info("Product {} removed from fridge", productName);
                break;
            }
        }

        if (!found) {
            getContext().getLog().info("Product {} not found in fridge", productName);
        }
        return this;
    }

    private Behavior<FridgeCommand> onListProducts(ListProducts l) {
        getContext().getLog().info("Fridge contains the following products:");
        products.forEach(product -> getContext().getLog().info("Product: {}", product.getName()));
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