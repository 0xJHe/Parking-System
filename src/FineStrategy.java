public interface FineStrategy {
    double calculateFine(int overstayMinutes);
}

class FixedFine implements FineStrategy {

    @Override
    public double calculateFine(int overstayMinutes) {
        return 50.00; // RM 5 flat fine
    }
}

class ProgressiveFine implements FineStrategy {

    @Override
    public double calculateFine(int overstayMinutes) {
        int overstayHours = (int)Math.ceil(overstayMinutes / 60);
        if (overstayHours <= 24) {
            return 50.00;
        } else if (overstayHours <= 48) {
            return 150.00;
        } else if (overstayHours <= 72) {
            return 300.00;
        } else {
            return 500.00;
        }
    }
}

class HourlyFine implements FineStrategy {

    @Override
    public double calculateFine(int overstayMinutes) {
        int hours = (int) Math.ceil(overstayMinutes / 60.0);
        return hours * 20.00; // RM 3 per hour
    }
}