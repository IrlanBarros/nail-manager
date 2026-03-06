package application.usecase.appointment;

import domain.entity.Appointment;
import domain.entity.Service;
import domain.repository.AppointmentRepositoryInterface;
import domain.repository.ServiceRepositoryInterface;
import domain.policy.DiscountPolicy;

public class AddServiceToAppointmentUseCase 
{
    private final AppointmentRepositoryInterface appointmentRepository;
    private final ServiceRepositoryInterface serviceRepository;

    public AddServiceToAppointmentUseCase(
        AppointmentRepositoryInterface appointmentRepository,
        ServiceRepositoryInterface serviceRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
    }

    public Appointment execute(Long appointmentId, Long serviceId, DiscountPolicy discountPolicy) 
    {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found."));

        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found."));

        appointment.addService(service);

        appointment.lockFinalPrice(discountPolicy); 

        appointmentRepository.save(appointment);

        return appointment;
    }
}