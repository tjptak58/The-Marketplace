import java.util.ArrayList;
import java.io.*;
/**
 * A buyer class
 * 
 * @authors Tyler, Vijay, Shreya, Rohan
 * @version rev1
 */
public class Buyer extends Person{

    private ArrayList<Product> shoppingCart; 
    private ArrayList<Product> purchased;
    private String cart;
    private String history;


    public Buyer() {
    }
    
    public Buyer(String username , String password, String email, String cart , String history) {
        super(username, password, email);
        this.cart = cart;
        this.history = history;
        this.shoppingCart = new ArrayList<Product>();
        this.purchased = new ArrayList<Product>();

        try {
            BufferedReader buf = new BufferedReader(new FileReader(new File(cart)));
            String s = buf.readLine();
            while (true) {
                if (s == null) {
                    break;
                } else {
                    String[] split = s.split(";");
                    shoppingCart.add(new Product(split[0], split[1], split[2], Integer.parseInt(split[3]), Double.parseDouble(split[4])));
                    s = buf.readLine();
                }  
            }
            buf.close();

            BufferedReader buftwo = new BufferedReader(new FileReader(new File(history)));
            s = buftwo.readLine();
            while (true) {
                if (s == null) {
                    break;
                } else {
                    String[] split = s.split(";");
                    purchased.add(new Product(split[0], split[1], split[2], Integer.parseInt(split[3]), Double.parseDouble(split[4])));
                    s = buftwo.readLine();
                }  
            }
            buftwo.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    

    /*
     * Gets all product in users.txt that have a name, description, or store that matches the search parameter
     */
    public ArrayList<Product> searchProducts(String searchParameter , ArrayList<Product> market) {
        ArrayList<Product> products = new ArrayList<Product>();
        for (Product p : market) {
            if (p.getProductName().contains(searchParameter)) {
                products.add(p);
            } else if (p.getDescription().contains(searchParameter)) {
                products.add(p);
            } if (p.getStoreName().contains(searchParameter)) {
                products.add(p);
            } 
        }
        return products;
    }

    /*
     * Sorts an Arraylist of products based on price
     * Sorts highest price to lowest if highToLow = true    NOT SURE IF I EVEN NEED THIS
     * if it's false, sorts lowest to highest
     */
    public ArrayList<Product> sortPrice(ArrayList<Product> products , boolean highToLow) {
        ArrayList<Product> output = new ArrayList<Product>();
        ArrayList<Product> p = products;
        while (p.size() > 0) {
            double max = 0;
            int index = 0;
            for (int i = 0 ; i < p.size() ; i++) {
                double price = p.get(i).getPrice();
                if (price >= max) {
                    max = price;
                    index = i;
                }
            }
            output.add(p.get(index));
            p.remove(index);
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<Product> reversed = new ArrayList<Product>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    /*
     * Sorts an Arraylist of products based on quantity
     * Sorts highest price to lowest if highToLow = true
     * if it's false, sorts lowest to highest
     */
    public ArrayList<Product> sortQuantity(ArrayList<Product> products , boolean highToLow) {
        ArrayList<Product> output = new ArrayList<Product>();
        ArrayList<Product> p = products;
        while (p.size() > 0) {
            int max = 0;
            int index = 0;
            for (int i = 0 ; i < p.size() ; i++) {
                int quantity = p.get(i).getQuantity();
                if (quantity >= max) {
                    max = quantity;
                    index = i;
                }
            }
            output.add(p.get(index));
            p.remove(index);
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<Product> reversed = new ArrayList<Product>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    public void updateHistory() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(new File(history) , false));
            for (Product product : purchased) {
                pw.print(product.getProductName() + ";");
                pw.print(product.getStoreName() + ";");
                pw.print(product.getDescription() + ";");
                pw.print(Integer.toString(product.getQuantity()) + ",");
                pw.print(Double.toString(product.getPrice()) + "\n");

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addToCart(Product product) {
        shoppingCart.add(product);
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(new File(cart) , true));           
            pw.print(product.getProductName() + ";");
            pw.print(product.getStoreName() + ";");
            pw.print(product.getDescription() + ";");
            pw.print(Integer.toString(product.getQuantity()) + ",");
            pw.print(Double.toString(product.getPrice()) + "\n");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportHistory(String path) {
        try {
            BufferedReader buf = new BufferedReader(new FileReader(new File(history)));
            PrintWriter pw = new PrintWriter(new FileWriter(new File(path) , true));           
            String s = buf.readLine();
            while (true) {
                if (s == null) {
                    break;
                } else {
                    String[] split = s.split(";");
                    Product p = new Product(split[0], split[1], split[2], Integer.parseInt(split[3]), Double.parseDouble(split[4]));
                    pw.println(p.toString());
                    s = buf.readLine();
                }  
            }
            buf.close();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    }
