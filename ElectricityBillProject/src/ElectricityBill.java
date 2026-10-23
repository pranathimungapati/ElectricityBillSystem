
import java.io.BufferedWriter;
import java.io.IOException;

public class ElectricityBill {
    private int consumerId;
    private String consumerName;
    private double unitsConsumed;
    protected double totalBill;

    public static String companyName = "TSSPDCL";
    public static double serviceCharge = 50.0;
    public static int consumerCount = 0;

    public ElectricityBill() {
        consumerCount++;
        this.consumerId = consumerCount;
    }

    public ElectricityBill(String consumerName, double unitsConsumed) throws InvalidUnitsException {
        this();
        this.consumerName = consumerName;
        setUnitsConsumed(unitsConsumed);
    }

    public String getConsumerName() { return consumerName; }
    public double getUnitsConsumed() { return unitsConsumed; }
    public double getTotalBill() { return totalBill; }

    public void setUnitsConsumed(double unitsConsumed) throws InvalidUnitsException {
        if (unitsConsumed < 0) {
            throw new InvalidUnitsException("Invalid units: " + unitsConsumed, unitsConsumed);
        }
        this.unitsConsumed = unitsConsumed;
    }

    public double calculateBill() {
        double rate = (unitsConsumed <= 100) ? 3.0 : (unitsConsumed <= 300 ? 5.0 : 6.5);
        this.totalBill = (rate * unitsConsumed) + serviceCharge;
        return this.totalBill;
    }

    public double calculateBill(boolean includeGST) {
        calculateBill();
        if (includeGST) {
            this.totalBill = this.totalBill + (0.05 * this.totalBill);
        }
        return this.totalBill;
    }

    public String compareBill(ElectricityBill e2) {
        return (this.totalBill > e2.totalBill) ? this.consumerName : e2.consumerName;
    }

    public void displayDetails(BufferedWriter bw) throws IOException {
        bw.write("ID: " + consumerId + ", Name: " + consumerName + ", Units: " + unitsConsumed + ", Bill: " + String.format("%.2f", totalBill));
        bw.newLine();
    }

    public static String findHighestBill(ElectricityBill[] bills) {
        double maxBill = -1;
        String topConsumer = "";
        for (ElectricityBill b : bills) {
            if (b != null) {
                b.calculateBill();
                if (b.getTotalBill() > maxBill) {
                    maxBill = b.getTotalBill();
                    topConsumer = b.getConsumerName();
                }
            }
        }
        return topConsumer;
    }
}
