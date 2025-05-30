package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents an order in the pharmacy system
 */
public class Order {

    public enum Status {
        PENDING("Pending"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled"),
        DELIVERED("Delivered"),
        READY_FOR_PICKUP("Ready for Pickup"),
        PAYMENT_PENDING("Payment Pending"),
        PAYMENT_FAILED("Payment Failed");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum PaymentMethod {
        WALLET("Wallet"),
        CREDIT_CARD("Credit Card"),
        CASH_ON_DELIVERY("Cash on Delivery"),
        FREE("Free"),
        NOT_PAID("Not Paid");
        
        private final String displayName;
        
        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum DeliveryMethod {
        PICKUP("Pickup"),
        DELIVERY("Delivery");
        
        private final String displayName;
        
        DeliveryMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Required fields
    private int id;
    private int patientId;
    private Date orderDate;
    private Status status;
    private List<OrderItem> items;
    private double totalAmount;
    private PaymentMethod paymentMethod;
    
    // Optional fields
    private String patientName;
    private String patientPhone;
    private String patientAddress;
    private DeliveryMethod deliveryMethod;
    private Date deliveryDate;
    private Date completionDate;
    private String prescriptionId;
    private String notes;
    private String trackingNumber;
    private boolean isPaid;
    private Date paymentDate;
    private String paymentReference;
    
    /**
     * Constructor for creating a new order
     * 
     * @param id The ID of the order
     * @param patientId The ID of the patient placing the order
     */
    public Order(int id, int patientId) {
        this.id = id;
        this.patientId = patientId;
        this.items = new ArrayList<>();
        this.orderDate = new Date();
        this.status = Status.PENDING;
        this.totalAmount = 0.0;
        this.paymentMethod = PaymentMethod.NOT_PAID;
        this.deliveryMethod = DeliveryMethod.PICKUP;
        this.isPaid = false;
    }
    
    /**
     * Constructor for loading an order from file storage
     * 
     * @param id The order ID number
     * @param patientId The ID of the patient placing the order
     * @param orderDateStr The order date as a string
     */
    public Order(int id, int patientId, String orderDateStr) {
        this.id = id;
        this.patientId = patientId;
        this.items = new ArrayList<>();
        this.status = Status.PENDING;
        this.paymentMethod = PaymentMethod.NOT_PAID;
        this.deliveryMethod = DeliveryMethod.PICKUP;
        this.isPaid = false;
        
        try {
            // Parse date string - expect format like "yyyy-MM-dd HH:mm:ss"
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.orderDate = dateFormat.parse(orderDateStr);
        } catch (Exception e) {
            // Fallback to current date if parsing fails
            this.orderDate = new Date();
        }
    }
    
    /**
     * Constructor for creating a new order with a specific date
     * 
     * @param id The order ID number
     * @param patientId The ID of the patient placing the order
     * @param orderDate The order date
     */
    public Order(int id, int patientId, Date orderDate) {
        this.id = id;
        this.patientId = patientId;
        this.orderDate = orderDate;
        this.items = new ArrayList<>();
        this.status = Status.PENDING;
        this.totalAmount = 0.0;
        this.paymentMethod = PaymentMethod.NOT_PAID;
        this.deliveryMethod = DeliveryMethod.PICKUP;
        this.isPaid = false;
    }
    
    /**
     * Calculate the total amount for this order
     * 
     * @return The total amount
     */
    public double calculateTotal() {
        return items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }
    
    /**
     * Add an item to this order
     * 
     * @param item The item to add
     */
    public void addItem(OrderItem item) {
        // Check if the item already exists in the order
        for (OrderItem existingItem : items) {
            if (existingItem.getMedicineId() == item.getMedicineId()) {
                // Update quantity instead of adding a new item
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                totalAmount = calculateTotal();
                return;
            }
        }
        
        // Add as a new item
        items.add(item);
        totalAmount = calculateTotal();
    }
    
    /**
     * Remove an item from this order
     * 
     * @param medicineId The ID of the medicine to remove
     * @return true if removed, false if not found
     */
    public boolean removeItem(int medicineId) {
        boolean removed = items.removeIf(item -> item.getMedicineId() == medicineId);
        
        if (removed) {
            totalAmount = calculateTotal();
        }
        
        return removed;
    }
    
    /**
     * Update the quantity of an item in this order
     * 
     * @param medicineId The ID of the medicine to update
     * @param newQuantity The new quantity
     * @return true if updated, false if not found
     */
    public boolean updateItemQuantity(int medicineId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeItem(medicineId);
        }
        
        for (OrderItem item : items) {
            if (item.getMedicineId() == medicineId) {
                item.setQuantity(newQuantity);
                totalAmount = calculateTotal();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Process payment from the patient's wallet
     * 
     * @param patient The patient making the payment
     * @return true if payment was successful, false otherwise
     */
    public boolean processPaymentFromWallet(Patient patient) {
        if (isPaid) {
            return true; // Already paid
        }
        
        Wallet wallet = patient.getWallet();
        
        if (wallet == null) {
            return false;
        }
        
        // Handle zero or negative amount orders (free prescriptions, etc.)
        if (totalAmount <= 0) {
            // Mark as paid without wallet transaction
            this.isPaid = true;
            this.paymentMethod = PaymentMethod.FREE;
            this.paymentDate = new Date();
            // Use UUID for unique reference for consistency
            this.paymentReference = "Free Order #" + id + "-" + UUID.randomUUID().toString();
            
            if (this.status == Status.PAYMENT_PENDING) {
                this.status = Status.PROCESSING;
            }
            return true;
        }
        
        boolean success = wallet.withdraw(totalAmount, "Order #" + id);
        
        if (success) {
            this.isPaid = true;
            this.paymentMethod = PaymentMethod.WALLET;
            this.paymentDate = new Date();
            // Use UUID for unique reference instead of timestamp for better compatibility
            this.paymentReference = "Wallet Transaction: " + UUID.randomUUID().toString();
            
            if (this.status == Status.PAYMENT_PENDING) {
                this.status = Status.PROCESSING;
            }
        } else {
            this.status = Status.PAYMENT_FAILED;
        }
        
        return success;
    }
    
    /**
     * Set the prescription ID as an integer
     * 
     * @param prescriptionId The prescription ID
     */
    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = String.valueOf(prescriptionId);
    }
    
    /**
     * Process payment with a credit card
     * 
     * @param patient The patient making the payment
     * @param cardNumber The card number to use
     * @return true if payment was successful, false otherwise
     */
    public boolean processPaymentWithCard(Patient patient, String cardNumber) {
        if (isPaid) {
            return true; // Already paid
        }
        
        Wallet wallet = patient.getWallet();
        
        if (wallet == null) {
            return false;
        }
        
        // Check if the card exists in the wallet
        boolean cardValid = wallet.hasCard(cardNumber);
        
        if (!cardValid) {
            return false;
        }
        
        // Process card payment
        boolean success = true; // In a real system, this would call a payment gateway
        
        if (success) {
            this.isPaid = true;
            this.paymentMethod = PaymentMethod.CREDIT_CARD;
            this.paymentDate = new Date();
            // Create a reference with the UUID and the last 4 digits of the card
            this.paymentReference = "Card Transaction: " + UUID.randomUUID().toString() + "-" + cardNumber.substring(cardNumber.length() - 4);
            
            if (this.status == Status.PAYMENT_PENDING) {
                this.status = Status.PROCESSING;
            }
        } else {
            this.status = Status.PAYMENT_FAILED;
        }
        
        return success;
    }
    
    /**
     * Set the order as paid with cash on delivery
     */
    public void setPaymentMethodCashOnDelivery() {
        this.paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
        
        if (this.status == Status.PAYMENT_PENDING) {
            this.status = Status.PROCESSING;
        }
    }
    
    /**
     * Mark the order as delivered and paid (for cash on delivery)
     */
    public void markAsDeliveredAndPaid() {
        if (this.paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
            this.isPaid = true;
            this.paymentDate = new Date();
        }
        
        this.status = Status.DELIVERED;
        this.completionDate = new Date();
    }
    
    /**
     * Cancel this order
     * 
     * @return true if cancelled, false if already completed or delivered
     */
    public boolean cancel() {
        if (status == Status.COMPLETED || status == Status.DELIVERED) {
            return false;
        }
        
        status = Status.CANCELLED;
        return true;
    }
    
    /**
     * Get the ID of this order
     * 
     * @return The ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * Get the patient ID for this order
     * 
     * @return The patient ID
     */
    public int getPatientId() {
        return patientId;
    }
    
    /**
     * Set the patient ID for this order
     * 
     * @param patientId The patient ID
     */
    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
    
    /**
     * Get the order date
     * 
     * @return The order date
     */
    public Date getOrderDate() {
        return orderDate;
    }
    
    /**
     * Get the date/time of this order
     * Used by FileHandler for persistence
     * 
     * @return The date/time
     */
    public Date getDateTime() {
        return orderDate;
    }
    
    /**
     * Get the status of this order
     * 
     * @return The status
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * Set the status of this order
     * 
     * @param status The status
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
    /**
     * Get the items in this order
     * 
     * @return The items
     */
    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * Add a medicine to this order
     * 
     * @param medicine The medicine to add
     * @param quantity The quantity to add
     */
    public void addMedicine(Medicine medicine, int quantity) {
        if (medicine == null || quantity <= 0) {
            return;
        }
        
        OrderItem newItem = new OrderItem(medicine.getId(), medicine.getName(), quantity, medicine.getPrice());
        addItem(newItem);
    }
    
    /**
     * Get the medicines in this order as a map of medicine to quantity
     * 
     * @return A map of medicine to quantity
     */
    public Map<Medicine, Integer> getMedicines() {
        Map<Medicine, Integer> medicineMap = new HashMap<>();
        // In a real application, this would fetch medicines from a repository
        return medicineMap;
    }
    
    /**
     * Get a list of medicines in this order 
     * Used by FileHandler for persistence
     * 
     * @return A list of medicines
     */
    public List<Medicine> getMedicinesList() {
        return new ArrayList<>();
    }
    
    /**
     * Get a list of quantities for medicines in this order
     * Used by FileHandler for persistence
     * 
     * @return A list of quantities
     */
    public List<Integer> getQuantities() {
        return items.stream()
                .map(OrderItem::getQuantity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get the payment status of this order
     * 
     * @return The payment status
     */
    public String getPaymentStatus() {
        return isPaid ? "true" : "false";
    }
    
    /**
     * Display information about this order
     */
    public void displayInfo() {
        System.out.println(toDetailedString());
    }
    
    /**
     * Print a receipt for this order
     * 
     * @param customerName The name of the customer
     */
    public void printReceipt(String customerName) {
        System.out.println("\n===============================================");
        System.out.println("               EL-TA3BAN PHARMACY              ");
        System.out.println("                  ORDER RECEIPT                ");
        System.out.println("===============================================");
        System.out.println("Order #: " + id);
        System.out.println("Date: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate));
        System.out.println("Customer: " + customerName);
        System.out.println("-----------------------------------------------");
        System.out.println("Items:");
        
        for (OrderItem item : items) {
            System.out.printf("  %-30s %2d × %8.2f LE = %8.2f LE%n", 
                     item.getMedicineName(), 
                     item.getQuantity(),
                     item.getUnitPrice(),
                     item.getQuantity() * item.getUnitPrice());
        }
        
        System.out.println("-----------------------------------------------");
        System.out.printf("Total Amount: %33.2f LE%n", totalAmount);
        System.out.println("Payment Method: " + paymentMethod.getDisplayName());
        System.out.println("Payment Status: " + (isPaid ? "Paid" : "Not Paid"));
        
        if (isPaid && paymentDate != null) {
            System.out.println("Payment Date: " + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(paymentDate));
        }
        
        System.out.println("===============================================");
        System.out.println("Thank you for shopping at EL-TA3BAN Pharmacy!");
        System.out.println("===============================================\n");
    }
    
    /**
     * Get the total amount for this order
     * 
     * @return The total amount
     */
    public double getTotalAmount() {
        return totalAmount;
    }
    
    /**
     * Set the total amount for this order
     * 
     * @param totalAmount The total amount
     */
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    /**
     * Get the payment method for this order
     * 
     * @return The payment method
     */
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    /**
     * Set the payment method for this order
     * 
     * @param paymentMethod The payment method
     */
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    /**
     * Set the payment method for this order using a string value
     * Needed for file storage operations
     * 
     * @param paymentMethodStr The payment method as a string
     */
    public void setPaymentMethod(String paymentMethodStr) {
        try {
            this.paymentMethod = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to NOT_PAID if the payment method string is invalid
            this.paymentMethod = PaymentMethod.NOT_PAID;
        }
    }
    
    /**
     * Set the payment status using a string
     * Needed for file storage operations
     * 
     * @param paymentStatus The payment status as a string ("true" or "false")
     */
    public void setPaymentStatus(String paymentStatus) {
        if (paymentStatus != null) {
            this.isPaid = paymentStatus.trim().equalsIgnoreCase("true");
            
            // If marked as paid but no payment date, set it to now
            if (this.isPaid && this.paymentDate == null) {
                this.paymentDate = new Date();
            }
        }
    }
    
    /**
     * Get the patient name
     * 
     * @return The patient name
     */
    public String getPatientName() {
        return patientName;
    }
    
    /**
     * Set the patient name
     * 
     * @param patientName The patient name
     */
    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
    
    /**
     * Get the patient phone number
     * 
     * @return The patient phone number
     */
    public String getPatientPhone() {
        return patientPhone;
    }
    
    /**
     * Set the patient phone number
     * 
     * @param patientPhone The patient phone number
     */
    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }
    
    /**
     * Get the patient address
     * 
     * @return The patient address
     */
    public String getPatientAddress() {
        return patientAddress;
    }
    
    /**
     * Set the patient address
     * 
     * @param patientAddress The patient address
     */
    public void setPatientAddress(String patientAddress) {
        this.patientAddress = patientAddress;
    }
    
    /**
     * Get the delivery method
     * 
     * @return The delivery method
     */
    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }
    
    /**
     * Set the delivery method
     * 
     * @param deliveryMethod The delivery method
     */
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }
    
    /**
     * Get the delivery date
     * 
     * @return The delivery date
     */
    public Date getDeliveryDate() {
        return deliveryDate;
    }
    
    /**
     * Set the delivery date
     * 
     * @param deliveryDate The delivery date
     */
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    /**
     * Get the completion date
     * 
     * @return The completion date
     */
    public Date getCompletionDate() {
        return completionDate;
    }
    
    /**
     * Set the completion date
     * 
     * @param completionDate The completion date
     */
    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }
    
    /**
     * Get the prescription ID
     * 
     * @return The prescription ID
     */
    public String getPrescriptionId() {
        return prescriptionId;
    }
    
    /**
     * Set the prescription ID
     * 
     * @param prescriptionId The prescription ID
     */
    public void setPrescriptionId(String prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
    
    /**
     * Get the notes
     * 
     * @return The notes
     */
    public String getNotes() {
        return notes;
    }
    
    /**
     * Set the notes
     * 
     * @param notes The notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Get the tracking number
     * 
     * @return The tracking number
     */
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    /**
     * Set the tracking number
     * 
     * @param trackingNumber The tracking number
     */
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    /**
     * Check if this order is paid
     * 
     * @return true if paid, false otherwise
     */
    public boolean isPaid() {
        return isPaid;
    }
    
    /**
     * Set whether this order is paid
     * 
     * @param isPaid Whether this order is paid
     */
    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
    
    /**
     * Get the payment date
     * 
     * @return The payment date
     */
    public Date getPaymentDate() {
        return paymentDate;
    }
    
    /**
     * Set the payment date
     * 
     * @param paymentDate The payment date
     */
    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    /**
     * Get the payment reference
     * 
     * @return The payment reference
     */
    public String getPaymentReference() {
        return paymentReference;
    }
    
    /**
     * Set the payment reference
     * 
     * @param paymentReference The payment reference
     */
    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }
    
    /**
     * Get a formatted string representation of the total amount
     * 
     * @return The formatted total amount
     */
    public String getFormattedTotalAmount() {
        return String.format("%.2f LE", totalAmount);
    }
    
    /**
     * Get the number of items in this order
     * 
     * @return The number of items
     */
    public int getItemCount() {
        return items.size();
    }
    
    /**
     * Get the total quantity of all items in this order
     * 
     * @return The total quantity
     */
    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }
    
    /**
     * Get a formatted string representation of this order
     * 
     * @return The formatted string
     */
    @Override
    public String toString() {
        return String.format("Order [ID: %d, Patient ID: %d, Items: %d, Total: %s, Status: %s]", 
                id, patientId, getItemCount(), getFormattedTotalAmount(), status.getDisplayName());
    }
    
    /**
     * Get a detailed string representation of this order
     * 
     * @return The detailed string
     */
    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order #").append(id).append("\n");
        sb.append("Patient ID: ").append(patientId).append("\n");
        sb.append("Date: ").append(orderDate).append("\n");
        sb.append("Status: ").append(status.getDisplayName()).append("\n");
        sb.append("Payment Method: ").append(paymentMethod.getDisplayName()).append("\n");
        sb.append("Paid: ").append(isPaid ? "Yes" : "No").append("\n");
        sb.append("Items:\n");
        
        for (OrderItem item : items) {
            sb.append("  - ").append(item.toString()).append("\n");
        }
        
        sb.append("Total Amount: ").append(getFormattedTotalAmount()).append("\n");
        
        if (deliveryMethod != null) {
            sb.append("Delivery Method: ").append(deliveryMethod.getDisplayName()).append("\n");
        }
        
        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            sb.append("Tracking Number: ").append(trackingNumber).append("\n");
        }
        
        if (notes != null && !notes.isEmpty()) {
            sb.append("Notes: ").append(notes).append("\n");
        }
        
        return sb.toString();
    }
}