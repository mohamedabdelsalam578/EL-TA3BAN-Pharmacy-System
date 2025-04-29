package gui.pharmacist;

import gui.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.Prescription;
import models.PrescriptionStatus;
import models.Medicine;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class PrescriptionValidationPanel extends BasePanel {
    private StyledTable<Prescription> prescriptionTable;
    private JComboBox<PrescriptionStatus> statusFilter;
    private List<Prescription> prescriptions;

    public PrescriptionValidationPanel(MainFrame mainFrame) {
        super(mainFrame);
        initializeComponents();
        loadPrescriptions();
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusFilter = new JComboBox<>(PrescriptionStatus.values());
        statusFilter.insertItemAt(null, 0);
        statusFilter.setSelectedIndex(0);
        statusFilter.addActionListener(e -> filterPrescriptions());

        StyledButton refreshButton = new StyledButton("Refresh", ThemeIcons.REFRESH);
        refreshButton.addActionListener(e -> loadPrescriptions());
        
        filterPanel.add(new JLabel("Filter by Status: "));
        filterPanel.add(statusFilter);
        filterPanel.add(refreshButton);
        add(filterPanel, BorderLayout.NORTH);

        // Prescription Table
        String[] columns = {"ID", "Patient", "Doctor", "Date", "Status", "Medications"};
        prescriptionTable = new StyledTable<>(columns, prescription -> {
            Map<Medicine, Integer> meds = prescription.getMedicines();
            return new Object[]{
                prescription.getId(),
                mainFrame.getPharmacyService().getPatientName(prescription.getPatientId()),
                mainFrame.getPharmacyService().getDoctorName(prescription.getDoctorId()),
                prescription.getIssueDate(),
                prescription.getStatus(),
                meds != null ? meds.size() : 0
            };
        });
        
        JScrollPane scrollPane = new JScrollPane(prescriptionTable);
        add(scrollPane, BorderLayout.CENTER);

        // Action Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        StyledButton viewDetailsButton = new StyledButton("View Details", ThemeIcons.VIEW);
        StyledButton validateButton = new StyledButton("Validate", ThemeIcons.VALIDATE);
        StyledButton rejectButton = new StyledButton("Reject", ThemeIcons.REJECT);

        viewDetailsButton.addActionListener(e -> viewPrescriptionDetails());
        validateButton.addActionListener(e -> validatePrescription());
        rejectButton.addActionListener(e -> rejectPrescription());

        actionPanel.add(viewDetailsButton);
        actionPanel.add(validateButton);
        actionPanel.add(rejectButton);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void loadPrescriptions() {
        prescriptions = mainFrame.getPharmacyService().getPrescriptions();
        prescriptionTable.setData(prescriptions);
    }

    private void filterPrescriptions() {
        PrescriptionStatus selectedStatus = (PrescriptionStatus) statusFilter.getSelectedItem();
        List<Prescription> filtered = prescriptions.stream()
            .filter(prescription -> selectedStatus == null || prescription.getStatus() == selectedStatus)
            .toList();
        prescriptionTable.setData(filtered);
    }

    private void viewPrescriptionDetails() {
        Prescription selected = prescriptionTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Prescription Details\n");
        details.append("===================\n\n");
        details.append(String.format("ID: %d\n", selected.getId()));
        details.append(String.format("Patient: %s\n", 
            mainFrame.getPharmacyService().getPatientName(selected.getPatientId())));
        details.append(String.format("Doctor: %s\n", 
            mainFrame.getPharmacyService().getDoctorName(selected.getDoctorId())));
        details.append(String.format("Date: %s\n", selected.getIssueDate()));
        details.append(String.format("Status: %s\n\n", selected.getStatus()));
        details.append("Medications:\n");
        selected.getMedicines().forEach((medicine, quantity) ->
            details.append(String.format("- %s: %d units\n",
                medicine.getName(),
                quantity))
        );

        if (selected.getStatus() == PrescriptionStatus.REJECTED) {
            details.append(String.format("\nRejection Reason: %s", selected.getRejectionReason()));
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane,
            "Prescription Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void validatePrescription() {
        Prescription selected = prescriptionTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.getStatus() != PrescriptionStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "Only pending prescriptions can be validated",
                "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if all medications are available
        boolean canValidate = selected.getMedicines().entrySet().stream()
            .allMatch(entry -> entry.getKey().getStock() > 0);

        if (!canValidate) {
            JOptionPane.showMessageDialog(this, "Some medications are out of stock",
                "Cannot Validate", JOptionPane.ERROR_MESSAGE);
            return;
        }

        selected.setStatus(PrescriptionStatus.VALIDATED);
        mainFrame.getPharmacyService().saveDataToFiles();
        loadPrescriptions();
        
        JOptionPane.showMessageDialog(this, "Prescription has been validated",
            "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void rejectPrescription() {
        Prescription selected = prescriptionTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected.getStatus() != PrescriptionStatus.PENDING) {
            JOptionPane.showMessageDialog(this, "Only pending prescriptions can be rejected",
                "Invalid Operation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reason = JOptionPane.showInputDialog(this,
            "Please provide a reason for rejection:",
            "Reject Prescription",
            JOptionPane.QUESTION_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            selected.setStatus(PrescriptionStatus.REJECTED);
            selected.setRejectionReason(reason.trim());
            mainFrame.getPharmacyService().saveDataToFiles();
            loadPrescriptions();
            
            JOptionPane.showMessageDialog(this, "Prescription has been rejected",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
} 