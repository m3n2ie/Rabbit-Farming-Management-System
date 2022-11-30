/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.awt.Color;
import java.awt.Font;
import java.awt.print.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author User
 */
public class Reports extends javax.swing.JFrame {

    /**
     * Creates new form Print
     */
    Connection conn = null;

    public Reports() {
        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            Logger.getLogger(Reports.class.getName()).log(Level.SEVERE, null, ex);
        }
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Print");
        setResizable(false);
        setType(java.awt.Window.Type.UTILITY);

        jPanel1.setBackground(new java.awt.Color(245, 255, 245));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(" "));

        jComboBox1.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cages with Selling Stock", "Cages with Breeding Stock", "Mating Ready Rabbits", "Cages to Put Nest Boxes in", "Cages to Remove Nest Boxes from" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(204, 255, 204));
        jButton1.setText("Print");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //jTable1.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        //jTable1.setGridColor(Color.white);
        //jScrollPane1.setSize(930, 240);
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String todaysDate = dateFormat.format(cal.getTime());

        MessageFormat header = new MessageFormat(jComboBox1.getSelectedItem().toString() + " (" + todaysDate + ") ");
        try {
            jTable1.print(JTable.PrintMode.FIT_WIDTH, header, null);
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(null, "Couldn't Print Selected Records", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        try {
            DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
            Calendar cal = Calendar.getInstance();
            String todaysDate = dateFormat.format(cal.getTime());

            TblCage tblCage = new TblCage();
            TblMating tblMating = new TblMating();
            TblRabbit tblRabbit = new TblRabbit();
            TblWeight tblWeight = new TblWeight();

            Statement stmt = conn.createStatement();

            DefaultTableModel model = null;

            if (jComboBox1.getSelectedItem().toString().equals("Cages to Put Nest Boxes in")) {

                model = new DefaultTableModel(new String[]{"Cage Name", "",}, 0);
                for (int cageId : tblMating.getCageIdsNeedingNests()) {
                    model.addRow(new Object[]{tblCage.getCageName(cageId), ""});
                }

            } else if (jComboBox1.getSelectedItem().toString().equals("Cages with Breeding Stock")) {

                stmt = conn.createStatement();
                String query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE AND STOCKTYPE = 'Breeding Stock'";
                ResultSet rs = stmt.executeQuery(query);

                model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Weight (kg)", "Birth Date", "Age", "Alive Kids", "Dead Kids"}, 0);
                while (rs.next()) {
                    String r1 = tblCage.getCageName(tblRabbit.getCageId(rs.getInt("RABBITID")));
                    String r2 = rs.getString("GENDER");
                    String r3 = "";
                    String r4 = rs.getString("BIRTHDATE");
                    String r5 = tblMating.countKids(rs.getInt("RABBITID")) + "";
                    String r6 = tblMating.countDeadKids(tblMating.countDeadKids(rs.getInt("RABBITID"))) + "";
                    if (rs.getInt("WEIGHTID") < 0) {
                        r3 = "Not Available";
                    } else {
                        r3 = tblWeight.getCurrrentWeight(rs.getInt("RABBITID")) + "";
                    }
                    String r7 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";

                    model.addRow(new Object[]{r1, r2, r3, r4, r7, r5, r6});
                }

            } else if (jComboBox1.getSelectedItem().toString().equals("Cages with Selling Stock")) {

                stmt = conn.createStatement();
                String query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE AND STOCKTYPE = 'Selling Stock'";
                ResultSet rs = stmt.executeQuery(query);

                model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Weight (kg)", "Birth Date", "Age"}, 0);
                while (rs.next()) {
                    String r1 = tblCage.getCageName(tblRabbit.getCageId(rs.getInt("RABBITID")));
                    String r2 = rs.getString("GENDER");
                    String r3 = "";
                    String r4 = rs.getString("BIRTHDATE");
                    if (rs.getInt("WEIGHTID") < 0) {
                        r3 = "Not Available";
                    } else {
                        r3 = tblWeight.getCurrrentWeight(rs.getInt("RABBITID")) + "";
                    }
                    String r5 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";

                    model.addRow(new Object[]{r1, r2, r3, r4, r5});
                }

            } else if (jComboBox1.getSelectedItem().toString().equals("Mating Ready Rabbits")) {
                stmt = conn.createStatement();
                String query = "SELECT * FROM TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = False AND ACTIVE = True ORDER BY GENDER ASC";
                ResultSet rs = stmt.executeQuery(query);

                model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Age", "Dry Days"}, 0);
                while (rs.next()) {
                    String r1 = tblCage.getCageName(tblRabbit.getCageId(rs.getInt("RABBITID")));
                    String r2 = rs.getString("GENDER");
                    String r3 = DateOperations.getDifferenceInDays(new Date(rs.getString("BIRTHDATE"))) + " days";
                    String r4 = "Not_available";
                    if (!tblMating.getLatestMatingDate(rs.getInt("RABBITID")).equals("")) {
                        r4 = DateOperations.getDifferenceInDays(new Date(todaysDate), new Date(tblMating.getLatestMatingDate(rs.getInt("RABBITID")))) + " days";
                    }

                    model.addRow(new Object[]{r1, r2, r3, r4});
                }

            } else if (jComboBox1.getSelectedItem().toString().equals("Cages with Kids to Move")) {
                model = new DefaultTableModel(new String[]{"Cage Name", "",}, 0);
                for (int cageId : tblMating.getCageIdsWithKidsToMove()) {
                    model.addRow(new Object[]{tblCage.getCageName(cageId), ""});
                }
            }

            jTable1.setModel(model);

            TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jTable1.getModel());
            jTable1.setRowSorter(sorter);
            List<RowSorter.SortKey> sortKeys = new ArrayList<>();
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            if (jComboBox1.getSelectedItem().toString().equals("Mating Ready Rabbits")) {
                sortKeys.add(new RowSorter.SortKey(3, SortOrder.DESCENDING));
            }
            sorter.setSortKeys(sortKeys);

        } catch (SQLException ex) {
            Logger.getLogger(Reports.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Reports.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Reports().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}