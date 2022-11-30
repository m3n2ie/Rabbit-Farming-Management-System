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

/**
 *
 * @author User
 */
public class TblOptions {
    
    Connection conn;
    
    public TblOptions(){
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            //Logger.getLogger(TblCage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setOption(String opName, String opValue) throws SQLException{
        String query = "UPDATE TBL_OPTIONS SET OPVALUE = ? WHERE OPNAME = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, opValue);
        stmt.setString(2, opName);
        stmt.executeUpdate();
        stmt.close();
    }
    
    public String getOptionValue(String opName) throws SQLException{
        Statement stmt = conn.createStatement();
        String query = "SELECT OPVALUE FROM TBL_OPTIONS WHERE OPNAME= '"+opName+"'";
        ResultSet rs = stmt.executeQuery(query);
        rs.next();
        String value = rs.getString(1);
        stmt.close();
        return value;
    }
    
}
