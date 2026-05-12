public enum VehicleType {
    CAR("Car"),
    MOTORCYCLE("Motorcycle"),
    SUV("SUV"),
    HANDICAPPED_CAR("Handicapped Car");

    private String label;
    // constructor 
    VehicleType(String label) {
        this.label = label;
    }

    // override toString so VehicleType.values() can 
    // get the label not enum constants
    @Override
    public String toString() {
        return label; 
    }
}