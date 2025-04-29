package gui.dashboard;

import gui.MainFrame;
import gui.components.BasePanel;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeFonts;
import gui.theme.ThemeIcons;
import models.Prescription;
import models.Order;
import models.Medicine;
import models.Patient;
import models.User;
import models.Wallet;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDashboardPanel extends BasePanel {
    private JTabbedPane tabbedPane;
    private User patient;
    private Map<String, JLabel> profileValueLabels = new HashMap<>();

    public PatientDashboardPanel(MainFrame mainFrame) {
        super(mainFrame);
        this.patient = mainFrame.getCurrentUser();
        initializeComponents();
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Handle case when patient is null
        if (patient == null) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(ThemeColors.BACKGROUND);
            JLabel errorLabel = new JLabel("Error: Unable to load patient information", JLabel.CENTER);
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            JButton backButton = new JButton("Back to Login");
            backButton.addActionListener(e -> mainFrame.navigateTo("LOGOUT"));
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(backButton);
            
            errorPanel.add(errorLabel, BorderLayout.CENTER);
            errorPanel.add(buttonPanel, BorderLayout.SOUTH);
            add(errorPanel, BorderLayout.CENTER);
            return;
        }
        
        // Create top panel with welcome message and logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ThemeColors.PRIMARY);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + patient.getName());
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        topPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> mainFrame.navigateTo("LOGOUT"));
        topPanel.add(logoutButton, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);

        // Create tabbed pane for different functionalities
        tabbedPane = new JTabbedPane();
        
        // Create placeholder panels for now, we'll implement these later
        JPanel prescriptionsPanel = new JPanel();
        prescriptionsPanel.add(new JLabel("Prescriptions coming soon"));
        
        JPanel orderMedicinesPanel = new JPanel();
        orderMedicinesPanel.add(new JLabel("Order Medicines coming soon"));
        
        JPanel medicalHistoryPanel = new JPanel();
        medicalHistoryPanel.add(new JLabel("Medical History coming soon"));
        
        JPanel bookConsultationPanel = new JPanel();
        bookConsultationPanel.add(new JLabel("Book Consultation coming soon"));
        
        tabbedPane.addTab("My Prescriptions", prescriptionsPanel);
        tabbedPane.addTab("Order Medicines", orderMedicinesPanel);
        tabbedPane.addTab("Medical History", medicalHistoryPanel);
        tabbedPane.addTab("Book Consultation", bookConsultationPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
} 