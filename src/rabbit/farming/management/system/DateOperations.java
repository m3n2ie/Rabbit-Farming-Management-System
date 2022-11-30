/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author User
 */
public class DateOperations {
    
    public DateOperations(){
        
    }
    
    public static int getDifferenceInDays(Date date) {
        Calendar cal1 = Calendar.getInstance();
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);

        return (cal1.get(Calendar.DAY_OF_YEAR) + (cal1.get(Calendar.YEAR)*365) ) - ( cal2.get(Calendar.DAY_OF_YEAR) + (cal2.get(Calendar.YEAR)*365));
    }
    
    public static int getDifferenceInDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);

        return (cal1.get(Calendar.DAY_OF_YEAR) + (cal1.get(Calendar.YEAR)*365) ) - ( cal2.get(Calendar.DAY_OF_YEAR) + (cal2.get(Calendar.YEAR)*365));
    }
}
