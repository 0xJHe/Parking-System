import java.time.LocalDateTime;

public class Ticket {
    private String ticketID;
    private Vehicle vehicle;

    public Ticket(Vehicle v, ParkingSpot spot, LocalDateTime time) {
        this.vehicle = v;
        this.ticketID = generateTicketID();
    }

    // T-PLATE-TIMESTAMP
    private String generateTicketID() {
        long timestamp = System.currentTimeMillis();
        return "T-" + vehicle.getPlate().toUpperCase() + "-" + timestamp;
    }
    public String getTicketID() { return ticketID; }
}