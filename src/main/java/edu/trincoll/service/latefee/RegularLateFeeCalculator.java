package edu.trincoll.service.latefee;

public class RegularLateFeeCalculator implements LateFeeCalculator {
    @Override
    public double calculateLateFee(long daysLate) {
        return daysLate * 0.50;
    }
}