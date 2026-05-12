import javax.swing.*;

public class Parking extends JFrame {
    public static FineStrategy activeFineStrategy = new FixedFine();

    public Parking() {
        setTitle("Parking Lot Management System");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ParkingLot lot = new ParkingLot(3, 5, 5);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Customer", new CustomerPanel(lot));
        tabbedPane.addTab("Admin", new AdminPanel());

        add(tabbedPane);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Parking();
    }
}
