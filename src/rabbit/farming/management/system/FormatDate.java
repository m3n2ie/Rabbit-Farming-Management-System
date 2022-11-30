/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

/**
 *
 * @author Menzi
 */
public class FormatDate {
    public String format(String dateStr){
        return dateStr.substring(8, 10)+" "+dateStr.substring(4, 7)+" "+dateStr.substring(dateStr.length()-4, dateStr.length());
    }
    
    public String formatTime(String dateTimeStr){
        return dateTimeStr.substring(8, 10)+" "+dateTimeStr.substring(4, 7)+" "
                +dateTimeStr.substring(dateTimeStr.length()-4, dateTimeStr.length())+" "+dateTimeStr.substring(11, 16);
    }
}
