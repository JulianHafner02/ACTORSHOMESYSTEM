package at.fhv.sysarch.lab2.homeautomation.misc;

import java.util.ArrayList;
import java.util.List;

public class ProductFactory {
    private final static List<Product> predeterminedProducts = new ArrayList<>();

    static {
        predeterminedProducts.add(new Product("Apple", 150, 1, 0.99));
        predeterminedProducts.add(new Product("Beer", 350, 4, 4.50));
        predeterminedProducts.add(new Product("Chicken", 550, 7, 9.99));
        predeterminedProducts.add(new Product("Laugen", 175, 2, 1.35));
        predeterminedProducts.add(new Product("Donut", 125, 2, 2.99));
    }

    public static Product createProduct(String name) {
        for (Product product : predeterminedProducts) {
            if (product.getName().equalsIgnoreCase(name)) {
                return new Product(product.getName(), product.getWeight(), product.getSpace(), product.getPrice());
            }
        }
        return new Product("Apple", 100, 1, 0.99);
    }
}
