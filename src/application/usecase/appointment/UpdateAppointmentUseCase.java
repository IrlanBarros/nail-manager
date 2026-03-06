package application.usecase.appointment;

import domain.entity.Appointment;
import domain.entity.Service;
import domain.repository.AppointmentRepositoryInterface;
import domain.repository.ServiceRepositoryInterface;
import domain.valueobject.Description;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UpdateAppointmentUseCase 
{

    private final AppointmentRepositoryInterface appointmentRepository;
    private final ServiceRepositoryInterface serviceRepository;

    public UpdateAppointmentUseCase
    (
        AppointmentRepositoryInterface appointmentRepository, 
        ServiceRepositoryInterface serviceRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
    }

    public void execute
    (
        Long appointmentId, 
        LocalDateTime newDateTime, 
        List<Long> newServiceIds,
        String newDescriptionText 
    ) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found."));

        if (newDateTime != null) 
        {
            appointment.reschedule(newDateTime);
        }

        if (newDescriptionText != null) 
        {
            Description description = newDescriptionText.isBlank() ? null : new Description(newDescriptionText);
            appointment.updateDescription(description);
        }

        // Updates Services (Ensuring only active ones enter)
        if (newServiceIds != null && !newServiceIds.isEmpty()) 
        {
            List<Service> services = new ArrayList<>();
            
            for (Long sId : newServiceIds) 
            {
                Service service = serviceRepository.findById(sId)
                        .orElseThrow(() -> new IllegalArgumentException("Service not fount: ID - " + sId));
                
                if (!service.isActive()) 
                    throw new IllegalStateException("The service '" + service.getName() + "' is inactive.");
                
                services.add(service);
            }
            
            appointment.updateServices(services);
        }

        appointmentRepository.save(appointment);
    }
}