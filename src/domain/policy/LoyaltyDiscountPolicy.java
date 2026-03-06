package domain.policy;

import domain.entity.Customer;
import java.math.BigDecimal;

public class LoyaltyDiscountPolicy implements DiscountPolicy 
{
    private static final int MINIMUM_APPOINTMENTS = 5;
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");

    @Override
    public BigDecimal calculateDiscount(Customer customer, BigDecimal totalAmount) 
    {
        if (customer == null || totalAmount == null) 
            throw new IllegalArgumentException("Customer and total amount cannot be null.");

        if (customer.getTotalAppointments() >= MINIMUM_APPOINTMENTS) 
            return totalAmount.multiply(DISCOUNT_RATE);

        return BigDecimal.ZERO;
    }
}
