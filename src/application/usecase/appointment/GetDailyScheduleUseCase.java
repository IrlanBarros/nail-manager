package application.usecase.appointment;

import domain.entity.Appointment;
import domain.repository.AppointmentRepositoryInterface;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class GetDailyScheduleUseCase 
{
    private final AppointmentRepositoryInterface appointmentRepository;

    public GetDailyScheduleUseCase(AppointmentRepositoryInterface appointmentRepository) 
    {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Returns all appointments for a specific day.
     * @param date The desired date (e.g., LocalDate.now() for today's schedule).
     */
    public List<Appointment> execute(LocalDate date) 
    {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return appointmentRepository.findByPeriod(startOfDay, endOfDay);
    }
}