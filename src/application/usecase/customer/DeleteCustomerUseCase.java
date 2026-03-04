package application.usecase.customer;

import domain.entity.Customer;
import domain.enums.AppointmentStatus;
import domain.repository.CustomerRepositoryInterface;
import domain.repository.AppointmentRepositoryInterface;

public class DeleteCustomerUseCase 
{

    private final CustomerRepositoryInterface customerRepository;
    private final AppointmentRepositoryInterface appointmentRepository;

    public DeleteCustomerUseCase
    (
        CustomerRepositoryInterface customerRepository,
        AppointmentRepositoryInterface appointmentRepository
    ) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public void execute(Long id) 
    {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        boolean hasPendingAppointments = appointmentRepository.findByCustomerId(id).stream()
                .anyMatch(app -> app.getStatus() == AppointmentStatus.SCHEDULED 
                              || app.getStatus() == AppointmentStatus.IN_PROGRESS);

        if (hasPendingAppointments) 
        {
            throw new IllegalStateException("Is impossible to deactivate a customer who has pending or ongoing appointments.");
        }

        customer.deactivate();

        customerRepository.save(customer);
    }
}
