package com.group07.vra.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Vehicle Registration Authority - Group 07");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane mainTabs = new JTabbedPane();
        JPanel vehicleTab = createVehicleTab();
        mainTabs.addTab("Vehicles", vehicleTab);

        mainTabs.addTab("Owners", createEmptyTab("Owner Management UI"));
        mainTabs.addTab("Registrations", createEmptyTab("Registration UI"));
        mainTabs.addTab("Violations", createEmptyTab("Violations UI"));

        add(mainTabs);
    }

    private JPanel createVehicleTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton btAdd = new JButton("Add Vehicle");
        JButton btRemove = new JButton("Remove Vehicle");
        
        buttonPanel.add(btAdd);
        buttonPanel.add(btRemove);

        String[] columns = {"Plate_Num", "Chassis_Num", "Engine_Num", "Brand", "Model", "YOM", "Type"};
        
        //test how it look
        String[][] fakeData = {
            {"11A-2", "CH001", "EN001", "Yamaha", "abc", "2026", "Truck"},
            {"22B-3", "CH002", "EN002", "Honda", "xyz", "2025", "Car"}
        };
        
        JTable table = new JTable(fakeData, columns);
        JScrollPane tableScroll = new JScrollPane(table);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEmptyTab(String text) {
        JPanel panel = new JPanel(); 
        JLabel label = new JLabel(text);
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        MainFrame app = new MainFrame();
        app.setVisible(true);
    }
}