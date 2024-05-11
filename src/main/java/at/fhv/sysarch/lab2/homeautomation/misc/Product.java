package at.fhv.sysarch.lab2.homeautomation.misc;

public class Product {

    private String name;
    private int weight;
    private int space;
    private double price;

    public Product(String name, int weight, int space, double price) {
        this.name = name;
        this.weight = weight;
        this.space = space;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getSpace() {
        return space;
    }

    public void setSpace(int space) {
        this.space = space;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "name='" + name + '\'' +
                ", weight=" + weight +
                ", space=" + space +
                ", price=" + price +
                '}';
    }
}
