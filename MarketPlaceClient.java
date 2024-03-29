
import javax.print.event.PrintJobListener;
import javax.swing.*;
import java.io.*;
import java.awt.*; 
import java.awt.event.*;
import java.net.*;
import java.util.*; //I NEED TO CREATE PW,OOS,OIS,SCANNER an close it for each instance we're calling the server

/**
 * Main method for project that implements the marketplace
 *
 * @author
 *
 * @verion 11/14/22
 */

public class MarketPlaceClient extends JComponent implements Runnable {
    /**
     *Static string that stores the username of the logged in buyer or seller
     */
    static String username;

    /**
     * Static booleans to determine whether a buyer or seller has logged in
     */
    static boolean loggedInAsBuyer;
    static boolean loggedInAsSeller;
    Socket socket;
    static int portnumber;
    /* static PrintWriter pw;
    static Scanner in;
    static ObjectOutputStream oos;
    static ObjectInputStream ois; */
    ArrayList<String> myStoreNames = new ArrayList<String>();
    ArrayList<String> productNames = new ArrayList<String>();
    ArrayList<String> superStores = new ArrayList<String>();
    PrintWriter pw;
    Scanner in;
    ObjectOutputStream oos;
    ObjectInputStream ois;    



    public static void main(String[] args) {
        SwingUtilities.invokeLater(new MarketPlaceClient(portnumber , username , loggedInAsBuyer , loggedInAsSeller));
    }

    public MarketPlaceClient(int portnumber , String username , boolean loggedInAsBuyer , boolean loggedInAsSeller) {
        MarketPlaceClient.portnumber = portnumber;
        MarketPlaceClient.username = username;
        MarketPlaceClient.loggedInAsBuyer = loggedInAsBuyer;
        MarketPlaceClient.loggedInAsSeller = loggedInAsSeller;
        run();
    }

