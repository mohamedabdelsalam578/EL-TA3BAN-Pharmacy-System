package models;

import java.util.Date;

/**
 * Represents a medicine in the pharmacy system
 */
public class Medicine  {
 
    
    private int id;
    private String name;
    private String description;
    private String manufacturer;
    private double price;
    private int stock;
    private String category;
    private String dosage;
    private boolean prescription;
    private Date expiryDate;
    private String imageUrl;
    private int quantity; // Added for compatibility
    
    /**
     * Constructor for creating a new medicine - Used by the FileHandler
     * 
     * @param id The unique identifier for this medicine
     * @param name The name of this medicine
     * @param description The description of this medicine
     * @param manufacturer The manufacturer of this medicine
     * @param price The price of this medicine
     * @param quantity The quantity/stock of this medicine
     * @param category The category of this medicine
     * @param requiresPrescription Whether this medicine requires a prescription
     */
    public Medicine(int id, String name, String description, String manufacturer, 
                    double price, int quantity, String category, boolean requiresPrescription) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.manufacturer = manufacturer;
        this.price = price;
        this.stock = quantity;
        this.quantity = quantity; // Initialize quantity to match stock
        this.category = category;
        this.dosage = "As directed"; // Default dosage
        this.prescription = requiresPrescription;
        this.expiryDate = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000); // Default: 1 year from now
        this.imageUrl = "";
    }
    
    /**
     * Constructor for creating a new medicine with full details
     * 
     * @param id The unique identifier for this medicine
     * @param name The name of this medicine
     * @param description The description of this medicine
     * @param manufacturer The manufacturer of this medicine
     * @param price The price of this medicine
     * @param stock The stock quantity of this medicine
     * @param category The category of this medicine
     * @param dosage The dosage of this medicine
     * @param prescription Whether this medicine requires a prescription
     * @param expiryDate The expiry date of this medicine
     */
    public Medicine(int id, String name, String description, String manufacturer, double price, int stock,
                    String category, String dosage, boolean prescription, Date expiryDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.manufacturer = manufacturer;
        this.price = price;
        this.stock = stock;
        this.quantity = stock; // Initialize quantity to match stock
        this.category = category;
        this.dosage = dosage;
        this.prescription = prescription;
        this.expiryDate = expiryDate;
        this.imageUrl = "";
    }
    
    /**
     * Constructor for creating a new medicine with minimal information
     * 
     * @param id The unique identifier for this medicine
     * @param name The name of this medicine
     * @param description The description of this medicine
     * @param price The price of this medicine
     * @param stock The stock quantity of this medicine
     */
    public Medicine(int id, String name, String description, double price, int stock) {
        this(id, name, description, "", price, stock, "", "", false, null);
    }
    
    /**
     * Get the ID of this medicine
     * 
     * @return The ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Set the ID of this medicine
     * 
     * @param id The ID
     */
    public void setId(int id) {
        this.id = id;
    }
    
    /**
     * Get the name of this medicine
     * 
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the name of this medicine
     * 
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the description of this medicine
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description of this medicine
     * 
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the manufacturer of this medicine
     * 
     * @return The manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }
    
    /**
     * Set the manufacturer of this medicine
     * 
     * @param manufacturer The manufacturer
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    /**
     * Get the price of this medicine
     * 
     * @return The price
     */
    public double getPrice() {
        return price;
    }
    
    /**
     * Set the price of this medicine
     * 
     * @param price The price
     */
    public void setPrice(double price) {
        this.price = price;
    }
    
    /**
     * Get the stock quantity of this medicine
     * 
     * @return The stock quantity
     */
    public int getStock() {
        return stock;
    }
    
    /**
     * Set the stock quantity of this medicine
     * 
     * @param stock The stock quantity
     */
    public void setStock(int stock) {
        this.stock = stock;
        this.quantity = stock; // Keep quantity in sync with stock
    }
    
    /**
     * Get the quantity of this medicine (for compatibility with existing code)
     * 
     * @return The quantity
     */
    public int getQuantity() {
        return quantity;
    }
    
    /**
     * Set the quantity of this medicine (for compatibility with existing code)
     * 
     * @param quantity The quantity
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.stock = quantity; // Keep stock in sync with quantity
    }
    
    /**
     * Restore stock for canceled orders
     * 
     * @param quantity The quantity to restore
     * @return The new stock level
     */
    public int restoreStock(int quantity) {
        this.stock += quantity;
        this.quantity = this.stock; // Keep quantity in sync with stock
        return this.stock;
    }
    
    /**
     * Check if this medicine is in stock
     * 
     * @return true if in stock, false otherwise
     */
    public boolean isInStock() {
        return stock > 0;
    }
    
    /**
     * Check if this medicine is available in the requested quantity
     * 
     * @param quantity The requested quantity
     * @return true if available, false otherwise
     */
    public boolean isAvailable(int quantity) {
        return stock >= quantity;
    }
    
    /**
     * Update the stock quantity of this medicine
     * 
     * @param quantity The quantity to add (positive) or remove (negative)
     * @return The new stock quantity
     * @throws IllegalArgumentException If the updated stock would be negative
     */
    public int updateStock(int quantity) {
        int newStock = stock + quantity;
        
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        
        stock = newStock;
        return stock;
    }
    
    /**
     * Get the category of this medicine
     * 
     * @return The category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Set the category of this medicine
     * 
     * @param category The category
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Get the dosage of this medicine
     * 
     * @return The dosage
     */
    public String getDosage() {
        return dosage;
    }
    
    /**
     * Set the dosage of this medicine
     * 
     * @param dosage The dosage
     */
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    
    /**
     * Check if this medicine requires a prescription
     * 
     * @return true if prescription required, false otherwise
     */
    public boolean isPrescription() {
        return prescription;
    }
    
    /**
     * Check if this medicine requires a prescription (alternative method name)
     * 
     * @return true if prescription required, false otherwise
     */
    public boolean isRequiresPrescription() {
        return prescription;
    }
    
    /**
     * Set whether this medicine requires a prescription
     * 
     * @param prescription Whether prescription is required
     */
    public void setPrescription(boolean prescription) {
        this.prescription = prescription;
    }
    
    /**
     * Set whether this medicine requires a prescription (alternative method name)
     * 
     * @param requiresPrescription Whether prescription is required
     */
    public void setRequiresPrescription(boolean requiresPrescription) {
        this.prescription = requiresPrescription;
    }
    
    /**
     * Get the expiry date of this medicine
     * 
     * @return The expiry date
     */
    public Date getExpiryDate() {
        return expiryDate;
    }
    
    /**
     * Get the date/time of this medicine's expiry
     * Used by FileHandler for persistence
     * 
     * @return The date/time
     */
    public Date getDateTime() {
        return expiryDate;
    }
    
    /**
     * Set the expiry date of this medicine
     * 
     * @param expiryDate The expiry date
     */
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    /**
     * Check if this medicine is expired
     * 
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        
        Date now = new Date();
        return expiryDate.before(now);
    }
    
    /**
     * Get the image URL of this medicine
     * 
     * @return The image URL
     */
    public String getImageUrl() {
        return imageUrl;
    }
    
    /**
     * Set the image URL of this medicine
     * 
     * @param imageUrl The image URL
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    /**
     * Get a formatted string representation of the price
     * 
     * @return The formatted price
     */
    public String getFormattedPrice() {
        return String.format("%.2f LE", price);
    }
    
    /**
     * Get a formatted string representation of this medicine
     * 
     * @return The formatted string
     */
    @Override
    public String toString() {
        return String.format("Medicine [ID: %d, Name: %s, Price: %s, Stock: %d]", 
                id, name, getFormattedPrice(), stock);
    }
    
    /**
     * Display information about this medicine
     */
    public void displayInfo() {
        System.out.println("\n===== MEDICINE INFORMATION =====");
        System.out.println("ID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Price: " + getFormattedPrice());
        System.out.println("Stock: " + stock);
        System.out.println("Category: " + category);
        System.out.println("Manufacturer: " + manufacturer);
        System.out.println("Description: " + description);
        System.out.println("Requires Prescription: " + (prescription ? "Yes" : "No"));
        
        if (expiryDate != null) {
            System.out.println("Expiry Date: " + new java.text.SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
            System.out.println("Expired: " + (isExpired() ? "Yes" : "No"));
        }
    }
}