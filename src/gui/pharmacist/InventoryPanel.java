package gui.pharmacist;

import gui.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.Medicine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends BasePanel {
    private StyledTable<Medicine> medicineTable;
    private JTextField searchField;
    private JTextField quantityField;
    private List<Medicine> medicines;

    public InventoryPanel(MainFrame mainFrame) {
        super(mainFrame);
        initializeComponents();
        loadMedicines();
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        StyledButton searchButton = new StyledButton("Search", ThemeIcons.SEARCH, e -> searchMedicines());
        searchPanel.add(new JLabel("Search Medicine: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Description", "Price", "Stock", "Category"};
        medicineTable = new StyledTable<>(columns, medicine -> new Object[]{
            medicine.getId(),
            medicine.getName(),
            medicine.getDescription(),
            medicine.getPrice(),
            medicine.getStock(),
            medicine.getCategory()
        });
        
        JScrollPane scrollPane = new JScrollPane(medicineTable);
        add(scrollPane, BorderLayout.CENTER);

        // Stock Management Panel
        JPanel stockPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityField = new JTextField(5);
        StyledButton addStockButton = new StyledButton("Add Stock", ThemeIcons.ADD, e -> adjustStock(true));
        StyledButton reduceStockButton = new StyledButton("Reduce Stock", ThemeIcons.REMOVE, e -> adjustStock(false));
        StyledButton generateReportButton = new StyledButton("Generate Low Stock Report", ThemeIcons.REPORT, e -> generateLowStockReport());

        stockPanel.add(new JLabel("Quantity:"));
        stockPanel.add(quantityField);
        stockPanel.add(addStockButton);
        stockPanel.add(reduceStockButton);
        stockPanel.add(generateReportButton);

        add(stockPanel, BorderLayout.SOUTH);
    }

    private void loadMedicines() {
        medicines = mainFrame.getPharmacyService().getMedicines();
        medicineTable.setData(medicines);
    }

    private void searchMedicines() {
        String query = searchField.getText().toLowerCase().trim();
        List<Medicine> filtered = medicines.stream()
            .filter(med -> med.getName().toLowerCase().contains(query) ||
                         med.getDescription().toLowerCase().contains(query) ||
                         med.getCategory().toLowerCase().contains(query))
            .toList();
        medicineTable.setData(filtered);
    }

    private void adjustStock(boolean increase) {
        Medicine selected = medicineTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine first", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Please enter a positive quantity", 
                    "Invalid Quantity", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (increase) {
                selected.incrementStock(quantity);
            } else {
                if (selected.getStock() < quantity) {
                    JOptionPane.showMessageDialog(this, "Not enough stock available", 
                        "Invalid Operation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                selected.decrementStock(quantity);
            }

            mainFrame.getPharmacyService().saveDataToFiles();
            medicineTable.refresh();
            
            JOptionPane.showMessageDialog(this, "Stock updated successfully", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number", 
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateLowStockReport() {
        StringBuilder report = new StringBuilder();
        report.append("Low Stock Report\n");
        report.append("================\n\n");

        medicines.stream()
            .filter(med -> med.getStock() < 10)  // Assuming 10 is the threshold for low stock
            .forEach(med -> report.append(String.format(
                "Medicine: %s\nCurrent Stock: %d\nReorder Needed: Yes\n\n",
                med.getName(), med.getStock()
            )));

        JTextArea textArea = new JTextArea(report.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, 
            "Low Stock Report", JOptionPane.INFORMATION_MESSAGE);
    }
} 