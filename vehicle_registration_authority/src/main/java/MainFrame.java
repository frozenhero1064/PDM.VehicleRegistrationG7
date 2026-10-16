import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MainFrame extends JFrame {

    // Database connection settings - Update DB_PASS to match your MySQL password!
    private static final String DB_URL = "jdbc:mysql://localhost:3306/VehicleManagement";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "Duy16920()7"; 

    private DefaultTableModel vehicleTableModel;
    private JTable vehicleTable;

    public MainFrame() {
        setTitle("Vehicle Registration Authority - Group 07");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane mainTabs = new JTabbedPane();
        JPanel vehicleTab = createVehicleTab();
        mainTabs.addTab("Vehicles", vehicleTab);

        mainTabs.addTab("Owners", createEmptyTab("Owner Management UI"));
        mainTabs.addTab("Registrations", createEmptyTab("Registration UI"));
        mainTabs.addTab("Violations", createEmptyTab("Violations UI"));

        add(mainTabs);

        // Load vehicles from database on startup
        loadVehiclesFromDatabase();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private JPanel createVehicleTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton btAdd = new JButton("Add Vehicle");
        JButton btRemove = new JButton("Remove Vehicle");
        JButton btRefresh = new JButton("Refresh Data");

        buttonPanel.add(btAdd);
        buttonPanel.add(btRemove);
        buttonPanel.add(btRefresh);

        String[] columns = {"Plate_Num", "Chassis_Num", "Engine_Num", "Brand", "Model", "YOM", "Color", "Type"};
        vehicleTableModel = new DefaultTableModel(columns, 0);
        vehicleTable = new JTable(vehicleTableModel);
        JScrollPane tableScroll = new JScrollPane(vehicleTable);

        // Button Actions
        btAdd.addActionListener(e -> showAddVehicleDialog());
        btRemove.addActionListener(e -> removeSelectedVehicle());
        btRefresh.addActionListener(e -> loadVehiclesFromDatabase());

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    private void loadVehiclesFromDatabase() {
        vehicleTableModel.setRowCount(0); // Clear current table rows
        String query = "SELECT Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type FROM VEHICLE";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Object[] row = {
                    rs.getString("Plate_Num"),
                    rs.getString("Chassis_Num"),
                    rs.getString("Engine_Num"),
                    rs.getString("Brand"),
                    rs.getString("Model"),
                    rs.getInt("YOM"),
                    rs.getString("Color"),
                    rs.getString("Type")
                };
                vehicleTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            // If MySQL isn't connected yet, show a helpful message instead of crashing
            System.out.println("Database connection note: " + ex.getMessage());
        }
    }

    private void showAddVehicleDialog() {
        JTextField tfPlate = new JTextField();
        JTextField tfChassis = new JTextField();
        JTextField tfEngine = new JTextField();
        JTextField tfBrand = new JTextField();
        JTextField tfModel = new JTextField();
        JTextField tfYom = new JTextField();
        JTextField tfColor = new JTextField();
        JTextField tfType = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Plate Number (*):"));
        form.add(tfPlate);
        form.add(new JLabel("Chassis Number:"));
        form.add(tfChassis);
        form.add(new JLabel("Engine Number:"));
        form.add(tfEngine);
        form.add(new JLabel("Brand:"));
        form.add(tfBrand);
        form.add(new JLabel("Model:"));
        form.add(tfModel);
        form.add(new JLabel("Year of Manufacture (YOM):"));
        form.add(tfYom);
        form.add(new JLabel("Color:"));
        form.add(tfColor);
        form.add(new JLabel("Type (e.g., Car, Truck):"));
        form.add(tfType);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String plate = tfPlate.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Plate Number is required!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int yom = 0;
            try {
                yom = Integer.parseInt(tfYom.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "YOM must be a valid year (number)!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String insertSQL = "INSERT INTO VEHICLE (Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

                pstmt.setString(1, plate);
                pstmt.setString(2, tfChassis.getText().trim());
                pstmt.setString(3, tfEngine.getText().trim());
                pstmt.setString(4, tfBrand.getText().trim());
                pstmt.setString(5, tfModel.getText().trim());
                pstmt.setInt(6, yom);
                pstmt.setString(7, tfColor.getText().trim());
                pstmt.setString(8, tfType.getText().trim());

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Vehicle saved to database successfully!");
                loadVehiclesFromDatabase(); // Refresh table

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saving to database:\n" + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelectedVehicle() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle row in the table to remove.");
            return;
        }

        String plateNum = vehicleTableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete vehicle '" + plateNum + "' from the database?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            String deleteSQL = "DELETE FROM VEHICLE WHERE Plate_Num = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

                pstmt.setString(1, plateNum);
                pstmt.executeUpdate();
                loadVehiclesFromDatabase(); // Refresh table

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting vehicle:\n" + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createEmptyTab(String text) {
        JPanel panel = new JPanel();
        JLabel label = new JLabel(text);
        panel.add(label);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}