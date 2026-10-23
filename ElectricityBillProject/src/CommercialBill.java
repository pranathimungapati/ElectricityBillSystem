
import java.io.BufferedWriter;
import java.io.IOException;

public class CommercialBill extends ElectricityBill {
    private double connectedLoadKW;
    public static double demandChargePerKW = 120.0;

    public CommercialBill() { super(); }

    public CommercialBill(String consumerName, double unitsConsumed, double connectedLoadKW) throws InvalidUnitsException, InvalidLoadException {
        super(consumerName, unitsConsumed);
        setConnectedLoadKW(connectedLoadKW);
    }

    public void setConnectedLoadKW(double connectedLoadKW) throws InvalidLoadException {
        if (connectedLoadKW <= 0) {
            throw new InvalidLoadException("Invalid connected load: " + connectedLoadKW);
        }
        this.connectedLoadKW = connectedLoadKW;
    }

    @Override
    public double calculateBill() {
        double baseBill = super.calculateBill();
        this.totalBill = baseBill + (connectedLoadKW * demandChargePerKW);
        return this.totalBill;
    }

    public double calculatePenalty(int daysLate) {
        if (daysLate < 0) {
            throw new InvalidDaysLateException("Negative days late: " + daysLate);
        }
        calculateBill();
        return (daysLate == 0) ? 0.0 : (0.02 * totalBill * daysLate);
    }

    @Override
    public void displayDetails(BufferedWriter bw) throws IOException {
        super.displayDetails(bw);
        bw.write("Load KW: " + connectedLoadKW + ", Demand Charge: " + String.format("%.2f", (connectedLoadKW * demandChargePerKW)));
        bw.newLine();
    }
}
