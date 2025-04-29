package gui.pharmacist;

import gui.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.Order;
import models.OrderStatus;
import models.Patient;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OrderProcessingPanel extends BasePanel {
    private StyledTable<Order> orderTable;
    private JComboBox<OrderStatus> statusFilter;
    private List<Order> orders;

    public OrderProcessingPanel(MainFrame mainFrame) {
        super(mainFrame);
        initializeComponents();
        loadOrders();
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusFilter = new JComboBox<>(OrderStatus.values());
        statusFilter.insertItemAt(null, 0);
        statusFilter.setSelectedIndex(0);
        statusFilter.addActionListener(e -> filterOrders());

        StyledButton refreshButton = new StyledButton("Refresh", ThemeIcons.REFRESH);
        refreshButton.addActionListener(e -> loadOrders());
        
        filterPanel.add(new JLabel("Filter by Status: "));
        filterPanel.add(statusFilter);
        filterPanel.add(refreshButton);
        add(filterPanel, BorderLayout.NORTH);

        // Order Table
        String[] columns = {"Order ID", "Patient", "Date", "Status", "Total Items", "Total Amount"};
        orderTable = new StyledTable<>(columns, order -> new Object[]{
            order.getId(),
            order.getPatientName(),
            order.getOrderDate(),
            order.getStatus(),
            order.getItems().size(),
            order.getTotalAmount()
        });
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        StyledButton viewDetailsButton = new StyledButton("View Details", ThemeIcons.VIEW);
        StyledButton processButton = new StyledButton("Process Order", ThemeIcons.PROCESS);
        StyledButton completeButton = new StyledButton("Mark as Complete", ThemeIcons.COMPLETE);

        viewDetailsButton.addActionListener(e -> viewOrderDetails());
        processButton.addActionListener(e -> processOrder());
        completeButton.addActionListener(e -> completeOrder());

        actionPanel.add(viewDetailsButton);
        actionPanel.add(processButton);
        actionPanel.add(completeButton);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadOrders() {
        orders = mainFrame.getPharmacyService().getOrders();
        orderTable.setData(orders);
    }

    private void filterOrders() {
        OrderStatus selectedStatus = (OrderStatus) statusFilter.getSelectedItem();
        List<Order> filtered = orders.stream()
            .filter(order -> selectedStatus == null || order.getStatus() == selectedStatus)
            .toList();
        orderTable.setData(filtered);
    }

    private void viewOrderDetails() {
        Order selected = orderTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an order first",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Order Details\n");
        details.append("=============\n\n");
        details.append(String.format("Order ID: %d\n", selected.getId()));
        details.append(String.format("Patient: %s\n", selected.getPatientName()));
        details.append(String.format("Date: %s\n", selected.getOrderDate()));
        details.append(String.format("Status: %s\n\n", selected.getStatus()));
        details.append("Items:\n");
        selected.getItems().forEach(item ->
            details.append(String.format("- %s x%d (%.2f each)\n",
                item.getMedicine().getName(),
                item.getQuantity(),
                item.getMedicine().getPrice()))
        );
        details.append(String.format("\nTotal Amount: %.2f", selected.getTotalAmount()));

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane,
            "Order Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void processOrder() {
        Order selected = orderTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an order first",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.getStatus() != OrderStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "Only pending orders can be processed",
                "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if all medicines are in stock
        boolean canProcess = selected.getItems().stream()
            .allMatch(item -> item.getMedicine().getStock() >= item.getQuantity());

        if (!canProcess) {
            JOptionPane.showMessageDialog(this, "Insufficient stock for some items",
                "Cannot Process", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update stock and order status
        selected.getItems().forEach(item ->
            item.getMedicine().decrementStock(item.getQuantity()));
        selected.setStatus(OrderStatus.PROCESSING);
        
        mainFrame.getPharmacyService().saveDataToFiles();
        loadOrders();
        
        JOptionPane.showMessageDialog(this, "Order is now being processed",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void completeOrder() {
        Order selected = orderTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select an order first",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.getStatus() != OrderStatus.PROCESSING) {
            JOptionPane.showMessageDialog(this, "Only processing orders can be completed",
                "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selected.setStatus(OrderStatus.COMPLETED);
        mainFrame.getPharmacyService().saveDataToFiles();
        loadOrders();
        
        JOptionPane.showMessageDialog(this, "Order has been marked as complete",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }
} 