/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbit.farming.management.system;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Menzi
 */
public class MainFrame extends javax.swing.JFrame {

    TblCage tblCage = new TblCage();
    TblWeight tblWeight = new TblWeight();
    TblRabbit tblRabbit = new TblRabbit();
    TblMating tblMating = new TblMating();
    TblNotification tblNotification = new TblNotification();
    TblOptions tblOptions = new TblOptions();
    TblFostering tblFostering = new TblFostering();

    public static double matingWeightThreshold = 2.3;
    public static double sellingWeightThreshold = 1.9;
    public static int maxRabbitsInSellingCage = 10;
    public static int daysBeforePuttingNest = 28;
    public static int daysBeforeNestOut = 33;
    String date = "";

    boolean refreshing = true; //Tells The UI Components When The Program Is Refreshing

    Connection conn = null;

    boolean firstTimeLaunch = true;

    public MainFrame() {
        //splash.setProgress(20);

        initComponents();

        try {
            conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database Missing " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

            File dest = new File("C:/Conejo");
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Backup");

            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.showOpenDialog(null);

            try {
                File source = new File(fileChooser.getSelectedFile().getAbsolutePath());
            } catch (NullPointerException e) {
                JOptionPane.showMessageDialog(null, "Couldn't Restore Database, No Backup Selected " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            try {
                conn = DriverManager.getConnection("jdbc:derby:C:/Conejo/_database/rfms_database;restoreFrom=" + fileChooser.getSelectedFile().getAbsolutePath() + "/rfms_database");
                JOptionPane.showMessageDialog(null, "Database Restored Successfully");
                System.exit(0);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Couldn't Restore Database " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    public void updateTitleTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Calendar cal = Calendar.getInstance();
        date = dateFormat.format(cal.getTime());

        this.setTitle("Conejo - Rabbit Farming Management System 2.1                         " + date);
    }

    public void paintCages() {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    TblCage tblCage = new TblCage();

                    ArrayList<String> cages = tblCage.getCageNames();
                    int size = cages.size();
                    int loaded = 0;

                    jPanel22.removeAll();
                    for (int k = 0; k < tblCage.getMaxRow(); k++) {
                        for (int i = 0; i < tblCage.getMaxColumn(); i++) {
                            if (size != 0) {
                                JPanel panel = new JPanel();
                                panel.setBackground(new java.awt.Color(245, 255, 245));
                                panel.setFont(new java.awt.Font("Segio Light", 0, 12));
                                panel.setLayout(new GridLayout(tblCage.getMaxTier(), 1));
                                jPanel22.setLayout(new GridLayout(tblCage.getMaxRow(), tblCage.getMaxColumn()));
                                jPanel22.add(panel);
                                panel.setBorder(javax.swing.BorderFactory.createTitledBorder("R" + (k + 1) + "C" + (i + 1)));
                                panel.setSize(140, 140);
                                for (int j = 0; j < tblCage.getMaxTier(); j++) {
                                    if (cages.contains("R" + (k + 1) + "C" + (i + 1) + "T" + (j + 1))) {
                                        JButton btn = new JButton("R" + (k + 1) + "C" + (i + 1) + "T" + (j + 1));
                                        btn.setBackground(new java.awt.Color(245, 255, 245));
                                        panel.add(btn);
                                        btn.setSize(30, 30);
                                        btn.setLocation(j, j * 30);
                                        panel.repaint();

                                        btn.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                CageInfo.cageName = btn.getText();
                                                new CageInfo().show();
                                            }
                                        });

                                        size--;
                                        loaded++;
                                    } else {
                                        JPanel empty_panel = new JPanel();
                                        empty_panel.setSize(30, 30);
                                        empty_panel.setBackground(new java.awt.Color(245, 255, 245));
                                        panel.add(empty_panel);
                                    }

                                    //splash.updateProgress( ( (loaded / cages.size()) * 40) + 40);
                                }
                            }
                        }
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        new Thread(r).start();
    }

    public void refresh() {
        try {
            matingWeightThreshold = Double.parseDouble(tblOptions.getOptionValue("MatingWeightThreshold"));
            sellingWeightThreshold = Double.parseDouble(tblOptions.getOptionValue("SellingWeightThreshold"));
            maxRabbitsInSellingCage = Integer.parseInt(tblOptions.getOptionValue("MaximumRabbitsInSellingCage"));
            daysBeforePuttingNest = Integer.parseInt(tblOptions.getOptionValue("DaysBeforePuttingNest"));
            daysBeforeNestOut = Integer.parseInt(tblOptions.getOptionValue("DaysBeforeMovingKids")); //DaysBeforeTakingNestOut

            //Populates The ComboBoxes
            for (int id : tblMating.getRabbitsWithKidsCageIds("Doe F")) {
                jComboBox24.addItem(tblCage.getCageName(id));
            }
            //splash.setProgress(7);
            for (int id : tblMating.getPregnantMatingCageIds("Doe F")) {
                jComboBox13.addItem(tblCage.getCageName(id));
                jComboBox18.addItem(tblCage.getCageName(id));
                jComboBox14.addItem(tblCage.getCageName(id));
            }
            //splash.setProgress(9);
            for (int id : tblRabbit.getMatingCageIds("Buck M")) {
                jComboBox2.addItem(tblCage.getCageName(id));
            }
            //splash.setProgress(11);
            for (int id : tblRabbit.getMatingCageIds("Doe F")) {
                jComboBox3.addItem(tblCage.getCageName(id));
            }
            for (int id : tblRabbit.getCageIds("Buck M")) {
                jComboBox23.addItem(tblCage.getCageName(id));
            }
            for (int id : tblRabbit.getCageIds("Doe F")) {
                jComboBox22.addItem(tblCage.getCageName(id));
            }
            for (String cageName : tblFostering.getCagesWithKids()) {
                jComboBox36.addItem(cageName);
                jComboBox34.addItem(cageName);
                jComboBox31.addItem(cageName);
            }
            //splash.setProgress(20);

            for (int id : tblCage.getEmptyCageIds()) {
                jComboBox1.addItem(tblCage.getCageName(id));
                jComboBox11.addItem(tblCage.getCageName(id));
            }
            //splash.setProgress(25);
            for (int id : tblCage.getNonEmptyCageIds()) {
                jComboBox4.addItem(tblCage.getCageName(id));
                jComboBox5.addItem(tblCage.getCageName(id));
                if (tblCage.getStockType(id).equals("Breeding Stock")) {
                    jComboBox10.addItem(tblCage.getCageName(id));
                }
            }
            //splash.updateProgress(27);
            for (int id : tblRabbit.getStockCageIds("Breeding Stock")) {
                jComboBox8.addItem(tblCage.getCageName(id));
            }
            for (int id : tblRabbit.getStockCageIds("Selling Stock")) {
                jComboBox9.addItem(tblCage.getCageName(id));
            }
            for (int id : tblRabbit.getActiveRabbitIds()) {
                jComboBox6.addItem(tblCage.getCageName(tblRabbit.getCageId(id)));
            }
            //splash.updateProgress(30);
            for (int id : tblCage.getNonEmptyCageIds()) {
                if (tblCage.getStockType(id).equals("Breeding Stock")) {
                    jComboBox29.addItem(tblCage.getCageName(id));
                }
            }
            for (int id : tblCage.getEmptyCageIdsSellingStock(maxRabbitsInSellingCage)) {
                jComboBox30.addItem(tblCage.getCageName(id));
            }
            for (int id : tblCage.getNonEmptyCageIds()) {
                if (tblCage.getStockType(id).equals("Selling Stock")) {
                    jComboBox26.addItem(tblCage.getCageName(id));
                }
            }
            //splash.updateProgress(40);
            for (int id : tblCage.getEmptyCageIds()) {
                jComboBox27.addItem(tblCage.getCageName(id));
            }
            for (int id : tblRabbit.getMatedRabbitIds()) {
                boolean hasItem = false;

                for (int i = 0; i < jComboBox7.getItemCount(); i++) {
                    if (jComboBox7.getItemAt(i).equals(tblCage.getCageName(tblRabbit.getCageId(id)))) {
                        hasItem = true;
                    }
                }

                if (!hasItem) {
                    jComboBox7.addItem(tblCage.getCageName(tblRabbit.getCageId(id)));
                }
            }
            //splash.updateProgress(40);

            jSpinner5.hide();
            jLabel42.hide();
            jComboBox16.hide();

            //Table 1
            Statement stmt = conn.createStatement();
            String query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE";
            ResultSet rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Stock Type", "Birth Date", "Age"}, 0);
            while (rs.next()) {
                int r1 = rs.getInt("CAGEID");
                String r2 = rs.getString("GENDER");
                String r3 = rs.getString("STOCKTYPE");
                String r4 = rs.getString("BIRTHDATE");
                String r5 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";
                model.addRow(new Object[]{tblCage.getCageName(r1), r2, r3, r4, r5});
            }
            jTable1.setModel(model);
            //splash.updateProgress(83);

            //Table 2
            stmt = conn.createStatement();
            query = "SELECT * from TBL_MATING WHERE MALERABBITID = "
                    + tblRabbit.getRabbitId(jComboBox7.getItemCount() == 0 ? 0 : tblCage.getCageId(jComboBox7.getSelectedItem().toString()))
                    + "OR FEMALERABBITID = " + tblRabbit.getRabbitId(jComboBox7.getItemCount() == 0 ? 0 : tblCage.getCageId(jComboBox7.getSelectedItem().toString()));
            rs = stmt.executeQuery(query);

            model = new DefaultTableModel(new String[]{"Doe Cage", "Buck Cage", "Mating Date", "Pregnancy", "Kids", "Kids Birth Date"}, 0);
            while (rs.next()) {
                int r1 = rs.getInt("FEMALERABBITID");
                int r2 = rs.getInt("MALERABBITID");
                String r3 = rs.getString("DATETIME");
                boolean r4 = rs.getBoolean("PREGNANCY");
                int r5 = rs.getInt("KIDS");
                String r6 = rs.getString("BIRTHDATE");
                model.addRow(new Object[]{tblCage.getCageName(tblRabbit.getCageId(r1)), tblCage.getCageName(tblRabbit.getCageId(r2)), r3, r4, r5, r6});
            }
            jTable2.setModel(model);
            //splash.updateProgress(87);

            //Table 3
            stmt = conn.createStatement();
            query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE AND STOCKTYPE = 'Breeding Stock'";
            rs = stmt.executeQuery(query);

            model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Weight (kg)", "Birth Date", "Age", "Total Number of Kids", "Dead Kids"}, 0);
            while (rs.next()) {
                String r1 = tblCage.getCageName(tblRabbit.getCageId(rs.getInt("RABBITID")));
                String r2 = rs.getString("GENDER");
                String r3 = "";
                String r4 = rs.getString("BIRTHDATE");
                String r5 = tblMating.countKids(rs.getInt("RABBITID")) + "";
                String r6 = tblMating.countDeadKids(rs.getInt("RABBITID")) + "";
                if (rs.getInt("WEIGHTID") < 0) {
                    r3 = "Not Available";
                } else {
                    r3 = tblWeight.getCurrrentWeight(rs.getInt("RABBITID")) + "";
                }
                String r7 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";

                model.addRow(new Object[]{r1, r2, r3, r4, r7, r5, r6});
            }
            jTable3.setModel(model);
            //splash.updateProgress(89);

            //Table 4
            stmt = conn.createStatement();
            query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE AND STOCKTYPE = 'Selling Stock'";
            rs = stmt.executeQuery(query);

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
            jTable4.setModel(model);
            //splash.updateProgress(91);

            //Table 5
            stmt = conn.createStatement();
            query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE ";
            rs = stmt.executeQuery(query);

            model = new DefaultTableModel(new String[]{"Cage Name", "Weight (kg)", "Weighing Date", "Stock Type", "Birth Date"}, 0);
            while (rs.next()) {
                String r1 = tblCage.getCageName(tblRabbit.getCageId(rs.getInt("RABBITID")));
                if (rs.getInt("WEIGHTID") > 0) {
                    String r2 = tblWeight.getCurrrentWeight(rs.getInt("RABBITID")) + "";
                    String r3 = tblWeight.getLastWeighingDate(rs.getInt("RABBITID"));
                    String r4 = tblRabbit.getStockType(rs.getInt("RABBITID"));
                    String r5 = rs.getString("BIRTHDATE");
                    model.addRow(new Object[]{r1, r2, r3, r4, r5});
                }
            }
            jTable10.setModel(model);
            //splash.updateProgress(93);

            //Table 6
            stmt = conn.createStatement();
            query = "SELECT * from TBL_CAGE WHERE NESTINDATE IS NOT NULL ";
            rs = stmt.executeQuery(query);

            model = new DefaultTableModel(new String[]{"Cage Name", "Date Nest Placed"}, 0);
            while (rs.next()) {
                String r1 = rs.getString("CAGENAME");
                String r2 = rs.getString("NESTINDATE");
                model.addRow(new Object[]{r1, r2});
            }
            jTable5.setModel(model);

            //Table 7
            stmt = conn.createStatement();
            query = "SELECT * from TBL_FOSTERING WHERE FOSTERDATE IS NOT NULL";
            rs = stmt.executeQuery(query);

            model = new DefaultTableModel(new String[]{"Cage Name", "Number of Kids in Cage", "Kids Age"}, 0);
            while (rs.next()) {
                String r1 = rs.getString("CAGENAME");
                String r2 = rs.getInt("KIDS") + "";
                String r3 = rs.getString("FOSTERDATE");
                r3 = DateOperations.getDifferenceInDays(new Date(r3)) + " days";
                model.addRow(new Object[]{r1, r2, r3});
            }
            jTable11.setModel(model);

            //Sort Tables
//            TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(jTable1.getModel());
//            jTable1.setRowSorter(sorter);
//            java.util.List<RowSorter.SortKey> sortKeys = new ArrayList<>();
//            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
//            sorter.setSortKeys(sortKeys);
//
//            sorter = new TableRowSorter<TableModel>(jTable2.getModel());
//            jTable2.setRowSorter(sorter);
//            sorter.setSortKeys(sortKeys);
//
//            sorter = new TableRowSorter<TableModel>(jTable3.getModel());
//            jTable3.setRowSorter(sorter);
//            sorter.setSortKeys(sortKeys);
//
//            sorter = new TableRowSorter<TableModel>(jTable4.getModel());
//            jTable4.setRowSorter(sorter);
//            sorter.setSortKeys(sortKeys);
//
//            sorter = new TableRowSorter<TableModel>(jTable10.getModel());
//            jTable10.setRowSorter(sorter);
//            sorter.setSortKeys(sortKeys);
//
//            sorter = new TableRowSorter<TableModel>(jTable5.getModel());
//            jTable5.setRowSorter(sorter);
//            sorter.setSortKeys(sortKeys);
            //Dashboard
            tblNotification.createNotifications();

            jLabel18.setText(tblCage.countCages() + "");
            jLabel33.setText(tblRabbit.countRabbits() + "");
            jLabel35.setText(tblMating.countMating() + "");
            jLabel37.setText(tblNotification.countNotifications() + "");

            jTable9.setValueAt(tblCage.countCages("Breeding Stock"), 0, 1);//Cages
            jTable9.setValueAt(tblCage.countCages("Selling Stock"), 1, 1);
            jTable9.setValueAt(tblCage.countEmptyCages(), 2, 1);

            jTable8.setValueAt(tblRabbit.countRabbits("Breeding Stock"), 0, 1);//Rabbits
            jTable8.setValueAt(tblRabbit.countRabbits("Selling Stock"), 1, 1);
            jTable8.setValueAt(tblRabbit.countMarketReadyRabbits(sellingWeightThreshold), 3, 1);
            jTable8.setValueAt(tblRabbit.countRabbitsSold(), 4, 1);
            jTable8.setValueAt(tblRabbit.countRabbitsDeaths(), 5, 1);

            jTable6.setValueAt(tblRabbit.getMatingCageIds("Doe F").size(), 0, 1);//Matings
            jTable6.setValueAt(tblRabbit.getMatingCageIds("Buck M").size(), 1, 1);
            jTable6.setValueAt(tblMating.countPregnancies(true), 3, 1);
            jTable6.setValueAt(tblMating.countPregnancies(false), 4, 1);
            jTable6.setValueAt(tblMating.countKids(), 5, 1);

            int count = 0;//Notifications
            for (int i = 0; i < jTable7.getRowCount(); i++) {//Remove old notifications
                jTable7.setValueAt("", i, 0);
            }
            for (String notif : tblNotification.getNotifications()) {
                jTable7.setValueAt(notif, count, 0);
                count++;
            }

            //splash.updateProgress(98);
            //Disables All Action Buttons
        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        //splash.setProgress(100);
        jSpinner9.setVisible(false);
        jSpinner12.setVisible(false);
        jSpinner15.setVisible(false);
        jSpinner17.setVisible(false);
        jSpinner16.setVisible(false);

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        refreshing = false;

        updateTitleTime();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jComboBox33 = new javax.swing.JComboBox();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu8 = new javax.swing.JMenu();
        jMenu9 = new javax.swing.JMenu();
        jTabbedPane5 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jPanel33 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jPanel34 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jPanel35 = new javax.swing.JPanel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jPanel36 = new javax.swing.JPanel();
        jPanel37 = new javax.swing.JPanel();
        jScrollPane14 = new javax.swing.JScrollPane();
        jTable9 = new javax.swing.JTable();
        jPanel38 = new javax.swing.JPanel();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTable8 = new javax.swing.JTable();
        jPanel39 = new javax.swing.JPanel();
        jScrollPane11 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        jPanel40 = new javax.swing.JPanel();
        jScrollPane12 = new javax.swing.JScrollPane();
        jTable7 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel22 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jPanel32 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jSpinner2 = new javax.swing.JSpinner();
        jSpinner3 = new javax.swing.JSpinner();
        jPanel3 = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jComboBox19 = new javax.swing.JComboBox();
        jLabel26 = new javax.swing.JLabel();
        jComboBox20 = new javax.swing.JComboBox();
        jLabel27 = new javax.swing.JLabel();
        jComboBox21 = new javax.swing.JComboBox();
        jLabel29 = new javax.swing.JLabel();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel24 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jComboBox22 = new javax.swing.JComboBox();
        jComboBox23 = new javax.swing.JComboBox();
        jLabel28 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jCheckBox2 = new javax.swing.JCheckBox();
        jSpinner6 = new javax.swing.JSpinner();
        jSpinner7 = new javax.swing.JSpinner();
        jPanel11 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel19 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel30 = new javax.swing.JPanel();
        jComboBox8 = new javax.swing.JComboBox();
        jButton13 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jSpinner9 = new javax.swing.JSpinner();
        jPanel5 = new javax.swing.JPanel();
        jPanel27 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jComboBox9 = new javax.swing.JComboBox();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jCheckBox9 = new javax.swing.JCheckBox();
        jSpinner5 = new javax.swing.JSpinner();
        jComboBox15 = new javax.swing.JComboBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jSpinner12 = new javax.swing.JSpinner();
        jPanel26 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jLabel20 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jComboBox3 = new javax.swing.JComboBox();
        jButton5 = new javax.swing.JButton();
        jCheckBox8 = new javax.swing.JCheckBox();
        jSpinner15 = new javax.swing.JSpinner();
        jPanel29 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jComboBox7 = new javax.swing.JComboBox();
        jPanel41 = new javax.swing.JPanel();
        jPanel31 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        jComboBox17 = new javax.swing.JComboBox();
        jComboBox18 = new javax.swing.JComboBox();
        jPanel28 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jComboBox12 = new javax.swing.JComboBox();
        jComboBox13 = new javax.swing.JComboBox();
        jLabel45 = new javax.swing.JLabel();
        jSpinner10 = new javax.swing.JSpinner();
        jCheckBox11 = new javax.swing.JCheckBox();
        jSpinner17 = new javax.swing.JSpinner();
        jPanel43 = new javax.swing.JPanel();
        jButton11 = new javax.swing.JButton();
        jSpinner11 = new javax.swing.JSpinner();
        jLabel48 = new javax.swing.JLabel();
        jComboBox25 = new javax.swing.JComboBox();
        jLabel47 = new javax.swing.JLabel();
        jComboBox24 = new javax.swing.JComboBox();
        jLabel46 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        jButton6 = new javax.swing.JButton();
        jLabel38 = new javax.swing.JLabel();
        jSpinner8 = new javax.swing.JSpinner();
        jLabel21 = new javax.swing.JLabel();
        jCheckBox10 = new javax.swing.JCheckBox();
        jSpinner16 = new javax.swing.JSpinner();
        jPanel19 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable10 = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jPanel48 = new javax.swing.JPanel();
        jPanel50 = new javax.swing.JPanel();
        jScrollPane8 = new javax.swing.JScrollPane();
        jTable11 = new javax.swing.JTable();
        jLabel40 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jPanel51 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel56 = new javax.swing.JLabel();
        jComboBox31 = new javax.swing.JComboBox();
        jComboBox34 = new javax.swing.JComboBox();
        jButton18 = new javax.swing.JButton();
        jLabel59 = new javax.swing.JLabel();
        jSpinner4 = new javax.swing.JSpinner();
        jPanel52 = new javax.swing.JPanel();
        jButton19 = new javax.swing.JButton();
        jSpinner13 = new javax.swing.JSpinner();
        jLabel57 = new javax.swing.JLabel();
        jComboBox36 = new javax.swing.JComboBox();
        jLabel60 = new javax.swing.JLabel();
        jPanel49 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jLabel53 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel39 = new javax.swing.JLabel();
        jComboBox14 = new javax.swing.JComboBox();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel44 = new javax.swing.JPanel();
        jLabel54 = new javax.swing.JLabel();
        jComboBox29 = new javax.swing.JComboBox();
        jLabel55 = new javax.swing.JLabel();
        jComboBox30 = new javax.swing.JComboBox();
        jButton17 = new javax.swing.JButton();
        jPanel45 = new javax.swing.JPanel();
        jLabel50 = new javax.swing.JLabel();
        jComboBox26 = new javax.swing.JComboBox();
        jLabel51 = new javax.swing.JLabel();
        jComboBox27 = new javax.swing.JComboBox();
        jButton14 = new javax.swing.JButton();
        jLabel52 = new javax.swing.JLabel();
        jComboBox28 = new javax.swing.JComboBox();
        jPanel46 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jComboBox16 = new javax.swing.JComboBox();
        jButton4 = new javax.swing.JButton();
        jComboBox10 = new javax.swing.JComboBox();
        jComboBox11 = new javax.swing.JComboBox();
        jLabel58 = new javax.swing.JLabel();
        jComboBox32 = new javax.swing.JComboBox();
        jPanel47 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        jComboBox6 = new javax.swing.JComboBox();
        jButton12 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenu7 = new javax.swing.JMenu();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();

        jComboBox33.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox33.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox33.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Buck M", "Doe F" }));
        jComboBox33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox33ActionPerformed(evt);
            }
        });

        jMenu5.setText("jMenu5");

        jMenuItem1.setText("jMenuItem1");

        jMenuItem10.setText("jMenuItem10");

        jMenu8.setText("File");
        jMenuBar2.add(jMenu8);

        jMenu9.setText("Edit");
        jMenuBar2.add(jMenu9);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Rabbit Farming Management System");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setPreferredSize(new java.awt.Dimension(967, 559));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jTabbedPane5.setFont(new java.awt.Font("Segoe UI Semilight", 0, 16)); // NOI18N

        jPanel1.setBackground(new java.awt.Color(245, 255, 245));
        jPanel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jPanel8.setBackground(new java.awt.Color(245, 255, 245));

        jPanel16.setLayout(new java.awt.GridLayout(1, 0));

        jPanel25.setBackground(new java.awt.Color(235, 255, 245));
        jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder(" "));
        jPanel25.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel25MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel25MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel25MouseExited(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Segoe UI Light", 1, 20)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Total Number of Cages");

