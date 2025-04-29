package gui;

import javax.swing.JPanel;

public abstract class BasePanel extends JPanel {
    protected MainFrame mainFrame;

    public BasePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        initializeComponents();
    }

    protected abstract void initializeComponents();
} 