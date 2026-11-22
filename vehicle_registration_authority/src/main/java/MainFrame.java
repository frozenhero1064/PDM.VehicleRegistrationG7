import main.java.DatabaseDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
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

    private JComboBox<String> createChassisComboBox() {
        List<String> chassis = dao.getSingleColumnList("SELECT Chassis_Num FROM VEHICLE ORDER BY Chassis_Num");
        JComboBox<String> cb = new JComboBox<>(chassis.toArray(new String[0]));
        cb.setEditable(true);
        return cb;
    }

    private JComboBox<String> createPlateComboBox() {
        List<String> plates = dao.getSingleColumnList("SELECT Plate_Num FROM REGISTRATION ORDER BY Plate_Num");
        JComboBox<String> cb = new JComboBox<>(plates.toArray(new String[0]));
        cb.setEditable(true);
        return cb;
    }

    private JComboBox<String> createOwnerComboBox() {
        List<String> owners = dao.getSingleColumnList("SELECT OwnerID FROM Owner ORDER BY OwnerID");
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
        JButton btEdit = new JButton("Edit Selected");
        JButton btRemove = new JButton("Remove Vehicle");
        JButton btRefresh = new JButton("Refresh");

        topBar.add(new JLabel("Filter:"));
        topBar.add(tfVehicleSearch);
        topBar.add(btSearch);
        topBar.add(btAdd);
        topBar.add(btEdit);
        topBar.add(btRemove);
        topBar.add(btRefresh);

        String[] columns = {"Chassis Number", "Engine Number", "Brand", "Model", "YOM", "Color", "Type"};
        vehicleTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        vehicleTable = new JTable(vehicleTableModel);

        btSearch.addActionListener(e -> loadVehiclesFromDatabase(tfVehicleSearch.getText()));
        btAdd.addActionListener(e -> showVehicleDialog(false));
        btEdit.addActionListener(e -> showVehicleDialog(true));
        btRemove.addActionListener(e -> {
            int row = vehicleTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a vehicle row to remove.");
                return;
            }
            String chassis = strVal(vehicleTableModel, row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete vehicle '" + chassis + "' and its linked registrations/inspections/policies/violations?",
                    "Confirm Cascade Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dao.deleteVehicleCascade(chassis);
                    refreshAllTabs();
                } catch (SQLException ex) { showDbError(ex); }
            }
        });
        btRefresh.addActionListener(e -> {refreshAllTabs();});

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

        JTextField tfChassis = new JTextField(isEdit ? strVal(vehicleTableModel, row, 0) : "");
        tfChassis.setEditable(!isEdit);
        JTextField tfEngine = new JTextField(isEdit ? strVal(vehicleTableModel, row, 1) : "");
        JTextField tfBrand = new JTextField(isEdit ? strVal(vehicleTableModel, row, 2) : "");
        JTextField tfModel = new JTextField(isEdit ? strVal(vehicleTableModel, row, 3) : "");
        JTextField tfYom = new JTextField(isEdit ? strVal(vehicleTableModel, row, 4) : "");
        JTextField tfColor = new JTextField(isEdit ? strVal(vehicleTableModel, row, 5) : "");
        JTextField tfType = new JTextField(isEdit ? strVal(vehicleTableModel, row, 6) : "");

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Chassis Number:")); form.add(tfChassis);
        form.add(new JLabel("Engine Number:")); form.add(tfEngine);
        form.add(new JLabel("Brand:")); form.add(tfBrand);
        form.add(new JLabel("Model:")); form.add(tfModel);
        form.add(new JLabel("Year of Manufacture (YOM):")); form.add(tfYom);
        form.add(new JLabel("Color:")); form.add(tfColor);
        form.add(new JLabel("Type:")); form.add(tfType);

        if (JOptionPane.showConfirmDialog(this, form, isEdit ? "Edit Vehicle" : "Add New Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                int yom = Integer.parseInt(tfYom.getText().trim());
                if (isEdit) {
                    dao.updateVehicle(tfChassis.getText().trim(), tfEngine.getText().trim(),
                            tfBrand.getText().trim(), tfModel.getText().trim(), yom, tfColor.getText().trim(),
                            tfType.getText().trim());
                } else {
                    dao.insertVehicle(tfChassis.getText().trim(), tfEngine.getText().trim(),
                            tfBrand.getText().trim(), tfModel.getText().trim(), yom, tfColor.getText().trim(),
                            tfType.getText().trim());
                }
                refreshAllTabs();
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

        String[] cols = {"OwnerID", "Category", "Identity Card / Tax Code", "Full Name / Company", "Street", "District", "City"};
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
                    "Delete OwnerID " + ownerId + " and their subtype/contact/ownership records?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dao.deleteOwnerCascade(ownerId);
                    refreshAllTabs();
                } catch (SQLException ex) { showDbError(ex); }
            }
        });
        btRefresh.addActionListener(e -> {refreshAllTabs();});

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
        int nextId = dao.getNextIntId("Owner", "OwnerID");
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
        form.add(new JLabel("Identity_Card:")); form.add(tfIdCard);
        form.add(new JLabel("First Name:")); form.add(tfFname);
        form.add(new JLabel("Middle Name:")); form.add(tfMname);
        form.add(new JLabel("Last Name:")); form.add(tfLname);
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
        int nextId = dao.getNextIntId("Owner", "OwnerID");
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
        form.add(new JLabel("Tax Code:")); form.add(tfTax);
        form.add(new JLabel("Company Name:")); form.add(tfComp);
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
            panel.add(new JLabel("OwnerID: " + ownerId));
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
        JButton btTransferSP = new JButton("Transfer Ownership");
        JButton btRemove = new JButton("Delete Record");
        JButton btRefresh = new JButton("Refresh");

        top.add(btAdd); top.add(btTransferSP); top.add(btRemove); top.add(btRefresh);

        String[] cols = {"Ownership ID", "OwnerID", "Plate Number", "Start Date", "End Date", "Ownership Type"};
        ownershipTableModel = new DefaultTableModel(cols, 0);
        ownershipTable = new JTable(ownershipTableModel);

        btAdd.addActionListener(e -> {
            int nextId = dao.getNextIntId("Ownership", "Ownership_ID");
            JTextField tfId = new JTextField(String.valueOf(nextId));
            JComboBox<String> cbOwner = createOwnerComboBox();
            JComboBox<String> cbOldPlate = createPlateComboBox();
            JTextField tfStart = new JTextField("2026-01-01");
            JTextField tfEnd = new JTextField();
            JTextField tfType = new JTextField("New Purchase");

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Select OwnerID:")); f.add(cbOwner);
            f.add(new JLabel("Select Plate Number:")); f.add(cbOldPlate);
            f.add(new JLabel("Start_Date (YYYY-MM-DD):")); f.add(tfStart);
            f.add(new JLabel("End_Date (YYYY-MM-DD or blank):")); f.add(tfEnd);
            f.add(new JLabel("Transfer_Type:")); f.add(tfType);

            if (JOptionPane.showConfirmDialog(this, f, "Add Ownership", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    String plate = (String) cbOldPlate.getSelectedItem();   // adjust if your combo item has extra text
                    String end = tfEnd.getText().trim();

                    if (end.isEmpty() && dao.hasCurrentOwner(plate)) {
                        throw new SQLException("Plate " + plate + " is still owned. Use Transfer Ownership instead.");
                    }

                    Date endDate = tfEnd.getText().trim().isEmpty() ? null : Date.valueOf(tfEnd.getText().trim());
                    int ownerId = Integer.parseInt(cbOwner.getSelectedItem().toString().trim());
                    dao.executeUpdate(
                            "INSERT INTO Ownership (Ownership_ID, OwnerID, Plate_Num, Start_Date, End_Date, Transfer_Type) VALUES (?, ?, ?, ?, ?, ?)",
                            Integer.parseInt(tfId.getText().trim()), ownerId, cbOldPlate.getSelectedItem().toString().trim(),
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
            JComboBox<String> cbNewOwner = createOwnerComboBox();
            JTextField tfNewOwnId = new JTextField(String.valueOf(nextOwnId));
            JTextField tfNewIssuedBy = new JTextField("Cuc CSGT HN");

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Select Old Plate Number:")); f.add(cbOldPlate);
            f.add(new JLabel("New Plate Number:")); f.add(tfNewPlate);
            f.add(new JLabel("Select New Owner ID:")); f.add(cbNewOwner);
            f.add(new JLabel("New Ownership ID:")); f.add(tfNewOwnId);
            f.add(new JLabel("New Issuer:")); f.add(tfNewIssuedBy);

            if (JOptionPane.showConfirmDialog(this, f, "Transfer Ownership",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.callTransferOwnership(
                            cbOldPlate.getSelectedItem().toString().trim(), tfNewPlate.getText().trim(),
                            Integer.parseInt(cbNewOwner.getSelectedItem().toString().trim()),
                            Integer.parseInt(tfNewOwnId.getText().trim()), tfNewIssuedBy.getText().trim()
                    );
                    refreshAllTabs();
                    JOptionPane.showMessageDialog(this, "Transfered Vehicle Ownership successfully!");
                } catch (Exception ex) { showDbError(ex); }
            }
        });

        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(ownershipTable, ownershipTableModel, "Ownership", "Ownership_ID", this::loadOwnershipsFromDatabase));
        btRefresh.addActionListener(e -> {refreshAllTabs();});

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(ownershipTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadOwnershipsFromDatabase() {
        ownershipTableModel.setRowCount(0);
        try {
            for (Object[] r : dao.queryTable("SELECT Ownership_ID, OwnerID, Plate_Num, Start_Date, End_Date, Transfer_Type FROM Ownership ORDER BY Plate_Num", 6)) {
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

        String[] cols = {"Plate Number", "Chassis Number", "Engine Number", "Registration Date", "Expiry Date", "Status", "Issued By"};
        registrationTableModel = new DefaultTableModel(cols, 0);
        registrationTable = new JTable(registrationTableModel);

        btAdd.addActionListener(e -> {
            JTextField tfPlate = new JTextField("15A-98765");
            JComboBox<String> cbChassis = createChassisComboBox();
            JTextField tfRegDate = new JTextField("2026-01-01");
            JTextField tfExpDate = new JTextField("2036-01-01");
            JTextField tfIssued = new JTextField("Cuc CSGT SG");

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Vehicle Plate Number:")); f.add(tfPlate);
            f.add(new JLabel("Vehicle Chassis Number:")); f.add(cbChassis);
            f.add(new JLabel("Registration Date (YYYY-MM-DD):")); f.add(tfRegDate);
            f.add(new JLabel("Expiry Date (YYYY-MM-DD):")); f.add(tfExpDate);
            f.add(new JLabel("Issued By:")); f.add(tfIssued);

            if (JOptionPane.showConfirmDialog(this, f, "Add Registration", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO REGISTRATION (Plate_Num, Chassis_Num, Engine_Num, Registration_Date, Expiry_Date, Issued_By) VALUES (?, ?, ?, ?, ?, ?)",
                            tfPlate.getText().trim(), cbChassis.getSelectedItem().toString().trim(), " ", Date.valueOf(tfRegDate.getText().trim()), Date.valueOf(tfExpDate.getText().trim()),
                            tfIssued.getText().trim()
                    );
                    loadRegistrationsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(registrationTable, registrationTableModel, "REGISTRATION", "Plate_Num", this::refreshAllTabs));
        btRefresh.addActionListener(e -> {refreshAllTabs();});

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(registrationTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadRegistrationsFromDatabase() {
        registrationTableModel.setRowCount(0);
        try {
            dao.executeUpdate("UPDATE REGISTRATION r JOIN VEHICLE v ON r.Chassis_Num = v.Chassis_Num SET r.Engine_Num = v.Engine_Num");
            dao.executeUpdate("UPDATE REGISTRATION SET Status = CASE " + "WHEN Expiry_Date < CURDATE() THEN 'Expired' ELSE 'Active' END");
        } catch (SQLException ex) { System.out.println("Registration Status update note: " + ex.getMessage()); }

        try {
            for (Object[] r : dao.queryTable("SELECT Plate_Num, Chassis_Num, Engine_Num, Registration_Date, Expiry_Date, Status, Issued_By FROM REGISTRATION ORDER BY Plate_Num", 7)) {
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

        String[] cols = {"Policy Number", "Policy Name", "Start Date", "End Date", "Insurance Type", "Plate Number"};
        insuranceTableModel = new DefaultTableModel(cols, 0);
        insuranceTable = new JTable(insuranceTableModel);

        btAdd.addActionListener(e -> {
            int nextNum = insuranceTableModel.getRowCount() + 1;
            JTextField tfName = new JTextField("Bao hiem TNDS " + nextNum);
            JTextField tfStart = new JTextField("2026-01-01");
            JTextField tfEnd = new JTextField("2027-01-01");
            JTextField tfType = new JTextField("Compulsory");
            JComboBox<String> cbPlate = createPlateComboBox();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Policy Name:")); f.add(tfName);
            f.add(new JLabel("Start Date (YYYY-MM-DD):")); f.add(tfStart);
            f.add(new JLabel("End Date (YYYY-MM-DD):")); f.add(tfEnd);
            f.add(new JLabel("Insurance Type:")); f.add(tfType);
            f.add(new JLabel("Select Vehicle Plate Number:")); f.add(cbPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Insurance Policy", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO INSURANCEPOLICY (Policy_Name, Start_Date, End_Date, Insurance_Type, Plate_Num) VALUES (?, ?, ?, ?, ?)",
                            tfName.getText().trim(), Date.valueOf(tfStart.getText().trim()),
                            Date.valueOf(tfEnd.getText().trim()), tfType.getText().trim(), cbPlate.getSelectedItem().toString().trim()
                    );
                    loadInsuranceFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(insuranceTable, insuranceTableModel, "INSURANCEPOLICY", "Policy_Num", this::loadInsuranceFromDatabase));
        btRefresh.addActionListener(e -> {refreshAllTabs();});

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

        String[] cols = {"Inspection ID", "Inspection Date", "Result", "Inspector Name", "Expiry Date", "Notes", "Plate Number"};
        inspectionTableModel = new DefaultTableModel(cols, 0);
        inspectionTable = new JTable(inspectionTableModel);

        btAdd.addActionListener(e -> {
            JTextField tfDate = new JTextField("2026-01-01");
            JTextField tfRes = new JTextField("Pass");
            JTextField tfInsp = new JTextField("Nguyen Van Thanh");
            JTextField tfExp = new JTextField("2027-01-01");
            JTextField tfNotes = new JTextField("Standard check");
            JComboBox<String> cbPlate = createPlateComboBox();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Inspection Date (YYYY-MM-DD):")); f.add(tfDate);
            f.add(new JLabel("Result (Pass/Fail):")); f.add(tfRes);
            f.add(new JLabel("Inspector Name:")); f.add(tfInsp);
            f.add(new JLabel("Expiry Date (YYYY-MM-DD):")); f.add(tfExp);
            f.add(new JLabel("Notes:")); f.add(tfNotes);
            f.add(new JLabel("Select Vehicle Plate Num:")); f.add(cbPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Technical Inspection", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO INSPECTION (Inspection_Date, Result, Inspector_Name, Expiry_Date, Notes, Plate_Num) VALUES (?, ?, ?, ?, ?, ?)",
                            Date.valueOf(tfDate.getText().trim()), tfRes.getText().trim(),
                            tfInsp.getText().trim(), Date.valueOf(tfExp.getText().trim()), tfNotes.getText().trim(),
                            cbPlate.getSelectedItem().toString().trim()
                    );
                    loadInspectionsFromDatabase();
                } catch (Exception ex) { showDbError(ex); }
            }
        });
        btRemove.addActionListener(e -> removeSelectedRowByIntOrStr(inspectionTable, inspectionTableModel, "INSPECTION", "Inspection_ID", this::loadInspectionsFromDatabase));
        btRefresh.addActionListener(e -> {refreshAllTabs();});

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
        JButton btRefreshV = new JButton("Refresh");
        vTop.add(new JLabel("Traffic Violations:"));
        vTop.add(btAddV); vTop.add(btRemV); vTop.add(btRefreshV);

        String[] vCols = {"Violation ID", "Violation Type", "Violation Date", "Location", "Description", "Plate_Num"};
        violationTableModel = new DefaultTableModel(vCols, 0);
        violationTable = new JTable(violationTableModel);

        btAddV.addActionListener(e -> {
            JTextField tfType = new JTextField("Speeding");
            JTextField tfDate = new JTextField("2026-03-01");
            JTextField tfLoc = new JTextField("Vo Van Kiet, D1");
            JTextField tfDesc = new JTextField("Exceeded speed limit by 15km/h");
            JComboBox<String> cbPlate = createPlateComboBox();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Violation Type:")); f.add(tfType);
            f.add(new JLabel("Violation Date (YYYY-MM-DD):")); f.add(tfDate);
            f.add(new JLabel("Location:")); f.add(tfLoc);
            f.add(new JLabel("Description:")); f.add(tfDesc);
            f.add(new JLabel("Select Vehicle Plate Number:")); f.add(cbPlate);

            if (JOptionPane.showConfirmDialog(this, f, "Add Traffic Violation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    dao.executeUpdate(
                            "INSERT INTO TRAFFICVIOLATION (Violation_Type, Violation_Date, Location, Description, Plate_Num) VALUES (?, ?, ?, ?, ?)",
                            tfType.getText().trim(),
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
        btRefreshV.addActionListener(e -> {refreshAllTabs();});

        vPanel.add(vTop, BorderLayout.NORTH);
        vPanel.add(new JScrollPane(violationTable), BorderLayout.CENTER);

        // Bottom: Weak Entity FINE
        JPanel fPanel = new JPanel(new BorderLayout());
        JPanel fTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btAddF = new JButton("Issue Fine Notice");
        JButton btPaySP = new JButton("Pay Selected Fine");
        JButton btRemF = new JButton("Delete Fine");
        JButton btRefreshF = new JButton("Refresh");

        fTop.add(new JLabel("Fine Notices:"));
        fTop.add(btAddF); fTop.add(btPaySP); fTop.add(btRemF); fTop.add(btRefreshF);

        String[] fCols = {"Violation ID", "Amount (VND)", "Due Date", "Payment Status", "Paid Date"};
        fineTableModel = new DefaultTableModel(fCols, 0);
        fineTable = new JTable(fineTableModel);

        btAddF.addActionListener(e -> {
            List<String> violateIds = dao.getSingleColumnList("SELECT Violate_ID FROM TRAFFICVIOLATION ORDER BY Violate_ID");
            JComboBox<String> cbVid = new JComboBox<>(violateIds.toArray(new String[0]));
            int vRow = violationTable.getSelectedRow();
            if (vRow != -1) cbVid.setSelectedItem(strVal(violationTableModel, vRow, 0));

            JTextField tfAmt = new JTextField("1500000.00");
            JTextField tfDue = new JTextField("2026-04-01");
            JTextField tfStatus = new JTextField("Unpaid");
            JTextField tfPaid = new JTextField();

            JPanel f = new JPanel(new GridLayout(0, 2, 6, 6));
            f.add(new JLabel("Select Violation ID:")); f.add(cbVid);
            f.add(new JLabel("Amount (VND):")); f.add(tfAmt);
            f.add(new JLabel("Due Date (YYYY-MM-DD):")); f.add(tfDue);
            f.add(new JLabel("Payment Status:")); f.add(tfStatus);
            f.add(new JLabel("Paid Date (YYYY-MM-DD or blank):")); f.add(tfPaid);

            if (JOptionPane.showConfirmDialog(this, f, "Add Fine Notice", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try {
                    Date paidDate = tfPaid.getText().trim().isEmpty() ? null : Date.valueOf(tfPaid.getText().trim());
                    int vid = Integer.parseInt(cbVid.getSelectedItem().toString().trim());
                    dao.executeUpdate(
                            "INSERT INTO FINE (Violate_ID, Amount, Due_Date, Payment_Status, Paid_Date) VALUES (?, ?, ?, ?, ?)",
                            vid, Double.parseDouble(tfAmt.getText().trim()), Date.valueOf(tfDue.getText().trim()), tfStatus.getText().trim(), paidDate
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
            int violateId = Integer.parseInt(strVal(fineTableModel, r, 0));
            try {
                dao.callPayFine(violateId);
                loadFinesFromDatabase();
                JOptionPane.showMessageDialog(this, "Fine #" + violateId + " Marked as Paid!");
            } catch (SQLException ex) { showDbError(ex); }
        });

        btRemF.addActionListener(e -> {
            int r = fineTable.getSelectedRow();
            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Select a fine row to delete.");
                return;
            }
            int vid = Integer.parseInt(strVal(fineTableModel, r, 0));
            try {
                dao.executeUpdate("DELETE FROM FINE WHERE Violate_ID = ?", vid);
                loadFinesFromDatabase();
            } catch (SQLException ex) { showDbError(ex); }
        });

        btRefreshF.addActionListener(e -> {refreshAllTabs();});


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
            for (Object[] r : dao.queryTable("SELECT Violate_ID, Amount, Due_Date, Payment_Status, Paid_Date FROM FINE ORDER BY Violate_ID", 5)) {
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
        int confirm = JOptionPane.showConfirmDialog(this,"Delete record '" + keyVal + "' from " + dbTable + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
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
        JOptionPane.showMessageDialog(this, "Database Error:\n" + ex.getMessage(),"Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame app = new MainFrame();
            app.setVisible(true);
        });
    }
}
