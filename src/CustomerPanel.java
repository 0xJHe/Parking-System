import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerPanel extends JPanel {
    private JPanel floorPanel;
    private JTabbedPane floorTab;
    // entry variables
    private ParkingLot lot;
    private ParkingSpot spotSelected;
    private JLabel spotSelectedLabel;
    
    private JTextField plateField;
    private JComboBox<VehicleType> types;
    private JCheckBox handicappedCard;
    private JButton parkBtn;
    private JButton spotSelectedBtn;

    // exit vaiables
    private String exitPlate;
    private String exitTicketID;
    private String exitSpotID;
    private double totalFee;

    public CustomerPanel(ParkingLot lot) {
        this.lot = lot;
        setLayout(new BorderLayout());
        
        JTabbedPane customerTabs = new JTabbedPane();
        customerTabs.addTab("Entry", entryPanel());
        customerTabs.addTab("Exit", exitPanel());
        
        add(customerTabs, BorderLayout.CENTER);
    }

    private JPanel entryPanel() {
        JPanel panel = new JPanel(new BorderLayout(20,20));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel getDetailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        getDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Vehicle Details: Enter your vehicle details."), 
            new EmptyBorder(10, 10, 10, 10))
        );
        
        floorPanel = new JPanel(new BorderLayout(20, 20));
        floorPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Parking Floor: Select your floor and parking spot."), 
            new EmptyBorder(10, 10, 10, 10))
        );

        JLabel plateHint = new JLabel("Enter Your Car Plate Number:");
        plateField = new JTextField();
        plateField.setPreferredSize(new Dimension(80,25));
        JLabel vehicleHint = new JLabel("Vehicle Type:");
        types = new JComboBox<>(VehicleType.values());
        handicappedCard = new JCheckBox("Handicapped Card");
        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {setComponentsUsable(floorPanel, true);});

        floorTab = new JTabbedPane();
        ArrayList<ParkingFloor> floorList = lot.getFloorsList();
        for (int i = 1; i <= floorList.size(); i++) {
            ParkingFloor floor = floorList.get(i-1);
            floorTab.addTab("Floor " + i, spotPanel(lot, floor));
        }

        JPanel confirmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spotSelectedLabel = new JLabel("You selected spot: None");
        parkBtn = new JButton("Park Here");
        parkBtn.addActionListener(e -> {
            if (spotSelected == null) {
                JOptionPane.showMessageDialog(this, "Please select a parking spot first");
                return;
            }

            VehicleType selectedType = (VehicleType)types.getSelectedItem();
            String plate = plateField.getText().toUpperCase();

            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your vehicle plate number");
                return;
            }

            Vehicle v = CreateVehicleType.createVehicle(selectedType, plate);
            if (v == null) {
                JOptionPane.showMessageDialog(this, "Invalid Vehicle Type.");
                return;
            }

            // if spot cannot fit the vehicle
            if (!spotSelected.spotTypeValidation(v, handicappedCard.isSelected())) {
                JOptionPane.showMessageDialog(this, v.getType() + " Cannot park at " + spotSelected.getType() + " Spot");
                return;
            }

            spotSelected.parkVehicle(v);
            Ticket ticket = new Ticket(v, spotSelected, LocalDateTime.now());
            if(!saveVehicleEntry(v, ticket, spotSelected, handicappedCard.isSelected())) {
                return;
            }
            
            spotSelectedLabel.setText(plate + " parked in " + spotSelected.getSpotID());
            parkBtn.setEnabled(false);
            JOptionPane.showMessageDialog(this, "Parked Successfully!");
            spotSelectedBtn.setBackground(Color.RED);
            spotSelectedBtn.setEnabled(false);

            // Reset
            plateField.setText("");
            handicappedCard.setSelected(false);
            spotSelected = null;
            spotSelectedBtn = null;
            setComponentsUsable(floorPanel, false);

        });

        confirmPanel.add(spotSelectedLabel);
        confirmPanel.add(parkBtn);

        getDetailsPanel.add(plateHint);
        getDetailsPanel.add(plateField);
        getDetailsPanel.add(vehicleHint);
        getDetailsPanel.add(types);
        getDetailsPanel.add(handicappedCard);
        getDetailsPanel.add(submitBtn);

        floorPanel.add(floorTab, BorderLayout.CENTER);
        floorPanel.add(confirmPanel, BorderLayout.SOUTH);
        setComponentsUsable(floorPanel, false);

        panel.add(getDetailsPanel, BorderLayout.NORTH);
        panel.add(floorPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel exitPanel() {
        JPanel panel = new JPanel(new BorderLayout(20,20));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel getDetailsPanel = new JPanel(new GridLayout(1, 5, 20, 20));
        getDetailsPanel.setBorder(BorderFactory.createCompoundBorder(
            new TitledBorder("Vehicle Details: Enter your vehicle details."), 
            new EmptyBorder(10, 10, 10, 10))
        );

        JLabel plateHint = new JLabel("Enter Your Car Plate Number:");
        JTextField plateField = new JTextField();
        JButton findBtn = new JButton("Find");
        getDetailsPanel.add(plateHint);
        getDetailsPanel.add(plateField);
        getDetailsPanel.add(findBtn);

        JTextArea receipt = new JTextArea();
        receipt.setBorder(new TitledBorder("Receipt Details"));
        receipt.setEditable(false);

        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        JLabel methodLabel = new JLabel("Payment Method:");
        String[] methods = {"Cash", "Bank Card", "E-Wallet"};
        JComboBox<String> methodSelector = new JComboBox<>(methods);
        JButton paymentBtn = new JButton("Pay Fee & Exit");
        submitPanel.add(methodLabel);
        submitPanel.add(methodSelector);
        submitPanel.add(paymentBtn);
        setComponentsUsable(submitPanel, false);

        panel.add(getDetailsPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(receipt), BorderLayout.CENTER);
        panel.add(submitPanel, BorderLayout.SOUTH);

        findBtn.addActionListener(e -> {
            String plate = plateField.getText().toUpperCase();
            // error handling
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your car plate.");
                return;
            }

            ConnDB db = new ConnDB();
            db.connect();

            try {
                Statement s = db.getStatement();
                String sql = String.format( "SELECT t.id, t.spotID, t.entryTime, s.type, s.rate, v.handicapped_card " +
                    "FROM tickets t " + "JOIN spots s ON t.spotID = s.spot_id " +
                    "JOIN vehicles v ON t.plate = v.plate " + "WHERE t.plate = '%s'", plate);

                ResultSet rs = s.executeQuery(sql);

                if (rs.next()) {
                    exitPlate = plate;
                    exitTicketID = rs.getString("id");
                    exitSpotID = rs.getString("spotID");
                    // parse to convert the string to LocalDateTime
                    LocalDateTime entryTime = LocalDateTime.parse(rs.getString("entryTime"));
                    String spotType = rs.getString("type");
                    double spotRate = rs.getDouble("rate");
                    boolean handicappedCard = rs.getInt("handicapped_card") == 1;

                    // Calc duration
                    LocalDateTime exitTime = LocalDateTime.now();
                    int duration = calcHours(entryTime, exitTime);

                    // Calc parking fee
                    boolean handicappedSpot = spotType.equals("Handicapped");
                    double fee = calculateFee(duration, spotRate, handicappedSpot, handicappedCard);

                    // Calc fine
                    double fine = calcFine(entryTime, exitTime);

                    // check if got previous fine
                    Statement s2 = db.getStatement();
                    String sql2 = String.format("SELECT SUM(amount) as totalFines FROM fines WHERE plate = '%s'", plate);
                    ResultSet rs2 = s2.executeQuery(sql2);
                    double previousFines = 0.0;
                    if (rs2.next()) {
                        previousFines = rs2.getDouble("totalFines");
                    }

                    totalFee = fee + fine + previousFines;
                    generateReceipt(receipt, exitTicketID, plate, spotType, handicappedCard, entryTime, exitTime, 
                        duration, fee, fine, previousFines, totalFee);
                    // Enable the buttons
                    setComponentsUsable(submitPanel, true);
                    
                } else {
                    JOptionPane.showMessageDialog(this, plate + " Vehicle not found.");
                    receipt.setText("");
                    setComponentsUsable(submitPanel, false);
                }

            } catch (Exception error) {
                JOptionPane.showMessageDialog(this, "Error: " + error.getMessage());
            } finally {
                db.disconnect();
            }
        });

        paymentBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Pay RM " + String.format("%.2f", totalFee) + " via " + methodSelector.getSelectedItem() + "?",
                "Confirm Payment", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Payment payment = new Payment(exitPlate, totalFee, (String)methodSelector.getSelectedItem());
                
                
                if (payment.processPayment(exitTicketID, exitSpotID)) {
                    for (ParkingFloor floor: lot.getFloorsList()) { // every floor
                        for (ParkingSpot spot: floor.getSpotsList()) { // every spot
                            if (spot.getSpotID().equals(exitSpotID)) { // if the spot is equals to exitSpotID
                                spot.unparkVehicle();
                                break;
                            }
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Payment Successful.");
                    
                    // Reset
                    receipt.setText("");
                    plateField.setText("");
                    setComponentsUsable(submitPanel, false);
                    exitPlate = null;
                    refreshFloorTab();
                } else {
                    JOptionPane.showMessageDialog(this, "Payment Failed. Please try again.");
                }
            }

        });

        return panel;
    }

    private JPanel spotPanel(ParkingLot lot, ParkingFloor floor) {
        JPanel spotP = new JPanel(new GridLayout(lot.getRows(), lot.getSpots(), 10, 10));
        spotP.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        ArrayList<ParkingSpot> s = floor.getSpotsList();
        ArrayList<String[]> occupiedSpots = getOccupiedSpots();
        for (int i = 1; i <= s.size(); i++) {
            ParkingSpot currSpot = s.get(i-1);
            // Use html to show the spot type in next line
            JButton spotBtn = new JButton("<html>Spot " + currSpot.getSpotID() + "<br>" + currSpot.getType() + "</html>");

            String[] occuppiedSpotData = null;
            // check every occupied spot is same as current spot id
            for (String[] row: occupiedSpots) {
                if (row[0].equals(currSpot.getSpotID())) {
                    occuppiedSpotData = row;
                    break;
                }
            }

            // show green if spot is available
            if (currSpot.isSpotOccupied() || occuppiedSpotData != null) {
                spotBtn.setBackground(Color.RED);
                spotBtn.setEnabled(false);
                if (currSpot.isSpotOccupied() && occuppiedSpotData != null) { 
                    String plate = occuppiedSpotData[1];
                    String type = occuppiedSpotData[2];
                    String time = occuppiedSpotData[3];
                    
                    // 
                    VehicleType vehicleType = null;
                    for (VehicleType vt: VehicleType.values()) {
                        if (vt.toString().equals(type)) {
                            vehicleType = vt;
                            break;
                        }
                    }
                    
                    // Create and Park
                    if (vehicleType != null) {
                        Vehicle v = CreateVehicleType.createVehicle(vehicleType, plate);
                        v.setEntryTime(LocalDateTime.parse(time));
                        currSpot.parkVehicle(v);
                    }
                }
            } else {
                spotBtn.setBackground(Color.GREEN);
                spotBtn.addActionListener(e -> {
                    // spotIDSelected = currSpot.getSpotID();
                    spotSelected = currSpot;
                    spotSelectedBtn = spotBtn;
                    spotSelectedLabel.setText("You selected spot: " + currSpot.getSpotID());
                    // parkBtn.setEnabled(true);
                });
            }

            spotP.add(spotBtn);
        }
        
        return spotP;
    }

    private void setComponentsUsable(Container c, boolean b) {
        Component[] components = c.getComponents();
        for (Component com: components) {
            com.setEnabled(b);
            // if a component is a container, 
            // then call itself to set the status of its components
            if (com instanceof Container) {
                setComponentsUsable((Container) com, b);
            }
        }
    }

    // function to load occupied spots from DB
    private ArrayList<String[]> getOccupiedSpots() {
        ArrayList<String[]> occupiedSpots = new ArrayList<>();
        ConnDB db = new ConnDB();
        db.connect();
        Statement s = db.getStatement();
        
        if (s != null) {
            try {
                String sql = "SELECT s.spot_id, t.plate, v.type, t.entryTime " +
                             "FROM spots s " + "JOIN tickets t ON s.spot_id = t.spotID " +
                             "JOIN vehicles v ON t.plate = v.plate " + "WHERE s.occupied = 1";
                ResultSet rs = s.executeQuery(sql);
    
                while (rs.next()) {
                    String[] data = new String[4];
                    data[0] = rs.getString("spot_id");
                    data[1] = rs.getString("plate");
                    data[2] = rs.getString("type");
                    data[3] = rs.getString("entryTime");
                    // Add the occupied spot data to the list
                    occupiedSpots.add(data);
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
            } finally {
                db.disconnect();
            }
        }
        return occupiedSpots;
    }

    // function to save the vehicle entry to the database
    private boolean saveVehicleEntry(Vehicle v, Ticket t, ParkingSpot spot, boolean handicappedCard) {
        ConnDB db = new ConnDB();
        db.connect();
        Statement s = db.getStatement();
        
        if (s == null) return false;
        try {
            // Insert vehicle
            String sqlVehicle = String.format("INSERT OR IGNORE INTO vehicles (plate, type, handicapped_card) VALUES ('%s', '%s', %d)", 
                    v.getPlate(), v.getType(), handicappedCard ? 1 : 0);
            s.executeUpdate(sqlVehicle);

            /* Insert spots to occupied
            use insert/replace instead of update to make sure db work if a spot not in the db 
            make the spots can increase in the future */
            String sqlSpot = String.format("INSERT OR REPLACE INTO spots (spot_id, type, occupied, plate, rate) VALUES ('%s', '%s', 1, '%s', %.2f)",
                    spot.getSpotID(), spot.getType(), v.getPlate(), spot.rate);
            s.executeUpdate(sqlSpot);

            // Insert ticket
            String sqlTicket = String.format("INSERT INTO tickets (id, plate, spotID, entryTime) VALUES ('%s', '%s', '%s', '%s')",
                    t.getTicketID(), v.getPlate(), spot.getSpotID(), LocalDateTime.now().toString());
            s.executeUpdate(sqlTicket);

            System.out.println("Transaction Saved: " + t.getTicketID());
            return true;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
            return false;
        } finally {
            db.disconnect();
        }
    }

    // Calc duration
    private int calcHours(LocalDateTime entry, LocalDateTime exit) {
        Duration duration = Duration.between(entry, exit);
        int hours = (int)duration.toHours();
        if (hours <= 0) {
            return 1;
        } else {
            return hours;
        }
    }

    // Calc parking fee
    private double calculateFee(int hours, double rate, boolean handicappedSpot, boolean handicappedCard) {
        if (handicappedSpot || handicappedCard) {
            rate = 2.0;
        }
        if (handicappedSpot && handicappedCard) {
            rate = 0;
        }
        return hours * rate;
    }

    // Calc fine
    private double calcFine(LocalDateTime entry, LocalDateTime exit) {
        Duration duration = Duration.between(entry, exit);
        int hours = (int)duration.toHours();

        if (hours > 24) {
            return Parking.activeFineStrategy.calculateFine(hours);
        }
        return 0.0;
    }

    private void generateReceipt(JTextArea receiptJText, String ticketID, String plate, String spotType, boolean handicappedCard, 
        LocalDateTime entry, LocalDateTime exit, int duration, double fee, double fine, double previousFines, double total) {

        String text = "###### PARKING RECEIPT ######\n";
        text += " Ticket ID:    " + ticketID + "\n";
        text += " Plate No:     " + plate + "\n";
        text += " Spot Type:     " + spotType + "\n";
        if (handicappedCard) {
            text += " Handicapped Card Holder:     Yes\n";
        }
        text += "\n";
        
        // format the entry exit time to show date and time correctly
        text += " Entry Time:   " + entry.toString().replace("T", " ").substring(0, 16) + "\n";
        text += " Exit Time:    " + exit.toString().replace("T", " ").substring(0, 16) + "\n";
        text += " Duration:     " + duration + " Hour(s)\n";
        
        text += "\n";
        text += " Parking Fee:  RM " + String.format("%.2f", fee) + "\n";
        
        if (fine > 0) {
            text += " Fines:        RM " + String.format("%.2f", fine) + "\n";
        }
        if (previousFines > 0) {
            text += "Previous Fines:        RM " + String.format("%.2f", previousFines) + "\n";
        }

        text += "\n";
        text += "TOTAL:  RM " + String.format("%.2f", total) + "\n";
        text += "\n";

        receiptJText.setText(text);
    }

    private void refreshFloorTab() {
        floorTab.removeAll();
        ArrayList<ParkingFloor> floorList = lot.getFloorsList();
        for (int i = 1; i <= floorList.size(); i++) {
            ParkingFloor floor = floorList.get(i-1);
            floorTab.addTab("Floor " + i, spotPanel(lot, floor));
        }
        floorTab.revalidate();
        floorTab.repaint();
        setComponentsUsable(floorPanel, false);
    }
}