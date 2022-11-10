import java.util.ArrayList;
/**
 * A store class
 * 
 * @authors Tyler, Vijay, Shreya, Rohan
 * @version rev1
 */
public class Store {
    /*
     * the name of the store
     */
    private String sellerName;

    /*
     * Name of the store
     */
    private String storeName;

    /*
     * A list of products associated with the store
     */
    private ArrayList<Product> products;

    /*
     * A sale history of the store including
     * customer information and revenue
     */


    public Store(String sellerName , String storeName) {
        this.sellerName = sellerName;
        products = new ArrayList<Product>();

    }

    public Store(String sellerName, String storeName , ArrayList<Product> products) {
        this.sellerName = sellerName;
        this.products = products;

    }

    public Store() {
        sellerName = null;
        storeName = null;
        products = null;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Product> products) {
        this.products = products;
    }

    public void addProduct(Product p) {
        products.add(p);
    }


}