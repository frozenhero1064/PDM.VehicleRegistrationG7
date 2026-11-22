package main.java;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDAO {
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
        String sql = "SELECT Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type FROM VEHICLE";
        boolean filter = keyword != null && !keyword.trim().isEmpty();
        if (filter) {
            sql += " WHERE Chassis_Num LIKE ? OR Engine_Num LIKE ? OR Brand LIKE ? OR Model LIKE ?";
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filter) {
                String pat = "%" + keyword.trim() + "%";
                for (int i = 1; i <= 4; i++) ps.setString(i, pat);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{
                            rs.getString("Chassis_Num"),
                            rs.getString("Engine_Num"),
                            rs.getString("Brand"),
                            rs.getString("Model"),
                            rs.getInt("YOM"),
                            rs.getString("Color"),
                            rs.getString("Type"),
                    });
                }
            }
        }
        return rows;
    }

    public void insertVehicle(String chassis, String engine, String brand,
                              String model, int yom, String color, String type) throws SQLException {
        String sql = "INSERT INTO VEHICLE (Chassis_Num, Engine_Num, Brand, Model, YOM, Color, Type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chassis);
            ps.setString(2, engine);
            ps.setString(3, brand);
            ps.setString(4, model);
            ps.setInt(5, yom);
            ps.setString(6, color);
            ps.setString(7, type);
            ps.executeUpdate();
        }
    }

    public void updateVehicle(String chassis, String engine, String brand,
                              String model, int yom, String color, String type) throws SQLException {
        String sql = "UPDATE VEHICLE SET Engine_Num=?, Brand=?, Model=?, YOM=?, Color=?, Type=? WHERE Chassis_Num=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(7, chassis);
            ps.setString(1, engine);
            ps.setString(2, brand);
            ps.setString(3, model);
            ps.setInt(4, yom);
            ps.setString(5, color);
            ps.setString(6, type);
            ps.executeUpdate();
        }
    }

    public void deleteVehicleCascade(String chassisNum) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                String plateNum = "";

                try (PreparedStatement ps = conn.prepareStatement("SELECT Plate_Num FROM REGISTRATION WHERE Chassis_Num = ?")){
                    ps.setString(1, chassisNum);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            plateNum = rs.getString("Plate_Num");
                        }
                    }
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
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM VEHICLE WHERE Chassis_Num = ?")) {
                    ps.setString(1, chassisNum);
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
        String sql = "SELECT o.OwnerID, o.Street, o.District, o.City, " +
                "i.Identity_Card, i.Fname, i.Mname, i.Lname, i.DoB, i.Gender, " +
                "org.Tax_Code, org.Company_Name, org.Represent_Name " +
                "FROM Owner o " +
                "LEFT JOIN Individual i ON o.OwnerID = i.OwnerID " +
                "LEFT JOIN Organisation org ON o.OwnerID = org.OwnerID " +
                "ORDER BY o.OwnerID";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String idCard = rs.getString("Identity_Card");
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
                        rs.getInt("OwnerID"),
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
                        "INSERT INTO Owner (OwnerID, Street, District, City) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, ownerId);
                    ps.setString(2, street);
                    ps.setString(3, district);
                    ps.setString(4, city);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Individual (OwnerID, Fname, Mname, Lname, DoB, Gender, Identity_Card) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
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
                        "INSERT INTO Owner (OwnerID, Street, District, City) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, ownerId);
                    ps.setString(2, street);
                    ps.setString(3, district);
                    ps.setString(4, city);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Organisation (OwnerID, Company_Name, Tax_Code, Represent_Name) VALUES (?, ?, ?, ?)")) {
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
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tbl + " WHERE OwnerID = ?")) {
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
                    "INSERT INTO Owner_phone (OwnerID, Phone) VALUES (?, ?) ON DUPLICATE KEY UPDATE Phone = VALUES(Phone)")) {
                ps.setInt(1, ownerId);
                ps.setString(2, phone);
                ps.executeUpdate();
            }
        }
        if (email != null && !email.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Owner_email (OwnerID, email) VALUES (?, ?) ON DUPLICATE KEY UPDATE email = VALUES(email)")) {
                ps.setInt(1, ownerId);
                ps.setString(2, email);
                ps.executeUpdate();
            }
        }
    }

    public List<String> getOwnerContacts(String table, String col, int ownerId) throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT " + col + " FROM " + table + " WHERE OwnerID = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        }
        return list;
    }

    public void saveOwnerContacts(int ownerId, List<String> phones, List<String> emails) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                replaceContacts(conn, "Owner_phone", "Phone", ownerId, phones);
                replaceContacts(conn, "Owner_email", "email", ownerId, emails);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void replaceContacts(Connection conn, String table, String col, int ownerId, List<String> values) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE OwnerID = ?")) {
            ps.setInt(1, ownerId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO " + table + " (OwnerID, " + col + ") VALUES (?, ?)")) {
            for (String v : values) {
                ps.setInt(1, ownerId);
                ps.setString(2, v);
                ps.addBatch();
            }
            ps.executeBatch();
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

    public void callPayFine(int violateId) throws SQLException {
        String sql = "{CALL sp_PayFine(?)}";
        try (Connection conn = getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setInt(1, violateId);
            cs.execute();
        }
    }

    public void callTransferOwnership(String oldPlate, String newPlate, int newOwnerId, int newOwnershipId, String newIssuedBy) throws SQLException {
        String sql = "{CALL sp_TransferVehicleOwnership(?, ?, ?, ?, ?)}";
        try (Connection conn = getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, oldPlate);
            cs.setString(2, newPlate);
            cs.setInt(3, newOwnerId);
            cs.setInt(4, newOwnershipId);
            cs.setString(5, newIssuedBy);
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

    public boolean hasCurrentOwner(String plate) throws SQLException {
        String sql = "SELECT 1 FROM Ownership WHERE Plate_Num = ? AND End_Date IS NULL LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, plate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

}