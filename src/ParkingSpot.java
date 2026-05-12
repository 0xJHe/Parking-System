public class ParkingSpot {
    String spotID;
    String type;
    boolean occupied = false;
    Vehicle currVehicle = null;
    double rate;

    public ParkingSpot(int floor, int row, int spot, String type, double rate) {
        this.spotID = "F" + floor + "-R" + row + "-S" + spot;
        this.type = type;
        this.rate = rate;
    }

    public boolean spotTypeValidation(Vehicle v, boolean handicappedCard) {
        String vehicleType = v.getType();

        if (this.type.equals("Handicapped")) {
            // if it's handicapped car OR has hadicapped card
            if (vehicleType.equals("Handicapped Car") || handicappedCard) {
                return true;
            }
            return false;
        }

        if (this.type.equals("Compact")) {
            // Compact only for motorcycle, car and hadicapped car
            if (vehicleType.equals("Motorcycle") || vehicleType.equals("Car") || vehicleType.equals("Handicapped Car")) {
                return true;
            }
            return false;
        }

        if (this.type.equals("Regular")) {
            // Regular only for car, suv and hadicapped car
            if (vehicleType.equals("Car") || vehicleType.equals("SUV") || vehicleType.equals("Handicapped Car")) {
                return true;
            }
            return false;
        }

        if (this.type.equals("VIP Reserved")) {
            return true; 
        }

        return false;
    }

    public boolean isSpotOccupied() { return occupied; }
    public String getSpotID() { return spotID; }
    public String getType() { return type; }
    public void parkVehicle(Vehicle v) { 
        this.currVehicle = v;
        this.occupied = true;
    }
    public void unparkVehicle() {
        this.currVehicle = null;
        this.occupied = false;
    }
    public Vehicle getCurrVehicle() { return currVehicle; }

} 