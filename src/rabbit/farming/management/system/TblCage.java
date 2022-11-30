/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author Menzi
 */
public class TblCage {

    Connection conn;

    public TblCage() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            //Logger.getLogger(TblCage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<String> getCageNames() throws SQLException {
        ArrayList<String> cages = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT CAGENAME FROM TBL_CAGE WHERE ACTIVE = True";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            cages.add(rs.getString("CAGENAME"));
        }

        Collections.sort(cages, new CompareCages());
        stmt.close();
        return cages;
    }

    public ArrayList<Integer> getNonEmptyCageIds() throws SQLException {//Gets Cages With Rabbit(s) In Them
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT CAGEID FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("CAGEID"));
        }
        
        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public ArrayList<Integer> getAllCageIds() throws SQLException {
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT TBL_CAGE.CAGEID FROM TBL_CAGE WHERE ACTIVE = True";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("CAGEID"));
        }
        
        Collections.sort(ids);
        stmt.close();
        return ids;
    }

    public ArrayList<Integer> getEmptyCageIds() throws SQLException {
        ArrayList<Integer> ids = this.getAllCageIds();

        ids.removeAll(this.getNonEmptyCageIds());

        Collections.sort(ids);
        return ids;
    }

    public ArrayList<Integer> getEmptyCageIdsSellingStock(int maxCapacity) throws SQLException {
        ArrayList<Integer> ids = this.getAllCageIds();

        ArrayList<Integer> ids_copy = this.getAllCageIds();

        TblCage tblCage = new TblCage();

        for (Integer id : ids) {
            if (tblCage.getRabbitCount(id) >= maxCapacity) {
                ids_copy.remove(id);
            }else if(tblCage.getRabbitCount(id)>0){
                if(this.getStockType(id).equals("Breeding Stock")){
                   ids_copy.remove(id); 
                }
            }
        }

        Collections.sort(ids_copy);
        return ids_copy;
    }

    public int getRabbitCount(int cageId) throws SQLException {//Returns number of rabbits currently in cage
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT(CAGEID) FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False AND CAGEID = "+cageId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count =rs.getInt(1); 
        stmt.close();
        
        return count;
    }

    public int getRabbitCount(int cageId, String gender) throws SQLException {//Returns number of rabbits currently in cage
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT(CAGEID) FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False AND GENDER = '" + gender + "' AND CAGEID = "+cageId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count =rs.getInt(1);
        stmt.close();
        
        return count;
    }

    public int getCageId(String cageName) throws SQLException {
        if(cageName.equals("")){
            return 0;
        }
        Statement stmt = conn.createStatement();
        String query = "SELECT CAGEID FROM TBL_CAGE WHERE CAGENAME ='" + cageName + "'";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int id = rs.getInt("CAGEID");
        stmt.close();
        return id;
    }

    public String getCageName(int cageId) throws SQLException {

        Statement stmt = conn.createStatement();
        String query = "SELECT CAGENAME FROM TBL_CAGE WHERE CAGEID =" + cageId + "";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        String name = rs.getString("CAGENAME");
        stmt.close();
        
        return name;
    }

    public int getMaxTier() throws SQLException {
        int maxTier = 0;

        for (String c : this.getCageNames()) {
            if (Integer.parseInt(c.substring(c.indexOf("T") + 1)) > maxTier) {
                maxTier = Integer.parseInt(c.substring(c.length() - 1));
            }
        }

        return maxTier;
    }

    public int getMaxColumn() throws SQLException {
        int maxColumn = 0;

        for (String c : this.getCageNames()) {
            if (Integer.parseInt(c.substring(c.indexOf("C") + 1, c.indexOf("T"))) > maxColumn) {
                maxColumn = Integer.parseInt(c.substring(c.indexOf("C") + 1, c.indexOf("T")));
            }
        }

        return maxColumn;
    }

    public int getMaxRow() throws SQLException {
        int maxRow = 0;

        for (String c : this.getCageNames()) {
            if (Integer.parseInt(c.substring(1, c.indexOf("C"))) > maxRow) {
                maxRow = Integer.parseInt(c.substring(1, c.indexOf("C")));
            }
        }

        return maxRow;
    }

    public boolean hasCage(String cageName) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM TBL_CAGE WHERE CAGENAME = '" + cageName + "'";
        ResultSet rs = stmt.executeQuery(query);
        boolean result = rs.next();
        stmt.close();
        
        return result;
    }

    public void insertCage(String cageName) throws SQLException {

        String query = "INSERT INTO TBL_CAGE ( CAGENAME, NESTINDATE, ACTIVE)VALUES( ?, ?, ? )";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, cageName);
        stmt.setString(2, null);
        stmt.setBoolean(3, true);
        stmt.execute();
        stmt.close();
    }

    public int countCages() throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT COUNT(CAGEID) from TBL_CAGE WHERE ACTIVE = True";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        return count;
    }

    public void placeNestBox(int cageId) throws SQLException {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String query = "UPDATE TBL_CAGE SET NESTINDATE = ? WHERE CAGEID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, date);
        stmt.setInt(2, cageId);
        stmt.executeUpdate();
        stmt.close();
    }

    public void removeNestBox(int cageId) throws SQLException {
        String query = "UPDATE TBL_CAGE SET NESTINDATE = ? WHERE CAGEID = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, null);
        stmt.setInt(2, cageId);
        stmt.executeUpdate();
        stmt.close();
    }

    public int countCages(String stockType) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT DISTINCT TBL_CAGE.CAGEID from TBL_CAGE, TBL_RABBIT WHERE TBL_RABBIT.CAGEID "
                + "= TBL_CAGE.CAGEID AND TBL_RABBIT.SOLD = False AND DEATHDATE IS NULL"
                + " AND TBL_RABBIT.STOCKTYPE = '" + stockType + "'";
        ResultSet rs = stmt.executeQuery(query);
        int count = 0;
        while(rs.next()){
            count++;
        }
        stmt.close();
        return count;
    }

    public int countEmptyCages() throws SQLException {
        return this.getEmptyCageIds().size();
    }
    
    public ArrayList<Integer> getRabbitIdsInCage(int cageId) throws SQLException {//Returns number of rabbits currently in cage
        
        ArrayList<Integer> ids = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False AND CAGEID = "+cageId;
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            ids.add(rs.getInt("RABBITID"));
        }
        
        stmt.close();
        return ids;
    }
    
    public ArrayList<String> getCagesWithNests() throws SQLException {//Returns number of rabbits currently in cage
        
        ArrayList<String> cages = new ArrayList<>();

        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM TBL_CAGE WHERE NESTINDATE IS NOT NULL";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            cages.add(rs.getString("CAGENAME"));
        }
        
        stmt.close();
        return cages;
    }
    
    public String getStockType(int cageId) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT STOCKTYPE FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False AND CAGEID = "+cageId;
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        String type = rs.getString("STOCKTYPE");
        stmt.close();
        return type;
    }
    
    public boolean hasNest(int cageId) throws SQLException {
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM TBL_CAGE WHERE NESTINDATE IS NOT NULL AND CAGEID = "+ cageId;
        ResultSet rs = stmt.executeQuery(query);
        boolean result = rs.next();
        stmt.close();
        
        return result;
    }
    
}
