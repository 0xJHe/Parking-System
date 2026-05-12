import java.util.ArrayList;

public class ParkingLot {
    ArrayList<ParkingFloor> floors = new ArrayList<>();
    int rows, spots;

    public ParkingLot(int floors, int rows, int spots) {
        for (int f = 1; f <= floors; f++) {
            this.floors.add(new ParkingFloor(f, rows, spots));
        }
        this.rows = rows;
        this.spots = spots;
    }

    public ArrayList<ParkingFloor> getParkingLot() {
        return floors;
    }

    public int getTotalFloors() { return floors.size(); }
    public int getRows() { return rows; }
    public int getSpots() { return spots; }
    public ArrayList<ParkingFloor> getFloorsList() { return floors; }
}