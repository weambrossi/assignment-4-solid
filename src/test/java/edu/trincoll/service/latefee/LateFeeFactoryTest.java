package edu.trincoll.service.latefee;

import edu.trincoll.model.MembershipType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LateFeeFactoryTest {
    private final LateFeeCalculatorFactory factory = new LateFeeCalculatorFactory();

    @Test
    void testRegularFactory() {
        assertTrue(factory.getCalculator(MembershipType.REGULAR) instanceof RegularLateFeeCalculator);
    }

    @Test
    void testPremiumFactory() {
        assertTrue(factory.getCalculator(MembershipType.PREMIUM) instanceof PremiumLateFeeCalculator);
    }

    @Test
    void testStudentFactory() {
        assertTrue(factory.getCalculator(MembershipType.STUDENT) instanceof StudentLateFeeCalculator);
    }
}