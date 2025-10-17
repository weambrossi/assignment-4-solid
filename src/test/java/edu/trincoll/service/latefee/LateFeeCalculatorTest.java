package edu.trincoll.service.latefee;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LateFeeCalculatorTest {

    @Test
    void testRegulatLateFee() {
        LateFeeCalculator calc = new RegularLateFeeCalculator();
        assertEquals(2.50, calc.calculateLateFee(5), 0.001);
    }

    @Test
    void testPremiumLateFee() {
        LateFeeCalculator calc = new PremiumLateFeeCalculator();
        assertEquals(0.0, calc.calculateLateFee(10), 0.001);
    }

    @Test
    void testStudentLateFee() {
        LateFeeCalculator calc = new StudentLateFeeCalculator();
        assertEquals(2.50, calc.calculateLateFee(10), 0.001);
    }
}