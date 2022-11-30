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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Menzi
 */
public class TblNotification {

    Connection conn;

    public TblNotification() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            //Logger.getLogger(TblCage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int countNotifications() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT(NOTIFID) FROM TBL_NOTIFICATION";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public ArrayList<String> getNotifications() throws SQLException {
        ArrayList<String> notifs = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT MESSAGE FROM TBL_NOTIFICATION";
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            notifs.add(rs.getString(1));
        }
        stmt.close();
        return notifs;
    }

    public void createNotifications() throws SQLException {
        //Deletes old notifications and inserts new notifications
        String query_update = "DELETE FROM TBL_NOTIFICATION";
        PreparedStatement stmt_update = conn.prepareStatement(query_update);
        stmt_update.executeUpdate();
        
        TblMating tblMating = new TblMating();
        TblCage tblCage = new TblCage();

        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        for (int cageId : tblMating.getCageIdsNeedingNests()) {
            
            String query = "INSERT INTO TBL_NOTIFICATION (MESSAGE, DATETIME, SEEN) VALUES( ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "Put Nest Box in Cage "+tblCage.getCageName(cageId));
            stmt.setString(2, date);
            stmt.setBoolean(3, false);
            
            stmt.execute();
            stmt.close();
        }
        
        for (int cageId : tblMating.getCageIdsWithKidsToMove()) {
            
            String query = "INSERT INTO TBL_NOTIFICATION (MESSAGE, DATETIME, SEEN) VALUES( ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "Remove Nest Box From Cage "+tblCage.getCageName(cageId));
            stmt.setString(2, date);
            stmt.setBoolean(3, false);
            
            stmt.execute();
            stmt.close();
        }
        
        stmt_update.close();
        
    }

}
