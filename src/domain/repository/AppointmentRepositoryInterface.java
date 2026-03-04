package domain.repository;

import domain.entity.Appointment;
import domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepositoryInterface 
{
    /**
     * Persists an appointment in the data store.
     * @param appointment The appointment entity to be saved.
     */
    void save(Appointment appointment);

    /**
     * Retrieves an appointment by its unique identifier.
     * @param id The ID of the appointment.
     * @return An Optional containing the appointment if found, or empty otherwise.
     */
    Optional<Appointment> findById(Long id);

    /**
     * Retrieves all appointments associated with a specific customer.
     * @param customerId The unique identifier of the customer.
     * @return A list of appointments belonging to the customer.
     */
    List<Appointment> findByCustomerId(Long customerId);

    /**
     * Retrieves all appointments within a specific time range.
     * Useful for daily/weekly schedules and period-based reports.
     * @param start The beginning of the period.
     * @param end The end of the period.
     * @return A list of appointments within the specified timeframe.
     */
    List<Appointment> findByPeriod(LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves all appointments with a specific status.
     * @param status The status to filter by (e.g., CANCELLED, FINISHED).
     * @return A list of appointments matching the given status.
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Retrieves appointments based on their lifecycle stage (Active vs. History).
     * * @param activeFilter 
     * true: Returns SCHEDULED and IN_PROGRESS (Occupies the current schedule).
     * false: Returns FINISHED and CANCELLED (Historical/Past records).
     * null: Returns all records without status filtering.
     * * @return A list of appointments sorted chronologically by date and time.
     */
    List<Appointment> findAll(Boolean activeFilter);
}