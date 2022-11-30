/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 *
 * @author Menzi
 */
public class RabbitFarmingManagementSystem {

    /**
     * @param args the command line arguments
     * @throws java.sql.SQLException
     */
    public static void createDatabase() {
        try {

            Connection conn = DriverManager.getConnection("jdbc:derby:C:\\Program Files\\Conejo\\_database\\rfms_database\\;create=true");
            JOptionPane.showMessageDialog(null, "Database Created");
            // Do something with the Connection
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    public static int getDifferenceInDays(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);

        return (cal1.get(Calendar.DAY_OF_YEAR) + (cal1.get(Calendar.YEAR) * 365)) - (cal2.get(Calendar.DAY_OF_YEAR) + (cal2.get(Calendar.YEAR) * 365));
    }

    public static int getDifferenceInDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return (cal1.get(Calendar.DAY_OF_YEAR) + (cal1.get(Calendar.YEAR) * 365)) - (cal2.get(Calendar.DAY_OF_YEAR) + (cal2.get(Calendar.YEAR) * 365));
    }

    Connection conn = null;

    public static void main(String[] args) {
        for(int i=1; i<=40; i++){
            for(int j=1; j<=10; j++){
                for(int k=1; k<=2; k++){
                    System.out.println("INSERT INTO TBL_FOSTERING (CAGENAME) VALUES('R"+i+"C"+j+"T"+k+"');");
                }
            }
        }
    }

}
