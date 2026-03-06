package application.dto;

import java.math.BigDecimal;

/**
 * Pure DTO to transport data from the home screen without exposing entities.
 */
public class DashboardSummaryDTO 
{
    public final int pendingAppointments;
    public final int finishedAppointments;
    public final int cancelledAppointments;
    public final BigDecimal dailyRevenue;

    public DashboardSummaryDTO(int pending, int finished, int cancelled, BigDecimal revenue) 
    {
        this.pendingAppointments = pending;
        this.finishedAppointments = finished;
        this.cancelledAppointments = cancelled;
        this.dailyRevenue = revenue;
    }
}