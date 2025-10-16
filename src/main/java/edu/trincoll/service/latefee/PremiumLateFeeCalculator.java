package edu.trincoll.service.latefee;

public class PremiumLateFeeCalculator implements LateFeeCalculator {
    @Override
    public double calculateLateFee(long daysLate) {
        return 0.0;  // premium = no fee
    }
}
