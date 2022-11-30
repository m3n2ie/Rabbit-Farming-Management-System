/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.sql.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author Menzi
 */
public class TblWeight {
    Connection conn;
    DecimalFormat twoDeci = new DecimalFormat("0.00");

    public TblWeight() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            //Logger.getLogger(TblCage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<Double> getWeights(int rabbitId) throws SQLException{
        ArrayList<Double> weights = new ArrayList<>();
        
        Statement stmt = conn.createStatement();
        String query = "SELECT VALUE, DATETIME from TBL_WEIGHT WHERE RABBITID = "+rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        
        while(rs.next()){
            weights.add(rs.getDouble("VALUE"));
        }
        stmt.close();
        return weights;
    }
    
    public double getCurrrentWeight(int rabbitId) throws SQLException{
        Statement stmt = conn.createStatement();
        String query = "SELECT MAX(WEIGHTID) from TBL_WEIGHT WHERE RABBITID = "+rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int weightId = rs.getInt(1);
        query = "SELECT VALUE from TBL_WEIGHT WHERE WEIGHTID = "+weightId;
        ResultSet rs2 = stmt.executeQuery(query);
        rs2.next();
        double weight = rs2.getDouble(1);
        stmt.close();
        return Double.parseDouble(twoDeci.format(weight));
    }
    
    public String getLastWeighingDate(int rabbitId) throws SQLException{
        Statement stmt = conn.createStatement();
        String query = "SELECT MAX(WEIGHTID) from TBL_WEIGHT WHERE RABBITID = "+rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int weightId = rs.getInt(1);
        query = "SELECT DATETIME from TBL_WEIGHT WHERE WEIGHTID = "+weightId;
        ResultSet rs2 = stmt.executeQuery(query);
        rs2.next();
        String date = rs2.getString("DATETIME");
        stmt.close();
        return date;
    }
    
    public ArrayList<String> getWeightDates(int rabbitId) throws SQLException{
        ArrayList<String> dates = new ArrayList<>();
        
        Statement stmt = conn.createStatement();
        String query = "select VALUE, DATETIME from TBL_WEIGHT WHERE RABBITID = "+rabbitId;
        ResultSet rs = stmt.executeQuery(query);
        
        while(rs.next()){
            dates.add(rs.getString("DATETIME"));
        }
        stmt.close();
        return dates;
    }
    
    public int getLatestWeightId() throws SQLException{
        Statement stmt = conn.createStatement();
        String query = "SELECT MAX(WEIGHTID) FROM TBL_WEIGHT";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int  id = rs.getInt(1);
        stmt.close();
        return id;
    }
    
    public void insertWeight(int rabbitId, double weight) throws SQLException{
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        
        String query = "INSERT INTO TBL_WEIGHT ( RABBITID, VALUE, DATETIME) VALUES ( ?, ?, ? )";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.setDouble(2, Double.parseDouble(twoDeci.format(weight)));
        stmt.setString(3, date);
        stmt.execute();
        
        TblWeight tblWeight = new TblWeight();
        TblRabbit tblRabbit = new TblRabbit();
        
        tblRabbit.updateWeightId(tblWeight.getLatestWeightId(), rabbitId);
        stmt.close();
    }
    
    public void insertWeight(int rabbitId, double weight, String date) throws SQLException{
        
        String query = "INSERT INTO TBL_WEIGHT ( RABBITID, VALUE, DATETIME) VALUES ( ?, ?, ? )";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, rabbitId);
        stmt.setDouble(2, Double.parseDouble(twoDeci.format(weight)));
        stmt.setString(3, date);
        stmt.execute();
        
        TblWeight tblWeight = new TblWeight();
        TblRabbit tblRabbit = new TblRabbit();
        
        tblRabbit.updateWeightId(tblWeight.getLatestWeightId(), rabbitId);
        stmt.close();
    }
    
}
