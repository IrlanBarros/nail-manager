package application.usecase.appointment;

import domain.entity.Appointment;
import domain.entity.Customer;
import domain.policy.DiscountPolicy;
import domain.repository.AppointmentRepositoryInterface;
import domain.repository.CustomerRepositoryInterface;
import java.math.BigDecimal;

public class FinishAppointmentUseCase 
{

    private final AppointmentRepositoryInterface appointmentRepository;
    private final CustomerRepositoryInterface customerRepository;
    private final DiscountPolicy discountPolicy;

    public FinishAppointmentUseCase
    (
        AppointmentRepositoryInterface appointmentRepository,
        CustomerRepositoryInterface customerRepository,
        DiscountPolicy discountPolicy
    ) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.discountPolicy = discountPolicy;
    }

    public BigDecimal execute(Long appointmentId) 
    {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found."));

        appointment.finish();

        Customer customer = appointment.getCustomer();
        
        if (!customer.isActive()) 
            throw new IllegalStateException("Is impossible to finalize service for an inactive customer.");

        customer.registerAppointment();

        BigDecimal finalAmount = appointment.calculateTotal(this.discountPolicy);

        appointmentRepository.save(appointment);
        customerRepository.save(customer); 

        return finalAmount;
    }
}