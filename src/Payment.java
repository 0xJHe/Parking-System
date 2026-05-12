import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class Payment {
    private String plate;
    private double amount;
    private String method;
    private LocalDateTime paymentTime;

    public Payment(String plate, double amount, String method) {
        this.plate = plate;
        this.amount = amount;
        this.method = method;
        this.paymentTime = LocalDateTime.now();
    }

    public boolean processPayment(String ticketID, String spotID) {
        ConnDB db = new ConnDB();
        db.connect();
        Statement s = db.getStatement();

        try {
            // Insert transactions for record
            String sql = String.format(
                "INSERT INTO transactions (plate, amount, time, method) VALUES ('%s', %.2f, '%s', '%s')",
                this.plate, this.amount, this.paymentTime.toString(), this.method
            );
            s.executeUpdate(sql);

            // Delete the fines
            String sql2 = String.format("DELETE FROM fines WHERE plate = '%s'", this.plate);
            s.executeUpdate(sql2);

            // Release spot in database
            String sql3 = String.format(
                "UPDATE spots SET occupied = 0, plate = NULL WHERE spot_id = '%s'", 
                spotID
            );
            s.executeUpdate(sql3);

            // Delete ticket in database
            String sql4 = String.format("DELETE FROM tickets WHERE id = '%s'", ticketID);
            s.executeUpdate(sql4);

            return true;

        } catch (SQLException e) {
            System.out.println("Payment Error: " + e.getMessage());
            return false;
        } finally {
            db.disconnect();
        }
    }
    
    public double getAmount() { return amount; }
}