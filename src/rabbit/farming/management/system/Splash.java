/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import javax.swing.*;

/**
 *
 * @author Menzi
 */
public class Splash extends JFrame {

    JLabel jLabel1 = new javax.swing.JLabel();
    JProgressBar jProgressBar1 = new javax.swing.JProgressBar();
    JPanel panel = new JPanel();

    Splash() {
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/splash.png")));
        jProgressBar1.setForeground(new java.awt.Color(102, 255, 153));
        jProgressBar1.setValue(0);
        jProgressBar1.setStringPainted(true);

        panel.setBackground(new java.awt.Color(245, 255, 245));
        panel.setLayout(new BorderLayout());

        this.add(panel);
        panel.add(jLabel1, BorderLayout.CENTER);
        panel.add(jProgressBar1, BorderLayout.SOUTH);
        this.setBounds(0, 0, 600, 314);
        this.setUndecorated(true);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
    }
    
    public void updateProgress() {
        int i = 0;
        while (i <= 100) {
            jProgressBar1.setValue(i);
            i = i + 1;
            try {
                Thread.sleep((int) Math.random() * 2 + 104);
            } catch (Exception e) {
                
            }
        }
        this.setVisible(false);
    }
    
}
