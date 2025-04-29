package gui.dashboard;

import gui.components.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.Prescription;
import models.Doctor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PrescriptionsPanel extends BasePanel {
    private StyledTable<Prescription> prescriptionsTable;
    
    public PrescriptionsPanel(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(ThemeColors.BACKGROUND);

        // Create prescriptions table
        String[] columns = {"ID", "Doctor", "Date", "Status", "Medicines"};
        prescriptionsTable = new StyledTable<>(columns, prescription -> new Object[]{
            prescription.getId(),
            getDoctorName(prescription.getDoctorId()),
            prescription.getIssueDate(),
            prescription.getStatus(),
            prescription.getMedicinesList()
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(prescriptionsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(ThemeColors.BACKGROUND);

        StyledButton viewButton = new StyledButton("View Details", ThemeIcons.VIEW, e -> viewPrescriptionDetails());
        StyledButton exportButton = new StyledButton("Export", ThemeIcons.EXPORT, e -> exportPrescriptions());

        buttonsPanel.add(viewButton);
        buttonsPanel.add(exportButton);
        add(buttonsPanel, BorderLayout.SOUTH);

        // Load prescriptions
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        List<Prescription> prescriptions = mainFrame.getPharmacyService()
            .getPrescriptionsForPatient(mainFrame.getCurrentUser().getId());
        prescriptionsTable.setData(prescriptions);
    }

    private String getDoctorName(int doctorId) {
        Doctor doctor = mainFrame.getPharmacyService().getDoctorById(doctorId);
        return doctor != null ? doctor.getName() : "Unknown Doctor";
    }

    private void viewPrescriptionDetails() {
        Prescription selected = prescriptionsTable.getSelectedItem();
        if (selected != null) {
            mainFrame.navigateTo("PRESCRIPTION_DETAILS", selected);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a prescription to view", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exportPrescriptions() {
        // TODO: Implement export functionality
        JOptionPane.showMessageDialog(this, 
            "Export functionality will be implemented soon", 
            "Coming Soon", 
            JOptionPane.INFORMATION_MESSAGE);
    }
} 