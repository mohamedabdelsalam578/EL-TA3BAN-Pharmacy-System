package gui.dashboard;

import gui.components.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.Medicine;
import models.Order;
import models.OrderItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OrderMedicinesPanel extends BasePanel {
    private StyledTable<Medicine> medicineTable;
    private JSpinner quantitySpinner;
    private JLabel totalLabel;
    private List<OrderItem> cartItems;
    
    public OrderMedicinesPanel(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(ThemeColors.BACKGROUND);

        // Create search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(ThemeColors.BACKGROUND);
        
        JTextField searchField = new JTextField(20);
        StyledButton searchButton = new StyledButton("Search", ThemeIcons.SEARCH, e -> searchMedicines(searchField.getText()));
        
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        add(searchPanel, BorderLayout.NORTH);

        // Create medicine table
        String[] columns = {"ID", "Name", "Description", "Price", "Stock"};
        medicineTable = new StyledTable<>(columns, medicine -> new Object[]{
            medicine.getId(),
            medicine.getName(),
            medicine.getDescription(),
            String.format("%.2f", medicine.getPrice()),
            medicine.getStock()
        });

        JScrollPane scrollPane = new JScrollPane(medicineTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create order panel
        JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        orderPanel.setBackground(ThemeColors.BACKGROUND);

        orderPanel.add(new JLabel("Quantity: "));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        orderPanel.add(quantitySpinner);

        StyledButton addToCartButton = new StyledButton("Add to Cart", ThemeIcons.ADD, e -> addToCart());
        StyledButton viewCartButton = new StyledButton("View Cart", ThemeIcons.CART, e -> viewCart());
        
        orderPanel.add(addToCartButton);
        orderPanel.add(viewCartButton);
        
        totalLabel = new JLabel("Total: $0.00");
        orderPanel.add(totalLabel);
        
        add(orderPanel, BorderLayout.SOUTH);

        // Load medicines
        loadMedicines();
    }

    private void loadMedicines() {
        List<Medicine> medicines = mainFrame.getPharmacyService().getMedicines();
        medicineTable.setData(medicines);
    }

    private void searchMedicines(String query) {
        List<Medicine> medicines = mainFrame.getPharmacyService().searchMedicines(query);
        medicineTable.setData(medicines);
    }

    private void addToCart() {
        Medicine selected = medicineTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a medicine to add to cart", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        if (quantity > selected.getStock()) {
            JOptionPane.showMessageDialog(this, 
                "Not enough stock available", 
                "Invalid Quantity", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        OrderItem item = new OrderItem(selected, quantity);
        cartItems.add(item);
        updateTotal();

        JOptionPane.showMessageDialog(this, 
            "Added " + quantity + " " + selected.getName() + " to cart", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewCart() {
        mainFrame.navigateTo("CART");
    }

    private void updateTotal() {
        double total = cartItems.stream()
            .mapToDouble(item -> item.getMedicine().getPrice() * item.getQuantity())
            .sum();
        totalLabel.setText(String.format("Total: $%.2f", total));
    }
} 