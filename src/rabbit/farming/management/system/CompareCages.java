/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.util.Comparator;

/**
 *
 * @author Menzi
 */
public class CompareCages implements Comparator<String>, java.io.Serializable {

    @Override
    public int compare(String s1, String s2) {
        s1 = s1.replace("R", "");
        s1 = s1.replace("C", ";");
        s1 = s1.replace("T", ";");

        s2 = s2.replace("R", "");
        s2 = s2.replace("C", ";");
        s2 = s2.replace("T", ";");

        String[] temp1 = s1.split(";");
        String[] temp2 = s2.split(";");

        int sum1 = 0, sum2 = 0;

        for (int i = 0; i < 3; i++) {
            sum1 += Integer.parseInt(temp1[i]);
            sum2 += Integer.parseInt(temp2[i]);
        }

        if (sum1 < sum2) {
            return -1;
        } else if (sum1 == sum2) {
            return 0;
        } else {
            return 1;
        }
    }

}
