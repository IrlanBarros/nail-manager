package application.usecase.dashboard;

import application.dto.DashboardSummaryDTO;
import domain.entity.Appointment;
import domain.entity.Transaction;
import domain.enums.AppointmentStatus;
import domain.enums.TransactionType;
import domain.repository.AppointmentRepositoryInterface;
import domain.repository.TransactionRepositoryInterface;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class GetDashboardSummaryUseCase 
{
    private final AppointmentRepositoryInterface appointmentRepository;
    private final TransactionRepositoryInterface transactionRepository;

    public GetDashboardSummaryUseCase(
        AppointmentRepositoryInterface appointmentRepository,
        TransactionRepositoryInterface transactionRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Generates quick metrics for the main screen (Dashboard) for the current day.
     */
    public DashboardSummaryDTO execute(LocalDate date) 
    {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 1. Search for today's appointments
        List<Appointment> dailyAppointments = appointmentRepository.findByPeriod(startOfDay, endOfDay);
        
        int pending = 0;
        int finished = 0;
        int cancelled = 0;

        for (Appointment appt : dailyAppointments) 
        {
            if (appt.getStatus() == AppointmentStatus.SCHEDULED || appt.getStatus() == AppointmentStatus.IN_PROGRESS) {
                pending++;
            } else if (appt.getStatus() == AppointmentStatus.FINISHED) {
                finished++;
            } else if (appt.getStatus() == AppointmentStatus.CANCELLED) {
                cancelled++;
            }
        }

        // 2. Search for the day's revenue (income)
        List<Transaction> dailyIncomes = transactionRepository.findByTypeAndPeriod(
            TransactionType.INCOME, startOfDay, endOfDay
        );

        BigDecimal revenue = BigDecimal.ZERO;
        for (Transaction t : dailyIncomes) 
        {
            if (t.isActive()) 
            {
                revenue = revenue.add(t.getAmount());
            }
        }

        return new DashboardSummaryDTO(pending, finished, cancelled, revenue);
    }
}