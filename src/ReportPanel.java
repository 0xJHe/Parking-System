import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReportPanel extends JPanel {
    private JTabbedPane tabs;

    public ReportPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        tabs = new JTabbedPane();
        tabs.addTab("Current Vehicles", currVehiclesPanel());
        tabs.addTab("Revenue", revenuePanel());
        tabs.addTab("Occupancy", occupancyPanel());
        tabs.addTab("Unpaid Fines", finesPanel());

        add(tabs, BorderLayout.CENTER);

    }

    private JPanel currVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Spot ID", "Plate", "Vehicle Type", "Entry Time"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        ConnDB db = new ConnDB();
        db.connect();
        try {
            Statement s = db.getStatement();
            String sql = "SELECT s.spot_id, s.plate, v.type, t.entryTime " +
                         "FROM spots s " + "JOIN vehicles v ON s.plate = v.plate " +
                         "JOIN tickets t ON s.plate = t.plate " + "WHERE s.occupied = 1";
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                // Remove the T to show the formatted time
                String time = rs.getString("entryTime").replace("T", " ").substring(0, 16);
                model.addRow(new Object[]{
                    rs.getString("spot_id"),
                    rs.getString("plate"),
                    rs.getString("type"),
                    time
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } finally {
            db.disconnect();
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel revenuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel totalLabel = new JLabel("Total Revenue: RM 0.00");
        totalLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        String[] cols = {"ID", "Plate", "Amount (RM)", "Date", "Method"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        double totalRevenue = 0.0;
        ConnDB db = new ConnDB();
        db.connect();
        try {
            Statement s = db.getStatement();
            String sql = "SELECT * FROM transactions ORDER BY id DESC";
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                double amount = rs.getDouble("amount");
                totalRevenue += amount;
                
                String time = rs.getString("time").replace("T", " ").substring(0, 16);
                String method = rs.getString("method");

                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("plate"),
                    String.format("%.2f", amount),
                    time,
                    method
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } finally {
            db.disconnect();
        }

        totalLabel.setText("Total Revenue Collected: RM " + String.format("%.2f", totalRevenue));
        
        panel.add(totalLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel occupancyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Floor", "Total Spots", "Occupied", "Available", "Occupancy %"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        ConnDB db = new ConnDB();
        db.connect();
        try {
            Statement s = db.getStatement();
            // extract and group by the floor from spot_id F1/F2/F3 as floor
            // gets total spots and SUM(occupied) gets the number of occupied spots
            String sql = "SELECT " + "substr(spot_id, 1, instr(spot_id, '-') - 1) as floor, " +
                         "COUNT(*) as Total, " + "SUM(occupied) as occupiedCount " +
                         "FROM spots " + "GROUP BY floor";
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                String floor = rs.getString("floor");
                int total = rs.getInt("Total");
                int occupied = rs.getInt("occupiedCount");
                int available = total - occupied;
                
                // Calc percentage
                double percent = 0.0;
                if (total > 0) {
                    percent = ((double)occupied / total) * 100;
                }

                model.addRow(new Object[]{
                    floor, 
                    total, 
                    occupied, 
                    available, 
                    String.format("%.1f%%", percent)
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } finally {
            db.disconnect();
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel finesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Plate", "Unpaid Fines(RM)", "Vehicle Type"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        ConnDB db = new ConnDB();
        db.connect();
        try {
            Statement s = db.getStatement();
            String sql = "SELECT f.plate, f.amount, v.type " + "FROM fines f " +
                         "JOIN vehicles v ON f.plate = v.plate";
            ResultSet rs = s.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("plate"),
                    String.format("%.2f", rs.getDouble("amount")),
                    rs.getString("type")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        } finally {
            db.disconnect();
        }

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    public void refresh() {
        tabs.setComponentAt(0, currVehiclesPanel());
        tabs.setComponentAt(1, revenuePanel());
        tabs.setComponentAt(2, occupancyPanel());
        tabs.setComponentAt(3, finesPanel());
    }
}