package gui.pharmacist;

import gui.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.User;

import javax.swing.*;
import java.awt.*;

public class PharmacistDashboardPanel extends BasePanel {
    private JTabbedPane tabbedPane;
    private User pharmacist;

    public PharmacistDashboardPanel(MainFrame mainFrame, User pharmacist) {
        super(mainFrame);
        this.pharmacist = pharmacist;
        initializeComponents();
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Create top panel with welcome message and logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ThemeColors.PRIMARY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel welcomeLabel = new JLabel("Welcome, Pharmacist " + pharmacist.getName());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        StyledButton logoutButton = new StyledButton("Logout", ThemeIcons.LOGOUT, e -> mainFrame.navigateTo("LOGOUT"));
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // Create tabbed pane for different functionalities
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Inventory Management", new InventoryPanel(mainFrame));
        tabbedPane.addTab("Order Processing", new OrderProcessingPanel(mainFrame));
        tabbedPane.addTab("Prescription Validation", new PrescriptionValidationPanel(mainFrame));
        
        add(tabbedPane, BorderLayout.CENTER);
    }
} 