import java.time.LocalDateTime;

public abstract class Vehicle {

    String type;
    String plate;
    LocalDateTime entryTime;
    LocalDateTime exitTime;

    public String getPlate() { return plate; }
    public String getType() { return type; }

    public void setEntryTime(LocalDateTime t) {
        this.entryTime = t;
    }
}

// Factory method so can easily add a new vehicle type
class CreateVehicleType {
    // static so no need create the obj
    public static Vehicle createVehicle(VehicleType type, String plate) {

        switch (type) {
            case MOTORCYCLE:
                return new Motorcycle(plate, LocalDateTime.now());
            case CAR:
                return new Car(plate, LocalDateTime.now());
            case SUV:
                return new SUV(plate, LocalDateTime.now());
            case HANDICAPPED_CAR:
                return new HandicappedCar(plate, LocalDateTime.now());
            default:
                return null;
        }
        
    }
}

class Motorcycle extends Vehicle {
    public Motorcycle(String plate, LocalDateTime entryTime) {
        this.plate = plate;
        this.entryTime = entryTime;
        this.type = "Motorcycle";
    }
    
}

class Car extends Vehicle {
    public Car(String plate, LocalDateTime entryTime) {
        this.plate = plate;
        this.entryTime = entryTime;
        this.type = "Car";
    }

}

class SUV extends Vehicle {
    public SUV(String plate, LocalDateTime entryTime) {
        this.plate = plate;
        this.entryTime = entryTime;
        this.type = "SUV";
    }
}

class HandicappedCar extends Vehicle {
    public HandicappedCar(String plate, LocalDateTime entryTime) {
        this.plate = plate;
        this.entryTime = entryTime;
        this.type = "Handicapped Car";
    }
}