/**
 * Purchase class
 * 
 * Project 5
 * 
 * @author 
 * 
 * @version 12/7/22
 */
public class Purchase {
    /*
     * Each purchase object will log 
     */
    //Each store has a field that is a path to a text file with purchase logs
    //Beginning of server while creating store objects read from that file to create an array of purchase objects
    //When a purchase is made create a new Purchase object and add to the array
    //Write back to purchase log file when server thread ends

    //Creating fields for this class
    private String purchasedProductName;
    private double saleRevenue;
    private String buyerUserName;
    
    //QUANTITY WILL ALWAYS BE ONE
    
    public Purchase(String purchasedProductName, double saleRevenue, String buyerUserName) {
        this.purchasedProductName = purchasedProductName;
        this.saleRevenue = saleRevenue;
        this.buyerUserName = buyerUserName;
    }

    public String getProductName() {
        return purchasedProductName;
    }

    public double getSaleRevenue() {
        return saleRevenue;
    }

    public String getBuyerUsername() {
        return buyerUserName;
    }

    //No setters required 


    public String purchaseFileString() {
        String s = "";
        s += purchasedProductName + ";" + saleRevenue + ";" + buyerUserName;
        return s;
    }
    
}