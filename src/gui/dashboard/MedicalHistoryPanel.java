package gui.dashboard;

import gui.components.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.MedicalRecord;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MedicalHistoryPanel extends BasePanel {
    private StyledTable<MedicalRecord> recordsTable;
    
    public MedicalHistoryPanel(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(ThemeColors.BACKGROUND);

        // Create medical records table
        String[] columns = {"Date", "Doctor", "Diagnosis", "Treatment", "Notes"};
        recordsTable = new StyledTable<>(columns, record -> new Object[]{
            record.getDate(),
            getDoctorName(record.getDoctorId()),
            record.getDiagnosis(),
            record.getTreatment(),
            record.getNotes()
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(recordsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(ThemeColors.BACKGROUND);

        StyledButton viewButton = new StyledButton("View Details", ThemeIcons.VIEW, e -> viewRecordDetails());
        StyledButton exportButton = new StyledButton("Export History", ThemeIcons.EXPORT, e -> exportHistory());

        buttonsPanel.add(viewButton);
        buttonsPanel.add(exportButton);
        add(buttonsPanel, BorderLayout.SOUTH);

        // Load medical records
        loadMedicalRecords();
    }

    private void loadMedicalRecords() {
        List<MedicalRecord> records = mainFrame.getPharmacyService()
            .getMedicalRecordsForPatient(mainFrame.getCurrentUser().getId());
        recordsTable.setData(records);
    }

    private String getDoctorName(int doctorId) {
        return mainFrame.getPharmacyService().getDoctorById(doctorId).getName();
    }

    private void viewRecordDetails() {
        MedicalRecord selected = recordsTable.getSelectedItem();
        if (selected != null) {
            mainFrame.navigateTo("MEDICAL_RECORD_DETAILS", selected);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a record to view", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void exportHistory() {
        // TODO: Implement export functionality
        JOptionPane.showMessageDialog(this, 
            "Export functionality will be implemented soon", 
            "Coming Soon", 
            JOptionPane.INFORMATION_MESSAGE);
    }
} 