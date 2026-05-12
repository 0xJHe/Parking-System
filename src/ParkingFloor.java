import java.util.ArrayList;

public class ParkingFloor {
    ArrayList<ParkingSpot> spots = new ArrayList<>();

    public ParkingFloor(int currFloor, int rows, int spots) {
        for (int r = 1; r <= rows; r++) { // rows
            for (int s = 1; s <= spots; s++) { //spots
                String type;
                double rate;
                int split = (rows / 2) + 1;
                if (r == 1) {
                    if (s <= 3) {
                        type = "Handicapped";
                        rate = 2.0;
                    } else {
                        type = "VIP Reserved";
                        rate = 10.0;
                    }

                } else if (r <= split) {
                    type = "Compact";
                    rate = 2.0;
                } else {
                    type = "Regular";
                    rate = 5.0;
                }
                this.spots.add(new ParkingSpot(currFloor, r, s, type, rate));
            }
        }


    }
    public ArrayList<ParkingSpot> getSpotsList() { return spots; }


}