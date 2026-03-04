package domain.policy;

import domain.entity.Customer;
import java.math.BigDecimal;

public interface DiscountPolicy 
{
    BigDecimal calculateDiscount(Customer customer, BigDecimal totalAmount);
}
