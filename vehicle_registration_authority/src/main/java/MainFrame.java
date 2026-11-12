import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    public static class DatabaseDAO {
        private static final String DB_URL = "jdbc:mysql://localhost:3306/VehicleManagement";
        private static final String DB_USER = "root";
        private static final String DB_PASS = "1234";

        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        }

        public int getNextIntId(String table, String pkCol) {
            String sql = "SELECT COALESCE(MAX(" + pkCol + "), 0) + 1 FROM " + table;
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            } catch (SQLException ignored) {}
            return 16;
        }

        public List<String> getSingleColumnList(String sql) {
            List<String> list = new ArrayList<>();
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            } catch (SQLException ignored) {}
            return list;
        }

        public List<Object[]> getVehicles(String keyword) throws SQLException {
            List<Object[]> rows = new ArrayList<>();
            String sql = "SELECT Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type, Previous_Plate_Num FROM VEHICLE";
            boolean filter = keyword != null && !keyword.trim().isEmpty();
            if (filter) {
                sql += " WHERE Plate_Num LIKE ? OR Brand LIKE ? OR Model LIKE ? OR Chassis_Num LIKE ? OR Engine_Num LIKE ?";
            }
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                if (filter) {
                    String pat = "%" + keyword.trim() + "%";
                    for (int i = 1; i <= 5; i++) ps.setString(i, pat);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        rows.add(new Object[]{
                                rs.getString("Plate_Num"),
                                rs.getString("Chassis_Num"),
                                rs.getString("Engine_Num"),
                                rs.getString("Brand"),
                                rs.getString("Model"),
                                rs.getInt("YOM"),
                                rs.getString("Color"),
                                rs.getString("Type"),
                                rs.getString("Previous_Plate_Num")
                        });
                    }
                }
            }
            return rows;
        }

        public void insertVehicle(String plate, String chassis, String engine, String brand,
                                  String model, int yom, String color, String type, String prevPlate) throws SQLException {
            String sql = "INSERT INTO VEHICLE (Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type, Previous_Plate_Num) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, plate);
                ps.setString(2, chassis);
                ps.setString(3, engine);
                ps.setString(4, brand);
                ps.setString(5, model);
                ps.setInt(6, yom);
                ps.setString(7, color);
                ps.setString(8, type);
                if (prevPlate == null || prevPlate.isEmpty()) ps.setNull(9, Types.VARCHAR);
                else ps.setString(9, prevPlate);
                ps.executeUpdate();
            }
        }

        public void updateVehicle(String plate, String chassis, String engine, String brand,
                                  String model, int yom, String color, String type, String prevPlate) throws SQLException {
            String sql = "UPDATE VEHICLE SET Chassis_Num=?, Engine_Num=?, Brand=?, Model=?, YOM=?, Color=?, Type=?, Previous_Plate_Num=? WHERE Plate_Num=?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, chassis);
                ps.setString(2, engine);
                ps.setString(3, brand);
                ps.setString(4, model);
                ps.setInt(5, yom);
                ps.setString(6, color);
                ps.setString(7, type);
                if (prevPlate == null || prevPlate.isEmpty()) ps.setNull(8, Types.VARCHAR);
                else ps.setString(8, prevPlate);
                ps.setString(9, plate);
                ps.executeUpdate();
            }
        }

        public void deleteVehicleCascade(String plateNum) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = conn.prepareStatement("UPDATE VEHICLE SET Previous_Plate_Num = NULL WHERE Previous_Plate_Num = ?")) {
                        ps.setString(1, plateNum);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM FINE WHERE Violate_ID IN (SELECT Violate_ID FROM TRAFFICVIOLATION WHERE Plate_Num = ?)")) {
                        ps.setString(1, plateNum);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TRAFFICVIOLATION WHERE Plate_Num = ?")) {
                        ps.setString(1, plateNum);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM INSPECTION WHERE Plate_Num = ?")) {
                        ps.setString(1, plateNum);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM INSURANCEPOLICY WHERE Plate_Num = ?")) {
                        ps.setString(1, plateNum);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM REGISTRATION WHERE Plate_Num = ?")) {
                        ps.setString(1, plateNum);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM VEHICLE WHERE Plate_Num = ?")) {
                        ps.setString(1, plateNum);
                        ps.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        public List<Object[]> getOwners() throws SQLException {
            List<Object[]> rows = new ArrayList<>();
            String sql = "SELECT o.Owner_ID, o.Street, o.District, o.City, " +
                    "i.Indentity_Card, i.Fname, i.Mname, i.Lname, i.DoB, i.Gender, " +
                    "org.Tax_Code, org.Company_Name, org.Represent_Name " +
                    "FROM Owner o " +
                    "LEFT JOIN Individual i ON o.Owner_ID = i.Owner_ID " +
                    "LEFT JOIN Organisation org ON o.Owner_ID = org.Owner_ID " +
                    "ORDER BY o.Owner_ID";
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String idCard = rs.getString("Indentity_Card");
                    String taxCode = rs.getString("Tax_Code");
                    String category = (idCard != null) ? "Individual" : (taxCode != null ? "Organisation" : "Owner");
                    String identifier = (idCard != null) ? idCard : (taxCode != null ? taxCode : "");
                    String nameDetails;
                    if (idCard != null) {
                        String fn = rs.getString("Fname") != null ? rs.getString("Fname") : "";
                        String mn = rs.getString("Mname") != null ? rs.getString("Mname") : "";
                        String ln = rs.getString("Lname") != null ? rs.getString("Lname") : "";
                        nameDetails = (fn + " " + mn + " " + ln).trim().replaceAll("\\s+", " ");
                    } else {
                        String comp = rs.getString("Company_Name") != null ? rs.getString("Company_Name") : "";
                        String rep = rs.getString("Represent_Name") != null ? " (Rep: " + rs.getString("Represent_Name") + ")" : "";
                        nameDetails = comp + rep;
                    }
                    rows.add(new Object[]{
                            rs.getInt("Owner_ID"),
                            category,
                            identifier,
                            nameDetails,
                            rs.getString("Street"),
                            rs.getString("District"),
                            rs.getString("City")
                    });
                }
            }
            return rows;
        }

        public void insertIndividualOwner(int ownerId, String street, String district, String city,
                                          String idCard, String fname, String mname, String lname,
                                          String dob, String gender, String phone, String email) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Owner (Owner_ID, Street, District, City) VALUES (?, ?, ?, ?)")) {
                        ps.setInt(1, ownerId);
                        ps.setString(2, street);
                        ps.setString(3, district);
                        ps.setString(4, city);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Individual (Owner_ID, Fname, Mname, Lname, DoB, Gender, Indentity_Card) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, ownerId);
                        ps.setString(2, fname.isEmpty() ? "N/A" : fname);
                        ps.setString(3, mname.isEmpty() ? "" : mname);
                        ps.setString(4, lname.isEmpty() ? "N/A" : lname);
                        ps.setDate(5, Date.valueOf(dob));
                        ps.setString(6, gender);
                        ps.setString(7, idCard);
                        ps.executeUpdate();
                    }
                    upsertOwnerContacts(conn, ownerId, phone, email);
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        public void insertOrganisationOwner(int ownerId, String street, String district, String city,
                                            String taxCode, String companyName, String repName,
                                            String phone, String email) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Owner (Owner_ID, Street, District, City) VALUES (?, ?, ?, ?)")) {
                        ps.setInt(1, ownerId);
                        ps.setString(2, street);
                        ps.setString(3, district);
                        ps.setString(4, city);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO Organisation (Owner_ID, Company_Name, Tax_Code, Represent_Name) VALUES (?, ?, ?, ?)")) {
                        ps.setInt(1, ownerId);
                        ps.setString(2, companyName);
                        ps.setString(3, taxCode);
                        ps.setString(4, repName);
                        ps.executeUpdate();
                    }
                    upsertOwnerContacts(conn, ownerId, phone, email);
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        public void deleteOwnerCascade(int ownerId) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    String[] childTables = {"Owner_phone", "Owner_email", "Ownership", "Individual", "Organisation", "Owner"};
                    for (String tbl : childTables) {
                        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tbl + " WHERE Owner_ID = ?")) {
                            ps.setInt(1, ownerId);
                            ps.executeUpdate();
                        }
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        private void upsertOwnerContacts(Connection conn, int ownerId, String phone, String email) throws SQLException {
            if (phone != null && !phone.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Owner_phone (Owner_ID, Phone) VALUES (?, ?) ON DUPLICATE KEY UPDATE Phone = VALUES(Phone)")) {
                    ps.setInt(1, ownerId);
                    ps.setString(2, phone);
                    ps.executeUpdate();
                }
            }
            if (email != null && !email.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Owner_email (Owner_ID, email) VALUES (?, ?) ON DUPLICATE KEY UPDATE email = VALUES(email)")) {
                    ps.setInt(1, ownerId);
                    ps.setString(2, email);
                    ps.executeUpdate();
                }
            }
        }

        public List<String> getOwnerContacts(String table, String col, int ownerId) throws SQLException {
            List<String> list = new ArrayList<>();
            String sql = "SELECT " + col + " FROM " + table + " WHERE Owner_ID = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, ownerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(rs.getString(1));
                }
            }
            return list;
        }

        public void saveOwnerContacts(int ownerId, String phone, String email) throws SQLException {
            try (Connection conn = getConnection()) {
                upsertOwnerContacts(conn, ownerId, phone, email);
            }
        }

        public void deleteViolationCascade(int violateId) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM FINE WHERE Violate_ID = ?")) {
                        ps.setInt(1, violateId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM TRAFFICVIOLATION WHERE Violate_ID = ?")) {
                        ps.setInt(1, violateId);
                        ps.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        public void callRegisterNewVehicle(String plate, String chassis, String engine, String brand,
                                           String model, int yom, String color, String type,
                                           int ownerId, int regId, String policyNum) throws SQLException {
            String sql = "{CALL sp_RegisterNewVehicle(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            try (Connection conn = getConnection();
                 CallableStatement cs = conn.prepareCall(sql)) {
                cs.setString(1, plate);
                cs.setString(2, chassis);
                cs.setString(3, engine);
                cs.setString(4, brand);
                cs.setString(5, model);
                cs.setInt(6, yom);
                cs.setString(7, color);
                cs.setString(8, type);
                cs.setInt(9, ownerId);
                cs.setInt(10, regId);
                cs.setString(11, policyNum);
                cs.execute();
            }
        }

        public void callPayFine(int fineNum, int violateId) throws SQLException {
            String sql = "{CALL sp_PayFine(?, ?)}";
            try (Connection conn = getConnection();
                 CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, fineNum);
                cs.setInt(2, violateId);
                cs.execute();
            }
        }

        public void callTransferOwnership(String oldPlate, String newPlate, String newChassis,
                                          String newEngine, int newOwnerId, int newOwnershipId) throws SQLException {
            String sql = "{CALL sp_TransferVehicleOwnership(?, ?, ?, ?, ?, ?)}";
            try (Connection conn = getConnection();
                 CallableStatement cs = conn.prepareCall(sql)) {
                cs.setString(1, oldPlate);
                cs.setString(2, newPlate);
                cs.setString(3, newChassis);
                cs.setString(4, newEngine);
                cs.setInt(5, newOwnerId);
                cs.setInt(6, newOwnershipId);
                cs.execute();
            }
        }

        public List<Object[]> queryTable(String sql, int colCount) throws SQLException {
            List<Object[]> rows = new ArrayList<>();
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Object[] r = new Object[colCount];
                    for (int i = 0; i < colCount; i++) r[i] = rs.getObject(i + 1);
                    rows.add(r);
                }
            }
            return rows;
        }

        public void executeUpdate(String sql, Object... params) throws SQLException {
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                ps.executeUpdate();
            }
        }
    }

    private final DatabaseDAO dao = new DatabaseDAO();

    private DefaultTableModel vehicleTableModel;
    private JTable vehicleTable;
    private JTextField tfVehicleSearch;

    private DefaultTableModel ownerTableModel;
    private JTable ownerTable;

    private DefaultTableModel ownershipTableModel;
    private JTable ownershipTable;

    private DefaultTableModel registrationTableModel;
    private JTable registrationTable;

    private DefaultTableModel insuranceTableModel;
    private JTable insuranceTable;

    private DefaultTableModel inspectionTableModel;
    private JTable inspectionTable;

    private DefaultTableModel violationTableModel;
    private JTable violationTable;

    private DefaultTableModel fineTableModel;
    private JTable fineTable;

    public MainFrame() {
        setTitle("Vehicle Registration Authority - Group 07 (Synced with database.sql)");
        setSize(1120, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane mainTabs = new JTabbedPane();
        mainTabs.addTab("Vehicles", createVehicleTab());
        mainTabs.addTab("Owners (Individual / Org)", createOwnerTab());
        mainTabs.addTab("Ownership History", createOwnershipTab());
        mainTabs.addTab("Registrations", createRegistrationTab());
        mainTabs.addTab("Insurance Policies", createInsuranceTab());
        mainTabs.addTab("Inspections", createInspectionTab());
        mainTabs.addTab("Violations & Fines", createViolationAndFineTab());

        add(mainTabs);
        refreshAllTabs();
    }

    private void refreshAllTabs() {
        loadVehiclesFromDatabase("");
        loadOwnersFromDatabase();
        loadOwnershipsFromDatabase();
        loadRegistrationsFromDatabase();
        loadInsuranceFromDatabase();
        loadInspectionsFromDatabase();
        loadViolationsFromDatabase();
        loadFinesFromDatabase();
    }

    private JComboBox<String> createPlateComboBox() {
        List<String> plates = dao.getSingleColumnList("SELECT Plate_Num FROM VEHICLE ORDER BY Plate_Num");
        JComboBox<String> cb = new JComboBox<>(plates.toArray(new String[0]));
        cb.setEditable(true);
        return cb;
    }

    private JComboBox<String> createOwnerComboBox() {
        List<String> owners = dao.getSingleColumnList("SELECT Owner_ID FROM Owner ORDER BY Owner_ID");
        JComboBox<String> cb = new JComboBox<>(owners.toArray(new String[0]));
        cb.setEditable(true);
        return cb;
    }

    private JPanel createVehicleTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfVehicleSearch = new JTextField(14);
        JButton btSearch = new JButton("Search");
        JButton btAdd = new JButton("Add Vehicle");
        JButton btFullReg = new JButton("Full Register (SP1)");
        JButton btEdit = new JButton("Edit Selected");
        JButton btRemove = new JButton("Remove Vehicle");
        JButton btRefresh = new JButton("Refresh");

        topBar.add(new JLabel("Filter:"));
        topBar.add(tfVehicleSearch);
        topBar.add(btSearch);
        topBar.add(btAdd);
        topBar.add(btFullReg);
        topBar.add(btEdit);
        topBar.add(btRemove);
        topBar.add(btRefresh);

        String[] columns = {"Plate_Num", "Chassis_Num", "Engine_Num", "Brand", "Model", "YOM", "Color", "Type", "Previous_Plate_Num"};
        vehicleTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        vehicleTable = new JTable(vehicleTableModel);

        btSearch.addActionListener(e -> loadVehiclesFromDatabase(tfVehicleSearch.getText()));
        btAdd.addActionListener(e -> showVehicleDialog(false));
        btFullReg.addActionListener(e -> showFullRegisterSPDialog());
        btEdit.addActionListener(e -> showVehicleDialog(true));
        btRemove.addActionListener(e -> {
            int row = vehicleTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a vehicle row to remove.");
                return;
            }
            String plate = strVal(vehicleTableModel, row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete vehicle '" + plate + "' and its linked registrations/inspections/policies/violations?",
                    "Confirm Cascade Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dao.deleteVehicleCascade(plate);
                    refreshAllTabs();
                } catch (SQLException ex) { showDbError(ex); }
            }
        });
        btRefresh.addActionListener(e -> {
            tfVehicleSearch.setText("");
            refreshAllTabs();
        });

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(vehicleTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadVehiclesFromDatabase(String keyword) {
        vehicleTableModel.setRowCount(0);
        try {
            for (Object[] row : dao.getVehicles(keyword)) {
                vehicleTableModel.addRow(row);
            }
        } catch (SQLException ex) {
            System.out.println("Vehicle load note: " + ex.getMessage());
        }
    }

    private void showVehicleDialog(boolean isEdit) {
        int row = vehicleTable.getSelectedRow();
        if (isEdit && row == -1) {
            JOptionPane.showMessageDialog(this, "Select a vehicle row to edit.");
            return;
        }

        JTextField tfPlate = new JTextField(isEdit ? strVal(vehicleTableModel, row, 0) : "51K-88899");
        tfPlate.setEditable(!isEdit);
        JTextField tfChassis = new JTextField(isEdit ? strVal(vehicleTableModel, row, 1) : "CHS016ENG16");
        JTextField tfEngine = new JTextField(isEdit ? strVal(vehicleTableModel, row, 2) : "ENG100016");
        JTextField tfBrand = new JTextField(isEdit ? strVal(vehicleTableModel, row, 3) : "Toyota");
        JTextField tfModel = new JTextField(isEdit ? strVal(vehicleTableModel, row, 4) : "Corolla Cross");
        JTextField tfYom = new JTextField(isEdit ? strVal(vehicleTableModel, row, 5) : "2026");
        JTextField tfColor = new JTextField(isEdit ? strVal(vehicleTableModel, row, 6) : "White");
        JTextField tfType = new JTextField(isEdit ? strVal(vehicleTableModel, row, 7) : "SUV");
        JTextField tfPrevPlate = new JTextField(isEdit ? strVal(vehicleTableModel, row, 8) : "");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Plate Number (*):")); form.add(tfPlate);
        form.add(new JLabel("Chassis Number (* Unique):")); form.add(tfChassis);
        form.add(new JLabel("Engine Number (* Unique):")); form.add(tfEngine);
        form.add(new JLabel("Brand:")); form.add(tfBrand);
        form.add(new JLabel("Model:")); form.add(tfModel);
        form.add(new JLabel("Year of Manufacture (YOM) (*):")); form.add(tfYom);
        form.add(new JLabel("Color:")); form.add(tfColor);
        form.add(new JLabel("Type:")); form.add(tfType);
        form.add(new JLabel("Previous_Plate_Num (Blank or existing plate):")); form.add(tfPrevPlate);

        if (JOptionPane.showConfirmDialog(this, form, isEdit ? "Edit Vehicle" : "Add New Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                int yom = Integer.parseInt(tfYom.getText().trim());
                if (isEdit) {
                    dao.updateVehicle(tfPlate.getText().trim(), tfChassis.getText().trim(), tfEngine.getText().trim(),
                            tfBrand.getText().trim(), tfModel.getText().trim(), yom, tfColor.getText().trim(),
                            tfType.getText().trim(), tfPrevPlate.getText().trim());
                } else {
                    dao.insertVehicle(tfPlate.getText().trim(), tfChassis.getText().trim(), tfEngine.getText().trim(),
                            tfBrand.getText().trim(), tfModel.getText().trim(), yom, tfColor.getText().trim(),
                            tfType.getText().trim(), tfPrevPlate.getText().trim());
                }
                refreshAllTabs();
            } catch (Exception ex) { showDbError(ex); }
        }
    }

    private void showFullRegisterSPDialog() {
        int nextRegId = dao.getNextIntId("REGISTRATION", "Registration_ID");
        JTextField tfPlate = new JTextField("51G-" + (10000 + nextRegId));
        JTextField tfChassis = new JTextField("CHS0" + nextRegId + "ENG0" + nextRegId);
        JTextField tfEngine = new JTextField("ENG1000" + nextRegId);
        JTextField tfBrand = new JTextField("Honda");
        JTextField tfModel = new JTextField("CR-V");
        JTextField tfYom = new JTextField("2026");
        JTextField tfColor = new JTextField("Black");
        JTextField tfType = new JTextField("SUV");
        JComboBox<String> cbOwner = createOwnerComboBox();
        JTextField tfRegId = new JTextField(String.valueOf(nextRegId));
        JTextField tfPolicyNum = new JTextField(String.format("POL%03d", nextRegId));

        JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
        f.add(new JLabel("Plate Number (*):")); f.add(tfPlate);
        f.add(new JLabel("Chassis Number (*):")); f.add(tfChassis);
        f.add(new JLabel("Engine Number (*):")); f.add(tfEngine);
        f.add(new JLabel("Brand:")); f.add(tfBrand);
        f.add(new JLabel("Model:")); f.add(tfModel);
        f.add(new JLabel("YOM (*):")); f.add(tfYom);
        f.add(new JLabel("Color:")); f.add(tfColor);
        f.add(new JLabel("Type:")); f.add(tfType);
        f.add(new JLabel("Select Existing Owner_ID (*):")); f.add(cbOwner);
        f.add(new JLabel("New Registration_ID (*):")); f.add(tfRegId);
        f.add(new JLabel("New Policy_Num (*):")); f.add(tfPolicyNum);

        if (JOptionPane.showConfirmDialog(this, f, "Transaction Script 1: sp_RegisterNewVehicle",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int ownerId = Integer.parseInt(cbOwner.getSelectedItem().toString().trim());
                dao.callRegisterNewVehicle(
                        tfPlate.getText().trim(), tfChassis.getText().trim(), tfEngine.getText().trim(),
                        tfBrand.getText().trim(), tfModel.getText().trim(), Integer.parseInt(tfYom.getText().trim()),
                        tfColor.getText().trim(), tfType.getText().trim(), ownerId,
                        Integer.parseInt(tfRegId.getText().trim()), tfPolicyNum.getText().trim()
                );
                refreshAllTabs();
                JOptionPane.showMessageDialog(this, "sp_RegisterNewVehicle executed successfully!");
            } catch (Exception ex) { showDbError(ex); }
        }
    }

    private JPanel createOwnerTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btAddInd = new JButton("Add Individual Owner");
        JButton btAddOrg = new JButton("Add Organisation Owner");
        JButton btContacts = new JButton("View / Set Phone & Email");
        JButton btRemove = new JButton("Delete Owner");
        JButton btRefresh = new JButton("Refresh");

        topBar.add(btAddInd);
        topBar.add(btAddOrg);
        topBar.add(btContacts);
        topBar.add(btRemove);
        topBar.add(btRefresh);

        String[] cols = {"Owner_ID", "Category", "Indentity_Card / Tax_Code", "Full Name / Company", "Street", "District", "City"};
        ownerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        ownerTable = new JTable(ownerTableModel);

        btAddInd.addActionListener(e -> showAddIndividualDialog());
        btAddOrg.addActionListener(e -> showAddOrganisationDialog());
        btContacts.addActionListener(e -> showOwnerContactsDialog());
        btRemove.addActionListener(e -> {
            int row = ownerTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an owner row to delete.");
                return;
            }
            int ownerId = Integer.parseInt(strVal(ownerTableModel, row, 0));
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete Owner_ID " + ownerId + " and their subtype/contact/ownership records?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dao.deleteOwnerCascade(ownerId);
                    refreshAllTabs();
                } catch (SQLException ex) { showDbError(ex); }
            }
        });
        btRefresh.addActionListener(e -> loadOwnersFromDatabase());

        panel.add(topBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(ownerTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadOwnersFromDatabase() {
        ownerTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.getOwners()) ownerTableModel.addRow(r);
        } catch (SQLException ex) {
            System.out.println("Owner load note: " + ex.getMessage());
        }
    }

    private void showAddIndividualDialog() {
        int nextId = dao.getNextIntId("Owner", "Owner_ID");
        JTextField tfOwnerId = new JTextField(String.valueOf(nextId));
        JTextField tfIdCard = new JTextField("07920000" + (1000 + nextId));
        JTextField tfFname = new JTextField("Nguyen");
        JTextField tfMname = new JTextField("Van");
        JTextField tfLname = new JTextField("Khoa");
        JTextField tfDob = new JTextField("2000-01-15");
        JTextField tfGender = new JTextField("Male");
        JTextField tfStreet = new JTextField("102 Nguyen Hue");
        JTextField tfDistrict = new JTextField("District 1");
        JTextField tfCity = new JTextField("Ho Chi Minh City");
        JTextField tfPhone = new JTextField("09091234" + nextId);
        JTextField tfEmail = new JTextField("owner" + nextId + "@gmail.com");

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Owner_ID (Int *):")); form.add(tfOwnerId);
        form.add(new JLabel("Indentity_Card (* Unique):")); form.add(tfIdCard);
        form.add(new JLabel("First Name (*):")); form.add(tfFname);
        form.add(new JLabel("Middle Name (*):")); form.add(tfMname);
        form.add(new JLabel("Last Name (*):")); form.add(tfLname);
        form.add(new JLabel("DoB (YYYY-MM-DD):")); form.add(tfDob);
        form.add(new JLabel("Gender:")); form.add(tfGender);
        form.add(new JLabel("Street:")); form.add(tfStreet);
        form.add(new JLabel("District:")); form.add(tfDistrict);
        form.add(new JLabel("City:")); form.add(tfCity);
        form.add(new JLabel("Phone (Optional):")); form.add(tfPhone);
        form.add(new JLabel("Email (Optional):")); form.add(tfEmail);

        if (JOptionPane.showConfirmDialog(this, form, "Add Individual Owner", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                dao.insertIndividualOwner(
                        Integer.parseInt(tfOwnerId.getText().trim()), tfStreet.getText().trim(), tfDistrict.getText().trim(), tfCity.getText().trim(),
                        tfIdCard.getText().trim(), tfFname.getText().trim(), tfMname.getText().trim(), tfLname.getText().trim(),
                        tfDob.getText().trim(), tfGender.getText().trim(), tfPhone.getText().trim(), tfEmail.getText().trim()
                );
                loadOwnersFromDatabase();
            } catch (Exception ex) { showDbError(ex); }
        }
    }

    private void showAddOrganisationDialog() {
        int nextId = dao.getNextIntId("Owner", "Owner_ID");
        JTextField tfOwnerId = new JTextField(String.valueOf(nextId));
        JTextField tfTax = new JTextField("0300999" + (100 + nextId));
        JTextField tfComp = new JTextField("Saigon Logistics Co., Ltd.");
        JTextField tfRep = new JTextField("Tran Minh Quan");
        JTextField tfStreet = new JTextField("25 Le Duan");
        JTextField tfDistrict = new JTextField("District 1");
        JTextField tfCity = new JTextField("Ho Chi Minh City");
        JTextField tfPhone = new JTextField("028388899" + nextId);
        JTextField tfEmail = new JTextField("contact" + nextId + "@company.vn");

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Owner_ID (Int *):")); form.add(tfOwnerId);
        form.add(new JLabel("Tax Code (* Unique):")); form.add(tfTax);
        form.add(new JLabel("Company Name (*):")); form.add(tfComp);
        form.add(new JLabel("Representative Name:")); form.add(tfRep);
        form.add(new JLabel("Street:")); form.add(tfStreet);
        form.add(new JLabel("District:")); form.add(tfDistrict);
        form.add(new JLabel("City:")); form.add(tfCity);
        form.add(new JLabel("Phone (Optional):")); form.add(tfPhone);
        form.add(new JLabel("Email (Optional):")); form.add(tfEmail);

        if (JOptionPane.showConfirmDialog(this, form, "Add Organisation Owner", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                dao.insertOrganisationOwner(
                        Integer.parseInt(tfOwnerId.getText().trim()), tfStreet.getText().trim(), tfDistrict.getText().trim(), tfCity.getText().trim(),
                        tfTax.getText().trim(), tfComp.getText().trim(), tfRep.getText().trim(),
                        tfPhone.getText().trim(), tfEmail.getText().trim()
                );
                loadOwnersFromDatabase();
            } catch (Exception ex) { showDbError(ex); }
        }
    }

    private void showOwnerContactsDialog() {
        int row = ownerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an owner row first.");
            return;
        }
        int ownerId = Integer.parseInt(strVal(ownerTableModel, row, 0));
        try {
            List<String> phones = dao.getOwnerContacts("Owner_phone", "Phone", ownerId);
            List<String> emails = dao.getOwnerContacts("Owner_email", "email", ownerId);

            JTextField tfNewPhone = new JTextField(phones.isEmpty() ? "" : phones.get(0));
            JTextField tfNewEmail = new JTextField(emails.isEmpty() ? "" : emails.get(0));

            JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
            panel.add(new JLabel("Owner_ID: " + ownerId));
            panel.add(new JLabel("Current Phone: " + (phones.isEmpty() ? "None" : String.join(", ", phones))));
            panel.add(new JLabel("Current Email: " + (emails.isEmpty() ? "None" : String.join(", ", emails))));
            panel.add(new JLabel("Set / Update Phone Number:"));
            panel.add(tfNewPhone);
            panel.add(new JLabel("Set / Update Email Address:"));
            panel.add(tfNewEmail);

            if (JOptionPane.showConfirmDialog(this, panel, "Owner Contacts", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                dao.saveOwnerContacts(ownerId, tfNewPhone.getText().trim(), tfNewEmail.getText().trim());
                JOptionPane.showMessageDialog(this, "Contact details saved!");
            }
        } catch (SQLException ex) { showDbError(ex); }
    }

    private JPanel createOwnershipTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAdd = new JButton("Add Ownership Record");
        JButton btTransferSP = new JButton("Transfer Ownership (SP3)");
        JButton btRemove = new JButton("Delete Record");
        JButton btRefresh = new JButton("Refresh");
        top.add(btAdd); top.add(btTransferSP); top.add(btRemove); top.add(btRefresh);

        String[] cols = {"Ownership_ID", "Owner_ID", "Start_Date", "End_Date", "Transfer_Type"};
        ownershipTableModel = new DefaultTableModel(cols, 0);
        ownershipTable = new JTable(ownershipTableModel);

        btAdd.addActionListener(e -> {
            int nextId = dao.getNextIntId("Ownership", "Ownership_ID");
            JTextField tfId = new JTextField(String.valueOf(nextId));
            JComboBox<String> cbOwner = createOwnerComboBox();
            JTextField tfStart = new JTextField("2026-01-01");
            JTextField tfEnd = new JTextField();
            JTextField tfType = new JTextField("New Purchase");

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Ownership_ID (Int *):")); f.add(tfId);
            f.add(new JLabel("Select Owner_ID (*):")); f.add(cbOwner);
            f.add(new JLabel("Start_Date (YYYY-MM-DD):")); f.add(tfStart);
            f.add(new JLabel("End_Date (YYYY-MM-DD or blank):")); f.add(tfEnd);
            f.add(new JLabel("Transfer_Type:")); f.add(tfType);

            if (JOptionPane.showConfirmDialog(this, f, "Add Ownership", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    Date endDate = tfEnd.getText().trim().isEmpty() ? null : Date.valueOf(tfEnd.getText().trim());
                    int ownerId = Integer.parseInt(cbOwner.getSelectedItem().toString().trim());
                    dao.executeUpdate(
                            "INSERT INTO Ownership (Ownership_ID, Owner_ID, Start_Date, End_Date, Transfer_Type) VALUES (?, ?, ?, ?, ?)",
                            Integer.parseInt(tfId.getText().trim()), ownerId,
                            Date.valueOf(tfStart.getText().trim()), endDate, tfType.getText().trim()
                    );
                    loadOwnershipsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });

        btTransferSP.addActionListener(e -> {
            int nextOwnId = dao.getNextIntId("Ownership", "Ownership_ID");
            JComboBox<String> cbOldPlate = createPlateComboBox();
            JTextField tfNewPlate = new JTextField("51H-" + (20000 + nextOwnId));
            JTextField tfNewChassis = new JTextField("CHS999ENG" + nextOwnId);
            JTextField tfNewEngine = new JTextField("ENG9999" + nextOwnId);
            JComboBox<String> cbNewOwner = createOwnerComboBox();
            JTextField tfNewOwnId = new JTextField(String.valueOf(nextOwnId));

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Select Old Plate_Num (*):")); f.add(cbOldPlate);
            f.add(new JLabel("New Plate_Num (*):")); f.add(tfNewPlate);
            f.add(new JLabel("New Chassis_Num (*):")); f.add(tfNewChassis);
            f.add(new JLabel("New Engine_Num (*):")); f.add(tfNewEngine);
            f.add(new JLabel("Select New Owner_ID (*):")); f.add(cbNewOwner);
            f.add(new JLabel("New Ownership_ID (Int *):")); f.add(tfNewOwnId);

            if (JOptionPane.showConfirmDialog(this, f, "Transaction Script 3: sp_TransferVehicleOwnership",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.callTransferOwnership(
                            cbOldPlate.getSelectedItem().toString().trim(), tfNewPlate.getText().trim(),
                            tfNewChassis.getText().trim(), tfNewEngine.getText().trim(),
                            Integer.parseInt(cbNewOwner.getSelectedItem().toString().trim()),
                            Integer.parseInt(tfNewOwnId.getText().trim())
                    );
                    refreshAllTabs();
                    JOptionPane.showMessageDialog(this, "sp_TransferVehicleOwnership executed successfully!");
                } catch (Exception ex) { showDbError(ex); }
            }
        });

        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(ownershipTable, ownershipTableModel, "Ownership", "Ownership_ID", this::loadOwnershipsFromDatabase));
        btRefresh.addActionListener(e -> loadOwnershipsFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(ownershipTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadOwnershipsFromDatabase() {
        ownershipTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Ownership_ID, Owner_ID, Start_Date, End_Date, Transfer_Type FROM Ownership ORDER BY Ownership_ID", 5)) {
                ownershipTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Ownership load note: " + ex.getMessage()); }
    }

    private JPanel createRegistrationTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAdd = new JButton("Add Certificate");
        JButton btRemove = new JButton("Remove Certificate");
        JButton btRefresh = new JButton("Refresh");
        top.add(btAdd); top.add(btRemove); top.add(btRefresh);

        String[] cols = {"Registration_ID", "Registration_Date", "Expiry_Date", "Status", "Issued_By", "Plate_Num"};
        registrationTableModel = new DefaultTableModel(cols, 0);
        registrationTable = new JTable(registrationTableModel);

        btAdd.addActionListener(e -> {
            int nextId = dao.getNextIntId("REGISTRATION", "Registration_ID");
            JTextField tfId = new JTextField(String.valueOf(nextId));
            JTextField tfRegDate = new JTextField("2026-01-01");
            JTextField tfExpDate = new JTextField("2036-01-01");
            JTextField tfStatus = new JTextField("Active");
            JTextField tfIssued = new JTextField("Cuc CSGT SG");
            JComboBox<String> cbPlate = createPlateComboBox();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Registration_ID (Int *):")); f.add(tfId);
            f.add(new JLabel("Registration_Date (YYYY-MM-DD):")); f.add(tfRegDate);
            f.add(new JLabel("Expiry_Date (YYYY-MM-DD):")); f.add(tfExpDate);
            f.add(new JLabel("Status:")); f.add(tfStatus);
            f.add(new JLabel("Issued_By:")); f.add(tfIssued);
            f.add(new JLabel("Select Vehicle Plate_Num (*):")); f.add(cbPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Registration", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO REGISTRATION (Registration_ID, Registration_Date, Expiry_Date, Status, Issued_By, Plate_Num) VALUES (?, ?, ?, ?, ?, ?)",
                            Integer.parseInt(tfId.getText().trim()), Date.valueOf(tfRegDate.getText().trim()),
                            Date.valueOf(tfExpDate.getText().trim()), tfStatus.getText().trim(),
                            tfIssued.getText().trim(), cbPlate.getSelectedItem().toString().trim()
                    );
                    loadRegistrationsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(registrationTable, registrationTableModel, "REGISTRATION", "Registration_ID", this::loadRegistrationsFromDatabase));
        btRefresh.addActionListener(e -> loadRegistrationsFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(registrationTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadRegistrationsFromDatabase() {
        registrationTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Registration_ID, Registration_Date, Expiry_Date, Status, Issued_By, Plate_Num FROM REGISTRATION ORDER BY Registration_ID", 6)) {
                registrationTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Registration load note: " + ex.getMessage()); }
    }

    private JPanel createInsuranceTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAdd = new JButton("Add Policy");
        JButton btRemove = new JButton("Remove Policy");
        JButton btRefresh = new JButton("Refresh");
        top.add(btAdd); top.add(btRemove); top.add(btRefresh);

        String[] cols = {"Policy_Num", "Policy_Name", "Start_Date", "End_Date", "Insurance_Type", "Plate_Num"};
        insuranceTableModel = new DefaultTableModel(cols, 0);
        insuranceTable = new JTable(insuranceTableModel);

        btAdd.addActionListener(e -> {
            int nextNum = insuranceTableModel.getRowCount() + 1;
            JTextField tfNum = new JTextField(String.format("POL%03d", nextNum));
            JTextField tfName = new JTextField("Bao hiem TNDS " + nextNum);
            JTextField tfStart = new JTextField("2026-01-01");
            JTextField tfEnd = new JTextField("2027-01-01");
            JTextField tfType = new JTextField("Compulsory");
            JComboBox<String> cbPlate = createPlateComboBox();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Policy_Num (* Unique):")); f.add(tfNum);
            f.add(new JLabel("Policy_Name:")); f.add(tfName);
            f.add(new JLabel("Start_Date (YYYY-MM-DD):")); f.add(tfStart);
            f.add(new JLabel("End_Date (YYYY-MM-DD):")); f.add(tfEnd);
            f.add(new JLabel("Insurance_Type:")); f.add(tfType);
            f.add(new JLabel("Select Vehicle Plate_Num (*):")); f.add(cbPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Insurance Policy", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO INSURANCEPOLICY (Policy_Num, Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num) VALUES (?, ?, ?, ?, ?, ?)",
                            tfNum.getText().trim(), tfName.getText().trim(), Date.valueOf(tfStart.getText().trim()),
                            Date.valueOf(tfEnd.getText().trim()), tfType.getText().trim(), cbPlate.getSelectedItem().toString().trim()
                    );
                    loadInsuranceFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(insuranceTable, insuranceTableModel, "INSURANCEPOLICY", "Policy_Num", this::loadInsuranceFromDatabase));
        btRefresh.addActionListener(e -> loadInsuranceFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(insuranceTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadInsuranceFromDatabase() {
        insuranceTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Policy_Num, Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num FROM INSURANCEPOLICY ORDER BY Policy_Num", 6)) {
                insuranceTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Insurance load note: " + ex.getMessage()); }
    }

    private JPanel createInspectionTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAdd = new JButton("Add Inspection");
        JButton btRemove = new JButton("Remove Inspection");
        JButton btRefresh = new JButton("Refresh");
        top.add(btAdd); top.add(btRemove); top.add(btRefresh);

        String[] cols = {"Inspection_ID", "Inspection_Date", "Result", "Inspector_Name", "Expiry_Date", "Notes", "Plate_Num"};
        inspectionTableModel = new DefaultTableModel(cols, 0);
        inspectionTable = new JTable(inspectionTableModel);

        btAdd.addActionListener(e -> {
            int nextId = dao.getNextIntId("INSPECTION", "Inspection_ID");
            JTextField tfId = new JTextField(String.valueOf(nextId));
            JTextField tfDate = new JTextField("2026-01-01");
            JTextField tfRes = new JTextField("Pass");
            JTextField tfInsp = new JTextField("Nguyen Van Thanh");
            JTextField tfExp = new JTextField("2027-01-01");
            JTextField tfNotes = new JTextField("Standard check");
            JComboBox<String> cbPlate = createPlateComboBox();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Inspection_ID (Int *):")); f.add(tfId);
            f.add(new JLabel("Inspection_Date (YYYY-MM-DD):")); f.add(tfDate);
            f.add(new JLabel("Result (Pass/Fail):")); f.add(tfRes);
            f.add(new JLabel("Inspector_Name:")); f.add(tfInsp);
            f.add(new JLabel("Expiry_Date (YYYY-MM-DD):")); f.add(tfExp);
            f.add(new JLabel("Notes:")); f.add(tfNotes);
            f.add(new JLabel("Select Vehicle Plate_Num (*):")); f.add(cbPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Technical Inspection", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO INSPECTION (Inspection_ID, Inspection_Date, Result, Inspector_Name, Expiry_Date, Notes, Plate_Num) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            Integer.parseInt(tfId.getText().trim()), Date.valueOf(tfDate.getText().trim()), tfRes.getText().trim(),
                            tfInsp.getText().trim(), Date.valueOf(tfExp.getText().trim()), tfNotes.getText().trim(),
                            cbPlate.getSelectedItem().toString().trim()
                    );
                    loadInspectionsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(inspectionTable, inspectionTableModel, "INSPECTION", "Inspection_ID", this::loadInspectionsFromDatabase));
        btRefresh.addActionListener(e -> loadInspectionsFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(inspectionTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadInspectionsFromDatabase() {
        inspectionTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Inspection_ID, Inspection_Date, Result, Inspector_Name, Expiry_Date, Notes, Plate_Num FROM INSPECTION ORDER BY Inspection_ID", 7)) {
                inspectionTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Inspection load note: " + ex.getMessage()); }
    }

    private JPanel createViolationAndFineTab() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);

        JPanel vPanel = new JPanel(new BorderLayout());
        JPanel vTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAddV = new JButton("Add Traffic Violation");
        JButton btRemV = new JButton("Delete Violation");
        JButton btRefV = new JButton("Refresh All");
        vTop.add(new JLabel("Traffic Violations:"));
        vTop.add(btAddV); vTop.add(btRemV); vTop.add(btRefV);

        String[] vCols = {"Violate_ID", "Violation_Type", "Violation_Date", "Location", "Description", "Plate_Num"};
        violationTableModel = new DefaultTableModel(vCols, 0);
        violationTable = new JTable(violationTableModel);

        btAddV.addActionListener(e -> {
            int nextId = dao.getNextIntId("TRAFFICVIOLATION", "Violate_ID");
            JTextField tfId = new JTextField(String.valueOf(nextId));
            JTextField tfType = new JTextField("Speeding");
            JTextField tfDate = new JTextField("2026-03-01");
            JTextField tfLoc = new JTextField("Vo Van Kiet, D1");
            JTextField tfDesc = new JTextField("Exceeded speed limit by 15km/h");
            JComboBox<String> cbPlate = createPlateComboBox();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Violate_ID (Int *):")); f.add(tfId);
            f.add(new JLabel("Violation_Type:")); f.add(tfType);
            f.add(new JLabel("Violation_Date (YYYY-MM-DD):")); f.add(tfDate);
            f.add(new JLabel("Location:")); f.add(tfLoc);
            f.add(new JLabel("Description:")); f.add(tfDesc);
            f.add(new JLabel("Select Vehicle Plate_Num (*):")); f.add(cbPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Traffic Violation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO TRAFFICVIOLATION (Violate_ID, Violation_Type, Violation_Date, Location, Description, Plate_Num) VALUES (?, ?, ?, ?, ?, ?)",
                            Integer.parseInt(tfId.getText().trim()), tfType.getText().trim(),
                            Date.valueOf(tfDate.getText().trim()), tfLoc.getText().trim(),
                            tfDesc.getText().trim(), cbPlate.getSelectedItem().toString().trim()
                    );
                    loadViolationsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemV.addActionListener(e -> {
            int row = violationTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a traffic violation row to delete.");
                return;
            }
            int vid = Integer.parseInt(strVal(violationTableModel, row, 0));
            if (JOptionPane.showConfirmDialog(this, "Delete Violate_ID " + vid + " and its associated Fine notices?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.deleteViolationCascade(vid);
                    loadViolationsFromDatabase();
                    loadFinesFromDatabase();
                } catch (SQLException ex) { showDbError(ex); }
            }
        });
        btRefV.addActionListener(e -> {
            loadViolationsFromDatabase();
            loadFinesFromDatabase();
        });

        vPanel.add(vTop, BorderLayout.NORTH);
        vPanel.add(new JScrollPane(violationTable), BorderLayout.CENTER);

        // Bottom: Weak Entity FINE
        JPanel fPanel = new JPanel(new BorderLayout());
        JPanel fTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAddF = new JButton("Issue Fine Notice");
        JButton btPaySP = new JButton("Pay Selected Fine (SP2)");
        JButton btRemF = new JButton("Delete Fine");
        fTop.add(new JLabel("Fine Notices (Weak Entity):"));
        fTop.add(btAddF); fTop.add(btPaySP); fTop.add(btRemF);

        String[] fCols = {"Fine_Num (Partial PK)", "Violate_ID (Owner PK/FK)", "Amount", "Due_Date", "Payment_Status", "Paid_Date"};
        fineTableModel = new DefaultTableModel(fCols, 0);
        fineTable = new JTable(fineTableModel);

        btAddF.addActionListener(e -> {
            int nextFineNum = dao.getNextIntId("FINE", "Fine_Num");
            List<String> violateIds = dao.getSingleColumnList("SELECT Violate_ID FROM TRAFFICVIOLATION ORDER BY Violate_ID");
            JComboBox<String> cbVid = new JComboBox<>(violateIds.toArray(new String[0]));
            int vRow = violationTable.getSelectedRow();
            if (vRow != -1) cbVid.setSelectedItem(strVal(violationTableModel, vRow, 0));

            JTextField tfFnum = new JTextField(String.valueOf(nextFineNum));
            JTextField tfAmt = new JTextField("1500000.00");
            JTextField tfDue = new JTextField("2026-04-01");
            JTextField tfStatus = new JTextField("Unpaid");
            JTextField tfPaid = new JTextField();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Fine_Num (Int *):")); f.add(tfFnum);
            f.add(new JLabel("Select Violate_ID (*):")); f.add(cbVid);
            f.add(new JLabel("Amount:")); f.add(tfAmt);
            f.add(new JLabel("Due_Date (YYYY-MM-DD):")); f.add(tfDue);
            f.add(new JLabel("Payment_Status:")); f.add(tfStatus);
            f.add(new JLabel("Paid_Date (YYYY-MM-DD or blank):")); f.add(tfPaid);

            if (JOptionPane.showConfirmDialog(this, f, "Add Fine Notice", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    Date paidDate = tfPaid.getText().trim().isEmpty() ? null : Date.valueOf(tfPaid.getText().trim());
                    int vid = Integer.parseInt(cbVid.getSelectedItem().toString().trim());
                    dao.executeUpdate(
                            "INSERT INTO FINE (Fine_Num, Violate_ID, Amount, Due_Date, Payment_Status, Paid_Date) VALUES (?, ?, ?, ?, ?, ?)",
                            Integer.parseInt(tfFnum.getText().trim()), vid,
                            Double.parseDouble(tfAmt.getText().trim()), Date.valueOf(tfDue.getText().trim()),
                            tfStatus.getText().trim(), paidDate
                    );
                    loadFinesFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });

        btPaySP.addActionListener(e -> {
            int r = fineTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Select an Unpaid fine row in the bottom table first.");
                return;
            }
            int fineNum = Integer.parseInt(strVal(fineTableModel, r, 0));
            int violateId = Integer.parseInt(strVal(fineTableModel, r, 1));
            try {
                dao.callPayFine(fineNum, violateId);
                loadFinesFromDatabase();
                JOptionPane.showMessageDialog(this, "Fine #" + fineNum + " marked as Paid via sp_PayFine!");
            } catch (SQLException ex) { showDbError(ex); }
        });

        btRemF.addActionListener(e -> {
            int r = fineTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Select a fine row to delete.");
                return;
            }
            int fnum = Integer.parseInt(strVal(fineTableModel, r, 0));
            int vid = Integer.parseInt(strVal(fineTableModel, r, 1));
            try {
                dao.executeUpdate("DELETE FROM FINE WHERE Fine_Num = ? AND Violate_ID = ?", fnum, vid);
                loadFinesFromDatabase();
            } catch (SQLException ex) { showDbError(ex); }
        });

        fPanel.add(fTop, BorderLayout.NORTH);
        fPanel.add(new JScrollPane(fineTable), BorderLayout.CENTER);

        splitPane.setTopComponent(vPanel);
        splitPane.setBottomComponent(fPanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(splitPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadViolationsFromDatabase() {
        violationTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Violate_ID, Violation_Type, Violation_Date, Location, Description, Plate_Num FROM TRAFFICVIOLATION ORDER BY Violate_ID", 6)) {
                violationTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Violation load note: " + ex.getMessage()); }
    }

    private void loadFinesFromDatabase() {
        fineTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Fine_Num, Violate_ID, Amount, Due_Date, Payment_Status, Paid_Date FROM FINE ORDER BY Fine_Num", 6)) {
                fineTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Fine load note: " + ex.getMessage()); }
    }

    private void removeSelectedRowByIntOrStr(JTable table, DefaultTableModel model, String dbTable, String pkCol, Runnable onRefresh) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a row in the table to remove.");
            return;
        }
        String keyVal = strVal(model, selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete record '" + keyVal + "' from " + dbTable + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                dao.executeUpdate("DELETE FROM " + dbTable + " WHERE " + pkCol + " = ?", keyVal);
                onRefresh.run();
            } catch (SQLException ex) { showDbError(ex); }
        }
    }

    private String strVal(DefaultTableModel model, int row, int col) {
        Object val = model.getValueAt(row, col);
        return val == null ? "" : val.toString();
    }

    private void showDbError(Exception ex) {
        JOptionPane.showMessageDialog(this, "Database Error:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}