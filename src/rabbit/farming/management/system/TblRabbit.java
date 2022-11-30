/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.logging.*;

/**
 *
 * @author Menzi
 */
public class TblRabbit {

    Connection conn;

    public TblRabbit() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            //Logger.getLogger(TblCage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateWeightId(int weightId, int rabbitId) throws SQLException {
        String query = "UPDATE TBL_RABBIT SET WEIGHTID = ? WHERE RABBITID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, weightId);
        stmt.setInt(2, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public ArrayList<Integer> getWeightedRabbitIds() throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT RABBITID from TBL_WEIGHT";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("RABBITID"));
        }

        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public ArrayList<Integer> getCageIds(String gender) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT CAGEID FROM TBL_RABBIT WHERE GENDER = '" + gender + "' AND DEATHDATE IS NULL AND SOLD = False AND ACTIVE = True";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("CAGEID"));
        }

        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public ArrayList<Integer> getMatingCageIds(String gender) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT CAGEID FROM TBL_RABBIT WHERE GENDER = '" + gender + "' AND DEATHDATE IS NULL AND SOLD = False AND ACTIVE = True";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("CAGEID"));
        }

        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public ArrayList<Integer> getStockCageIds(String stockType) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT CAGEID FROM TBL_RABBIT WHERE STOCKTYPE = '" + stockType + "' AND DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("CAGEID"));
        }

        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public int getRabbitId(int cageId) throws SQLException {
        if (cageId == 0) {
            return 0;
        }
        Statement stmt = conn.createStatement();
        String query = "SELECT RABBITID FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False AND CAGEID = " + cageId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int id = rs.getInt("RABBITID");
        stmt.close();
        return id;
    }

    public String getStockType(int rabbitId) throws SQLException {
        if (rabbitId == 0) {
            return "";
        }
        Statement stmt = conn.createStatement();
        String query = "SELECT STOCKTYPE FROM TBL_RABBIT WHERE RABBITID = " + rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        String type = rs.getString("STOCKTYPE");
        stmt.close();
        return type;
    }

    public ArrayList<Integer> getRabbitsId(int cageId, String gender) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT RABBITID FROM TBL_RABBIT WHERE CAGEID = " + cageId + " AND GENDER = '" + gender + "' AND DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("RABBITID"));
        }
        stmt.close();
        return ids;
    }

    public int getCageId(int rabbitId) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT CAGEID FROM TBL_RABBIT WHERE RABBITID = " + rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();

        int id = rs.getInt("CAGEID");
        stmt.close();
        return id;
    }

    public ArrayList<Integer> getMatedRabbitIds() throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT MALERABBITID, FEMALERABBITID from TBL_MATING";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            if (!ids.contains(rs.getInt("MALERABBITID"))) {
                ids.add(rs.getInt("MALERABBITID"));
            }
            if (!ids.contains(rs.getInt("FEMALERABBITID"))) {
                ids.add(rs.getInt("FEMALERABBITID"));
            }
        }
        stmt.close();
        return ids;
    }

    public void move(int rabbitId, int destinationCageId) throws SQLException {
        String query = "UPDATE TBL_RABBIT SET CAGEID = ? WHERE RABBITID = ? AND DEATHDATE IS NULL AND SOLD = False";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, destinationCageId);
        stmt.setInt(2, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public void deactivate(int rabbitId) throws SQLException {
        String query = "UPDATE TBL_RABBIT SET ACTIVE = False WHERE RABBITID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public void recordDeath(int rabbitId) throws SQLException {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        String query = "UPDATE TBL_RABBIT SET DEATHDATE = ? WHERE RABBITID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, date);
        stmt.setInt(2, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public void recordDeath(int rabbitId, String date) throws SQLException {
        String query = "UPDATE TBL_RABBIT SET DEATHDATE = ? WHERE RABBITID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, date);
        stmt.setInt(2, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public void recordSale(int rabbitId) throws SQLException {

        String query = "UPDATE TBL_RABBIT SET SOLD = True WHERE RABBITID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public ArrayList<Integer> getAllRabbitIds() throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();
        Statement stmt = conn.createStatement();
        String query = "SELECT RABBITID FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("RABBITID"));
        }

        stmt.close();
        return ids;
    }

    public int getLatestRabbitId() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT MAX(RABBITID) FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int id = rs.getInt(1);
        stmt.close();
        return id;
    }

    public void insertRabbit(String breed, String gender, String stockType,
            String birthDate, double weight, int motherId, int fatherId, int cageId) throws SQLException {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        String query = "INSERT INTO TBL_RABBIT ( BREED, GENDER, STOCKTYPE, "
                + "BIRTHDATE, WEIGHTID, MOTHERRABBITID, FATHERRABBITID, CAGEID, SOLD, ACTIVE)"
                + "VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, breed);
        stmt.setString(2, gender);
        stmt.setString(3, stockType);
        stmt.setString(4, birthDate);
        stmt.setInt(5, -1);
        stmt.setInt(6, motherId);
        stmt.setInt(7, fatherId);
        stmt.setInt(8, cageId);
        stmt.setBoolean(9, false);
        stmt.setBoolean(10, false);
        stmt.execute();

        TblWeight tblWeight = new TblWeight();
        tblWeight.insertWeight(getLatestRabbitId(), weight);

        updateWeightId(tblWeight.getLatestWeightId(), getLatestRabbitId());
        stmt.close();
    }

    public int countRabbits() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT( DISTINCT RABBITID ) from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public int countRabbits(String stockType) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT( DISTINCT RABBITID ) FROM TBL_RABBIT WHERE STOCKTYPE = '" + stockType + "' AND DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public int countMarketReadyRabbits(double thresholdWeight) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT(DISTINCT TBL_RABBIT.RABBITID) FROM TBL_RABBIT,  TBL_WEIGHT WHERE TBL_RABBIT.WEIGHTID = TBL_WEIGHT.WEIGHTID "
                + "AND TBL_WEIGHT.VALUE > " + thresholdWeight + " AND STOCKTYPE = 'Selling Stock' AND TBL_RABBIT.DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public int countRabbitsDeaths() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT( DISTINCT RABBITID ) FROM TBL_RABBIT WHERE DEATHDATE IS NOT NULL";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public int countRabbitsSold() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT( DISTINCT RABBITID) FROM TBL_RABBIT WHERE SOLD = True";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public void activate(int rabbitId) throws SQLException {

        String query = "UPDATE TBL_RABBIT SET ACTIVE = True WHERE RABBITID = ? AND STOCKTYPE = 'Breeding Stock'";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public ArrayList<Integer> getActiveRabbitIds() throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT RABBITID from TBL_RABBIT WHERE ACTIVE = True";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("RABBITID"));
        }
        stmt.close();
        return ids;
    }

    public void retire(int rabbitId) throws SQLException {

        String query = "UPDATE TBL_RABBIT SET ACTIVE = False WHERE RABBITID = ? AND STOCKTYPE = 'Breeding Stock'";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

    public void updateStockType(int rabbitId, String stockType) throws SQLException {

        String query = "UPDATE TBL_RABBIT SET STOCKTYPE = ? WHERE RABBITID = ? ";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, stockType);
        stmt.setInt(2, rabbitId);
        stmt.executeUpdate();
        stmt.close();
    }

}
