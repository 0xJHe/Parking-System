import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AdminPanel extends JPanel {
    public AdminPanel() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JComboBox<String> fineStrategyBox = new JComboBox<>(
                new String[]{"Fixed", "Hourly", "Progressive"}
        );

        fineStrategyBox.addActionListener(e -> {
            String selected = (String) fineStrategyBox.getSelectedItem();
            switch (selected) {
                case "Fixed":
                    Parking.activeFineStrategy = new FixedFine();
                    break;
                case "Hourly":
                    Parking.activeFineStrategy = new HourlyFine();
                    break;
                case "Progressive":
                    Parking.activeFineStrategy = new ProgressiveFine();
                    break;
            }
            JOptionPane.showMessageDialog(this, "Strategy Updated to: " + selected);
        });

        JButton refreshReportsBtn = new JButton("Refresh Reports");

        topPanel.add(new JLabel("Active Fine Strategy:"));
        topPanel.add(fineStrategyBox);
        topPanel.add(refreshReportsBtn);

        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Reports"), 
            new EmptyBorder(10, 10, 10, 10))
        );
        ReportPanel reports = new ReportPanel();
        reportPanel.add(reports);

        refreshReportsBtn.addActionListener(e -> reports.refresh());

        add(topPanel, BorderLayout.NORTH);
        add(reportPanel, BorderLayout.CENTER);
    }


}