    public void run() {  //can't close, use shutdownOutput()
        try {
            socket = new Socket("localhost", 4242); //MarketplaceClient.portnumber
            pw = new PrintWriter(socket.getOutputStream());
            in = new Scanner(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream()); 
            if (loggedInAsBuyer) {
                pw.println("GETSUPERSTORES");
                pw.flush();
                superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                buyerMain(superStores);
            } else if (loggedInAsSeller) {     
                pw.println("VIEWSTORES");
                pw.println(username);
                pw.flush();
                myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWSTORES
                sellerMain(myStoreNames);           
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Creates the Home Page for Buyer
     */
    public void buyerMain(ArrayList<String> superStores) {
        JFrame buyerMain = new JFrame("THE MARKETPLACE");
        buyerMain.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(buyerMain, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    buyerMain.dispose();
                }
            }
        });


        Container buyerMainPanel = buyerMain.getContentPane();
        buyerMainPanel.setLayout(new BorderLayout());

        JPanel buyerMainNorth = new JPanel(new FlowLayout());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Refreshes the Home Page
                buyerMain.dispose();
                var productNames = new ArrayList<String>();
                try {
                    pw.println("GETSUPERSTORES");
                    pw.flush();
                    ArrayList<String> superStoresProxy = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                    buyerMain(superStoresProxy); 
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
                 
                
            }
        
        }); 
        JLabel title = new JLabel("<html><h1>THE MARKETPLACE</h1></html>");     //Creates title for home page
        JPanel sort = new JPanel(new FlowLayout());
        String[] s = {"Sort the menu","Most Products Sold","Least Products Sold","My Most Shopped Stores",
        "My Least Shopped Stores"};
        JComboBox sortBox = new JComboBox(s);



        sortBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    ArrayList<String> superStoresProxy = new ArrayList<String>();
                    try {
                        pw.println("GETSUPERSTORES");
                        pw.flush();
                        superStoresProxy = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                    String choice;
                    JComboBox getSelection = (JComboBox) event.getSource();
                    choice = (String) getSelection.getSelectedItem();
                    if (choice == s[1]) { 
                        ArrayList<String> sorted = sortProductsSold(superStoresProxy , true);
                        buyerMain.dispose();
                        buyerMain(sorted);
                    } else if (choice == s[2]) {
                        ArrayList<String> sorted = sortProductsSold(superStoresProxy , false);
                        buyerMain.dispose();
                        buyerMain(sorted);
                    } else if (choice == s[3]) {
                        ArrayList<String> sorted = sortMostShopped(superStoresProxy , true);
                        buyerMain.dispose();
                        buyerMain(sorted);
                    } else if (choice == s[4]) {
                        ArrayList<String> sorted = sortMostShopped(superStoresProxy , false);
                        buyerMain.dispose();
                        buyerMain(sorted);
                    }  
                }
            }
        });
        sort.add(sortBox);
        buyerMainNorth.add(sort);


        buyerMainNorth.add(title);
        buyerMainNorth.add(refresh);
        buyerMain.add(buyerMainNorth, BorderLayout.NORTH);

        JPanel buyerMainSouth = new JPanel(new FlowLayout());
        JButton purchaseHistory = new JButton("Purchase History");
        purchaseHistory.addActionListener(new ActionListener() {         
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Link to the purchase history of the user
                ArrayList<String> history = new ArrayList<String>(); 
                try {
                    pw.println("GETPURCHASEHISTORY");
                    pw.println(username);
                    pw.flush();
                    history = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETPURCHASEHISTORY
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                displayPurchaseHistory(history);   
                buyerMain.dispose();
            }
        
        });

        JButton viewCart = new JButton("View Cart");                
        viewCart.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Link to the shopping cart of a user
                ArrayList<String> cart = new ArrayList<String>(); 
                try {    
                    pw.println("VIEWCART");
                    pw.println(username);
                    pw.flush();
                    cart = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWCART
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                displayCart(cart);
                buyerMain.dispose();
            }
        
        });
        JButton editAccount = new JButton("Edit Account");
        editAccount.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Link to the page to edit a buyer account
                var info = new ArrayList<String>(); 
                try {
                    pw.println("GETACCOUNTINFO");   //ROHANFIX Only works if the user is a buyer, not a seller
                    pw.println(username);
                    pw.flush();
                    info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETACCOUNTINFO
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                editAccount(info);
                buyerMain.dispose();
            }
        
        });
        JButton logout = new JButton("Logout");
        logout.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Link to the page to goodbye page
                buyerMain.dispose();
                goodbye();
                
            }
        
        });
        buyerMainSouth.add(purchaseHistory);
        buyerMainSouth.add(viewCart);
        buyerMainSouth.add(editAccount);
        buyerMainSouth.add(logout);
        buyerMain.add(buyerMainSouth, BorderLayout.SOUTH);

        JPanel buyerMainCentral = new JPanel(new BorderLayout());
        JPanel productsPanel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(productsPanel, BoxLayout.Y_AXIS); //Variable number of products from product list
        productsPanel.setLayout(boxlayout);
        ArrayList<JPanel> panels = new ArrayList<JPanel>();
        
        for (String store : superStores) {
            JPanel store1 = new JPanel(new FlowLayout());
            JLabel store1Text = new JLabel(store);
            store1.add(store1Text);
            JButton store1Add = new JButton("Shop");
            store1Add.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { // ACTION LISTENER - Add to cart Ask the user how many they
                    try {
                        pw.println("GETPRODUCTSINSTORE");
                        pw.println(store);
                        pw.flush();
                        ArrayList<String> productsProxy = (ArrayList<String>) ois.readObject(); // SERVERREQUEST - 
                        buyerViewStore(store, productsProxy);
                        buyerMain.dispose();    
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
            store1.add(store1Add);
            panels.add(store1);

        }

        for (JPanel p : panels) {  //Displays the panels
            productsPanel.add(p);
        }

        //Do something to reorganize panels

        JScrollPane scrollPane = new JScrollPane(productsPanel); 
        buyerMainCentral.add(scrollPane , BorderLayout.CENTER);
        buyerMain.add(buyerMainCentral, BorderLayout.CENTER);
            
            

        buyerMain.pack();
        buyerMain.setSize(800, 600);
        buyerMain.setLocationRelativeTo(null);
        buyerMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        buyerMain.setVisible(true);

    }
    /*
    * Creates the info page for a store
    */
    public void buyerViewStore(String storeName , ArrayList<String> products) {
        JFrame buyerViewStore = new JFrame(storeName);
        buyerViewStore.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(buyerViewStore, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    buyerViewStore.dispose();
                }
            }
        });

        Container buyerViewStorePanel = buyerViewStore.getContentPane();
        buyerViewStorePanel.setLayout(new BorderLayout());

        JPanel buyerViewStoreNorth = new JPanel(new FlowLayout());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Refreshes the Home Page
                buyerViewStore.dispose();
                ArrayList<String> pInStore = new ArrayList<String>();
                try {
                    pw.println("GETPRODUCTSINSTORE");
                    pw.println(storeName);
                    pw.flush();
                    pInStore = (ArrayList<String>) ois.readObject(); //SERVERREQUEST - GETPRODUCTSINSTORE 
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                buyerViewStore(storeName , pInStore);  
                
            }
        
        }); 
        JLabel title = new JLabel("<html><h1>THE MARKETPLACE</h1></html>");     //Creates title for home page
        buyerViewStoreNorth.add(title);
        buyerViewStoreNorth.add(refresh);
        buyerViewStore.add(buyerViewStoreNorth, BorderLayout.NORTH);

        JPanel buyerViewStoreCentral = new JPanel(new BorderLayout());
        JPanel searchAndSort = new JPanel(new FlowLayout());
        JTextField searchText = new JTextField(10);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {   //ACTION LISTENER - Searches for a product
                ArrayList<String> searched = searchProducts(storeName , searchText.getText());      
                buyerViewStore.dispose();
                buyerViewStore(storeName , searched);
                
            }
        
        });
        String[] s = {"Sort the menu","Sort by price High to Low","Sort by price Low to High","Sort by quantity High to Low","Sort by quantity Low to Hight"};
        JComboBox sortBox = new JComboBox(s);
        sortBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    String choice;
                    JComboBox getSelection = (JComboBox) event.getSource();
                    choice = (String) getSelection.getSelectedItem();
                    if (choice.equals(s[1])) { 
                        buyerViewStore.dispose();
                        ArrayList<String> sorted = sortPrice(storeName , true);
                        buyerViewStore(storeName , sorted);
                    } else if (choice.equals(s[2])) {
                        buyerViewStore.dispose();
                        ArrayList<String> sorted = sortPrice(storeName , false);
                        buyerViewStore(storeName , sorted);
                    } else if (choice.equals(s[3])) {
                        buyerViewStore.dispose();
                        ArrayList<String> sorted = sortQuantity(storeName , true);
                        buyerViewStore(storeName , sorted);
                    } else if (choice.equals(s[4])) {
                        buyerViewStore.dispose();
                        ArrayList<String> sorted = sortQuantity(storeName , false);
                        buyerViewStore(storeName , sorted);
                    }    
                }
            }
        });
        searchAndSort.add(searchText);                    //Creates search and sort bars
        searchAndSort.add(searchButton);
        searchAndSort.add(sortBox);
        buyerViewStoreCentral.add(searchAndSort , BorderLayout.NORTH);

        JPanel productsPanel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(productsPanel, BoxLayout.Y_AXIS); //Variable number of products from product list
        productsPanel.setLayout(boxlayout);
        ArrayList<JPanel> panels = new ArrayList<JPanel>();
        
        try {
            pw.println("GETSUPERSTORES");
            pw.flush();  
            superStores = (ArrayList<String>) ois.readObject();  //SERVERREQUEST - GETSUPERSTORES
        } catch (Exception e) {
            e.printStackTrace();
        }
        

        
        for (String product : products) {
            JPanel product1 = new JPanel(new FlowLayout());
            JLabel product1Text = new JLabel(product);
            product1.add(product1Text);
            JButton product1Add = new JButton("Add To Cart");
            product1Add.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) { // ACTION LISTENER - Add to cart Ask the user how many they
                                                             // want to buy
                    String s = JOptionPane.showInputDialog("How many items do you want to buy?");
                    if (s == null) {
                        buyerViewStore.dispose();
                        buyerViewStore(storeName, products);
                    } else {
                        try {
                            pw.println("ADDTOCART");
                            pw.println(product);
                            pw.println(storeName);
                            pw.println(s);
                            pw.println(username);
                            pw.flush();
                            String flag = in.nextLine(); // SERVERREQUEST - ADDTOCART
                            if (flag.equals("ERROR")) {
                                JOptionPane.showMessageDialog(null,
                                        "This item is out of stock for your purchase amount",
                                        "ERROR", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }

                }
            });
            product1.add(product1Add);
            JButton product1Info = new JButton("Info");
            product1Info.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    displayProductInfo(product, storeName); // ACTION LISTENER - Sends the user to the Product info page
                                                            // for the specific product
                    buyerViewStore.dispose();

                }
            });
            product1.add(product1Info);
            panels.add(product1);
        }
        

        for (JPanel p : panels) {  //Displays the panels
            productsPanel.add(p);
        }

        JPanel buyerViewStoreSouth = new JPanel(new FlowLayout());
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //Send the user back to the buyer home
                try {
                    pw.println("GETSUPERSTORES");
                    pw.flush();
                    superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                    buyerMain(superStores);
                    buyerViewStore.dispose();    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        buyerViewStoreSouth.add(back);
        buyerViewStore.add(buyerViewStoreSouth, BorderLayout.SOUTH);

        //Do something to reorganize panels

        JScrollPane scrollPane = new JScrollPane(productsPanel); 
        buyerViewStoreCentral.add(scrollPane , BorderLayout.CENTER);
        buyerViewStore.add(buyerViewStoreCentral, BorderLayout.CENTER);
            
            

        buyerViewStore.pack();
        buyerViewStore.setSize(800, 600);
        buyerViewStore.setLocationRelativeTo(null);
        buyerViewStore.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        buyerViewStore.setVisible(true);

    }




    /*
    * Creates the info page for unique products
    */
    public void displayProductInfo(String product , String store) {
        
        JFrame productInfo = new JFrame(store);
        productInfo.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(productInfo, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    productInfo.dispose();
                }
            }
        });
        Container productInfoPanel = productInfo.getContentPane();
        productInfoPanel.setLayout(new BorderLayout());

        JPanel productInfoNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>PRODUCT INFO</h1></html>");
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Refreshes the Home Page
                productInfo.dispose();
                displayProductInfo(product , store);
                
            }
        
        }); 
        productInfoNorth.add(title);
        productInfo.add(productInfoNorth, BorderLayout.NORTH);

        JPanel productInfoCentral = new JPanel();
        BoxLayout boxlayout = new BoxLayout(productInfoCentral, BoxLayout.Y_AXIS); //Add Product info
        productInfoCentral.setLayout(boxlayout);
        ArrayList<String> info = new ArrayList<String>();
        try {
            pw.println("PRODUCTINFO");
            pw.println(product);
            pw.println(store);
            pw.flush();
            info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST PRODUCTINFO      
        } catch (Exception e) {
            e.printStackTrace();
        }
        JLabel productNameLabel = new JLabel(String.format("Product Name: %s" , product));
        JLabel storeNameLabel = new JLabel(String.format("Store Name: %s" , info.get(0)));
        JLabel descriptionLabel = new JLabel(String.format("Description: %s" , info.get(1)));
        JLabel quantityLabel = new JLabel(String.format("Quantity: %s" , info.get(2)));
        JLabel priceLabel = new JLabel(String.format("Price: %s" , info.get(3)));
        productInfoCentral.add(productNameLabel);
        productInfoCentral.add(storeNameLabel);
        productInfoCentral.add(descriptionLabel);
        productInfoCentral.add(quantityLabel);
        productInfoCentral.add(priceLabel);
        productInfo.add(productInfoCentral , BorderLayout.CENTER);

        JPanel productInfoSouth = new JPanel(new FlowLayout());
        JButton addToCart = new JButton("Add to Cart");
        addToCart.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Link to add a product to the cart
                String s = JOptionPane.showInputDialog("How many items do you want to buy?");
                try {
                    pw.println("ADDTOCART");
                    pw.println(product);
                    pw.println(store);
                    pw.println(s);
                    pw.println(username);
                    pw.flush();   
                    String flag = in.nextLine(); //SERVERREQUEST - ADDTOCART
                    if (flag.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "This item is out of stock for your purchase amount", 
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                    } 
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        JButton contactSeller = new JButton("Contact Seller"); //WHAT IS THIS FUNCTION SUPPOSED TO DO?
        contactSeller.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //Send the user back to the buyer home
                String output = "";
                ArrayList<String> info = new ArrayList<String>();
                try {
                    pw.println("PRODUCTINFO");
                    pw.println(product);
                    pw.println(store);
                    pw.flush();
                    info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST PRODUCTINFO      
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                try {
                    pw.println("CONTACTSELLER");
                    pw.println(info.get(0)); //gets store title
                    pw.flush();
                    var returned = (ArrayList<String>) ois.readObject(); //SERVERREQUEST CONTACTSELLER   
                    for (String name : returned) {
                        output += name + "\n";
                    }      
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                productInfo.dispose();
                displayProductInfo(product , store);
                JOptionPane.showMessageDialog(null, output);
            }
        
        });
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //Send the user back to the buyer home
                try {
                    pw.println("GETPRODUCTSINSTORE");
                    pw.println(store);
                    pw.flush();
                    var productNamesProxy = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETPRODCUTSINSTORE
                    buyerViewStore(store , productNamesProxy);
                    productInfo.dispose();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        productInfoSouth.add(addToCart);
        productInfoSouth.add(contactSeller);
        productInfoSouth.add(back);
        productInfo.add(productInfoSouth, BorderLayout.SOUTH);

        productInfo.setSize(800, 600);
        productInfo.setLocationRelativeTo(null);
        productInfo.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        productInfo.setVisible(true);
        productInfo.requestFocus();  //Requests focus for the frame
    }

    /*
     * Creates the page for user to see their purchase history
     */
    public void displayPurchaseHistory(ArrayList<String> history) {

        JFrame purchaseHistory = new JFrame("THE MARKETPLACE");
        purchaseHistory.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(purchaseHistory, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    purchaseHistory.dispose();
                }
            }
        });
        Container purchaseHistoryPanel = purchaseHistory.getContentPane();
        purchaseHistoryPanel.setLayout(new BorderLayout());

        JPanel purchaseHistoryNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>PURCHASE HISTORY</h1></html>");
        purchaseHistoryNorth.add(title);
        purchaseHistory.add(purchaseHistoryNorth, BorderLayout.NORTH);

        JPanel purchaseHistoryCentral = new JPanel();
        BoxLayout boxlayout = new BoxLayout(purchaseHistoryCentral, BoxLayout.Y_AXIS); //Add Product info
        purchaseHistoryCentral.setLayout(boxlayout);
        for (String line : history) {
            purchaseHistoryCentral.add(new JLabel(line));  //Gets the user purchase history and displays it : how does marketplace do this?
        }
        JScrollPane scrollPane = new JScrollPane(purchaseHistoryCentral); 
        purchaseHistory.add(scrollPane , BorderLayout.CENTER);

        JPanel purchaseHistorySouth = new JPanel(new FlowLayout());
        JTextField exportText = new JTextField("purchaseHistory.txt");  //FilePath
        JButton exportProduct = new JButton("Export to File"); //Adds bottom buttons
        exportProduct.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //Exports the purchase history to a file
                exportHistory(exportText.getText() , history);
            }
        });
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //Send the user back to the buyer home
                try {
                    pw.println("GETSUPERSTORES");
                    pw.flush();
                    superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                    buyerMain(superStores);
                    purchaseHistory.dispose();    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        purchaseHistorySouth.add(exportText);
        purchaseHistorySouth.add(exportProduct);
        purchaseHistorySouth.add(back);
        purchaseHistory.add(purchaseHistorySouth, BorderLayout.SOUTH);

        purchaseHistory.setSize(800, 600);
        purchaseHistory.setLocationRelativeTo(null);
        purchaseHistory.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        purchaseHistory.setVisible(true);
        purchaseHistory.requestFocus();
    }

    /*
     * Allows the user to see and edit their cart or checkout.
     */
    public void displayCart(ArrayList<String> viewCart) {

        JFrame cart = new JFrame("THE MARKETPLACE");
        cart.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(cart, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    cart.dispose();
                }
            }
        });
        Container cartPanel = cart.getContentPane();
        cartPanel.setLayout(new BorderLayout());

        JPanel cartNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>CART</h1></html>");     //Createds title
        cartNorth.add(title);
        cart.add(cartNorth, BorderLayout.NORTH);

        JPanel cartSouth = new JPanel(new FlowLayout());
        JButton continueShopping = new JButton("Continue Shopping");
        continueShopping.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {   
   //ACTION LISTENER - Sends the user back to the main page
                try {
                    pw.println("GETSUPERSTORES");
                    pw.flush();
                    superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                    buyerMain(superStores);
                    cart.dispose();    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        JButton checkout = new JButton("Checkout");                 
        checkout.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {   
                int choice = JOptionPane.showConfirmDialog(null, "Are you sure?");      //ACTION LISTENER - Sends the user back to the main page
                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        pw.println("PURCHASE");  //SERVERREQUEST PURCHASE
                        pw.println(username);
                        pw.println("GETSUPERSTORES");
                        pw.flush();
                        superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                        buyerMain(superStores);
                        JOptionPane.showMessageDialog(null, "Purchase Confirmed", "THE MARKETPLACE", JOptionPane.PLAIN_MESSAGE);   
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    cart.dispose();
                }
            }
        
        });
        cartSouth.add(continueShopping);
        cartSouth.add(checkout);
        cart.add(cartSouth, BorderLayout.SOUTH);

        JPanel cartCentral = new JPanel(new BorderLayout());
        JPanel productsPanel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(productsPanel, BoxLayout.Y_AXIS); //Variable number of products from product list
        productsPanel.setLayout(boxlayout);
        ArrayList<JPanel> panels = new ArrayList<JPanel>();
        for (String line : viewCart) {    
            JPanel product1 = new JPanel(new FlowLayout());
            JLabel product1Text = new JLabel(line); //Populates shopping cart on screen
            product1.add(product1Text);
            JButton delete = new JButton("Delete");
            delete.addActionListener(new ActionListener() {      
                public void actionPerformed(ActionEvent e) {   //ACTION LISTENER - removes an item from the cart and updates the page
                    try {
                        String[] split = line.split(";");
                        pw.println("DELETEPRODUCTCART");
                        pw.println(split[0]); //product name       FIXXX THIS
                        pw.println(split[1]); //store name
                        pw.println(username); 
                        pw.flush(); 
                        cart.dispose();
                        pw.println("VIEWCART");
                        pw.flush();
                        ArrayList<String> view = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWCART
                        cart.dispose();
                        displayCart(view);
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    
                }
            
            });
            product1.add(delete);
            panels.add(product1);
        }
        for (JPanel p : panels) {  //Displays the panels
            productsPanel.add(p);
        }
        JScrollPane scrollPane = new JScrollPane(productsPanel); 
        cartCentral.add(scrollPane , BorderLayout.CENTER);
        cart.add(cartCentral, BorderLayout.CENTER);
        

        cart.pack();
        cart.setSize(800, 600);
        cart.setLocationRelativeTo(null);
        cart.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        cart.setVisible(true);
        cart.requestFocus();
    }

    /*
     * Opens a page for a user to edit their account
     */
    public void editAccount(ArrayList<String> edit) {

        JFrame account = new JFrame("THE MARKETPLACE");
        account.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(account, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    account.dispose();
                }
            }
        });
        Container accountPanel = account.getContentPane();
        accountPanel.setLayout(new BorderLayout());

        JPanel accountNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>EDIT ACCOUNT</h1></html>");     //Createds title
        accountNorth.add(title);
        account.add(accountNorth, BorderLayout.NORTH);
        JPanel accountCentral = new JPanel(new BorderLayout());
        JPanel accountFields = new JPanel();
        BoxLayout boxlayout = new BoxLayout(accountFields, BoxLayout.Y_AXIS);
        accountFields.setLayout(boxlayout);
        
        JPanel password = new JPanel(new FlowLayout());
        JLabel passwordText = new JLabel("Password: ");
        JTextField passwordField = new JTextField(edit.get(1)); //prepopulated with password
        password.add(passwordText);
        password.add(passwordField);
        JPanel email = new JPanel(new FlowLayout());
        JLabel emailText = new JLabel("Email: ");
        JTextField emailField = new JTextField(edit.get(0)); //prepopulated with email
        email.add(emailText);
        email.add(emailField);

        JPanel accountSouth = new JPanel(new FlowLayout());
        JButton update = new JButton("Update");
        update.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {   
                     //ACTION LISTENER - Sends the user back to the main page
                ArrayList<String> out = new ArrayList<String>();
                String p = passwordField.getText();
                String em = emailField.getText();
                boolean invalidInput = false;
                if (p.contains(";") || em.contains(";")) {
                    JOptionPane.showMessageDialog(null, "Email or Password cannot contain semicolons", "Error", JOptionPane.ERROR_MESSAGE);
                    invalidInput = true;
                }
                if (!em.contains("@")) {
                    JOptionPane.showMessageDialog(null, "Email must contain @", "Error", JOptionPane.ERROR_MESSAGE);
                    invalidInput = true;
                }
                if (!em.contains(".")) {
                    JOptionPane.showMessageDialog(null, "Email must contain .", "Error", JOptionPane.ERROR_MESSAGE);
                    invalidInput = true;
                }
                out.add(em);
                out.add(p);
                if (!invalidInput) {
                    try {
                        pw.println("UPDATEACCOUNTINFO");
                        pw.println(username);
                        pw.flush();
                        oos.writeObject(out);
                        oos.flush();   
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }    
                    account.dispose();
                }
                if (loggedInAsBuyer) {
                    try {
                        pw.println("GETSUPERSTORES");
                        pw.flush();
                        superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    buyerMain(superStores);
                } else if (loggedInAsSeller) {
                    try {
                        pw.println("VIEWSTORES");
                        pw.println(username);
                        pw.flush();
                        myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                        account.dispose(); 
                        sellerMain(myStoreNames); 
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                
            }
        
        });
        
        JButton back = new JButton("Back");                 //Displays Bottom Buttons
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {   
                account.dispose();      //ACTION LISTENER - Sends the user back to the main page
                try {
                    if (loggedInAsBuyer) {
                        try {
                            pw.println("GETSUPERSTORES");
                            pw.flush();
                            superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                            buyerMain(superStores);
                            account.dispose();    
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    } else if (loggedInAsSeller) {
                        pw.println("VIEWSTORES");
                        pw.println(username);
                        pw.flush();
                        myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                        sellerMain(myStoreNames);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
            }
        
        });
        accountSouth.add(update);
        accountSouth.add(back);
        account.add(accountSouth, BorderLayout.SOUTH);

        accountFields.add(password);
        accountFields.add(email);
        JScrollPane scrollPane = new JScrollPane(accountFields); 
        accountCentral.add(scrollPane , BorderLayout.CENTER);
        account.add(accountCentral, BorderLayout.CENTER);
        account.pack();
        account.setSize(800, 600);
        account.setLocationRelativeTo(null);
        account.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        account.setVisible(true);
        account.requestFocus();
    }

    /*
     * Opens the home page for a seller
     */
    public void sellerMain(ArrayList<String> storeNames) {

        JFrame sellerMain = new JFrame("THE MARKETPLACE");
        sellerMain.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(sellerMain, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    sellerMain.dispose();
                }
            }
        });
        Container sellerMainPanel = sellerMain.getContentPane();
        sellerMainPanel.setLayout(new BorderLayout());

        JPanel sellerMainNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>THE MARKETPLACE</h1></html>");     //Creates title
        sellerMainNorth.add(title);
        JButton addStore = new JButton("Add Store");
        addStore.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {   
                try {
                    String s = "";
                    boolean invalidInput = true;
                    while (invalidInput) {
                        s = JOptionPane.showInputDialog(null, "Enter Store Name:");     //ACTION LISTENER - adds a store
                        if (s.contains(";")) {
                            JOptionPane.showMessageDialog(null, "Store Name cannot contain a semicolon", "ERROR", JOptionPane.ERROR_MESSAGE);
                        } else {
                            invalidInput = false;
                        }
                    }
                    

                    if (s != null) {
                        pw.println("ADDSTORE"); //SERVERREQUEST - ADDSTORE
                        pw.println(s);
                        pw.println(username);
                        pw.flush();
                        String confirmation = in.nextLine();
                        System.out.println(confirmation);
                        if (confirmation.equals("ERROR")) {
                            JOptionPane.showMessageDialog(null, "There is already a store with that name", "ERROR", JOptionPane.ERROR_MESSAGE);
                        } else {
                            String returned = in.nextLine();
                            pw.println("VIEWSTORES");
                            pw.println(username);
                            pw.flush();
                            myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES
                        }
                    }
                    
                       
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                sellerMain.dispose();
                sellerMain(myStoreNames); 
                
            }
        
        });
        sellerMainNorth.add(addStore);
        sellerMain.add(sellerMainNorth, BorderLayout.NORTH);

        JPanel sellerMainCentral = new JPanel(new BorderLayout());
        JLabel yourStores = new JLabel("<html><h2>Your Stores:</h2></html>"); 
        sellerMainCentral.add(yourStores, BorderLayout.NORTH);

        JPanel storesPanel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(storesPanel, BoxLayout.Y_AXIS); //Variable number of stores from store list
        storesPanel.setLayout(boxlayout);
        ArrayList<JPanel> panels = new ArrayList<JPanel>();
        for (String line : storeNames) {
            JPanel store1 = new JPanel(new FlowLayout());
            JLabel store1Text = new JLabel(line);
            store1.add(store1Text);
            JButton store1Edit = new JButton("Edit");  
            store1Edit.addActionListener(new ActionListener() {      
                public void actionPerformed(ActionEvent e) {           //ACTION LISTENER links to edit page
                    displayEditStore(line);
                    sellerMain.dispose();
                }
            
            });
            store1.add(store1Edit);
            JButton store1Delete = new JButton("Delete");
            store1Delete.addActionListener(new ActionListener() {      
                public void actionPerformed(ActionEvent e) {  
                    ArrayList<String> returned = new ArrayList<String>();
                    try {
                        pw.println("DELETESTORE");
                        pw.println(line);
                        pw.flush();
                        returned = (ArrayList<String>) ois.readObject(); //SERVERREQUEST - DELETESTORE
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    } 
                    sellerMain.dispose();                          //ACTION LISTENER Deletes store
                    sellerMain(returned);
                    
                }
            
            });
            store1.add(store1Delete);
            panels.add(store1);
        }
        for (JPanel p : panels) {  //Displays the panels
            storesPanel.add(p);
        }
        JScrollPane scrollPane = new JScrollPane(storesPanel); 
        sellerMainCentral.add(scrollPane , BorderLayout.CENTER);
        sellerMain.add(sellerMainCentral, BorderLayout.CENTER);

        JButton dash = new JButton("View Dashboard");
        dash.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                sellerDashboard(true);
                sellerMain.dispose();
            }
        
        });
        
        JPanel sellerMainSouth = new JPanel(new FlowLayout());
        JButton viewInCart = new JButton("Products in Customer's carts");
        viewInCart.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Link to the page to edit a buyer account
                var info = new ArrayList<String>(); 
                try {
                    pw.println("NUMINCART");   //ROHANFIX Only works if the user is a buyer, not a seller
                    pw.println(username);
                    pw.flush();
                    info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST NUMINCART
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                viewProductsInCarts(info);
                sellerMain.dispose();
            }
        
        });
        
        JButton editAccount = new JButton("Edit Account");
        editAccount.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) { 
                var info = new ArrayList<String>(); 
                try {
                   pw.println("GETACCOUNTINFO"); 
                   pw.println(username);
                   pw.flush();
                   info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETACCOUNTINFO
                } catch (Exception e1) {
                    e1.printStackTrace();
                } 
                editAccount(info);
                sellerMain.dispose();
            }
        
        });
        JButton logout = new JButton("Logout");
        logout.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                goodbye();          //Links to goodbye page
                sellerMain.dispose();
            }
        
        });
        sellerMainSouth.add(dash); 
        sellerMainSouth.add(viewInCart);
        sellerMainSouth.add(editAccount);
        sellerMainSouth.add(logout);
        sellerMain.add(sellerMainSouth , BorderLayout.SOUTH);

        sellerMain.pack();
        sellerMain.setSize(800, 600);
        sellerMain.setLocationRelativeTo(null);
        sellerMain.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        sellerMain.setVisible(true);
        sellerMain.requestFocus();

    }

    /*
     * Opens a menu to edit a chosen store
     */
    public void displayEditStore(String line) {

        JFrame editStore = new JFrame("THE MARKETPLACE");
        editStore.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(editStore, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    editStore.dispose();
                }
            }
        });
        Container editStorePanel = editStore.getContentPane();
        editStorePanel.setLayout(new BorderLayout());

        JPanel editStoreNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>" + line + "</h1></html>");     //Creates title
        editStoreNorth.add(title);
        JButton addProduct = new JButton("Add Product");
        addProduct.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                displayAddProduct(line);
                editStore.dispose();
            }
        
        });
        editStoreNorth.add(addProduct);
        editStore.add(editStoreNorth, BorderLayout.NORTH);

        JPanel editStoreCentral = new JPanel(new BorderLayout());
        JPanel yourStoresWindow = new JPanel(new FlowLayout());
        JLabel yourStores = new JLabel("<html>Products in:</html>"); 
        JLabel storeTitle = new JLabel(line);
        yourStoresWindow.add(yourStores);
        yourStoresWindow.add(storeTitle);

        editStoreCentral.add(yourStoresWindow, BorderLayout.NORTH);

        JPanel productsPanel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(productsPanel, BoxLayout.Y_AXIS); //Variable number of products from product list
        productsPanel.setLayout(boxlayout);
        ArrayList<JPanel> panels = new ArrayList<JPanel>();
        ArrayList<String> products = new ArrayList<String>();
        try {
            pw.println("VIEWPRODUCTS");
            pw.println(line);
            pw.flush();
            products = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWPRODCUTS

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String p : products) {
            JPanel product1 = new JPanel(new FlowLayout());
            JLabel product1Text = new JLabel(p);
            product1.add(product1Text);
            JButton product1Edit = new JButton("Edit");  
            product1Edit.addActionListener(new ActionListener() {      
                public void actionPerformed(ActionEvent e) {  
                    editStore.dispose();          //Links to stats page
                    displayEditProduct(p , line);
                    
                }
            
            });
            product1.add(product1Edit);
            JButton product1Delete = new JButton("Delete");
            product1Delete.addActionListener(new ActionListener() {      
                public void actionPerformed(ActionEvent e) {  
                    try {
                        pw.println("DELETEPRODUCT");
                        pw.println(p); //SERVERREQUEST - DELETEPRODCUT
                        pw.println(line);
                        pw.flush();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    editStore.dispose();
                    displayEditStore(line);
                }
            
            });
            product1.add(product1Delete);
            panels.add(product1);
        }
        for (JPanel p : panels) {  //Displays the panels
            productsPanel.add(p);
        }
        JScrollPane scrollPane = new JScrollPane(productsPanel); 
        editStoreCentral.add(scrollPane , BorderLayout.CENTER);
        editStore.add(editStoreCentral, BorderLayout.CENTER);

        JPanel editStoreSouth = new JPanel(new FlowLayout());
        JTextField importText = new JTextField(6);  //FilePath
        JButton importProduct = new JButton("Import");
        importProduct.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  //ACTIONLISTENER Import PRODUCATS
                ArrayList<String> errors = new ArrayList<String>();
                ArrayList<String> imported = importProducts(importText.getText());
                boolean invalidInput = false;
                if (imported == null) {
                    JOptionPane.showMessageDialog(null, "No File Found" , "Error" , JOptionPane.ERROR_MESSAGE);
                    invalidInput = true;
                    editStore.dispose();
                    displayEditStore(line);
                }
                if (!invalidInput) {
                    for (String i : imported) {
                        try {
                            pw.println("ADDPRODUCT");     //SERVERREQUEST ADDPRODUCT
                            pw.println(i);
                            String[] split = i.split(";");
                            pw.println(split[1]);
                            pw.flush();
                            String returned = in.nextLine();
                            if (returned.equals("ERROR")) {
                                errors.add(i);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                    editStore.dispose();
                    displayEditStore(line);
                    if (errors.size() > 0) {
                        String errorMessage = "Error adding these products: ";
                        for (String error : errors) {
                            errorMessage +=  error + ", ";
                        }
                        errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
                        JOptionPane.showMessageDialog(null, errorMessage,"Error", JOptionPane.ERROR_MESSAGE);
                    }    
                }


            }
        
        });
        JTextField exportText = new JTextField(6);  //FilePath
        JButton exportProduct = new JButton("Export"); //Adds bottom buttons
        exportProduct.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                exportProducts(exportText.getText(), line);  //ACTIONLISTENER EXPORT PRODUCATS
            }
        
        });
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                try {
                    pw.println("VIEWSTORES");
                    pw.println(username);
                    pw.flush();
                    myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWSTORES
                    sellerMain(myStoreNames);
                    editStore.dispose();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        JButton stats = new JButton("Sales Summary");
        stats.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                displaySales(line);          //Links to stats page
                editStore.dispose();
            }
        
        });
        editStoreSouth.add(stats);
        editStoreSouth.add(importText);
        editStoreSouth.add(importProduct);
        editStoreSouth.add(exportText);
        editStoreSouth.add(exportProduct);
        editStoreSouth.add(back);
        editStore.add(editStoreSouth, BorderLayout.SOUTH);

        editStore.pack();
        editStore.setSize(800, 600);
        editStore.setLocationRelativeTo(null);
        editStore.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        editStore.setVisible(true);
        editStore.requestFocus();
    }
    /*
     * Opens a page for a seller to edit
     */
    public void displayEditProduct(String product , String storeString) {
        
        ArrayList<String> info = new ArrayList<>();
        try {
            pw.println("PRODUCTINFO");
            pw.println(product);
            pw.println(storeString);
            pw.flush();
            info = (ArrayList<String>) ois.readObject();   //SERVERREQUEST - PRODUCTINFO
        } catch (Exception e) {
            e.printStackTrace();
        } 
        JFrame editProduct = new JFrame(storeString);
        editProduct.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(editProduct, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    editProduct.dispose();
                }
            }
        });
        Container editProductPanel = editProduct.getContentPane();
        editProductPanel.setLayout(new BorderLayout());

        JPanel editProductNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>EDIT PRODUCT</h1></html>");     //Createds title
        editProductNorth.add(title);
        editProduct.add(editProductNorth, BorderLayout.NORTH);


        JPanel editProductCentral = new JPanel(new BorderLayout());
        JPanel editProductFields = new JPanel();
        BoxLayout boxlayout = new BoxLayout(editProductFields, BoxLayout.Y_AXIS); //Variable number of products from product list
        editProductFields.setLayout(boxlayout);
        
        JPanel productName = new JPanel(new FlowLayout());
        JLabel productNameText = new JLabel("Product Name: ");
        JTextField productNameField = new JTextField(product); //prepopulated with product name
        productName.add(productNameText);
        productName.add(productNameField);

        JPanel description = new JPanel(new FlowLayout());
        JLabel descriptionText = new JLabel("Description: ");
        JTextField descriptionField = new JTextField(info.get(1)); //prepopulated with descirption
        description.add(descriptionText);
        description.add(descriptionField);

        JPanel quantity = new JPanel(new FlowLayout());
        JLabel quantityText = new JLabel("Quantity: ");
        JTextField quantityField = new JTextField(info.get(2)); //prepopulated with descirption
        quantity.add(quantityText);
        quantity.add(quantityField);

        JPanel price = new JPanel(new FlowLayout());
        JLabel priceText = new JLabel("Price: ");
        JTextField priceField = new JTextField(info.get(3)); //prepopulated with descirption
        price.add(priceText);
        price.add(priceField);

        editProductFields.add(productName);
        editProductFields.add(description);
        editProductFields.add(quantity);
        editProductFields.add(price);


        JPanel editProductSouth = new JPanel(new FlowLayout());
        JButton update = new JButton("Update");
        update.addActionListener(new ActionListener() {   
            public void actionPerformed(ActionEvent e) {  
                boolean invalidInput = false;
                if (productNameField.getText().contains(";")) {
                    JOptionPane.showMessageDialog(null, "Product Name cannot contain a semicolon",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     editProduct.dispose();
                     displayEditProduct(product , storeString);
                     invalidInput = true;
                }   
                if (descriptionField.getText().contains(";")) {
                    JOptionPane.showMessageDialog(null, "Description cannot contain a semicolon",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     editProduct.dispose();
                     displayEditProduct(product , storeString);
                     invalidInput  = true;
                }    
                try {
                    int test = Integer.parseInt(quantityField.getText());
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, "Quantity must be a valid Integer",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     editProduct.dispose();
                     displayEditProduct(product , storeString);
                     invalidInput = true;
                }
                try {
                    double test1 = Double.parseDouble(priceField.getText());
                } catch (Exception e3) {
                    JOptionPane.showMessageDialog(null, "Price must be a valid double",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     editProduct.dispose();
                     displayEditProduct(product , storeString);
                     invalidInput = true;
                }  
                if (!invalidInput) {
                    String pInfo = "";
                    pInfo += productNameField.getText() + ";";
                    pInfo += storeString + ";";
                    pInfo += descriptionField.getText() + ";";
                    pInfo += quantityField.getText() + ";";
                    pInfo += priceField.getText();
                    String confirmed = ""; 
                    try {
                        pw.println("EDITPRODUCT");
                        pw.println(pInfo);
                        pw.flush();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    
                    if (confirmed.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "There is already a product with that name in your store",
                         "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
    
                    displayEditStore(storeString);
                    editProduct.dispose();
                }
            
            }
        });

        JButton back = new JButton("Back");                 //Displays Bottom Buttons
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                displayEditStore(storeString);
                editProduct.dispose();
            }
        
        });
        editProductSouth.add(update);
        editProductSouth.add(back);
        editProduct.add(editProductSouth, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(editProductFields); 
        editProductCentral.add(scrollPane , BorderLayout.CENTER);
        editProduct.add(editProductCentral, BorderLayout.CENTER);
        editProduct.pack();
        editProduct.setSize(800, 600);
        editProduct.setLocationRelativeTo(null);
        editProduct.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        editProduct.setVisible(true);
        editProduct.requestFocus();
    }

    /*
     * Opens a page for a seller to add a product
     */
    public void displayAddProduct(String storeName) {                               //UPDATE TO ADD EVERYTHING

        JFrame addProduct = new JFrame(storeName);
        addProduct.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(addProduct, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    addProduct.dispose();
                }
            }
        });
        Container addProductPanel = addProduct.getContentPane();
        addProductPanel.setLayout(new BorderLayout());

        JPanel addProductNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>ADD PRODUCT</h1></html>");     //Createds title
        addProductNorth.add(title);
        addProduct.add(addProductNorth, BorderLayout.NORTH);

        

        JPanel addProductCentral = new JPanel(new BorderLayout());
        JPanel addProductFields = new JPanel();
        BoxLayout boxlayout = new BoxLayout(addProductFields, BoxLayout.Y_AXIS); //Variable number of products from product list
        addProductFields.setLayout(boxlayout);
        
        JPanel productName = new JPanel(new FlowLayout());
        JLabel productNameText = new JLabel("Product Name: ");
        JTextField productNameField = new JTextField(10); //prepopulated with product name
        productName.add(productNameText);
        productName.add(productNameField);

        JPanel description = new JPanel(new FlowLayout());
        JLabel descriptionText = new JLabel("Description: ");
        JTextField descriptionField = new JTextField(10); //prepopulated with descirption
        description.add(descriptionText);
        description.add(descriptionField);

        JPanel quantity = new JPanel(new FlowLayout());
        JLabel quantityText = new JLabel("Quantity: ");
        JTextField quantityField = new JTextField(10); //prepopulated with descirption
        quantity.add(quantityText);
        quantity.add(quantityField);

        JPanel price = new JPanel(new FlowLayout());
        JLabel priceText = new JLabel("Price: ");
        JTextField priceField = new JTextField(10); //prepopulated with descirption
        price.add(priceText);
        price.add(priceField);

        JPanel addProductSouth = new JPanel(new FlowLayout());
        JButton create = new JButton("Create");
        create.addActionListener(new ActionListener() {   
            public void actionPerformed(ActionEvent e) {
                boolean validInput = true;
                if (productNameField.getText().contains(";")) {
                    JOptionPane.showMessageDialog(null, "Product Name cannot contain a semicolon",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     validInput = false;
                } else if (descriptionField.getText().contains(";")) {
                    JOptionPane.showMessageDialog(null, "Description cannot contain a semicolon",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     validInput = false;
                } else if (quantityField.getText().contains(";")) {
                    JOptionPane.showMessageDialog(null, "Quantity cannot contain a semicolon",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     validInput = false;
                } else if (priceField.getText().contains(";")) {
                    JOptionPane.showMessageDialog(null, "Price cannot contain a semicolon",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                     validInput = false;
                } 
                try {
                    int q = Integer.parseInt(quantityField.getText());
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, "Quanity must be a valid Integer",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                    validInput = false;
                }

                try {
                    double d = Double.parseDouble(priceField.getText());
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(null, "Price must be a valid float",
                     "ERROR", JOptionPane.ERROR_MESSAGE);
                    validInput = false;
                }


                if (validInput) {
                    String pInfo = "";
                    pInfo += productNameField.getText() + ";";
                    pInfo += storeName + ";";
                    pInfo += descriptionField.getText() + ";";
                    pInfo += quantityField.getText() + ";";
                    pInfo += priceField.getText();
                    System.out.println(pInfo);
                    String confirmed = "";
                    try {
                        pw.println("ADDPRODUCT");
                        pw.println(pInfo);
                        pw.println(storeName);
                        pw.flush();
                        confirmed = in.nextLine();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    if (confirmed.equals("ERROR")) {
                        JOptionPane.showMessageDialog(null, "There is already a product with that name in your store",
                         "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                    addProduct.dispose();
                    displayEditStore(storeName);    
                } else {
                    addProduct.dispose();
                    displayAddProduct(storeName);
                }
                
            }
        
        });
        JButton back = new JButton("Back");                 //Displays Bottom Buttons
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                displayEditStore(storeName);
                addProduct.dispose();
            }
        
        });
        addProductSouth.add(create);
        addProductSouth.add(back);
        addProduct.add(addProductSouth, BorderLayout.SOUTH);

        addProductFields.add(productName);
        addProductFields.add(description);
        addProductFields.add(quantity);
        addProductFields.add(price);

        JScrollPane scrollPane = new JScrollPane(addProductFields); 
        addProductCentral.add(scrollPane , BorderLayout.CENTER);
        addProduct.add(addProductCentral, BorderLayout.CENTER);
        addProduct.pack();
        addProduct.setSize(800, 600);
        addProduct.setLocationRelativeTo(null);
        addProduct.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addProduct.setVisible(true);
        addProduct.requestFocus();
    }

    public void displaySales(String storeName) {

        JFrame stats = new JFrame("THE MARKETPLACE");
        stats.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(stats, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    stats.dispose();
                }
            }
        });
        Container statsPanel = stats.getContentPane();
        statsPanel.setLayout(new BorderLayout());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Refreshes the Home Page
                stats.dispose();
                var info = new ArrayList<String>(); 
                try {
                    pw.println("NUMINCART");   //ROHANFIX Only works if the user is a buyer, not a seller
                    pw.println(username);
                    pw.flush();
                    info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST NUMINCART
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }    
                displaySales(storeName);
                
            }
        
        }); 
        JPanel statsNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>SALES SUMMARY</h1></html>");     //Createds title
        statsNorth.add(title);
        statsNorth.add(refresh);
        stats.add(statsNorth, BorderLayout.NORTH);
        
        JPanel statsCentral = new JPanel(new BorderLayout());
        JPanel sort = new JPanel(new FlowLayout());       
        statsCentral.add(sort , BorderLayout.NORTH);
        JPanel statsItems = new JPanel();
        BoxLayout boxlayout = new BoxLayout(statsItems, BoxLayout.Y_AXIS); //Add Product info
        statsItems.setLayout(boxlayout);
        ArrayList<String> returned = new ArrayList<String>(); 
        try {
            pw.println("SALESBYSTORE");
            pw.println(storeName);
            pw.flush();
            returned = (ArrayList<String>) ois.readObject();//SERVERREQUEST - SALESSBYSTORE
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        for (String line : returned) {
            statsItems.add(new JLabel(line));
        }
        
        JScrollPane scrollPane = new JScrollPane(statsItems); 
        statsCentral.add(scrollPane , BorderLayout.CENTER);
        stats.add(statsCentral,  BorderLayout.CENTER);

        JPanel statsSouth = new JPanel(new FlowLayout());
        JTextField exportText = new JTextField("sales.txt");  //FilePath
        JButton exportProduct = new JButton("Export to File"); //Adds bottom buttons
        exportProduct.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //Exports the purchase history to a file
                ArrayList<String> returned = new ArrayList<String>(); 
                try {
                    pw.println("SALESBYSTORE");
                    pw.println(storeName);
                    pw.flush();
                    returned = (ArrayList<String>) ois.readObject();//SERVERREQUEST - SALESSBYSTORE

                } catch (Exception e1) {
                    e1.printStackTrace();
                }    
                exportSales(exportText.getText() , returned);
            }
        });
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                displayEditStore(storeName);
                stats.dispose();
            }
        
        });

        statsSouth.add(exportText);
        statsSouth.add(exportProduct);
        statsSouth.add(back);
        stats.add(statsSouth, BorderLayout.SOUTH);

        stats.pack();
        stats.setSize(800, 600);
        stats.setLocationRelativeTo(null);
        stats.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        stats.setVisible(true);
        stats.requestFocus();
    }

    public void sellerDashboard(Boolean sorted) { //FIX NEEDS TO BE ABLE TO SORT DASHBOARD

        try {
            pw.println("VIEWSTORES");
            pw.println(username);
            pw.flush();
            myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWSTORES

        } catch (Exception e) {
            e.printStackTrace();
        }

        ArrayList<String> customers = new ArrayList<>(); 
        ArrayList<String> products = new ArrayList<>();

        JFrame sellerDash = new JFrame("THE MARKETPLACE");
        sellerDash.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(sellerDash, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    sellerDash.dispose();
                }
            }
        });
        Container sellerDashPanel = sellerDash.getContentPane();
        sellerDashPanel.setLayout(new BorderLayout());

        JPanel sellerDashNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>DASHBOARD</h1></html>");
        sellerDashNorth.add(title);
        sellerDash.add(sellerDashNorth, BorderLayout.NORTH);

        JPanel sort = new JPanel(new FlowLayout());
        String[] s = {"Sort the menu","Best Sellers","Worst Sellers"};
        JComboBox sortBox = new JComboBox(s);
        sort.add(sortBox);
        sortBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                String choice;
                JComboBox getSelection = (JComboBox) event.getSource();
                choice = (String) getSelection.getSelectedItem();
                if (choice == s[1]) { 
                    sellerDash.dispose();
                    sellerDashboard(true);
                } else if (choice == s[2]) {
                    sellerDash.dispose();
                    sellerDashboard(false);
                }
            }
        }
        
        });
       
        sellerDashNorth.add(sort);

        JPanel sellerDashCentral = new JPanel();
        BoxLayout boxlayout = new BoxLayout(sellerDashCentral, BoxLayout.Y_AXIS); //Add Product info
        sellerDashCentral.setLayout(boxlayout);
        
        for (String store : myStoreNames) {
            customers.clear();
            products.clear();
            try {
                pw.println("CUSTOMERLIST");
                pw.println(store);
                pw.flush();
                customers = (ArrayList<String>) ois.readObject(); //SERVERREQUEST CUSTOMERLIST
 
            } catch (Exception e) {
                e.printStackTrace();
            }
            sellerDashCentral.add(new JLabel(store));
            sellerDashCentral.add(new JLabel("  "));
            sellerDashCentral.add(new JLabel("CUSTOMERS:"));
            customers = sortBestCustomers(customers , store , sorted);
            for (String customer : customers) {
                sellerDashCentral.add(new JLabel(customer));
            }
            try {
                pw.println("VIEWPRODUCTS");
                pw.println(store);
                pw.flush();
                products = (ArrayList<String>) ois.readObject(); //SERVERREQUEST CUSTOMERLIST

            } catch (Exception e) {
                e.printStackTrace();
            }
            products = sortBestProducts(products , store , sorted);
            sellerDashCentral.add(new JLabel("PRODUCTS:"));
            for (String product : products) {
                sellerDashCentral.add(new JLabel(product));
            }
            sellerDashCentral.add(new JLabel("---------------------------"));

        }


        JScrollPane scrollPane = new JScrollPane(sellerDashCentral); 
        sellerDash.add(scrollPane , BorderLayout.CENTER);

        JPanel sellerDashSouth = new JPanel(new FlowLayout());
        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                try {
                    pw.println("VIEWSTORES");
                    pw.println(username);
                    pw.flush();
                    myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWSTORES
                    sellerMain(myStoreNames);
                    sellerDash.dispose();

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        sellerDashSouth.add(back);
        sellerDash.add(sellerDashSouth, BorderLayout.SOUTH);

        sellerDash.setSize(800, 600);
        sellerDash.setLocationRelativeTo(null);
        sellerDash.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        sellerDash.setVisible(true);
        sellerDash.requestFocus();
    }

    public void goodbye() {

        try {
            pw.println("LOGOUT");
            pw.flush();
            LoginOrCreateAccount loginOrCreateAccount= new LoginOrCreateAccount();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame goodbye = new JFrame("THE MARKETPLACE");
        Container goodbyePanel = goodbye.getContentPane();
        goodbyePanel.setLayout(new BorderLayout());

        JPanel goodbyeNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>BYE! THANKS FOR USING THE MARKETPLACE</h1></html>");     //Createds title
        goodbyeNorth.add(title);
        goodbye.add(goodbyeNorth, BorderLayout.NORTH);

        goodbye.pack();
        goodbye.setSize(800, 600);
        goodbye.setLocationRelativeTo(null);
        goodbye.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        goodbye.setVisible(true);
        goodbye.requestFocus();


        
    }

    public boolean exportHistory(String path , ArrayList<String> purchases) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pwf = new PrintWriter(new FileWriter(f, true));           
            for (String sale : purchases) {
                pwf.println(sale); 
            }
            pwf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exportSales(String path , ArrayList<String> sales) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                f.createNewFile();
            }
            PrintWriter pwf = new PrintWriter(new FileWriter(f, true));           
            for (String sale : sales) {
                pwf.println(sale); 
            }
            pwf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Sorts an Arraylist of products based on price
     * Sorts highest price to lowest if highToLow = true    NOT SURE IF I EVEN NEED THIS
     * if it's false, sorts lowest to highest
     */
    public ArrayList<String> sortPrice(String store , boolean highToLow) {
        try {
            pw.println("GETSUPERSTORES");
            pw.flush();
            superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES   

        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> productsWithInfo = new ArrayList<String>();
        ArrayList<String> productNamesProxy = new ArrayList<String>();
        try {
            pw.println("GETPRODUCTSINSTORE");
            pw.println(store);
            pw.flush();
            productNamesProxy = (ArrayList<String>) ois.readObject(); // SERVERREQUEST GETPRODCUTSINSTORE

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String product : productNamesProxy) {
            ArrayList<String> info = new ArrayList<>();
            try {
                pw.println("PRODUCTINFO");
                pw.println(product);
                pw.println(store);
                pw.flush();
                info = (ArrayList<String>) ois.readObject(); // SERVERREQUEST PRODUCTINFO
                String out = product + ";" + info.get(0) + ";" + info.get(1) + ";" + info.get(2) + ";" + info.get(3);
                productsWithInfo.add(out);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> output = new ArrayList<String>();
        
        while (productsWithInfo.size() > 0) {
            double max = 0;
            int index = 0;
            for (int i = 0 ; i < productsWithInfo.size() ; i++) {
                String[] productSplit = productsWithInfo.get(i).split(";");
                Double price = Double.parseDouble(productSplit[4]);
                if (price >= max) {
                    max = price;
                    index = i;
                }
            }
            String outProxy = productsWithInfo.get(index);
            String[] split = outProxy.split(";");
            output.add(split[0]);
            productsWithInfo.remove(index);
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<String> reversed = new ArrayList<String>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    /*
     * Sorts an Arraylist of products based on price
     * Sorts highest price to lowest if highToLow = true    NOT SURE IF I EVEN NEED THIS
     * if it's false, sorts lowest to highest
     */
    public ArrayList<String> sortQuantity(String store , boolean highToLow) {
        try {
            pw.println("GETSUPERSTORES");
            pw.flush();
            superStores = (ArrayList<String>) ois.readObject(); //SERVERREQUEST GETSUPERSTORES   

        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<String> productsWithInfo = new ArrayList<String>();
        ArrayList<String> productNamesProxy = new ArrayList<String>();
        try {
            pw.println("GETPRODUCTSINSTORE");
            pw.println(store);
            pw.flush();
            productNamesProxy = (ArrayList<String>) ois.readObject(); // SERVERREQUEST GETPRODCUTSINSTORE

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String product : productNamesProxy) {
            ArrayList<String> info = new ArrayList<>();
            try {
                pw.println("PRODUCTINFO");
                pw.println(product);
                pw.println(store);
                pw.flush();
                info = (ArrayList<String>) ois.readObject(); // SERVERREQUEST PRODUCTINFO
                String out = product + ";" + info.get(0) + ";" + info.get(1) + ";" + info.get(2) + ";" + info.get(3);
                productsWithInfo.add(out);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> output = new ArrayList<String>();
        
        while (productsWithInfo.size() > 0) {
            int max = 0;
            int index = 0;
            for (int i = 0 ; i < productsWithInfo.size() ; i++) {
                String[] productSplit = productsWithInfo.get(i).split(";");
                int price = Integer.parseInt(productSplit[3]);
                if (price >= max) {
                    max = price;
                    index = i;
                }
            }
            String outProxy = productsWithInfo.get(index);
            String[] split = outProxy.split(";");
            output.add(split[0]);
            productsWithInfo.remove(index);
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<String> reversed = new ArrayList<String>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    /*
     * Sorts an Arraylist of stores based on number of products sold
     * Sorts highest price to lowest if highToLow = true
     * if it's false, sorts lowest to highest
     */
    public ArrayList<String> sortProductsSold(ArrayList<String> stores , boolean highToLow) {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<String> s = stores;
        while (s.size() > 0) {
            int max = 0;
            String picked = "";
            int index = 0;
            for (int i = 0 ; i < s.size() ; i++) {
                int count = 0;
                try {
                    pw.println("GETPRODUCTSINSTORE");
                    pw.println(s.get(i));
                    pw.flush();
                    ArrayList<String> products = (ArrayList<String>) ois.readObject();
                    for (String product : products) {
                        pw.println("NUMSALES");
                        pw.println(s.get(i));
                        pw.println(product);
                        pw.flush();
                        int num = Integer.parseInt(in.nextLine());
                        count += num;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (count >= max) {
                    max = count;
                    picked = s.get(i);
                    index = i;
                } 
            }   
            output.add(picked);
            s.remove(index); 
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<String> reversed = new ArrayList<String>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    public ArrayList<String> sortBestCustomers(ArrayList<String> customers , String storeName, boolean highToLow) {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<String> s = customers;
        while (s.size() > 0) {
            int max = 0;
            String picked = "";
            int index = 0;
            for (int i = 0 ; i < s.size() ; i++) {
                String[] split = s.get(i).split(";");
                int num = Integer.parseInt(split[1]);
                if (num >= max) {
                    max = num;
                    picked = s.get(i);
                    index = i;
                } 
            }   
            output.add(picked);
            s.remove(index); 
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<String> reversed = new ArrayList<String>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    public ArrayList<String> sortBestProducts(ArrayList<String> products , String storeName, boolean highToLow) {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<String> s = products;
        while (s.size() > 0) {
            int max = 0;
            String picked = "";
            int index = 0;
            for (int i = 0 ; i < s.size() ; i++) {
                int num = 0;
                try {
                    pw.println("NUMSALES");
                    pw.println(storeName);
                    pw.println(s.get(i));
                    pw.flush();
                    num = Integer.parseInt(in.nextLine());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (num >= max) {
                    max = num;
                    picked = s.get(i);
                    index = i;
                } 
            }   
            output.add(picked);
            s.remove(index); 
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<String> reversed = new ArrayList<String>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    /*
     * Sorts an Arraylist of products based on number sold
     * Sorts highest price to lowest if highToLow = true
     * if it's false, sorts lowest to highest
     */
    public ArrayList<String> sortMostShopped(ArrayList<String> stores , boolean highToLow) {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<String> s = stores;
        while (s.size() > 0) {
            int max = 0;
            String picked = "";
            int index = 0;
            for (int i = 0 ; i < s.size() ; i++) {
                int count = 0;
                try {
                    pw.println("CUSTOMERLIST");
                    pw.println(s.get(i));
                    pw.flush();
                    ArrayList<String> customers = (ArrayList<String>) ois.readObject();
                    for (String customer : customers) {
                        if (customer.contains(username)) {
                            String[] split = customer.split(";");
                            count += Integer.parseInt(split[1]);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (count >= max) {
                    max = count;
                    picked = s.get(i);
                    index = i;
                } 
            }   
            output.add(picked);
            s.remove(index); 
        }
        if (highToLow) {
            return output;
        } else {
            ArrayList<String> reversed = new ArrayList<String>();
            for (int i = output.size() - 1 ; i >= 0 ; i--) {
                reversed.add(output.get(i));
            }
            return reversed;
        }
    }

    /*
     * Gets all product in users.txt that have a name, description, or store that matches the search parameter
     */
    public ArrayList<String> searchProducts(String store , String searchParameter) {
        ArrayList<String> output = new ArrayList<String>();
        var productNamesProxy = new ArrayList<String>();
        try {
            pw.println("GETPRODUCTSINSTORE");
            pw.println(store);
            pw.flush();
            productNamesProxy = (ArrayList<String>) ois.readObject(); // SERVERREQUEST GETPRODCUTSINSTORE
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String line : productNamesProxy) {
            ArrayList<String> productInfo = new ArrayList<>();
            try {
                pw.println("PRODUCTINFO");
                pw.println(line);
                pw.println(store);
                pw.flush();
                productInfo = (ArrayList<String>) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (store.contains(searchParameter)) {
                output.add(line);
            } else if (productInfo.get(1).contains(searchParameter)) {
                output.add(line);
            } else if (line.contains(searchParameter)) {
                output.add(line);
            }
        }
        
        
        return output;
    }

    public ArrayList<String> importProducts(String path) {
        try {
            var output = new ArrayList<String>();
            BufferedReader buf = new BufferedReader(new FileReader(new File(path)));
            String s = buf.readLine();
            while (true) {
                if (s == null) {
                    break;
                } else {
                    output.add(s);
                    s = buf.readLine();
                }  
            }
            return output;

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    //Changed return type from void to boolean 11/13/22
    public boolean exportProducts(String path , String storeName) {
        try {
            ArrayList<String> products = new ArrayList<String>(); //SERVERREQUEST - VIEWPRODUCTS
            try {
                pw.println("VIEWPRODUCTS");
                pw.println(storeName);
                pw.flush();
                products = (ArrayList<String>) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
            PrintWriter filepw = new PrintWriter(new FileWriter(new File(path)));
            for (String line : products) {
                ArrayList<String> info = new ArrayList<>();
                try {
                    pw.println("PRODUCTINFO");
                    pw.println(line);
                    pw.println(storeName);
                    pw.flush();
                    info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST PRODUCTINFO    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (String commas : info) {
                    if (commas.contains(";")) {
                        JOptionPane.showMessageDialog(null, "Product values can't contain commas", "Error", JOptionPane.ERROR_MESSAGE);
                        filepw.flush();
                        filepw.close();
                        return false;
                    }
                }
                String out = line + ";" + info.get(0) + ";" + info.get(1) + ";" + info.get(2) + ";" + info.get(3);
                filepw.println(out);

            }
            filepw.flush();
            filepw.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void viewProductsInCarts(ArrayList<String> info) {
        JFrame viewcart = new JFrame("THE MARKETPLACE");
        viewcart.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(viewcart, 
                    "Are you sure you want to close this window?", "Close Window?", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    pw.println("LOGOUT");
                    pw.flush();
                    viewcart.dispose();
                }
            }
        });
        Container viewcartpanel = viewcart.getContentPane();
        viewcartpanel.setLayout(new BorderLayout());
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {           //ACTION LISTENER - Refreshes the Home Page
                viewcart.dispose();
                var info = new ArrayList<String>(); 
                try {
                    pw.println("NUMINCART");   //ROHANFIX Only works if the user is a buyer, not a seller
                    pw.println(username);
                    pw.flush();
                    info = (ArrayList<String>) ois.readObject(); //SERVERREQUEST NUMINCART
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
                }    
                viewProductsInCarts(info);
                
            }
        
        }); 

        JPanel viewcartNorth = new JPanel(new FlowLayout());
        JLabel title = new JLabel("<html><h1>Products in Customers Carts</h1></html>");     //Createds title
        viewcartNorth.add(title);
        viewcartNorth.add(refresh);

        viewcart.add(viewcartNorth, BorderLayout.NORTH);

        JPanel productsPanel = new JPanel();
        BoxLayout boxlayout = new BoxLayout(productsPanel, BoxLayout.Y_AXIS); //Variable number of products from product list
        productsPanel.setLayout(boxlayout);
        for (String s : info) {
            JLabel jl = new JLabel(s);
            productsPanel.add(jl);
        }


        viewcart.add(viewcartNorth, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(productsPanel); 
        viewcart.add(scrollPane , BorderLayout.CENTER);

        JPanel viewcartSouth = new JPanel(new FlowLayout());

        JButton back = new JButton("Back");
        back.addActionListener(new ActionListener() {      
            public void actionPerformed(ActionEvent e) {  
                try {
                    pw.println("VIEWSTORES");
                    pw.println(username);
                    pw.flush();
                    myStoreNames = (ArrayList<String>) ois.readObject(); //SERVERREQUEST VIEWSTORES
                    sellerMain(myStoreNames);
                    viewcart.dispose();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        
        });
        viewcartSouth.add(back);
        viewcart.add(viewcartSouth, BorderLayout.SOUTH);


        viewcart.pack();
        viewcart.setSize(800, 600);
        viewcart.setLocationRelativeTo(null);
        viewcart.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        viewcart.setVisible(true);
        viewcart.requestFocus();
    }
}