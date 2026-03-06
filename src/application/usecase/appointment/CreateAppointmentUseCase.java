package application.usecase.appointment;

import domain.entity.Appointment;
import domain.entity.Customer;
import domain.repository.AppointmentRepositoryInterface;
import domain.repository.CustomerRepositoryInterface;
import domain.valueobject.Description;
import java.time.LocalDateTime;

public class CreateAppointmentUseCase 
{

    private final AppointmentRepositoryInterface appointmentRepository;
    private final CustomerRepositoryInterface customerRepository;

    public CreateAppointmentUseCase
    (
        AppointmentRepositoryInterface appointmentRepo, 
        CustomerRepositoryInterface customerRepo
    ) {
        this.appointmentRepository = appointmentRepo;
        this.customerRepository = customerRepo;
    }

    public Appointment execute
    (
        Long customerId, LocalDateTime dateTime, 
        String descriptionText
    ) {
        if (dateTime.isBefore(LocalDateTime.now())) 
            throw new IllegalArgumentException("Is impossible to schedule an appointment for a past date.");

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Costumer not found."));

        if (!customer.isActive()) 
            throw new IllegalStateException("Is impossible to schedule services for an inactive customer.");

        Description description = (descriptionText != null) ? new Description(descriptionText) : null;

        Appointment appointment = new Appointment(customer, dateTime, description);

        appointmentRepository.save(appointment);

        return appointment;
    }
}