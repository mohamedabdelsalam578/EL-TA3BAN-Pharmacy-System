package gui.dashboard;

import gui.components.BasePanel;
import gui.MainFrame;
import gui.components.StyledButton;
import gui.components.StyledTable;
import gui.theme.ThemeColors;
import gui.theme.ThemeIcons;
import models.Doctor;
import models.Consultation;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class BookConsultationPanel extends BasePanel {
    private StyledTable<Doctor> doctorsTable;
    private JComboBox<String> timeSlotCombo;
    private JTextArea reasonArea;
    
    public BookConsultationPanel(MainFrame mainFrame) {
        super(mainFrame);
    }

    @Override
    protected void initializeComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(ThemeColors.BACKGROUND);

        // Create doctors table
        String[] columns = {"Name", "Specialization", "Rating", "Available Slots"};
        doctorsTable = new StyledTable<>(columns, doctor -> new Object[]{
            doctor.getName(),
            doctor.getSpecialization(),
            doctor.getRating(),
            getAvailableSlots(doctor)
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(doctorsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create booking panel
        JPanel bookingPanel = new JPanel(new GridBagLayout());
        bookingPanel.setBackground(ThemeColors.BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Time slot selection
        gbc.gridx = 0; gbc.gridy = 0;
        bookingPanel.add(new JLabel("Select Time:"), gbc);

        gbc.gridx = 1;
        timeSlotCombo = new JComboBox<>(new String[]{"9:00 AM", "10:00 AM", "11:00 AM", "2:00 PM", "3:00 PM", "4:00 PM"});
        bookingPanel.add(timeSlotCombo, gbc);

        // Reason for consultation
        gbc.gridx = 0; gbc.gridy = 1;
        bookingPanel.add(new JLabel("Reason:"), gbc);

        gbc.gridx = 1;
        reasonArea = new JTextArea(3, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        bookingPanel.add(reasonScroll, gbc);

        // Book button
        gbc.gridx = 1; gbc.gridy = 2;
        StyledButton bookButton = new StyledButton("Book Consultation", ThemeIcons.CALENDAR, e -> bookConsultation());
        bookingPanel.add(bookButton, gbc);

        add(bookingPanel, BorderLayout.SOUTH);

        // Load doctors
        loadDoctors();
    }

    private void loadDoctors() {
        List<Doctor> doctors = mainFrame.getPharmacyService().getAvailableDoctors();
        doctorsTable.setData(doctors);
    }

    private String getAvailableSlots(Doctor doctor) {
        return String.join(", ", doctor.getAvailableTimeSlots());
    }

    private void bookConsultation() {
        Doctor selected = doctorsTable.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select a doctor", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String timeSlot = (String) timeSlotCombo.getSelectedItem();
        String reason = reasonArea.getText().trim();

        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please provide a reason for the consultation", 
                "Missing Information", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Consultation consultation = new Consultation(
                mainFrame.getCurrentUser().getId(),
                selected.getId(),
                LocalDateTime.now(),
                reason
            );

            mainFrame.getPharmacyService().bookConsultation(consultation);
            
            JOptionPane.showMessageDialog(this, 
                "Consultation booked successfully", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
                
            // Clear form
            reasonArea.setText("");
            timeSlotCombo.setSelectedIndex(0);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to book consultation: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 