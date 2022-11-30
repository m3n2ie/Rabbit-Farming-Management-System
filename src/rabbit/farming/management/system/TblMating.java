/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Menzi
 */
public class TblMating {

    Connection conn;

    public TblMating() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            //Logger.getLogger(TblCage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void mate(int maleId, int femaleId) throws SQLException {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        String query = "INSERT INTO TBL_MATING ( MALERABBITID, FEMALERABBITID, DATETIME, PREGNANCY)VALUES( ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, maleId);
        stmt.setInt(2, femaleId);
        stmt.setString(3, date);
        stmt.setBoolean(4, true);
        stmt.execute();
        stmt.close();
    }

    public void mate(int maleId, int femaleId, String date) throws SQLException {
        String query = "INSERT INTO TBL_MATING ( MALERABBITID, FEMALERABBITID, DATETIME, PREGNANCY)VALUES( ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, maleId);
        stmt.setInt(2, femaleId);
        stmt.setString(3, date);
        stmt.setBoolean(4, true);
        stmt.execute();
        stmt.close();
    }

    public int countMating() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT(DISTINCT MATINGID) FROM TBL_MATING";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public void recordFalsePregnancy(int rabbitId, String date) throws SQLException {
        String query = "UPDATE TBL_MATING SET PREGNANCY = FALSE WHERE FEMALERABBITID = ? AND DATETIME = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.setString(2, date);
        stmt.executeUpdate();
        stmt.close();
    }

    public void recordBirth(int rabbitId, String matingDate, int kids) throws SQLException {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date2 = dateFormat.format(cal.getTime());

        String query = "UPDATE TBL_MATING SET KIDS = " + kids + ", KIDSATBIRTH = " + kids + ", BIRTHDATE = '" + date2 + "' WHERE FEMALERABBITID = ? AND DATETIME = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.setString(2, matingDate);
        stmt.executeUpdate();
        stmt.close();
    }

    public void recordBirth(int rabbitId, String matingDate, String birthDate, int kids) throws SQLException {

        String query = "UPDATE TBL_MATING SET KIDS = " + kids + ", KIDSATBIRTH = " + kids + ", BIRTHDATE = '" + birthDate + "' WHERE FEMALERABBITID = ? AND DATETIME = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.setString(2, matingDate);
        stmt.executeUpdate();
        stmt.close();
    }

    public void updateKids(int rabbitId, String date, int kids) throws SQLException {
        String query = "UPDATE TBL_MATING SET KIDS = " + kids + " WHERE FEMALERABBITID = ? AND DATETIME = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.setString(2, date);
        stmt.executeUpdate();
        stmt.close();
    }

    public int countPregnancies(boolean pregnancyType) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT( DISTINCT MATINGID ) FROM TBL_MATING WHERE BIRTHDATE IS NULL AND PREGNANCY = " + pregnancyType;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public int countKids() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT SUM(KIDS) FROM TBL_MATING WHERE KIDS IS NOT NULL";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public int countKids(int rabbitId) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT SUM(KIDS) FROM TBL_MATING WHERE MALERABBITID = " + rabbitId + " OR FEMALERABBITID = " + rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
            stmt.close();
            return count;
        }
        stmt.close();
        return count;
    }

    public int countDeadKids(int rabbitId) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT SUM(KIDSATBIRTH) FROM TBL_MATING WHERE MALERABBITID = " + rabbitId + " OR FEMALERABBITID = " + rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        int count = 0;
        if (rs.next()) {
            if (rs.getInt(1) - countKids(rabbitId) > 0) {
                count = rs.getInt(1) - countKids(rabbitId);
                stmt.close();
                return count;
            }
        }
        stmt.close();
        return count;
    }

    public ArrayList<String> getMatingDates(int rabbitId) throws SQLException {
        ArrayList<String> dates = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DATETIME FROM TBL_MATING WHERE FEMALERABBITID = " + rabbitId + " ORDER BY MATINGID DESC";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            dates.add(rs.getString("DATETIME"));
        }

        stmt.close();
        return dates;
    }

    public String getLatestMatingDate(int rabbitId) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT DATETIME FROM TBL_MATING WHERE PREGNANCY = True AND ( FEMALERABBITID = " + rabbitId + " OR MALERABBITID = " + rabbitId + " ) ORDER BY MATINGID DESC";
        ResultSet rs = stmt.executeQuery(query);
        String date = "";

        if (rs.next()) {
            return date = rs.getString("DATETIME");
        }
        stmt.close();
        return date;
    }

    public ArrayList<Integer> getPregnantMatingCageIds(String gender) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT TBL_RABBIT.CAGEID from TBL_MATING, TBL_RABBIT WHERE"
                + " (TBL_RABBIT.RABBITID = TBL_MATING.MALERABBITID OR TBL_RABBIT.RABBITID = TBL_MATING.FEMALERABBITID)"
                + " AND TBL_RABBIT.DEATHDATE IS NULL AND PREGNANCY = True AND TBL_MATING.BIRTHDATE IS NULL "
                + "AND TBL_RABBIT.GENDER = '" + gender + "'";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("CAGEID"));
        }

        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public ArrayList<Integer> getRabbitsWithKidsCageIds(String gender) throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT TBL_RABBIT.CAGEID from TBL_MATING, TBL_RABBIT WHERE"
                + " (TBL_RABBIT.RABBITID = TBL_MATING.MALERABBITID OR TBL_RABBIT.RABBITID = TBL_MATING.FEMALERABBITID)"
                + " AND TBL_RABBIT.DEATHDATE IS NULL AND TBL_MATING.BIRTHDATE IS NOT NULL "
                + "AND TBL_RABBIT.GENDER = '" + gender + "'";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("CAGEID"));
        }

        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public ArrayList<Integer> getCageIdsNeedingNests() throws SQLException {
        ArrayList<Integer> cageIds = new ArrayList<>();
        TblRabbit tblRabbit = new TblRabbit();
        TblCage tblCage = new TblCage();

        Statement stmt = conn.createStatement();
        String query = "SELECT FEMALERABBITID, DATETIME FROM TBL_MATING WHERE PREGNANCY = True AND BIRTHDATE IS NULL";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            Date date = new Date(rs.getString("DATETIME"));
            if (!tblCage.hasNest(tblRabbit.getCageId(rs.getInt("FEMALERABBITID")))) {
                if (DateOperations.getDifferenceInDays(date) >= MainFrame.daysBeforePuttingNest && DateOperations.getDifferenceInDays(date) <= MainFrame.daysBeforePuttingNest + 20) {//Doesn't show notification if it is more than 20 days due
                    if (!cageIds.contains(tblRabbit.getCageId(rs.getInt("FEMALERABBITID")))) {
                        cageIds.add(tblRabbit.getCageId(rs.getInt("FEMALERABBITID")));
                    }
                }
            }
        }

        Collections.sort(cageIds);
        stmt.close();
        return cageIds;
    }

    public ArrayList<Integer> getCageIdsWithKidsToMove() throws SQLException {
        ArrayList<Integer> cageIds = new ArrayList<>();
        TblRabbit tblRabbit = new TblRabbit();
        TblCage tblCage = new TblCage();

        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM TBL_MATING WHERE PREGNANCY = True AND BIRTHDATE IS NOT NULL";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            Date date = new Date(rs.getString("BIRTHDATE"));
            if (tblCage.hasNest(tblRabbit.getCageId(rs.getInt("FEMALERABBITID")))) {
                if (DateOperations.getDifferenceInDays(date) >= MainFrame.daysBeforeNestOut && DateOperations.getDifferenceInDays(date) <= (MainFrame.daysBeforeNestOut + 20)) {//Doesn't show notification if it is more than 20 days due   
                    if (!cageIds.contains(tblRabbit.getCageId(rs.getInt("FEMALERABBITID")))) {
                        cageIds.add(tblRabbit.getCageId(rs.getInt("FEMALERABBITID")));
                    }
                }
            }
        }

        Collections.sort(cageIds);
        stmt.close();
        return cageIds;
    }

}
