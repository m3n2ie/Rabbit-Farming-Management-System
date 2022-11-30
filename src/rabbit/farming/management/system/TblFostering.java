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

/**
 *
 * @author Menzi
 */
public class TblFostering {

    Connection conn;

    public TblFostering() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            //Logger.getLogger(TblFostering.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void recordFostering(String cageName, int kids) throws SQLException {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        String query = "UPDATE TBL_FOSTERING SET FOSTERDATE = ?,  KIDS = ? WHERE CAGENAME = '" + cageName + "'";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, date);
        stmt.setInt(2, kids);
        stmt.execute();
        stmt.close();
    }
    
    public void recordFostering(String cageName, int kids, String date) throws SQLException {
        
        String query = "UPDATE TBL_FOSTERING SET FOSTERDATE = ?,  KIDS = ? WHERE CAGENAME = '" + cageName + "'";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, date);
        stmt.setInt(2, kids);
        stmt.execute();
        stmt.close();
    }

    public void updateKids(String cageName, int kids) throws SQLException {
//        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
//        Calendar cal = Calendar.getInstance();
//        String date = dateFormat.format(cal.getTime());

        if (kids == 0) {
            String query = "UPDATE TBL_FOSTERING SET FOSTERDATE = ?,  KIDS = ? WHERE CAGENAME = '" + cageName + "'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, null);
            stmt.setInt(2, kids);
            stmt.execute();
            stmt.close();
        } else {
            String query = "UPDATE TBL_FOSTERING SET KIDS = ? WHERE CAGENAME = '" + cageName + "'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, kids);
            stmt.execute();
            stmt.close();
        }
    }

    public boolean moveKids(String currentCage, String destinationCage, int NumberOfKidsToMove) throws SQLException {
        if (!currentCage.equals(destinationCage)) {
//            DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
//            Calendar cal = Calendar.getInstance();
//            String date = dateFormat.format(cal.getTime());

//            String query = "UPDATE TBL_FOSTERING SET FOSTERDATE = ? WHERE CAGENAME = '" + destinationCage + "'";
//            PreparedStatement stmt = conn.prepareStatement(query);
//            stmt.setString(1, date);
//            stmt.execute();
            Statement st = conn.createStatement();
            String query = "SELECT KIDS FROM TBL_FOSTERING WHERE CAGENAME = '" + currentCage + "'";
            ResultSet rs = st.executeQuery(query);
            rs.next();
            int kidsInCurrentCage = rs.getInt("KIDS");

            if (NumberOfKidsToMove > kidsInCurrentCage) {
                return false;
            }

            st = conn.createStatement();
            query = "SELECT KIDS FROM TBL_FOSTERING WHERE CAGENAME = '" + destinationCage + "'";
            rs = st.executeQuery(query);
            rs.next();
            int kidsInDestCage = rs.getInt("KIDS");

            updateKids(destinationCage, kidsInDestCage + NumberOfKidsToMove);
            updateKids(currentCage, kidsInCurrentCage - NumberOfKidsToMove);

            st.close();
//            stmt.close();
        } else {
            return false;
        }
        return true;
    }

    public ArrayList<String> getCagesWithKids() throws SQLException {
        ArrayList<String> cages = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM TBL_FOSTERING WHERE FOSTERDATE IS NOT NULL";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            cages.add(rs.getString("CAGENAME"));
        }

        stmt.close();
        return cages;
    }

    public ArrayList<String> getAllDoeCages() throws SQLException {
        ArrayList<String> cages = new ArrayList<>();
        TblRabbit tblRabbit = new TblRabbit();
        TblCage tblCage = new TblCage();

        for (int id : tblRabbit.getMatingCageIds("Doe F")) {
            cages.add(tblCage.getCageName(id));
        }

        return cages;
    }

    public void insertCage(String cageName) throws SQLException {

        String query = "INSERT INTO TBL_FOSTERING (CAGENAME) VALUES(?)";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, cageName);
        stmt.execute();
        stmt.close();
    }

}
