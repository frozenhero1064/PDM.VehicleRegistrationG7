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

        private boolean hasPrevPlateColumn(Connection conn) {
            try {
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet rs = meta.getColumns(null, null, "VEHICLE", "Prev_Plate_Num")) {
                    return rs.next();
                }
            } catch (SQLException e) {
                return false;
            }
        }

        public List<Object[]> getVehicles(String keyword) throws SQLException {
            List<Object[]> rows = new ArrayList<>();
            try (Connection conn = getConnection()) {
                boolean hasPrev = hasPrevPlateColumn(conn);
                String cols = hasPrev
                        ? "Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type, Prev_Plate_Num"
                        : "Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type";
                String sql = "SELECT " + cols + " FROM VEHICLE";
                boolean filter = keyword != null && !keyword.trim().isEmpty();
                if (filter) {
                    sql += " WHERE Plate_Num LIKE ? OR Brand LIKE ? OR Model LIKE ? OR Chassis_Num LIKE ? OR Engine_Num LIKE ?";
                }
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
                                    hasPrev ? rs.getString("Prev_Plate_Num") : ""
                            });
                        }
                    }
                }
            }
            return rows;
        }

        public void insertVehicle(String plate, String chassis, String engine, String brand,
                                  String model, int yom, String color, String type, String prevPlate) throws SQLException {
            try (Connection conn = getConnection()) {
                boolean hasPrev = hasPrevPlateColumn(conn);
                String sql = hasPrev
                        ? "INSERT INTO VEHICLE (Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type, Prev_Plate_Num) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        : "INSERT INTO VEHICLE (Plate_Num, Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, plate);
                    ps.setString(2, chassis);
                    ps.setString(3, engine);
                    ps.setString(4, brand);
                    ps.setString(5, model);
                    ps.setInt(6, yom);
                    ps.setString(7, color);
                    ps.setString(8, type);
                    if (hasPrev) {
                        if (prevPlate == null || prevPlate.isEmpty()) ps.setNull(9, Types.VARCHAR);
                        else ps.setString(9, prevPlate);
                    }
                    ps.executeUpdate();
                }
            }
        }

        public void updateVehicle(String plate, String chassis, String engine, String brand,
                                  String model, int yom, String color, String type, String prevPlate) throws SQLException {
            try (Connection conn = getConnection()) {
                boolean hasPrev = hasPrevPlateColumn(conn);
                String sql = hasPrev
                        ? "UPDATE VEHICLE SET Chassis_Num=?, Engine_Num=?, Brand=?, Model=?, YOM=?, Color=?, Type=?, Prev_Plate_Num=? WHERE Plate_Num=?"
                        : "UPDATE VEHICLE SET Chassis_Num=?, Engine_Num=?, Brand=?, Model=?, YOM=?, Color=?, Type=? WHERE Plate_Num=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, chassis);
                    ps.setString(2, engine);
                    ps.setString(3, brand);
                    ps.setString(4, model);
                    ps.setInt(5, yom);
                    ps.setString(6, color);
                    ps.setString(7, type);
                    if (hasPrev) {
                        if (prevPlate == null || prevPlate.isEmpty()) ps.setNull(8, Types.VARCHAR);
                        else ps.setString(8, prevPlate);
                        ps.setString(9, plate);
                    } else {
                        ps.setString(8, plate);
                    }
                    ps.executeUpdate();
                }
            }
        }

        public void deleteByKey(String table, String keyCol, String keyVal) throws SQLException {
            String sql = "DELETE FROM " + table + " WHERE " + keyCol + " = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, keyVal);
                ps.executeUpdate();
            }
        }

        public List<Object[]> getOwners() throws SQLException {
            List<Object[]> rows = new ArrayList<>();
            String sql = "SELECT o.OwnerID, o.Street, o.District, o.City, " +
                    "i.Identity_Card, i.Fname, i.Mname, i.Lname, i.DOB, i.Gender, " +
                    "org.Tax_Code, org.Company_Name, org.Represent_Name " +
                    "FROM OWNER o " +
                    "LEFT JOIN INDIVIDUAL i ON o.OwnerID = i.OwnerID " +
                    "LEFT JOIN ORGANISATION org ON o.OwnerID = org.OwnerID";
            try (Connection conn = getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String idCard = rs.getString("Identity_Card");
                    String taxCode = rs.getString("Tax_Code");
                    String category = (idCard != null) ? "Individual" : (taxCode != null ? "Organisation" : "Unspecified");
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
                            rs.getString("OwnerID"),
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

        public void insertIndividualOwner(String ownerId, String street, String district, String city,
                                          String idCard, String fname, String mname, String lname,
                                          String dob, String gender, String phone, String email) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO OWNER (OwnerID, Street, District, City) VALUES (?, ?, ?, ?)")) {
                        ps.setString(1, ownerId);
                        ps.setString(2, street);
                        ps.setString(3, district);
                        ps.setString(4, city);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO INDIVIDUAL (Identity_Card, OwnerID, Fname, Mname, Lname, DOB, Gender) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setString(1, idCard);
                        ps.setString(2, ownerId);
                        ps.setString(3, fname);
                        ps.setString(4, mname);
                        ps.setString(5, lname);
                        ps.setDate(6, Date.valueOf(dob));
                        ps.setString(7, gender);
                        ps.executeUpdate();
                    }
                    insertContactIfPresent(conn, ownerId, phone, email);
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        public void insertOrganisationOwner(String ownerId, String street, String district, String city,
                                            String taxCode, String companyName, String repName,
                                            String phone, String email) throws SQLException {
            try (Connection conn = getConnection()) {
                conn.setAutoCommit(false);
                try {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO OWNER (OwnerID, Street, District, City) VALUES (?, ?, ?, ?)")) {
                        ps.setString(1, ownerId);
                        ps.setString(2, street);
                        ps.setString(3, district);
                        ps.setString(4, city);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "INSERT INTO ORGANISATION (Tax_Code, OwnerID, Company_Name, Represent_Name) VALUES (?, ?, ?, ?)")) {
                        ps.setString(1, taxCode);
                        ps.setString(2, ownerId);
                        ps.setString(3, companyName);
                        ps.setString(4, repName);
                        ps.executeUpdate();
                    }
                    insertContactIfPresent(conn, ownerId, phone, email);
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        private void insertContactIfPresent(Connection conn, String ownerId, String phone, String email) throws SQLException {
            if (phone != null && !phone.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO OWNER_PHONE (Phone, OwnerID) VALUES (?, ?)")) {
                    ps.setString(1, phone);
                    ps.setString(2, ownerId);
                    ps.executeUpdate();
                }
            }
            if (email != null && !email.isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO OWNER_EMAIL (Email, OwnerID) VALUES (?, ?)")) {
                    ps.setString(1, email);
                    ps.setString(2, ownerId);
                    ps.executeUpdate();
                }
            }
        }

        public List<String> getOwnerContacts(String table, String col, String ownerId) throws SQLException {
            List<String> list = new ArrayList<>();
            String sql = "SELECT " + col + " FROM " + table + " WHERE OwnerID = ?";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, ownerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(rs.getString(1));
                }
            }
            return list;
        }

        public void addOwnerContact(String table, String col, String ownerId, String val) throws SQLException {
            String sql = "INSERT INTO " + table + " (" + col + ", OwnerID) VALUES (?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, val);
                ps.setString(2, ownerId);
                ps.executeUpdate();
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
        setTitle("Vehicle Registration Authority - Group 07");
        setSize(1050, 680);
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

    private JPanel createVehicleTab() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tfVehicleSearch = new JTextField(18);
        JButton btSearch = new JButton("Search");
        JButton btAdd = new JButton("Add Vehicle");
        JButton btEdit = new JButton("Edit Selected");
        JButton btRemove = new JButton("Remove Vehicle");
        JButton btRefresh = new JButton("Refresh Data");

        topBar.add(new JLabel("Filter:"));
        topBar.add(tfVehicleSearch);
        topBar.add(btSearch);
        topBar.add(btAdd);
        topBar.add(btEdit);
        topBar.add(btRemove);
        topBar.add(btRefresh);

        String[] columns = {"Plate_Num", "Chassis_Num", "Engine_Num", "Brand", "Model", "YOM", "Color", "Type", "Prev_Plate_Num"};
        vehicleTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        vehicleTable = new JTable(vehicleTableModel);

        btSearch.addActionListener(e -> loadVehiclesFromDatabase(tfVehicleSearch.getText()));
        btAdd.addActionListener(e -> showVehicleDialog(false));
        btEdit.addActionListener(e -> showVehicleDialog(true));
        btRemove.addActionListener(e -> removeSelectedRow(vehicleTable, vehicleTableModel, "VEHICLE", "Plate_Num", () -> loadVehiclesFromDatabase("")));
        btRefresh.addActionListener(e -> {
            tfVehicleSearch.setText("");
            loadVehiclesFromDatabase("");
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

        JTextField tfPlate = new JTextField(isEdit ? strVal(vehicleTableModel, row, 0) : "");
        tfPlate.setEditable(!isEdit);
        JTextField tfChassis = new JTextField(isEdit ? strVal(vehicleTableModel, row, 1) : "");
        JTextField tfEngine = new JTextField(isEdit ? strVal(vehicleTableModel, row, 2) : "");
        JTextField tfBrand = new JTextField(isEdit ? strVal(vehicleTableModel, row, 3) : "");
        JTextField tfModel = new JTextField(isEdit ? strVal(vehicleTableModel, row, 4) : "");
        JTextField tfYom = new JTextField(isEdit ? strVal(vehicleTableModel, row, 5) : "");
        JTextField tfColor = new JTextField(isEdit ? strVal(vehicleTableModel, row, 6) : "");
        JTextField tfType = new JTextField(isEdit ? strVal(vehicleTableModel, row, 7) : "");
        JTextField tfPrevPlate = new JTextField(isEdit ? strVal(vehicleTableModel, row, 8) : "");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Plate Number (*):")); form.add(tfPlate);
        form.add(new JLabel("Chassis Number (* Unique - BR1):")); form.add(tfChassis);
        form.add(new JLabel("Engine Number (* Unique - BR1):")); form.add(tfEngine);
        form.add(new JLabel("Brand:")); form.add(tfBrand);
        form.add(new JLabel("Model:")); form.add(tfModel);
        form.add(new JLabel("Year of Manufacture (YOM) (*):")); form.add(tfYom);
        form.add(new JLabel("Color:")); form.add(tfColor);
        form.add(new JLabel("Type (e.g., Car, Truck):")); form.add(tfType);
        form.add(new JLabel("Replaced Plate (Optional - BR9):")); form.add(tfPrevPlate);

        int res = JOptionPane.showConfirmDialog(this, form, isEdit ? "Edit Vehicle" : "Add New Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            String plate = tfPlate.getText().trim();
            String chassis = tfChassis.getText().trim();
            String engine = tfEngine.getText().trim();
            if (plate.isEmpty() || chassis.isEmpty() || engine.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Plate, Chassis, and Engine numbers are required (BR1)!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int yom;
            try {
                yom = Integer.parseInt(tfYom.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "YOM must be a valid integer year!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (isEdit) {
                    dao.updateVehicle(plate, chassis, engine, tfBrand.getText().trim(), tfModel.getText().trim(),
                            yom, tfColor.getText().trim(), tfType.getText().trim(), tfPrevPlate.getText().trim());
                } else {
                    dao.insertVehicle(plate, chassis, engine, tfBrand.getText().trim(), tfModel.getText().trim(),
                            yom, tfColor.getText().trim(), tfType.getText().trim(), tfPrevPlate.getText().trim());
                }
                loadVehiclesFromDatabase("");
            } catch (SQLException ex) {
                showDbError(ex);
            }
        }
    }

    private JPanel createOwnerTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btAddInd = new JButton("Add Individual Owner");
        JButton btAddOrg = new JButton("Add Organisation Owner");
        JButton btContacts = new JButton("Phones & Emails (BR10)");
        JButton btRemove = new JButton("Delete Owner");
        JButton btRefresh = new JButton("Refresh");

        topBar.add(btAddInd);
        topBar.add(btAddOrg);
        topBar.add(btContacts);
        topBar.add(btRemove);
        topBar.add(btRefresh);

        String[] cols = {"OwnerID", "Category (BR2)", "ID_Card / Tax_Code", "Full Name / Company", "Street", "District", "City"};
        ownerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        ownerTable = new JTable(ownerTableModel);

        btAddInd.addActionListener(e -> showAddIndividualDialog());
        btAddOrg.addActionListener(e -> showAddOrganisationDialog());
        btContacts.addActionListener(e -> showOwnerContactsDialog());
        btRemove.addActionListener(e -> removeSelectedRow(ownerTable, ownerTableModel, "OWNER", "OwnerID", this::loadOwnersFromDatabase));
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
        JTextField tfOwnerId = new JTextField();
        JTextField tfIdCard = new JTextField();
        JTextField tfFname = new JTextField();
        JTextField tfMname = new JTextField();
        JTextField tfLname = new JTextField();
        JTextField tfDob = new JTextField("2000-01-01");
        JTextField tfGender = new JTextField("M");
        JTextField tfStreet = new JTextField();
        JTextField tfDistrict = new JTextField();
        JTextField tfCity = new JTextField();
        JTextField tfPhone = new JTextField();
        JTextField tfEmail = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("OwnerID (*):")); form.add(tfOwnerId);
        form.add(new JLabel("Identity Card (* BR2):")); form.add(tfIdCard);
        form.add(new JLabel("First Name:")); form.add(tfFname);
        form.add(new JLabel("Middle Name:")); form.add(tfMname);
        form.add(new JLabel("Last Name:")); form.add(tfLname);
        form.add(new JLabel("DOB (YYYY-MM-DD):")); form.add(tfDob);
        form.add(new JLabel("Gender:")); form.add(tfGender);
        form.add(new JLabel("Street:")); form.add(tfStreet);
        form.add(new JLabel("District:")); form.add(tfDistrict);
        form.add(new JLabel("City:")); form.add(tfCity);
        form.add(new JLabel("Initial Phone (Optional):")); form.add(tfPhone);
        form.add(new JLabel("Initial Email (Optional):")); form.add(tfEmail);

        if (JOptionPane.showConfirmDialog(this, form, "Add Individual Owner", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                dao.insertIndividualOwner(
                        tfOwnerId.getText().trim(), tfStreet.getText().trim(), tfDistrict.getText().trim(), tfCity.getText().trim(),
                        tfIdCard.getText().trim(), tfFname.getText().trim(), tfMname.getText().trim(), tfLname.getText().trim(),
                        tfDob.getText().trim(), tfGender.getText().trim(), tfPhone.getText().trim(), tfEmail.getText().trim()
                );
                loadOwnersFromDatabase();
            } catch (Exception ex) {
                showDbError(ex);
            }
        }
    }

    private void showAddOrganisationDialog() {
        JTextField tfOwnerId = new JTextField();
        JTextField tfTax = new JTextField();
        JTextField tfComp = new JTextField();
        JTextField tfRep = new JTextField();
        JTextField tfStreet = new JTextField();
        JTextField tfDistrict = new JTextField();
        JTextField tfCity = new JTextField();
        JTextField tfPhone = new JTextField();
        JTextField tfEmail = new JTextField();

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("OwnerID (*):")); form.add(tfOwnerId);
        form.add(new JLabel("Tax Code (* BR2):")); form.add(tfTax);
        form.add(new JLabel("Company Name (*):")); form.add(tfComp);
        form.add(new JLabel("Representative Name:")); form.add(tfRep);
        form.add(new JLabel("Street:")); form.add(tfStreet);
        form.add(new JLabel("District:")); form.add(tfDistrict);
        form.add(new JLabel("City:")); form.add(tfCity);
        form.add(new JLabel("Initial Phone (Optional):")); form.add(tfPhone);
        form.add(new JLabel("Initial Email (Optional):")); form.add(tfEmail);

        if (JOptionPane.showConfirmDialog(this, form, "Add Organisation Owner", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                dao.insertOrganisationOwner(
                        tfOwnerId.getText().trim(), tfStreet.getText().trim(), tfDistrict.getText().trim(), tfCity.getText().trim(),
                        tfTax.getText().trim(), tfComp.getText().trim(), tfRep.getText().trim(),
                        tfPhone.getText().trim(), tfEmail.getText().trim()
                );
                loadOwnersFromDatabase();
            } catch (Exception ex) {
                showDbError(ex);
            }
        }
    }

    private void showOwnerContactsDialog() {
        int row = ownerTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an owner first to view/add Phones & Emails.");
            return;
        }
        String ownerId = strVal(ownerTableModel, row, 0);
        try {
            List<String> phones = dao.getOwnerContacts("OWNER_PHONE", "Phone", ownerId);
            List<String> emails = dao.getOwnerContacts("OWNER_EMAIL", "Email", ownerId);

            JTextField tfNewPhone = new JTextField();
            JTextField tfNewEmail = new JTextField();

            JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
            panel.add(new JLabel("OwnerID: " + ownerId));
            panel.add(new JLabel("Registered Phones: " + (phones.isEmpty() ? "None" : String.join(", ", phones))));
            panel.add(new JLabel("Registered Emails: " + (emails.isEmpty() ? "None" : String.join(", ", emails))));
            panel.add(new JLabel("Add Phone Number:"));
            panel.add(tfNewPhone);
            panel.add(new JLabel("Add Email Address:"));
            panel.add(tfNewEmail);

            if (JOptionPane.showConfirmDialog(this, panel, "Owner Contacts (BR10)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (!tfNewPhone.getText().trim().isEmpty()) {
                    dao.addOwnerContact("OWNER_PHONE", "Phone", ownerId, tfNewPhone.getText().trim());
                }
                if (!tfNewEmail.getText().trim().isEmpty()) {
                    dao.addOwnerContact("OWNER_EMAIL", "Email", ownerId, tfNewEmail.getText().trim());
                }
            }
        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private JPanel createOwnershipTab() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAdd = new JButton("Add Ownership Record");
        JButton btRemove = new JButton("Delete Record");
        JButton btRefresh = new JButton("Refresh");
        top.add(btAdd); top.add(btRemove); top.add(btRefresh);

        String[] cols = {"Ownership_ID", "Plate_Num", "OwnerID", "Start_Date", "End_Date", "Transfer_Type"};
        ownershipTableModel = new DefaultTableModel(cols, 0);
        ownershipTable = new JTable(ownershipTableModel);

        btAdd.addActionListener(e -> {
            JTextField tfId = new JTextField();
            JTextField tfPlate = new JTextField();
            JTextField tfOwner = new JTextField();
            JTextField tfStart = new JTextField("2026-01-01");
            JTextField tfEnd = new JTextField();
            JTextField tfType = new JTextField("New Registration");

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Ownership ID (*):")); f.add(tfId);
            f.add(new JLabel("Plate Number (*):")); f.add(tfPlate);
            f.add(new JLabel("OwnerID (*):")); f.add(tfOwner);
            f.add(new JLabel("Start Date (YYYY-MM-DD):")); f.add(tfStart);
            f.add(new JLabel("End Date (YYYY-MM-DD or blank):")); f.add(tfEnd);
            f.add(new JLabel("Transfer Type:")); f.add(tfType);

            if (JOptionPane.showConfirmDialog(this, f, "Add Ownership (BR3)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    Date endDate = tfEnd.getText().trim().isEmpty() ? null : Date.valueOf(tfEnd.getText().trim());
                    dao.executeUpdate(
                            "INSERT INTO OWNERSHIP (Ownership_ID, Plate_Num, OwnerID, Start_Date, End_Date, Transfer_Type) VALUES (?, ?, ?, ?, ?, ?)",
                            tfId.getText().trim(), tfPlate.getText().trim(), tfOwner.getText().trim(),
                            Date.valueOf(tfStart.getText().trim()), endDate, tfType.getText().trim()
                    );
                    loadOwnershipsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRow(ownershipTable, ownershipTableModel, "OWNERSHIP", "Ownership_ID", this::loadOwnershipsFromDatabase));
        btRefresh.addActionListener(e -> loadOwnershipsFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(ownershipTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadOwnershipsFromDatabase() {
        ownershipTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Ownership_ID, Plate_Num, OwnerID, Start_Date, End_Date, Transfer_Type FROM OWNERSHIP", 6)) {
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
            JTextField tfId = new JTextField();
            JTextField tfRegDate = new JTextField("2026-01-01");
            JTextField tfExpDate = new JTextField("2028-01-01");
            JTextField tfStatus = new JTextField("Active");
            JTextField tfIssued = new JTextField("HCMC Traffic Authority");
            JTextField tfPlate = new JTextField();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Registration ID (*):")); f.add(tfId);
            f.add(new JLabel("Registration Date (YYYY-MM-DD):")); f.add(tfRegDate);
            f.add(new JLabel("Expiry Date (YYYY-MM-DD):")); f.add(tfExpDate);
            f.add(new JLabel("Status:")); f.add(tfStatus);
            f.add(new JLabel("Issued By:")); f.add(tfIssued);
            f.add(new JLabel("Plate Number (*):")); f.add(tfPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Registration (BR4)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO REGISTRATION (Registration_ID, Registration_Date, Expiry_Date, Status, Issued_By, Plate_Num) VALUES (?, ?, ?, ?, ?, ?)",
                            tfId.getText().trim(), Date.valueOf(tfRegDate.getText().trim()), Date.valueOf(tfExpDate.getText().trim()),
                            tfStatus.getText().trim(), tfIssued.getText().trim(), tfPlate.getText().trim()
                    );
                    loadRegistrationsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRow(registrationTable, registrationTableModel, "REGISTRATION", "Registration_ID", this::loadRegistrationsFromDatabase));
        btRefresh.addActionListener(e -> loadRegistrationsFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(registrationTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadRegistrationsFromDatabase() {
        registrationTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Registration_ID, Registration_Date, Expiry_Date, Status, Issued_By, Plate_Num FROM REGISTRATION", 6)) {
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
            JTextField tfNum = new JTextField();
            JTextField tfName = new JTextField();
            JTextField tfStart = new JTextField("2026-01-01");
            JTextField tfEnd = new JTextField("2027-01-01");
            JTextField tfType = new JTextField("Compulsory Civil Liability");
            JTextField tfPlate = new JTextField();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Policy Number (*):")); f.add(tfNum);
            f.add(new JLabel("Policy Name:")); f.add(tfName);
            f.add(new JLabel("Start Date (YYYY-MM-DD):")); f.add(tfStart);
            f.add(new JLabel("End Date (YYYY-MM-DD):")); f.add(tfEnd);
            f.add(new JLabel("Insurance Type:")); f.add(tfType);
            f.add(new JLabel("Plate Number (*):")); f.add(tfPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Insurance Policy (BR5)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO INSURANCEPOLICY (Policy_Num, Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num) VALUES (?, ?, ?, ?, ?, ?)",
                            tfNum.getText().trim(), tfName.getText().trim(), Date.valueOf(tfStart.getText().trim()),
                            Date.valueOf(tfEnd.getText().trim()), tfType.getText().trim(), tfPlate.getText().trim()
                    );
                    loadInsuranceFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRow(insuranceTable, insuranceTableModel, "INSURANCEPOLICY", "Policy_Num", this::loadInsuranceFromDatabase));
        btRefresh.addActionListener(e -> loadInsuranceFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(insuranceTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadInsuranceFromDatabase() {
        insuranceTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Policy_Num, Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num FROM INSURANCEPOLICY", 6)) {
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
            JTextField tfId = new JTextField();
            JTextField tfDate = new JTextField("2026-01-01");
            JTextField tfRes = new JTextField("Pass");
            JTextField tfInsp = new JTextField();
            JTextField tfExp = new JTextField("2027-01-01");
            JTextField tfNotes = new JTextField();
            JTextField tfPlate = new JTextField();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Inspection ID (*):")); f.add(tfId);
            f.add(new JLabel("Inspection Date (YYYY-MM-DD):")); f.add(tfDate);
            f.add(new JLabel("Result (Pass/Fail):")); f.add(tfRes);
            f.add(new JLabel("Inspector Name:")); f.add(tfInsp);
            f.add(new JLabel("Expiry Date (YYYY-MM-DD):")); f.add(tfExp);
            f.add(new JLabel("Notes:")); f.add(tfNotes);
            f.add(new JLabel("Plate Number (*):")); f.add(tfPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Technical Inspection (BR6)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO INSPECTION (Inspection_ID, Inspection_Date, Result, Inspector_Name, Expiry_Date, Notes, Plate_Num) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            tfId.getText().trim(), Date.valueOf(tfDate.getText().trim()), tfRes.getText().trim(),
                            tfInsp.getText().trim(), Date.valueOf(tfExp.getText().trim()), tfNotes.getText().trim(), tfPlate.getText().trim()
                    );
                    loadInspectionsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRow(inspectionTable, inspectionTableModel, "INSPECTION", "Inspection_ID", this::loadInspectionsFromDatabase));
        btRefresh.addActionListener(e -> loadInspectionsFromDatabase());

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(inspectionTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadInspectionsFromDatabase() {
        inspectionTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Inspection_ID, Inspection_Date, Result, Inspector_Name, Expiry_Date, Notes, Plate_Num FROM INSPECTION", 7)) {
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
        vTop.add(new JLabel("Traffic Violations (BR7):"));
        vTop.add(btAddV); vTop.add(btRemV); vTop.add(btRefV);

        String[] vCols = {"Violate_ID", "Violate_Datetime", "Street", "District", "City", "Violate_Type", "Description", "Plate_Num"};
        violationTableModel = new DefaultTableModel(vCols, 0);
        violationTable = new JTable(violationTableModel);

        btAddV.addActionListener(e -> {
            JTextField tfId = new JTextField();
            JTextField tfDt = new JTextField("2026-05-01 08:30:00");
            JTextField tfSt = new JTextField();
            JTextField tfDist = new JTextField();
            JTextField tfCity = new JTextField("Ho Chi Minh City");
            JTextField tfType = new JTextField("Speeding");
            JTextField tfDesc = new JTextField();
            JTextField tfPlate = new JTextField();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Violate ID (*):")); f.add(tfId);
            f.add(new JLabel("Datetime (YYYY-MM-DD HH:MM:SS):")); f.add(tfDt);
            f.add(new JLabel("Street:")); f.add(tfSt);
            f.add(new JLabel("District:")); f.add(tfDist);
            f.add(new JLabel("City:")); f.add(tfCity);
            f.add(new JLabel("Violation Type:")); f.add(tfType);
            f.add(new JLabel("Description:")); f.add(tfDesc);
            f.add(new JLabel("Plate Number (*):")); f.add(tfPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Traffic Violation (BR7)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO TRAFFICVIOLATION (Violate_ID, Violate_Datetime, Street, District, City, Violate_Type, Description, Plate_Num) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                            tfId.getText().trim(), Timestamp.valueOf(tfDt.getText().trim()), tfSt.getText().trim(),
                            tfDist.getText().trim(), tfCity.getText().trim(), tfType.getText().trim(), tfDesc.getText().trim(), tfPlate.getText().trim()
                    );
                    loadViolationsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemV.addActionListener(e -> removeSelectedRow(violationTable, violationTableModel, "TRAFFICVIOLATION", "Violate_ID", () -> {
            loadViolationsFromDatabase();
            loadFinesFromDatabase();
        }));
        btRefV.addActionListener(e -> {
            loadViolationsFromDatabase();
            loadFinesFromDatabase();
        });

        vPanel.add(vTop, BorderLayout.NORTH);
        vPanel.add(new JScrollPane(violationTable), BorderLayout.CENTER);

        JPanel fPanel = new JPanel(new BorderLayout());
        JPanel fTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAddF = new JButton("Issue Fine for Violation");
        JButton btRemF = new JButton("Delete Selected Fine");
        fTop.add(new JLabel("Fine Notices (Weak Entity - BR8):"));
        fTop.add(btAddF); fTop.add(btRemF);

        String[] fCols = {"Violate_ID (PK/FK)", "Fine_Num (Partial PK)", "Amount", "Due_Date", "Payment_Status", "Paid_Date"};
        fineTableModel = new DefaultTableModel(fCols, 0);
        fineTable = new JTable(fineTableModel);

        btAddF.addActionListener(e -> {
            int vRow = violationTable.getSelectedRow();
            String defaultViolateId = vRow != -1 ? strVal(violationTableModel, vRow, 0) : "";

            JTextField tfVid = new JTextField(defaultViolateId);
            JTextField tfFnum = new JTextField("1");
            JTextField tfAmt = new JTextField("1500000");
            JTextField tfDue = new JTextField("2026-06-01");
            JTextField tfStatus = new JTextField("Unpaid");
            JTextField tfPaid = new JTextField();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Violate ID (* Owner Key):")); f.add(tfVid);
            f.add(new JLabel("Fine Num (* Partial Key):")); f.add(tfFnum);
            f.add(new JLabel("Amount:")); f.add(tfAmt);
            f.add(new JLabel("Due Date (YYYY-MM-DD):")); f.add(tfDue);
            f.add(new JLabel("Payment Status:")); f.add(tfStatus);
            f.add(new JLabel("Paid Date (YYYY-MM-DD or blank):")); f.add(tfPaid);

            if (JOptionPane.showConfirmDialog(this, f, "Add Fine Notice (BR8)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    Date paidDate = tfPaid.getText().trim().isEmpty() ? null : Date.valueOf(tfPaid.getText().trim());
                    dao.executeUpdate(
                            "INSERT INTO FINE (Violate_ID, Fine_Num, Amount, Due_Date, Payment_Status, Paid_Date) VALUES (?, ?, ?, ?, ?, ?)",
                            tfVid.getText().trim(), tfFnum.getText().trim(), Double.parseDouble(tfAmt.getText().trim()),
                            Date.valueOf(tfDue.getText().trim()), tfStatus.getText().trim(), paidDate
                    );
                    loadFinesFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });

        btRemF.addActionListener(e -> {
            int r = fineTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Select a fine row to delete.");
                return;
            }
            String vid = strVal(fineTableModel, r, 0);
            String fnum = strVal(fineTableModel, r, 1);
            try {
                dao.executeUpdate("DELETE FROM FINE WHERE Violate_ID = ? AND Fine_Num = ?", vid, fnum);
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
            for (Object[] r : dao.queryTable("SELECT Violate_ID, Violate_Datetime, Street, District, City, Violate_Type, Description, Plate_Num FROM TRAFFICVIOLATION", 8)) {
                violationTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Violation load note: " + ex.getMessage()); }
    }

    private void loadFinesFromDatabase() {
        fineTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Violate_ID, Fine_Num, Amount, Due_Date, Payment_Status, Paid_Date FROM FINE", 6)) {
                fineTableModel.addRow(r);
            }
        } catch (SQLException ex) { System.out.println("Fine load note: " + ex.getMessage()); }
    }

    private void removeSelectedRow(JTable table, DefaultTableModel model, String dbTable, String pkCol, Runnable onRefresh) {
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
                dao.deleteByKey(dbTable, pkCol, keyVal);
                onRefresh.run();
            } catch (SQLException ex) {
                showDbError(ex);
            }
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