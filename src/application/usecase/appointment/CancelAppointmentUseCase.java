package application.usecase.appointment;

import domain.entity.Appointment;
import domain.repository.AppointmentRepositoryInterface;

public class CancelAppointmentUseCase 
{

    private final AppointmentRepositoryInterface appointmentRepository;

    public CancelAppointmentUseCase(AppointmentRepositoryInterface appointmentRepository) 
    {
        this.appointmentRepository = appointmentRepository;
    }

    public void execute(Long appointmentId) 
    {
        if (appointmentId == null) 
        {
            throw new IllegalArgumentException("The appointment ID is mandatory.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found."));

        appointment.cancel();

        appointmentRepository.save(appointment);
    }
}