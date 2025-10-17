package edu.trincoll.service.latefee;

public class StudentLateFeeCalculator implements LateFeeCalculator {
    @Override
    public double calculateLateFee(long daysLate) {
        return daysLate * 0.25; // student = half fee
    }
}