        jLabel18.setFont(new java.awt.Font("Segoe UI Light", 0, 24)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("jLabel18");

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel16.add(jPanel25);

        jPanel33.setBackground(new java.awt.Color(235, 255, 245));
        jPanel33.setBorder(javax.swing.BorderFactory.createTitledBorder(" "));
        jPanel33.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel33MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel33MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel33MouseExited(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Segoe UI Light", 1, 20)); // NOI18N
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel32.setText("Total Number of Rabbits");

        jLabel33.setFont(new java.awt.Font("Segoe UI Light", 0, 24)); // NOI18N
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("jLabel33");

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel33, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addComponent(jLabel32, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel33, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel16.add(jPanel33);

        jPanel34.setBackground(new java.awt.Color(235, 255, 245));
        jPanel34.setBorder(javax.swing.BorderFactory.createTitledBorder(" "));
        jPanel34.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel34MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel34MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel34MouseExited(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Segoe UI Light", 1, 20)); // NOI18N
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setText("Total Number of Matings");

        jLabel35.setFont(new java.awt.Font("Segoe UI Light", 0, 24)); // NOI18N
        jLabel35.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel35.setText("jLabel35");

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel35, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel34, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel34Layout.createSequentialGroup()
                .addComponent(jLabel34, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel35, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel16.add(jPanel34);

        jPanel35.setBackground(new java.awt.Color(235, 255, 245));
        jPanel35.setBorder(javax.swing.BorderFactory.createTitledBorder(" "));
        jPanel35.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel35MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel35MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel35MouseExited(evt);
            }
        });

        jLabel36.setFont(new java.awt.Font("Segoe UI Light", 1, 20)); // NOI18N
        jLabel36.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel36.setText("Notifications");

        jLabel37.setFont(new java.awt.Font("Segoe UI Light", 0, 24)); // NOI18N
        jLabel37.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel37.setText("jLabel37");

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel37, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jLabel36, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
        );
        jPanel35Layout.setVerticalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel35Layout.createSequentialGroup()
                .addComponent(jLabel36, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel16.add(jPanel35);

        jPanel36.setBackground(new java.awt.Color(245, 255, 245));
        jPanel36.setLayout(new java.awt.GridLayout(1, 0));

        jPanel37.setBackground(new java.awt.Color(245, 255, 245));
        jPanel37.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel37MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel37MouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jPanel37MouseExited(evt);
            }
        });

        jTable9.setBackground(new java.awt.Color(245, 255, 245));
        jTable9.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable9.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Total Breeding Stock Cages", null},
                {"Total Selling Stock Cages", null},
                {"Total  Empty Cages", null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {"", null}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable9.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jTable9.setGridColor(new java.awt.Color(245, 255, 245));
        jTable9.setRowHeight(38);
        jTable9.setSelectionBackground(new java.awt.Color(235, 255, 245));
        jTable9.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane14.setViewportView(jTable9);
        if (jTable9.getColumnModel().getColumnCount() > 0) {
            jTable9.getColumnModel().getColumn(0).setMinWidth(175);
        }

        javax.swing.GroupLayout jPanel37Layout = new javax.swing.GroupLayout(jPanel37);
        jPanel37.setLayout(jPanel37Layout);
        jPanel37Layout.setHorizontalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
        );
        jPanel37Layout.setVerticalGroup(
            jPanel37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
        );

        jPanel36.add(jPanel37);

        jPanel38.setBackground(new java.awt.Color(245, 255, 245));

        jTable8.setBackground(new java.awt.Color(245, 255, 245));
        jTable8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable8.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Breeding Stock", null},
                {"Selling Stock", null},
                {"", null},
                {"Market Ready Rabbits", null},
                {"Rabbits Sold", null},
                {"Rabbit Deaths", null},
                {null, null},
                {null, null},
                {null, null},
                {"", null}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable8.setGridColor(new java.awt.Color(245, 255, 245));
        jTable8.setRowHeight(38);
        jTable8.setSelectionBackground(new java.awt.Color(235, 255, 245));
        jTable8.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane13.setViewportView(jTable8);
        if (jTable8.getColumnModel().getColumnCount() > 0) {
            jTable8.getColumnModel().getColumn(0).setMinWidth(175);
        }

        javax.swing.GroupLayout jPanel38Layout = new javax.swing.GroupLayout(jPanel38);
        jPanel38.setLayout(jPanel38Layout);
        jPanel38Layout.setHorizontalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 235, Short.MAX_VALUE)
            .addGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
        );
        jPanel38Layout.setVerticalGroup(
            jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
            .addGroup(jPanel38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))
        );

        jPanel36.add(jPanel38);

        jPanel39.setBackground(new java.awt.Color(245, 255, 245));

        jTable6.setBackground(new java.awt.Color(245, 255, 245));
        jTable6.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"Mating Ready Does", null},
                {"Mating Ready Bucks", null},
                {null, null},
                {"Number of True Pregnancies", null},
                {"Number of False Pregnancies", null},
                {"Number of Kids", null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable6.setGridColor(new java.awt.Color(245, 255, 245));
        jTable6.setRowHeight(38);
        jTable6.setSelectionBackground(new java.awt.Color(235, 255, 245));
        jTable6.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane11.setViewportView(jTable6);
        if (jTable6.getColumnModel().getColumnCount() > 0) {
            jTable6.getColumnModel().getColumn(0).setMinWidth(175);
        }

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 235, Short.MAX_VALUE)
            .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
        );
        jPanel39Layout.setVerticalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
            .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane11, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))
        );

        jPanel36.add(jPanel39);

        jPanel40.setBackground(new java.awt.Color(245, 255, 245));

        jTable7.setBackground(new java.awt.Color(245, 255, 245));
        jTable7.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jTable7.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable7.setGridColor(new java.awt.Color(245, 255, 245));
        jTable7.setRowHeight(38);
        jTable7.setSelectionBackground(new java.awt.Color(235, 255, 245));
        jTable7.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane12.setViewportView(jTable7);

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 235, Short.MAX_VALUE)
            .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
        );
        jPanel40Layout.setVerticalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 240, Short.MAX_VALUE)
            .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane12, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))
        );

        jPanel36.add(jPanel40);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane5.addTab("     Dashboard     ", jPanel1);

        jPanel2.setBackground(new java.awt.Color(245, 255, 245));

        jPanel13.setBackground(new java.awt.Color(245, 255, 245));
        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("View"));

        jScrollPane1.setBackground(new java.awt.Color(245, 255, 245));

        jPanel22.setBackground(new java.awt.Color(245, 255, 245));

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 952, Short.MAX_VALUE)
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 731, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel22);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 930, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
        );

        jPanel14.setBackground(new java.awt.Color(245, 255, 245));
        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Add New Cage"));

        jButton3.setBackground(new java.awt.Color(204, 255, 204));
        jButton3.setText("Add New Cage");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jPanel32.setBackground(new java.awt.Color(245, 255, 245));
        jPanel32.setBorder(javax.swing.BorderFactory.createTitledBorder("Cage Name"));

        jLabel8.setBackground(new java.awt.Color(245, 255, 245));
        jLabel8.setFont(new java.awt.Font("Segoe UI Light", 1, 20)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel32Layout = new javax.swing.GroupLayout(jPanel32);
        jPanel32.setLayout(jPanel32Layout);
        jPanel32Layout.setHorizontalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel32Layout.setVerticalGroup(
            jPanel32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel32Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel14.setText("Row: ");

        jLabel15.setText("Column: ");

        jLabel16.setText("Tier: ");

        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        jSpinner2.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
        jSpinner2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner2StateChanged(evt);
            }
        });

        jSpinner3.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(0), null, Integer.valueOf(7), Integer.valueOf(1)));
        jSpinner3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner3StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel16, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jSpinner2, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                            .addComponent(jSpinner3))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel14Layout.createSequentialGroup()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel14)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel15)
                            .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(jSpinner3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel32, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane5.addTab("     Cages     ", jPanel2);

        jPanel3.setBackground(new java.awt.Color(245, 255, 245));

        jPanel23.setBackground(new java.awt.Color(245, 255, 245));
        jPanel23.setBorder(javax.swing.BorderFactory.createTitledBorder("Create A New Rabbit  (For Breeding Stock or Selling Stock)"));
        jPanel23.setToolTipText("");

        jLabel25.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel25.setText("Breed:");

        jComboBox19.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox19.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox19.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "New Zealand White" }));

        jLabel26.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel26.setText("Gender:");

        jComboBox20.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox20.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox20.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Buck M", "Doe F" }));

        jLabel27.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel27.setText("Stock Type");

        jComboBox21.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox21.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox21.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Breeding Stock", "Selling Stock" }));
        jComboBox21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox21ActionPerformed(evt);
            }
        });

        jLabel29.setFont(new java.awt.Font("Tahoma", 2, 10)); // NOI18N
        jLabel29.setText("Weight = 0.1 kg");

        jCheckBox4.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox4.setText("Parents Information Available");
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });

        jPanel24.setBackground(new java.awt.Color(245, 255, 245));
        jPanel24.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Select Parents", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 12))); // NOI18N
        jPanel24.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jLabel30.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel30.setText("Mother:");

        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel31.setText("Father:");

        jComboBox22.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox22.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox22.setToolTipText("Select Mother's Cage");

        jComboBox23.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox23.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox23.setToolTipText("Select Father's Cage");
        jComboBox23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox23ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel31, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox22, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBox23, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(jComboBox22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, 23, Short.MAX_VALUE)
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(jComboBox23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel28.setText("Birth Date:");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel1.setText("Cage:");

        jComboBox1.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jButton1.setBackground(new java.awt.Color(204, 255, 204));
        jButton1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButton1.setText("Add A New Rabbit");
        jButton1.setToolTipText("");
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(204, 255, 204));
        jButton2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jButton2.setText("Add Multiple New Rabbits");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jCheckBox2.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox2.setText("Birth Date Unavailable");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jSpinner6.setModel(new javax.swing.SpinnerNumberModel(0.1d, 0.1d, 10.0d, 0.1d));

        jSpinner7.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        jSpinner7.setEditor(new javax.swing.JSpinner.DateEditor(jSpinner7, "dd MMM yyyy"));

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel26)
                            .addComponent(jLabel25)
                            .addComponent(jLabel27)
                            .addComponent(jLabel28)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel23Layout.createSequentialGroup()
                                .addComponent(jSpinner7, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBox2)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinner6, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jComboBox21, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox20, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox19, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jCheckBox4, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE)
                            .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(4, 4, 4))
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(jComboBox19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox4))
                .addGap(4, 4, 4)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel26))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel27)
                            .addComponent(jComboBox21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel28)
                                .addComponent(jCheckBox2)
                                .addComponent(jSpinner7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel29))
                            .addComponent(jSpinner6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)))
                    .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addContainerGap())
        );

        jPanel11.setBackground(new java.awt.Color(245, 255, 245));
        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Rabbits"));
        jPanel11.setToolTipText("");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(jTable1);

        jCheckBox1.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox1.setSelected(true);
        jCheckBox1.setText("View All Rabbits");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel7.setText("Select Rabbit Cage: ");

        jComboBox4.setBackground(new java.awt.Color(245, 255, 245));
        jComboBox4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox4ItemStateChanged(evt);
            }
        });
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 675, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel23, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane5.addTab("     Rabbit     ", jPanel3);

        jPanel4.setBackground(new java.awt.Color(245, 255, 245));

        jPanel10.setBackground(new java.awt.Color(245, 255, 245));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Breeding Stock"));

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTable3);

        jLabel19.setText("Search By Cage Name: ");

        jTextField1.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField1CaretUpdate(evt);
            }
        });
        jTextField1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTextField1InputMethodTextChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel19)
                .addGap(18, 18, 18)
                .addComponent(jTextField1)
                .addContainerGap())
            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 930, Short.MAX_VALUE)
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
        );

        jPanel30.setBackground(new java.awt.Color(245, 255, 245));
        jPanel30.setBorder(javax.swing.BorderFactory.createTitledBorder("Operations"));
        jPanel30.setToolTipText("");

        jComboBox8.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox8.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox8ActionPerformed(evt);
            }
        });

        jButton13.setBackground(new java.awt.Color(204, 255, 204));
        jButton13.setText("Record Rabbit Death");
        jButton13.setToolTipText("");
        jButton13.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel10.setText("Select Cage: ");

        jCheckBox3.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox3.setSelected(true);
        jCheckBox3.setText("Use Today's Date");
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        jSpinner9.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        jSpinner9.setEditor(new javax.swing.JSpinner.DateEditor(jSpinner9, "dd MMM yyyy HH:mm"));

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel30Layout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox8, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox3)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinner9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel30Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox3)
                    .addComponent(jSpinner9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton13)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane5.addTab("     Breeding Stock     ", jPanel4);

        jPanel5.setBackground(new java.awt.Color(245, 255, 245));

        jPanel27.setBackground(new java.awt.Color(245, 255, 245));
        jPanel27.setBorder(javax.swing.BorderFactory.createTitledBorder("Operations"));

        jLabel41.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel41.setText("Gender:");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel12.setText("Cage:");

        jComboBox9.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox9.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N

        jButton15.setBackground(new java.awt.Color(204, 255, 204));
        jButton15.setText("Record Rabbit Sale");
        jButton15.setToolTipText("");
        jButton15.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jButton16.setBackground(new java.awt.Color(204, 255, 204));
        jButton16.setText("Record Rabbit Death");
        jButton16.setToolTipText("");
        jButton16.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton16ActionPerformed(evt);
            }
        });

        jLabel13.setText("Quantity of Rabbits:");

        jCheckBox9.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox9.setText("Multiple");
        jCheckBox9.setToolTipText("Multiple rabbits with the same characteristics.");
        jCheckBox9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox9ActionPerformed(evt);
            }
        });

        jSpinner5.setModel(new javax.swing.SpinnerNumberModel(2, 2, 50, 1));

        jComboBox15.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox15.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox15.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Buck M", "Doe F" }));

        jCheckBox5.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox5.setSelected(true);
        jCheckBox5.setText("Use Today's Date");
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });

        jSpinner12.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        jSpinner12.setEditor(new javax.swing.JSpinner.DateEditor(jSpinner12, "dd MMM yyyy HH:mm"));

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel41)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox15, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox9, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                                .addComponent(jCheckBox9)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel13)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                                .addComponent(jCheckBox5)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinner12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jButton16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel41)
                    .addComponent(jComboBox15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox5)
                    .addComponent(jSpinner12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel13)
                    .addComponent(jSpinner5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton16)
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel26.setBackground(new java.awt.Color(245, 255, 245));
        jPanel26.setBorder(javax.swing.BorderFactory.createTitledBorder("Selling Stock"));

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane5.setViewportView(jTable4);

        jLabel20.setText("Search By Cage Name: ");

        jTextField2.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField2CaretUpdate(evt);
            }
        });

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addComponent(jLabel20)
                .addGap(18, 18, 18)
                .addComponent(jTextField2, javax.swing.GroupLayout.DEFAULT_SIZE, 799, Short.MAX_VALUE))
            .addComponent(jScrollPane5)
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel26Layout.createSequentialGroup()
                .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 239, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane5.addTab("     Selling Stock     ", jPanel5);

        jPanel6.setBackground(new java.awt.Color(245, 255, 245));

        jPanel15.setBackground(new java.awt.Color(245, 255, 245));
        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Mate"));

        jLabel2.setText("Select Buck Cage:");

        jLabel3.setText("Select Doe Cage");

        jComboBox2.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jComboBox3.setBackground(new java.awt.Color(204, 255, 204));

        jButton5.setBackground(new java.awt.Color(204, 255, 204));
        jButton5.setText("Mate Selected Pair");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jCheckBox8.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox8.setSelected(true);
        jCheckBox8.setText("Use Today's Date");
        jCheckBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox8ActionPerformed(evt);
            }
        });

        jSpinner15.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        jSpinner15.setEditor(new javax.swing.JSpinner.DateEditor(jSpinner15, "dd MMM yyyy HH:mm"));

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(34, 34, 34)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 336, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox8)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinner15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jSpinner15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBox8))
                    .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel29.setBackground(new java.awt.Color(245, 255, 245));
        jPanel29.setBorder(javax.swing.BorderFactory.createTitledBorder("Previous Matings"));

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(jTable2);

        jLabel9.setText("Select Rabbit Cage:");

        jComboBox7.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addGroup(jPanel29Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox7, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel29Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel41.setLayout(new java.awt.GridLayout(1, 0));

        jPanel31.setBackground(new java.awt.Color(245, 255, 245));
        jPanel31.setBorder(javax.swing.BorderFactory.createTitledBorder("False Pregnancy"));

        jLabel43.setText("Select Doe Cage: ");

        jLabel44.setText("Select Mating Date: ");

        jButton10.setBackground(new java.awt.Color(204, 255, 204));
        jButton10.setText("Record False Pregnancy");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jComboBox17.setBackground(new java.awt.Color(204, 255, 204));

        jComboBox18.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox18ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .addGroup(jPanel31Layout.createSequentialGroup()
                        .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel44)
                            .addComponent(jLabel43))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox18, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox17, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel31Layout.createSequentialGroup()
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(jComboBox18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel44))
                .addGap(25, 25, 25)
                .addComponent(jButton10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel41.add(jPanel31);

        jPanel28.setBackground(new java.awt.Color(245, 255, 245));
        jPanel28.setBorder(javax.swing.BorderFactory.createTitledBorder("Rabbit Birth"));

        jLabel23.setText("Select Doe Cage: ");

        jLabel24.setText("Select Mating Date: ");

        jButton7.setBackground(new java.awt.Color(204, 255, 204));
        jButton7.setText("Record Birth");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jComboBox12.setBackground(new java.awt.Color(204, 255, 204));

        jComboBox13.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox13ActionPerformed(evt);
            }
        });

        jLabel45.setText("Number of Kids: ");

        jSpinner10.setModel(new javax.swing.SpinnerNumberModel(1, 0, 50, 1));

        jCheckBox11.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox11.setSelected(true);
        jCheckBox11.setText("Today's Date as Birth Date");
        jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox11ActionPerformed(evt);
            }
        });

        jSpinner17.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        jSpinner17.setEditor(new javax.swing.JSpinner.DateEditor(jSpinner17, "dd MMM yyyy HH:mm"));

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(29, 29, 29)
                        .addComponent(jComboBox13, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel24)
                            .addComponent(jLabel45))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox12, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel28Layout.createSequentialGroup()
                                .addComponent(jSpinner10, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jCheckBox11, javax.swing.GroupLayout.PREFERRED_SIZE, 123, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(jSpinner17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(jComboBox13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jComboBox12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jSpinner17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBox11))
                    .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel45)
                        .addComponent(jSpinner10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(9, 9, 9)
                .addComponent(jButton7)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel41.add(jPanel28);
        jPanel28.getAccessibleContext().setAccessibleName("Birth");

        jPanel43.setBackground(new java.awt.Color(245, 255, 245));
        jPanel43.setBorder(javax.swing.BorderFactory.createTitledBorder("Update Kids of Parent"));

        jButton11.setBackground(new java.awt.Color(204, 255, 204));
        jButton11.setText("Update Kids");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jSpinner11.setModel(new javax.swing.SpinnerNumberModel(1, 1, 50, 1));

        jLabel48.setText("Number of Kids: ");

        jComboBox25.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox25ActionPerformed(evt);
            }
        });

        jLabel47.setText("Mating Date: ");

        jComboBox24.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox24ActionPerformed(evt);
            }
        });

        jLabel46.setText("Doe Cage: ");

        javax.swing.GroupLayout jPanel43Layout = new javax.swing.GroupLayout(jPanel43);
        jPanel43.setLayout(jPanel43Layout);
        jPanel43Layout.setHorizontalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel43Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel46)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox24, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel47)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBox25, javax.swing.GroupLayout.PREFERRED_SIZE, 204, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel48)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSpinner11, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel43Layout.setVerticalGroup(
            jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jComboBox24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel46)
                .addComponent(jLabel47)
                .addComponent(jComboBox25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel48)
                .addComponent(jSpinner11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton11))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel41, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel43, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel43, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane5.addTab("     Mating     ", jPanel6);

        jPanel9.setBackground(new java.awt.Color(245, 255, 245));

        jPanel18.setBackground(new java.awt.Color(245, 255, 245));
        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder("Weigh A Rabbit"));

        jLabel4.setText("Select Rabbit Cage:");

        jComboBox5.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox5ItemStateChanged(evt);
            }
        });

        jButton6.setBackground(new java.awt.Color(204, 255, 204));
        jButton6.setText("Record Weight");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel38.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel38.setText("Weight:");

        jSpinner8.setModel(new javax.swing.SpinnerNumberModel(0.09999999999999998d, 0.09999999999999998d, 50.0d, 0.1d));

        jLabel21.setText("kg");

        jCheckBox10.setBackground(new java.awt.Color(245, 255, 245));
        jCheckBox10.setSelected(true);
        jCheckBox10.setText("Use Today's Date");
        jCheckBox10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox10ActionPerformed(evt);
            }
        });

        jSpinner16.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE));
        jSpinner16.setEditor(new javax.swing.JSpinner.DateEditor(jSpinner16, "dd MMM yyyy HH:mm"));

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel38)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner8, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel21)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox10)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinner16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel38)
                        .addComponent(jSpinner8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel21))
                    .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jSpinner16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jCheckBox10))
                    .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jButton6)
                .addContainerGap())
        );

        jPanel19.setBackground(new java.awt.Color(245, 255, 245));
        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Weight History"));

        jPanel20.setBackground(new java.awt.Color(245, 255, 245));

        jTable10.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(jTable10);

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 930, Short.MAX_VALUE)
            .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 910, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 302, Short.MAX_VALUE)
            .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel20Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        jLabel6.setText("Enter Rabbit Cage:");

        jTextField3.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField3CaretUpdate(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addGap(12, 12, 12)
                .addComponent(jTextField3)
                .addContainerGap())
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane5.addTab("     Weighings     ", jPanel9);

        jPanel48.setBackground(new java.awt.Color(245, 255, 245));

        jPanel50.setBackground(new java.awt.Color(245, 255, 245));
        jPanel50.setBorder(javax.swing.BorderFactory.createTitledBorder("Cages with Kids"));

        jTable11.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane8.setViewportView(jTable11);

        jLabel40.setText("Search By Cage Name: ");

        jTextField5.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField5CaretUpdate(evt);
            }
        });

        javax.swing.GroupLayout jPanel50Layout = new javax.swing.GroupLayout(jPanel50);
        jPanel50.setLayout(jPanel50Layout);
        jPanel50Layout.setHorizontalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel50Layout.createSequentialGroup()
                .addComponent(jLabel40)
                .addGap(18, 18, 18)
                .addComponent(jTextField5))
            .addComponent(jScrollPane8)
        );
        jPanel50Layout.setVerticalGroup(
            jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel50Layout.createSequentialGroup()
                .addGroup(jPanel50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel40)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE))
        );

        jPanel51.setBackground(new java.awt.Color(245, 255, 245));
        jPanel51.setBorder(javax.swing.BorderFactory.createTitledBorder("Move Kids to a Fostering Mother"));

        jLabel11.setText("Select Destination Cage:");

        jLabel56.setText("Select Current Cage:");

        jComboBox31.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox31ActionPerformed(evt);
            }
        });

        jComboBox34.setBackground(new java.awt.Color(204, 255, 204));

        jButton18.setBackground(new java.awt.Color(204, 255, 204));
        jButton18.setText("Move Kids");
        jButton18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton18ActionPerformed(evt);
            }
        });

        jLabel59.setText("Number of Kids:");

        jSpinner4.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));

        javax.swing.GroupLayout jPanel51Layout = new javax.swing.GroupLayout(jPanel51);
        jPanel51.setLayout(jPanel51Layout);
        jPanel51Layout.setHorizontalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel51Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel51Layout.createSequentialGroup()
                        .addComponent(jLabel56)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox34, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox31, 0, 229, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel59)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel51Layout.setVerticalGroup(
            jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel51Layout.createSequentialGroup()
                .addGroup(jPanel51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox34, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel56)
                    .addComponent(jLabel11)
                    .addComponent(jComboBox31, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel59)
                    .addComponent(jSpinner4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton18)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel52.setBackground(new java.awt.Color(245, 255, 245));
        jPanel52.setBorder(javax.swing.BorderFactory.createTitledBorder("Update Kids in Foster Cage"));

        jButton19.setBackground(new java.awt.Color(204, 255, 204));
        jButton19.setText("Update Kids");
        jButton19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton19ActionPerformed(evt);
            }
        });

        jSpinner13.setModel(new javax.swing.SpinnerNumberModel(1, 0, 50, 1));

        jLabel57.setText("Number of Kids: ");

        jComboBox36.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox36ActionPerformed(evt);
            }
        });

        jLabel60.setText("Select Cage with Kids:");

        javax.swing.GroupLayout jPanel52Layout = new javax.swing.GroupLayout(jPanel52);
        jPanel52.setLayout(jPanel52Layout);
        jPanel52Layout.setHorizontalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel52Layout.createSequentialGroup()
                        .addComponent(jLabel60)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox36, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel57)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinner13, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel52Layout.setVerticalGroup(
            jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel52Layout.createSequentialGroup()
                .addGroup(jPanel52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox36, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel60)
                    .addComponent(jLabel57)
                    .addComponent(jSpinner13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addComponent(jButton19)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel48Layout = new javax.swing.GroupLayout(jPanel48);
        jPanel48.setLayout(jPanel48Layout);
        jPanel48Layout.setHorizontalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel48Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel50, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel51, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel52, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel48Layout.setVerticalGroup(
            jPanel48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel48Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel51, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel52, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel50, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel51.getAccessibleContext().setAccessibleName("");

        jTabbedPane5.addTab("     Fostering     ", jPanel48);

        jPanel49.setLayout(new javax.swing.BoxLayout(jPanel49, javax.swing.BoxLayout.LINE_AXIS));

        jPanel7.setBackground(new java.awt.Color(245, 255, 245));

        jPanel21.setBackground(new java.awt.Color(245, 255, 245));
        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder("Nest Box"));

        jPanel17.setBackground(new java.awt.Color(245, 255, 245));
        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("Cages With Nest Boxes"));

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane7.setViewportView(jTable5);

        jLabel53.setText("Search By Cage Name: ");

        jTextField4.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                jTextField4CaretUpdate(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel53)
                .addGap(18, 18, 18)
                .addComponent(jTextField4))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel53)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE))
        );

        jLabel39.setText("Select Cage: ");

        jComboBox14.setBackground(new java.awt.Color(204, 255, 204));

        jButton8.setBackground(new java.awt.Color(204, 255, 204));
        jButton8.setText("Place Nest Box");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton9.setBackground(new java.awt.Color(204, 255, 204));
        jButton9.setText("Remove Nest Box");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addComponent(jLabel39)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox14, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addComponent(jButton8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel39))
                .addGap(11, 11, 11)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton8)
                    .addComponent(jButton9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.setBackground(new java.awt.Color(245, 255, 245));
        jTabbedPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Actions"));

        jPanel44.setBackground(new java.awt.Color(245, 255, 245));

        jLabel54.setText("Select Current Cage: ");

        jComboBox29.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox29ActionPerformed(evt);
            }
        });

        jLabel55.setText("Select Destination Cage: ");

        jComboBox30.setBackground(new java.awt.Color(204, 255, 204));

        jButton17.setBackground(new java.awt.Color(204, 255, 204));
        jButton17.setText("Move Rabbit to Selling Stock");
        jButton17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton17ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel44Layout = new javax.swing.GroupLayout(jPanel44);
        jPanel44.setLayout(jPanel44Layout);
        jPanel44Layout.setHorizontalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton17, javax.swing.GroupLayout.DEFAULT_SIZE, 905, Short.MAX_VALUE)
                    .addGroup(jPanel44Layout.createSequentialGroup()
                        .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel55)
                            .addComponent(jLabel54))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox29, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox30, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel44Layout.setVerticalGroup(
            jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel44Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel54)
                    .addComponent(jComboBox29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel55))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(jButton17)
                .addGap(20, 20, 20))
        );

        jTabbedPane1.addTab("Move From Breeding Stock to Selling Stock", jPanel44);

        jPanel45.setBackground(new java.awt.Color(245, 255, 245));

        jLabel50.setText("Select Current Cage: ");

        jComboBox26.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox26ActionPerformed(evt);
            }
        });

        jLabel51.setText("Select Destination Cage: ");

        jComboBox27.setBackground(new java.awt.Color(204, 255, 204));

        jButton14.setBackground(new java.awt.Color(204, 255, 204));
        jButton14.setText("Move Rabbit to Breeding Stock");
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jLabel52.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel52.setText("Gender:");

        jComboBox28.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox28.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox28.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Buck M", "Doe F" }));

        javax.swing.GroupLayout jPanel45Layout = new javax.swing.GroupLayout(jPanel45);
        jPanel45.setLayout(jPanel45Layout);
        jPanel45Layout.setHorizontalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel45Layout.createSequentialGroup()
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel51)
                            .addComponent(jLabel50))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel45Layout.createSequentialGroup()
                                .addComponent(jComboBox26, 0, 362, Short.MAX_VALUE)
                                .addGap(49, 49, 49)
                                .addComponent(jLabel52)
                                .addGap(19, 19, 19)
                                .addComponent(jComboBox28, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBox27, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel45Layout.setVerticalGroup(
            jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel45Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel50)
                    .addComponent(jComboBox26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel52)
                    .addComponent(jComboBox28, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel51))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addComponent(jButton14)
                .addGap(20, 20, 20))
        );

        jTabbedPane1.addTab("Move From Selling Stock to Breeding Stock", jPanel45);

        jPanel46.setBackground(new java.awt.Color(245, 255, 245));

        jLabel5.setText("Select Current Cage: ");

        jLabel22.setText("Select Destination Cage: ");

        jLabel42.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel42.setText("Gender:");

        jComboBox16.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox16.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jComboBox16.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Buck M", "Doe F" }));

        jButton4.setBackground(new java.awt.Color(204, 255, 204));
        jButton4.setText("Move Rabbit");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jComboBox10.setBackground(new java.awt.Color(204, 255, 204));

        jComboBox11.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox11ActionPerformed(evt);
            }
        });

        jLabel58.setText("Select Stock Type: ");

        jComboBox32.setBackground(new java.awt.Color(204, 255, 204));
        jComboBox32.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Breeding Stock", "Selling Stock" }));
        jComboBox32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox32ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel46Layout = new javax.swing.GroupLayout(jPanel46);
        jPanel46.setLayout(jPanel46Layout);
        jPanel46Layout.setHorizontalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel46Layout.createSequentialGroup()
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel22)
                            .addComponent(jLabel58)
                            .addComponent(jLabel5))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel46Layout.createSequentialGroup()
                                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBox32, 0, 362, Short.MAX_VALUE)
                                    .addComponent(jComboBox10, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(50, 50, 50)
                                .addComponent(jLabel42)
                                .addGap(18, 18, 18)
                                .addComponent(jComboBox16, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jComboBox11, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel46Layout.setVerticalGroup(
            jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel46Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel58)
                    .addComponent(jComboBox32, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel42)
                    .addComponent(jComboBox16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jComboBox10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jComboBox11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addContainerGap(48, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Move to Different Cage, Same Stock Type", jPanel46);

        jPanel47.setBackground(new java.awt.Color(245, 255, 245));

        jPanel12.setBackground(new java.awt.Color(245, 255, 245));
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Retire a Mating Rabbit"));

        jLabel49.setText("Select Rabbit Cage: ");

        jComboBox6.setBackground(new java.awt.Color(204, 255, 204));

        jButton12.setBackground(new java.awt.Color(204, 255, 204));
        jButton12.setText("Retire Rabbit");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel49)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox6, 0, 765, Short.MAX_VALUE)
                        .addGap(6, 6, 6)))
                .addContainerGap())
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel49)
                    .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jButton12)
                .addContainerGap(44, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel47Layout = new javax.swing.GroupLayout(jPanel47);
        jPanel47.setLayout(jPanel47Layout);
        jPanel47Layout.setHorizontalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel47Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel47Layout.setVerticalGroup(
            jPanel47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel47Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Retire a Mating Rabbit", jPanel47);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("Move From Breeding Stock to Selling Stock");

        jPanel49.add(jPanel7);

        jTabbedPane5.addTab("     Other Actions     ", jPanel49);

        jMenuBar1.setBackground(new java.awt.Color(245, 255, 245));
        jMenuBar1.setFont(new java.awt.Font("Segoe UI Semilight", 0, 12)); // NOI18N

        jMenu1.setText("File");

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("Refresh");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);
        jMenu1.add(jSeparator1);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem3.setText("Exit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Options");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Notifications");

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem4.setText("Refresh Notifications");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem4);

        jMenuBar1.add(jMenu3);

        jMenu7.setText("Reports");

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        jMenuItem9.setText("View Reports");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu7.add(jMenuItem9);

        jMenuBar1.add(jMenu7);

        jMenu4.setText("Tools");

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem6.setText("Backup Database");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem6);

        jMenuBar1.add(jMenu4);

        jMenu6.setText("Help");

        jMenuItem7.setText("Online Support");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem7);

        jMenuItem8.setText("About");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu6.add(jMenuItem8);

        jMenuBar1.add(jMenu6);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane5)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane5)
        );

        getAccessibleContext().setAccessibleDescription("Organised and Easy Rabbit Farming");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed
        if (jCheckBox4.isSelected()) {
            jComboBox22.enable();
            jComboBox23.enable();
        } else {
            jComboBox22.disable();
            jComboBox23.disable();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox4ActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        if (firstTimeLaunch) {
            this.setVisible(false);
            refresh();
            this.setVisible(true);
            firstTimeLaunch = false;
            paintCages();
        }
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }//GEN-LAST:event_formWindowActivated

    private void jCheckBox9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox9ActionPerformed
        if (jCheckBox9.isSelected()) {
            jSpinner5.show();
            jLabel13.show();
        } else {
            jSpinner5.hide();
            jLabel13.hide();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox9ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        //Add New Rabbit
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String todaysDate = dateFormat.format(cal.getTime());

        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                if ("Breeding Stock".equals(jComboBox21.getSelectedItem().toString())) {

                    if (tblCage.getRabbitCount(tblCage.getCageId(jComboBox1.getSelectedItem().toString())) == 0) {
                        tblRabbit.insertRabbit(jComboBox19.getSelectedItem().toString(),
                                jComboBox20.getSelectedItem().toString(), jComboBox21.getSelectedItem().toString(),
                                !jCheckBox2.isSelected() ? new FormatDate().format(jSpinner7.getValue().toString()) : todaysDate,
                                Double.parseDouble(jSpinner6.getValue().toString()),
                                jCheckBox4.isSelected() && jComboBox22.getItemCount() > 0 ? tblRabbit.getRabbitId(tblCage.getCageId(jComboBox22.getSelectedItem().toString())) : -1,
                                jCheckBox4.isSelected() && jComboBox22.getItemCount() > 0 ? tblRabbit.getRabbitId(tblCage.getCageId(jComboBox23.getSelectedItem().toString())) : -1,
                                tblCage.getCageId(jComboBox1.getSelectedItem().toString()));
                        JOptionPane.showMessageDialog(null, "New Rabbit Successfully Added");
                    } else {
                        JOptionPane.showMessageDialog(null, "Cage Is At Full Capacity", "Error", JOptionPane.ERROR_MESSAGE);
                    }

                } else if ("Selling Stock".equals(jComboBox21.getSelectedItem().toString())) {

                    if (tblCage.getRabbitCount(tblCage.getCageId(jComboBox1.getSelectedItem().toString())) < maxRabbitsInSellingCage) {
                        tblRabbit.insertRabbit(jComboBox19.getSelectedItem().toString(),
                                jComboBox20.getSelectedItem().toString(), jComboBox21.getSelectedItem().toString(),
                                !jCheckBox2.isSelected() ? new FormatDate().format(jSpinner7.getValue().toString()) : todaysDate,
                                Double.parseDouble(jSpinner6.getValue().toString()),
                                jCheckBox4.isSelected() && jComboBox22.getItemCount() > 0 ? tblRabbit.getRabbitId(tblCage.getCageId(jComboBox22.getSelectedItem().toString())) : -1,
                                jCheckBox4.isSelected() && jComboBox22.getItemCount() > 0 ? tblRabbit.getRabbitId(tblCage.getCageId(jComboBox23.getSelectedItem().toString())) : -1,
                                tblCage.getCageId(jComboBox1.getSelectedItem().toString()));
                        JOptionPane.showMessageDialog(null, "New Rabbit Successfully Added");

                    } else {
                        JOptionPane.showMessageDialog(null, "Cage Is At Full Capacity", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox4.removeAllItems();
                jComboBox5.removeAllItems();
                jComboBox10.removeAllItems();
                for (int id : tblCage.getNonEmptyCageIds()) {
                    jComboBox4.addItem(tblCage.getCageName(id));
                    jComboBox5.addItem(tblCage.getCageName(id));
                    if (tblCage.getStockType(id).equals("Breeding Stock")) {
                        jComboBox10.addItem(tblCage.getCageName(id));
                    }
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jComboBox21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox21ActionPerformed
        if (!refreshing) {
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            try {
                if ("Breeding Stock".equals(jComboBox21.getSelectedItem().toString())) {
                    jComboBox1.removeAllItems();
                    for (int id : tblCage.getEmptyCageIds()) {
                        jComboBox1.addItem(tblCage.getCageName(id));
                    }
                } else if ("Selling Stock".equals(jComboBox21.getSelectedItem().toString())) {
                    jComboBox1.removeAllItems();
                    for (int id : tblCage.getEmptyCageIdsSellingStock(maxRabbitsInSellingCage)) {
                        jComboBox1.addItem(tblCage.getCageName(id));
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_jComboBox21ActionPerformed

    private void jComboBox5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox5ItemStateChanged

    }//GEN-LAST:event_jComboBox5ItemStateChanged

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {

                if (tblCage.getRabbitCount(tblCage.getCageId(jComboBox5.getSelectedItem().toString())) > 1) {//Assigns Average Weight to Rabbits Weighed In Groups
                    double avgWeight = Double.parseDouble(jSpinner8.getValue().toString()) / tblCage.getRabbitCount(tblCage.getCageId(jComboBox5.getSelectedItem().toString()));

                    for (int id : tblCage.getRabbitIdsInCage(tblCage.getCageId(jComboBox5.getSelectedItem().toString()))) {

                        if (jCheckBox10.isSelected()) {
                            tblWeight.insertWeight(id, avgWeight);
                        } else {
                            tblWeight.insertWeight(id, avgWeight, new FormatDate().formatTime(jSpinner16.getValue().toString()));
                        }

                    }

                } else if (tblCage.getRabbitCount(tblCage.getCageId(jComboBox5.getSelectedItem().toString())) == 1) {

                    if (jCheckBox10.isSelected()) {
                        tblWeight.insertWeight(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox5.getSelectedItem()
                                .toString())), Double.parseDouble(jSpinner8.getValue().toString()));
                    } else {
                        tblWeight.insertWeight(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox5.getSelectedItem()
                                .toString())), Double.parseDouble(jSpinner8.getValue().toString()),
                                new FormatDate().formatTime(jSpinner16.getValue().toString()));
                    }

                    if (Double.parseDouble(jSpinner8.getValue().toString()) >= matingWeightThreshold) {
                        tblRabbit.activate(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox5.getSelectedItem().toString())));
                    }
                }

                JOptionPane.showMessageDialog(null, "Weight Successfully Recorded");

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {

                if (jCheckBox8.isSelected()) {
                    tblMating.mate(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox2.getSelectedItem().toString())),
                            tblRabbit.getRabbitId(tblCage.getCageId(jComboBox3.getSelectedItem().toString())));
                } else {
                    tblMating.mate(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox2.getSelectedItem().toString())),
                            tblRabbit.getRabbitId(tblCage.getCageId(jComboBox3.getSelectedItem().toString())),
                            new FormatDate().formatTime(jSpinner15.getValue().toString()));
                }

                JOptionPane.showMessageDialog(null, "Mating Successful");

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if (jCheckBox1.isSelected()) {
            jComboBox4.disable();

            try {

                Statement stmt = conn.createStatement();
                String query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE ";
                ResultSet rs = stmt.executeQuery(query);

                DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Stock Type", "Birth Date", "Age"}, 0);
                while (rs.next()) {
                    int r1 = rs.getInt("CAGEID");
                    String r2 = rs.getString("GENDER");
                    String r3 = rs.getString("STOCKTYPE");
                    String r4 = rs.getString("BIRTHDATE");
                    String r5 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";
                    model.addRow(new Object[]{tblCage.getCageName(r1), r2, r3, r4, r5});
                }
                jTable1.setModel(model);
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            jComboBox4.enable();
            try {
                Statement stmt = conn.createStatement();
                String query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE AND CAGEID = "
                        + tblCage.getCageId(jComboBox4.getItemCount() == 0 ? "" : jComboBox4.getSelectedItem().toString());
                ResultSet rs = stmt.executeQuery(query);

                DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Stock Type", "Birth Date", "Age"}, 0);
                while (rs.next()) {
                    int r1 = rs.getInt("CAGEID");
                    String r2 = rs.getString("GENDER");
                    String r3 = rs.getString("STOCKTYPE");
                    String r4 = rs.getString("BIRTHDATE");
                    String r5 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";
                    model.addRow(new Object[]{tblCage.getCageName(r1), r2, r3, r4, r5});
                }
                jTable1.setModel(model);
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        if (!refreshing) {
            if (!jCheckBox1.isSelected()) {
                try {
                    Statement stmt = conn.createStatement();
                    String query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE AND CAGEID = " + tblCage.getCageId(jComboBox4.getSelectedItem().toString());
                    ResultSet rs = stmt.executeQuery(query);

                    DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Stock Type", "Birth Date", "Age"}, 0);
                    while (rs.next()) {
                        int r1 = rs.getInt("CAGEID");
                        String r2 = rs.getString("GENDER");
                        String r3 = rs.getString("STOCKTYPE");
                        String r4 = rs.getString("BIRTHDATE");
                        String r5 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";
                        model.addRow(new Object[]{tblCage.getCageName(r1), r2, r3, r4, r5});
                    }
                    jTable1.setModel(model);
                } catch (SQLException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        //Add Multiple New Rabbits (For Selling Stock Only)
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Calendar cal = Calendar.getInstance();
        String todaysDate = dateFormat.format(cal.getTime());

        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            switch (jComboBox21.getSelectedItem().toString()) {

                case "Breeding Stock":
                    JOptionPane.showMessageDialog(null, "This Action Is For Selling Stock Only", "Error", JOptionPane.WARNING_MESSAGE);
                    break;

                case "Selling Stock":
                    try {
                        int numOfRabbits = 0;
                        try {
                            numOfRabbits = Integer.parseInt(JOptionPane.showInputDialog("Enter Quantity Of Rabbits "));
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "No Rabbits Were Added");
                            break;
                        }
                        if ((tblCage.getRabbitCount(tblCage.getCageId(jComboBox1.getSelectedItem().toString())) + numOfRabbits) <= maxRabbitsInSellingCage) {

                            for (int i = 0; i < numOfRabbits; i++) {
                                tblRabbit.insertRabbit(jComboBox19.getSelectedItem().toString(),
                                        jComboBox20.getSelectedItem().toString(), jComboBox21.getSelectedItem().toString(),
                                        !jCheckBox2.isSelected() ? new FormatDate().format(jSpinner7.getValue().toString()) : todaysDate,
                                        Double.parseDouble(jSpinner6.getValue().toString()),
                                        jCheckBox4.isSelected() && jComboBox22.getItemCount() > 0 ? tblRabbit.getRabbitId(tblCage.getCageId(jComboBox22.getSelectedItem().toString())) : -1,
                                        jCheckBox4.isSelected() && jComboBox22.getItemCount() > 0 ? tblRabbit.getRabbitId(tblCage.getCageId(jComboBox23.getSelectedItem().toString())) : -1,
                                        tblCage.getCageId(jComboBox1.getSelectedItem().toString()));
                            }

                            JOptionPane.showMessageDialog(null, numOfRabbits + " New Rabbits Successfully Added");

                        } else {
                            JOptionPane.showMessageDialog(null, "Value Entered Exceeds Cage Capacity, There Are "
                                    + tblCage.getRabbitCount(tblCage.getCageId(jComboBox1.getSelectedItem().toString()))
                                    + " Rabbit(s) In The Cage Already", "Error", JOptionPane.ERROR_MESSAGE);
                        }

                        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                        jComboBox4.removeAllItems();
                        jComboBox5.removeAllItems();
                        jComboBox10.removeAllItems();
                        for (int id : tblCage.getNonEmptyCageIds()) {
                            jComboBox4.addItem(tblCage.getCageName(id));
                            jComboBox5.addItem(tblCage.getCageName(id));
                            if (tblCage.getStockType(id).equals("Breeding Stock")) {
                                jComboBox10.addItem(tblCage.getCageName(id));
                            }
                        }
                        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                    } catch (SQLException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jComboBox7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox7ActionPerformed
        if (!refreshing) {
            try {
                Statement stmt = conn.createStatement();
                String query;
                ResultSet rs;
                query = "SELECT * from TBL_MATING WHERE MALERABBITID = "
                        + tblRabbit.getRabbitId(jComboBox7.getItemCount() == 0 ? 0 : tblCage.getCageId(jComboBox7.getSelectedItem().toString()))
                        + "OR FEMALERABBITID = " + tblRabbit.getRabbitId(jComboBox7.getItemCount() == 0 ? 0 : tblCage.getCageId(jComboBox7.getSelectedItem().toString()));
                rs = stmt.executeQuery(query);

                DefaultTableModel model = new DefaultTableModel(new String[]{"Doe Cage", "Buck Cage", "Mating Date", "Pregnancy", "Kids", "Kids Birth Date"}, 0);
                while (rs.next()) {
                    int r1 = rs.getInt("FEMALERABBITID");
                    int r2 = rs.getInt("MALERABBITID");
                    String r3 = rs.getString("DATETIME");
                    boolean r4 = rs.getBoolean("PREGNANCY");
                    int r5 = rs.getInt("KIDS");
                    String r6 = rs.getString("BIRTHDATE");
                    model.addRow(new Object[]{tblCage.getCageName(tblRabbit.getCageId(r1)), tblCage.getCageName(tblRabbit.getCageId(r2)), r3, r4, r5, r6});
                }
                jTable2.setModel(model);
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_jComboBox7ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                if (jCheckBox3.isSelected()) {
                    tblRabbit.recordDeath(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox8.getSelectedItem().toString())));
                } else {
                    tblRabbit.recordDeath(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox8.getSelectedItem().toString())),
                            new FormatDate().formatTime(jSpinner9.getValue().toString()));
                }

                refreshing = true;
                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox8.removeAllItems();
                for (int id : tblRabbit.getStockCageIds("Breeding Stock")) {
                    jComboBox8.addItem(tblCage.getCageName(id));
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                refreshing = false;

                JOptionPane.showMessageDialog(null, "Rabbit Death Recorded Successfully", "", JOptionPane.PLAIN_MESSAGE);

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            if (!tblCage.hasCage(jLabel8.getText()) && !jLabel8.getText().equals("")) {

                tblCage.insertCage(jLabel8.getText());
                tblFostering.insertCage(jLabel8.getText());

                JOptionPane.showMessageDialog(null, "Cage " + jLabel8.getText() + " Successfully Added", "", JOptionPane.PLAIN_MESSAGE);

            } else if (tblCage.hasCage(jLabel8.getText()) && !jLabel8.getText().equals("")) {
                JOptionPane.showMessageDialog(null, "Cage " + jLabel8.getText() + " Already Exists", "Error", JOptionPane.WARNING_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(null, "Select Row, Column and Tier", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }

            paintCages();

        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        if (Integer.parseInt(jSpinner1.getValue().toString()) > 0
                && Integer.parseInt(jSpinner2.getValue().toString()) > 0
                && Integer.parseInt(jSpinner3.getValue().toString()) > 0) {
            jLabel8.setText("R" + jSpinner1.getValue().toString() + "C" + jSpinner2.getValue().toString() + "T" + jSpinner3.getValue().toString());
        } else {
            jLabel8.setText("");
        }
    }//GEN-LAST:event_jSpinner1StateChanged

    private void jSpinner2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner2StateChanged
        if (Integer.parseInt(jSpinner1.getValue().toString()) > 0
                && Integer.parseInt(jSpinner2.getValue().toString()) > 0
                && Integer.parseInt(jSpinner3.getValue().toString()) > 0) {
            jLabel8.setText("R" + jSpinner1.getValue().toString() + "C" + jSpinner2.getValue().toString() + "T" + jSpinner3.getValue().toString());
        } else {
            jLabel8.setText("");
        }
    }//GEN-LAST:event_jSpinner2StateChanged

    private void jSpinner3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner3StateChanged
        if (Integer.parseInt(jSpinner1.getValue().toString()) > 0
                && Integer.parseInt(jSpinner2.getValue().toString()) > 0
                && Integer.parseInt(jSpinner3.getValue().toString()) > 0) {
            jLabel8.setText("R" + jSpinner1.getValue().toString() + "C" + jSpinner2.getValue().toString() + "T" + jSpinner3.getValue().toString());
        } else {
            jLabel8.setText("");
        }
    }//GEN-LAST:event_jSpinner3StateChanged

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        if (jCheckBox2.isSelected()) {
            jSpinner7.hide();
            jLabel28.hide();
        } else {
            jSpinner7.show();
            jLabel28.show();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jPanel25MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel25MouseEntered
        jPanel25.setBackground(new java.awt.Color(240, 255, 245));
    }//GEN-LAST:event_jPanel25MouseEntered

    private void jPanel25MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel25MouseClicked
        jPanel25.setBackground(new java.awt.Color(245, 255, 245));
    }//GEN-LAST:event_jPanel25MouseClicked

    private void jPanel25MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel25MouseExited
        jPanel25.setBackground(new java.awt.Color(235, 255, 245));
    }//GEN-LAST:event_jPanel25MouseExited

    private void jPanel37MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel37MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel37MouseClicked

    private void jPanel37MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel37MouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel37MouseEntered

    private void jPanel37MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel37MouseExited
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel37MouseExited

    private void jPanel33MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel33MouseClicked
        jPanel33.setBackground(new java.awt.Color(245, 255, 245));
    }//GEN-LAST:event_jPanel33MouseClicked

    private void jPanel34MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel34MouseClicked
        jPanel34.setBackground(new java.awt.Color(245, 255, 245));
    }//GEN-LAST:event_jPanel34MouseClicked

    private void jPanel35MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel35MouseClicked
        jPanel35.setBackground(new java.awt.Color(245, 255, 245));
    }//GEN-LAST:event_jPanel35MouseClicked

    private void jPanel33MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel33MouseEntered
        jPanel33.setBackground(new java.awt.Color(240, 255, 245));
    }//GEN-LAST:event_jPanel33MouseEntered

    private void jPanel34MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel34MouseEntered
        jPanel34.setBackground(new java.awt.Color(240, 255, 245));
    }//GEN-LAST:event_jPanel34MouseEntered

    private void jPanel35MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel35MouseEntered
        jPanel35.setBackground(new java.awt.Color(240, 255, 245));
    }//GEN-LAST:event_jPanel35MouseEntered

    private void jPanel33MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel33MouseExited
        jPanel33.setBackground(new java.awt.Color(235, 255, 245));
    }//GEN-LAST:event_jPanel33MouseExited

    private void jPanel34MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel34MouseExited
        jPanel34.setBackground(new java.awt.Color(235, 255, 245));
    }//GEN-LAST:event_jPanel34MouseExited

    private void jPanel35MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel35MouseExited
        jPanel35.setBackground(new java.awt.Color(235, 255, 245));
    }//GEN-LAST:event_jPanel35MouseExited

    private void jTextField1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_jTextField1InputMethodTextChanged

    }//GEN-LAST:event_jTextField1InputMethodTextChanged

    private void jTextField1CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField1CaretUpdate
        try {
            Statement stmt = conn.createStatement();
            String query;
            ResultSet rs;
            stmt = conn.createStatement();
            query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND "
                    + "SOLD = FALSE AND STOCKTYPE = 'Breeding Stock'";
            rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Weight (kg)", "Birth Date", "Age", "Total Number of Kids", "Dead Kids"}, 0);
            while (rs.next()) {
                String r1 = tblCage.getCageName(tblRabbit.getCageId(rs.getInt("RABBITID")));
                String r2 = rs.getString("GENDER");
                String r3 = "";
                String r4 = rs.getString("BIRTHDATE");
                String r5 = tblMating.countKids(rs.getInt("RABBITID")) + "";
                String r6 = tblMating.countDeadKids(rs.getInt("RABBITID")) + "";
                if (rs.getInt("WEIGHTID") < 0) {
                    r3 = "Not Available";
                } else {
                    r3 = tblWeight.getCurrrentWeight(rs.getInt("RABBITID")) + "";
                }
                String r7 = DateOperations.getDifferenceInDays(new Date(r4)) + " days";

                if (r1.contains(jTextField1.getText().toUpperCase())) {
                    model.addRow(new Object[]{r1, r2, r3, r4, r7, r5, r6});
                }
            }
            jTable3.setModel(model);

        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_jTextField1CaretUpdate

    private void jTextField2CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField2CaretUpdate
        try {
            Statement stmt = conn.createStatement();
            String query;
            ResultSet rs;
            stmt = conn.createStatement();
            query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND "
                    + "SOLD = FALSE AND STOCKTYPE = 'Selling Stock'";
            rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Gender", "Weight (kg)", "Birth Date", "Age"}, 0);
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

                if (r1.contains(jTextField2.getText().toUpperCase())) {
                    model.addRow(new Object[]{r1, r2, r3, r4, r5});
                }
            }
            jTable4.setModel(model);
        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jTextField2CaretUpdate

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                ArrayList<Integer> ids = tblRabbit.getRabbitsId(tblCage.getCageId(jComboBox9.getSelectedItem().toString()), jComboBox15.getSelectedItem().toString());

                if (jCheckBox9.isSelected()) {

                    int rabbitCount = ids.size();

                    if (Integer.parseInt(jSpinner5.getValue().toString()) > rabbitCount) {
                        JOptionPane.showMessageDialog(null, "The Entered Number of Rabbits Exceeds the Number of Rabbits in Cage", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {

                        int count = 0;

                        for (int i = 0; i < Integer.parseInt(jSpinner5.getValue().toString()); i++) {
                            if (rabbitCount > 0) {
                                tblRabbit.recordSale(ids.get(i));
                                count++;
                            }
                            rabbitCount--;
                        }
                        JOptionPane.showMessageDialog(null, count + " Rabbit Sales Successfully Recorded");
                    }
                } else {
                    tblRabbit.recordSale(ids.get(0));
                    JOptionPane.showMessageDialog(null, "1 Rabbit Sale Successfully Recorded");
                }

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox9.removeAllItems();
                for (int id : tblRabbit.getStockCageIds("Selling Stock")) {
                    jComboBox9.addItem(tblCage.getCageName(id));
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumberFormatException | HeadlessException e) {
                JOptionPane.showMessageDialog(null, "Enter Valid Values", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                ArrayList<Integer> ids = tblRabbit.getRabbitsId(tblCage.getCageId(jComboBox9.getSelectedItem().toString()), jComboBox15.getSelectedItem().toString());
                if (jCheckBox9.isSelected()) {

                    int rabbitCount = ids.size();

                    if (Integer.parseInt(jSpinner5.getValue().toString()) > rabbitCount) {
                        JOptionPane.showMessageDialog(null, "The Entered Number of Rabbits Exceeds the Number of Rabbits in Cage", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {

                        int count = 0;

                        for (int i = 0; i < Integer.parseInt(jSpinner5.getValue().toString()); i++) {
                            if (rabbitCount > 0) {
                                if (jCheckBox5.isSelected()) {
                                    tblRabbit.recordDeath(ids.get(i));
                                } else {
                                    tblRabbit.recordDeath(ids.get(i), new FormatDate().formatTime(jSpinner12.getValue().toString()));
                                }
                                count++;
                            }
                            rabbitCount--;
                        }
                        JOptionPane.showMessageDialog(null, count + " Rabbit Deaths Successfully Recorded");
                    }
                } else {

                    if (jCheckBox5.isSelected()) {
                        tblRabbit.recordDeath(ids.get(0));
                    } else {
                        tblRabbit.recordDeath(ids.get(0), new FormatDate().formatTime(jSpinner12.getValue().toString()));
                    }
                    JOptionPane.showMessageDialog(null, "1 Rabbit Death Successfully Recorded");
                }

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox9.removeAllItems();
                for (int id : tblRabbit.getStockCageIds("Selling Stock")) {
                    jComboBox9.addItem(tblCage.getCageName(id));
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NumberFormatException | HeadlessException e) {
                JOptionPane.showMessageDialog(null, "Enter Valid Values", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jComboBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox11ActionPerformed

    }//GEN-LAST:event_jComboBox11ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {

                if (tblRabbit.getRabbitsId(tblCage.getCageId(jComboBox10.getSelectedItem().toString()), jComboBox16.getSelectedItem().toString()).size() > 0) {
                    int rabbitId = tblRabbit.getRabbitsId(tblCage.getCageId(jComboBox10.getSelectedItem().toString()), jComboBox16.getSelectedItem().toString()).get(0);

                    tblRabbit.move(rabbitId, tblCage.getCageId(jComboBox11.getSelectedItem().toString()));

                    JOptionPane.showMessageDialog(null, "Rabbit Successfully Moved");

                    this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

                    refreshing = true;
                    if (jComboBox32.getSelectedItem().toString().equals("Selling Stock")) {

                        jComboBox10.removeAllItems();
                        for (int id : tblCage.getNonEmptyCageIds()) {
                            if (tblCage.getStockType(id).equals("Selling Stock")) {
                                jComboBox10.addItem(tblCage.getCageName(id));
                            }
                        }

                        jComboBox11.removeAllItems();
                        for (int id : tblCage.getEmptyCageIdsSellingStock(maxRabbitsInSellingCage)) {
                            jComboBox11.addItem(tblCage.getCageName(id));
                        }

                        jLabel42.show();
                        jComboBox16.show();

                    } else if (jComboBox32.getSelectedItem().toString().equals("Breeding Stock")) {

                        jComboBox10.removeAllItems();
                        for (int id : tblCage.getNonEmptyCageIds()) {
                            if (tblCage.getStockType(id).equals("Breeding Stock")) {
                                jComboBox10.addItem(tblCage.getCageName(id));
                            }
                        }

                        jComboBox11.removeAllItems();
                        for (int id : tblCage.getEmptyCageIds()) {
                            jComboBox11.addItem(tblCage.getCageName(id));
                        }

                        jLabel42.hide();
                        jComboBox16.hide();

                    }
                    refreshing = false;

                    this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                } else {
                    JOptionPane.showMessageDialog(null, "The Current Cage Doesn't Have The Selected Gender", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox33ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox33ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                if (jCheckBox11.isSelected()) {
                    tblMating.recordBirth(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox13.getSelectedItem().toString())), jComboBox12.getSelectedItem().toString(), Integer.parseInt(jSpinner10.getValue().toString()));
                    tblFostering.recordFostering(jComboBox13.getSelectedItem().toString(), Integer.parseInt(jSpinner10.getValue().toString()));
                    JOptionPane.showMessageDialog(null, "Birth Successfully Recorded");
                } else {
                    tblMating.recordBirth(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox13.getSelectedItem().toString())), jComboBox12.getSelectedItem().toString(), new FormatDate().formatTime(jSpinner17.getValue().toString()), Integer.parseInt(jSpinner10.getValue().toString()));
                    tblFostering.recordFostering(jComboBox13.getSelectedItem().toString(), Integer.parseInt(jSpinner10.getValue().toString()), new FormatDate().formatTime(jSpinner17.getValue().toString()));
                    JOptionPane.showMessageDialog(null, "Birth Successfully Recorded");
                }

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox13.removeAllItems();
                jComboBox18.removeAllItems();
                for (int id : tblMating.getPregnantMatingCageIds("Doe F")) {
                    jComboBox13.addItem(tblCage.getCageName(id));
                    jComboBox18.addItem(tblCage.getCageName(id));
                }

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                tblCage.placeNestBox(tblCage.getCageId(jComboBox14.getSelectedItem().toString()));
                JOptionPane.showMessageDialog(null, "Nest Box Successfully Placed");

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                /*
                 for (int id : tblMating.getPregnantMatingCageIds("Doe F")) {
                 jComboBox13.addItem(tblCage.getCageName(id));
                 jComboBox18.addItem(tblCage.getCageName(id));
                 jComboBox14.addItem(tblCage.getCageName(id));
                 }*/
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                tblCage.removeNestBox(tblCage.getCageId(jComboBox14.getSelectedItem().toString()));
                JOptionPane.showMessageDialog(null, "Nest Box Successfully Removed");
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {

                tblMating.recordFalsePregnancy(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox18.getSelectedItem().toString())), jComboBox17.getSelectedItem().toString());

                JOptionPane.showMessageDialog(null, "False Pregnancy Recorded");

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox13.removeAllItems();
                jComboBox18.removeAllItems();
                for (int id : tblMating.getPregnantMatingCageIds("Doe F")) {
                    jComboBox13.addItem(tblCage.getCageName(id));
                    jComboBox18.addItem(tblCage.getCageName(id));
                }

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jComboBox25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox25ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox25ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                tblMating.updateKids(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox24.getSelectedItem().toString())), jComboBox25.getSelectedItem().toString(), Integer.parseInt(jSpinner11.getValue().toString()));
                JOptionPane.showMessageDialog(null, "Kids Successfully Updated");

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox24.removeAllItems();
                for (int id : tblMating.getRabbitsWithKidsCageIds("Doe F")) {
                    jComboBox24.addItem(tblCage.getCageName(id));
                }

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jComboBox4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox4ItemStateChanged

    }//GEN-LAST:event_jComboBox4ItemStateChanged

    private void jComboBox23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox23ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox23ActionPerformed

    private void jComboBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox8ActionPerformed
        if (!refreshing) {
            jButton13.enable();
        }
    }//GEN-LAST:event_jComboBox8ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox18ActionPerformed
        if (!refreshing) {
            try {
                refreshing = true;
                jComboBox17.removeAllItems();

                if (jComboBox18.getItemCount() > 0) {
                    for (String date : tblMating.getMatingDates(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox18.getSelectedItem().toString())))) {
                        jComboBox17.addItem(date);
                    }
                }

                refreshing = false;
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jComboBox18ActionPerformed

    private void jComboBox13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox13ActionPerformed
        if (!refreshing) {
            try {
                refreshing = true;
                jComboBox12.removeAllItems();

                if (jComboBox13.getItemCount() > 0) {
                    for (String date : tblMating.getMatingDates(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox13.getSelectedItem().toString())))) {
                        jComboBox12.addItem(date);
                    }
                }
                refreshing = false;
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jComboBox13ActionPerformed

    private void jComboBox24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox24ActionPerformed
        if (!refreshing) {
            try {
                refreshing = true;
                jComboBox25.removeAllItems();

                if (jComboBox24.getItemCount() > 0) {
                    for (String date : tblMating.getMatingDates(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox24.getSelectedItem().toString())))) {
                        jComboBox25.addItem(date);
                    }
                }

                refreshing = false;
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jComboBox24ActionPerformed

    private void jTextField3CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField3CaretUpdate
        try {
            Statement stmt = conn.createStatement();
            String query;
            ResultSet rs;

            query = "SELECT * from TBL_RABBIT WHERE DEATHDATE IS NULL AND SOLD = FALSE";
            rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Weight (kg)", "Weighing Date", "Stock Type", "Birth Date"}, 0);
            while (rs.next()) {
                String r1 = tblCage.getCageName(tblRabbit.getCageId(rs.getInt("RABBITID")));
                if (rs.getInt("WEIGHTID") > 0 && r1.contains(jTextField3.getText().toUpperCase())) {
                    String r2 = tblWeight.getCurrrentWeight(rs.getInt("RABBITID")) + "";
                    String r3 = tblWeight.getLastWeighingDate(rs.getInt("RABBITID"));
                    String r4 = tblRabbit.getStockType(rs.getInt("RABBITID"));
                    String r5 = rs.getString("BIRTHDATE");
                    model.addRow(new Object[]{r1, r2, r3, r4, r5});
                }
            }
            jTable10.setModel(model);

        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jTextField3CaretUpdate

    private void jComboBox26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox26ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox26ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                if (tblRabbit.getRabbitsId(tblCage.getCageId(jComboBox26.getSelectedItem().toString()), jComboBox28.getSelectedItem().toString()).size() > 0) {
                    int rabbitId = tblRabbit.getRabbitsId(tblCage.getCageId(jComboBox26.getSelectedItem().toString()), jComboBox28.getSelectedItem().toString()).get(0);

                    tblRabbit.move(rabbitId, tblCage.getCageId(jComboBox27.getSelectedItem().toString()));
                    tblRabbit.updateStockType(rabbitId, "Breeding Stock");
                    JOptionPane.showMessageDialog(null, "Rabbit Successfully Moved to Breeding Stock");
                } else {
                    JOptionPane.showMessageDialog(null, "The Current Cage Doesn't Have The Selected Gender", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jComboBox29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox29ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox29ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                int rabbitId = tblRabbit.getRabbitId(tblCage.getCageId(jComboBox29.getSelectedItem().toString()));

                tblRabbit.move(rabbitId, tblCage.getCageId(jComboBox30.getSelectedItem().toString()));
                tblRabbit.updateStockType(rabbitId, "Selling Stock");
                tblRabbit.deactivate(rabbitId);
                JOptionPane.showMessageDialog(null, "Rabbit Successfully Moved to Selling Stock");

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox29.removeAllItems();
                jComboBox30.removeAllItems();
                jComboBox26.removeAllItems();
                jComboBox27.removeAllItems();
                jComboBox6.removeAllItems();
                for (int id : tblCage.getNonEmptyCageIds()) {
                    if (tblCage.getStockType(id).equals("Breeding Stock")) {
                        jComboBox29.addItem(tblCage.getCageName(id));
                    }
                }
                for (int id : tblCage.getEmptyCageIdsSellingStock(maxRabbitsInSellingCage)) {
                    jComboBox30.addItem(tblCage.getCageName(id));
                }
                for (int id : tblCage.getNonEmptyCageIds()) {
                    if (tblCage.getStockType(id).equals("Selling Stock")) {
                        jComboBox26.addItem(tblCage.getCageName(id));
                    }
                }
                for (int id : tblCage.getEmptyCageIds()) {
                    jComboBox27.addItem(tblCage.getCageName(id));
                }
                for (int id : tblRabbit.getActiveRabbitIds()) {
                    jComboBox6.addItem(tblCage.getCageName(tblRabbit.getCageId(id)));
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton17ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                tblRabbit.retire(tblRabbit.getRabbitId(tblCage.getCageId(jComboBox6.getSelectedItem().toString())));
                JOptionPane.showMessageDialog(null, "Rabbit Successfully Retired");

                this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
                jComboBox6.removeAllItems();
                for (int id : tblRabbit.getActiveRabbitIds()) {
                    jComboBox6.addItem(tblCage.getCageName(tblRabbit.getCageId(id)));
                }
                this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jTextField4CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField4CaretUpdate
        try {
            Statement stmt = conn.createStatement();
            String query;
            ResultSet rs;

            stmt = conn.createStatement();
            query = "SELECT * from TBL_CAGE WHERE NESTINDATE IS NOT NULL ";
            rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Date Nest Placed"}, 0);
            while (rs.next()) {
                String r1 = rs.getString("CAGENAME");
                String r2 = rs.getString("NESTINDATE");
                if (r1.contains(jTextField4.getText().toUpperCase())) {
                    model.addRow(new Object[]{r1, r2});
                }
            }

            jTable5.setModel(model);

        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jTextField4CaretUpdate

    private void jComboBox32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox32ActionPerformed
        if (!refreshing) {
            try {
                refreshing = true;
                if (jComboBox32.getSelectedItem().toString().equals("Selling Stock")) {

                    jComboBox10.removeAllItems();
                    for (int id : tblCage.getNonEmptyCageIds()) {
                        if (tblCage.getStockType(id).equals("Selling Stock")) {
                            jComboBox10.addItem(tblCage.getCageName(id));
                        }
                    }

                    jComboBox11.removeAllItems();
                    for (int id : tblCage.getEmptyCageIdsSellingStock(maxRabbitsInSellingCage)) {
                        jComboBox11.addItem(tblCage.getCageName(id));
                    }

                    jLabel42.show();
                    jComboBox16.show();

                } else if (jComboBox32.getSelectedItem().toString().equals("Breeding Stock")) {

                    jComboBox10.removeAllItems();
                    for (int id : tblCage.getNonEmptyCageIds()) {
                        if (tblCage.getStockType(id).equals("Breeding Stock")) {
                            jComboBox10.addItem(tblCage.getCageName(id));
                        }
                    }

                    jComboBox11.removeAllItems();
                    for (int id : tblCage.getEmptyCageIds()) {
                        jComboBox11.addItem(tblCage.getCageName(id));
                    }

                    jLabel42.hide();
                    jComboBox16.hide();

                }
                refreshing = false;

            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jComboBox32ActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (JOptionPane.showConfirmDialog(null, "Are You Sure?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            System.exit(0);
        }
    }//GEN-LAST:event_formWindowClosing

    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
        if (jCheckBox5.isSelected()) {
            jSpinner12.hide();
        } else {
            jSpinner12.show();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox5ActionPerformed

    private void jCheckBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox8ActionPerformed
        if (jCheckBox8.isSelected()) {
            jSpinner15.hide();
        } else {
            jSpinner15.show();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox8ActionPerformed

    private void jCheckBox10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox10ActionPerformed
        if (jCheckBox10.isSelected()) {
            jSpinner16.hide();
        } else {
            jSpinner16.show();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox10ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        if (jCheckBox3.isSelected()) {
            jSpinner9.hide();
        } else {
            jSpinner9.show();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox11ActionPerformed
        if (jCheckBox11.isSelected()) {
            jSpinner17.hide();
        } else {
            jSpinner17.show();
        }
        this.repaint();
    }//GEN-LAST:event_jCheckBox11ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        new About().setVisible(true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("www.gwayiba.co.za"));
        } catch (Exception e) {
            //
        }
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        File source = new File("C:/Conejo/_database");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Back Up Destination");

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.showOpenDialog(null);

        File dest = new File(fileChooser.getSelectedFile().getAbsolutePath() + "/_database");

        try {
            FileUtils.deleteDirectory(new File(fileChooser.getSelectedFile().getAbsolutePath() + "/_database"));
            FileUtils.copyDirectory(source, dest);
            JOptionPane.showMessageDialog(null, "Database Successfully Backed Up to " + fileChooser.getSelectedFile().getAbsolutePath() + "\nPlease Restart the Program");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Couldn't Back Up Database" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        new Reports().setVisible(true);
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        int count = 0;//Notifications
        try {
            tblNotification.createNotifications();

            for (String notif : tblNotification.getNotifications()) {
                jTable7.setValueAt(notif, count, 0);
                count++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        jTabbedPane5.setSelectedIndex(0);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        new Options().setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Are You Sure?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            System.exit(0);
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        //Removes Previously Added Items In ComboBoxes

        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        refreshing = true;
        if (jComboBox2.getItemCount() > 0) {
            jComboBox2.removeAllItems();
        }
        if (jComboBox3.getItemCount() > 0) {
            jComboBox3.removeAllItems();
        }
        if (jComboBox23.getItemCount() > 0) {
            jComboBox23.removeAllItems();
        }
        if (jComboBox22.getItemCount() > 0) {
            jComboBox22.removeAllItems();
        }
        if (jComboBox1.getItemCount() > 0) {
            jComboBox1.removeAllItems();
        }
        if (jComboBox4.getItemCount() > 0) {
            jComboBox4.removeAllItems();
        }
        if (jComboBox8.getItemCount() > 0) {
            jComboBox8.removeAllItems();
        }
        if (jComboBox9.getItemCount() > 0) {
            jComboBox9.removeAllItems();
        }
        if (jComboBox5.getItemCount() > 0) {
            jComboBox5.removeAllItems();
        }
        if (jComboBox10.getItemCount() > 0) {
            jComboBox10.removeAllItems();
        }
        if (jComboBox11.getItemCount() > 0) {
            jComboBox11.removeAllItems();
        }
        if (jComboBox7.getItemCount() > 0) {
            jComboBox7.removeAllItems();
        }
        if (jComboBox24.getItemCount() > 0) {
            jComboBox24.removeAllItems();
        }
        if (jComboBox14.getItemCount() > 0) {
            jComboBox14.removeAllItems();
        }
        if (jComboBox29.getItemCount() > 0) {
            jComboBox29.removeAllItems();
        }
        if (jComboBox30.getItemCount() > 0) {
            jComboBox30.removeAllItems();
        }
        if (jComboBox26.getItemCount() > 0) {
            jComboBox26.removeAllItems();
        }
        if (jComboBox27.getItemCount() > 0) {
            jComboBox27.removeAllItems();
        }
        if (jComboBox6.getItemCount() > 0) {
            jComboBox6.removeAllItems();
        }
        if (jComboBox13.getItemCount() > 0) {
            jComboBox13.removeAllItems();
        }
        if (jComboBox18.getItemCount() > 0) {
            jComboBox18.removeAllItems();
        }
        if (jComboBox36.getItemCount() > 0) {
            jComboBox36.removeAllItems();
        }
        if (jComboBox31.getItemCount() > 0) {
            jComboBox31.removeAllItems();
        }
        if (jComboBox34.getItemCount() > 0) {
            jComboBox34.removeAllItems();
        }

        refresh();
        jTabbedPane5.setSelectedIndex(0);

        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jTextField5CaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_jTextField5CaretUpdate
        try {
            Statement stmt = conn.createStatement();
            String query = "SELECT * from TBL_FOSTERING WHERE FOSTERDATE IS NOT NULL";
            ResultSet rs = stmt.executeQuery(query);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Cage Name", "Number of Kids in Cage", "Kids Age"}, 0);
            while (rs.next()) {
                String r1 = rs.getString("CAGENAME");
                if (r1.contains(jTextField5.getText().toUpperCase())) {
                    String r2 = rs.getInt("KIDS") + "";
                    String r3 = rs.getString("FOSTERDATE");
                    r3 = DateOperations.getDifferenceInDays(new Date(r3)) + " days";
                    model.addRow(new Object[]{r1, r2, r3});
                }
            }
            jTable11.setModel(model);

        } catch (SQLException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jTextField5CaretUpdate

    private void jComboBox31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox31ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox31ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                if (tblFostering.moveKids(jComboBox34.getSelectedItem().toString(), jComboBox31.getSelectedItem().toString(), Integer.parseInt(jSpinner4.getValue().toString()))) {
                    JOptionPane.showMessageDialog(null, "Kids Moved Successfully");
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Input", "Error", JOptionPane.ERROR_MESSAGE);
                }

                if (jComboBox34.getItemCount() > 0) {
                    jComboBox34.removeAllItems();
                }
                if (jComboBox36.getItemCount() > 0) {
                    jComboBox36.removeAllItems();
                }
                for (String cageName : tblFostering.getCagesWithKids()) {
                    jComboBox34.addItem(cageName);
                    jComboBox36.addItem(cageName);
                }
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Apply this action?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION) == 0) {
            try {
                tblFostering.updateKids(jComboBox36.getSelectedItem().toString(), Integer.parseInt(jSpinner13.getValue().toString()));
                JOptionPane.showMessageDialog(null, "Kids Successfully Updated");

                if (jComboBox34.getItemCount() > 0) {
                    jComboBox34.removeAllItems();
                }
                if (jComboBox36.getItemCount() > 0) {
                    jComboBox36.removeAllItems();
                }
                for (String cageName : tblFostering.getCagesWithKids()) {
                    jComboBox36.addItem(cageName);
                    jComboBox34.addItem(cageName);
                }
            } catch (SQLException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jComboBox36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox36ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox36ActionPerformed

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
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        new Splash().updateProgress();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox10;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox8;
    private javax.swing.JCheckBox jCheckBox9;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox10;
    private javax.swing.JComboBox jComboBox11;
    private javax.swing.JComboBox jComboBox12;
    private javax.swing.JComboBox jComboBox13;
    private javax.swing.JComboBox jComboBox14;
    private javax.swing.JComboBox jComboBox15;
    private javax.swing.JComboBox jComboBox16;
    private javax.swing.JComboBox jComboBox17;
    private javax.swing.JComboBox jComboBox18;
    private javax.swing.JComboBox jComboBox19;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox20;
    private javax.swing.JComboBox jComboBox21;
    private javax.swing.JComboBox jComboBox22;
    private javax.swing.JComboBox jComboBox23;
    private javax.swing.JComboBox jComboBox24;
    private javax.swing.JComboBox jComboBox25;
    private javax.swing.JComboBox jComboBox26;
    private javax.swing.JComboBox jComboBox27;
    private javax.swing.JComboBox jComboBox28;
    private javax.swing.JComboBox jComboBox29;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox30;
    private javax.swing.JComboBox jComboBox31;
    private javax.swing.JComboBox jComboBox32;
    private javax.swing.JComboBox jComboBox33;
    private javax.swing.JComboBox jComboBox34;
    private javax.swing.JComboBox jComboBox36;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JComboBox jComboBox8;
    private javax.swing.JComboBox jComboBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JLabel jLabel55;
    private javax.swing.JLabel jLabel56;
    private javax.swing.JLabel jLabel57;
    private javax.swing.JLabel jLabel58;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenu jMenu7;
    private javax.swing.JMenu jMenu8;
    private javax.swing.JMenu jMenu9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner10;
    private javax.swing.JSpinner jSpinner11;
    private javax.swing.JSpinner jSpinner12;
    private javax.swing.JSpinner jSpinner13;
    private javax.swing.JSpinner jSpinner15;
    private javax.swing.JSpinner jSpinner16;
    private javax.swing.JSpinner jSpinner17;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JSpinner jSpinner3;
    private javax.swing.JSpinner jSpinner4;
    private javax.swing.JSpinner jSpinner5;
    private javax.swing.JSpinner jSpinner6;
    private javax.swing.JSpinner jSpinner7;
    private javax.swing.JSpinner jSpinner8;
    private javax.swing.JSpinner jSpinner9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable10;
    private javax.swing.JTable jTable11;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    private javax.swing.JTable jTable7;
    private javax.swing.JTable jTable8;
    private javax.swing.JTable jTable9;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
