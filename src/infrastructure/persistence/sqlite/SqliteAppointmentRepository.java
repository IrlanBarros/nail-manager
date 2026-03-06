package infrastructure.persistence.sqlite;

import domain.entity.Appointment;
import domain.entity.Customer;
import domain.entity.Service;
import domain.enums.AppointmentStatus;
import domain.repository.AppointmentRepositoryInterface;
import domain.repository.CustomerRepositoryInterface;
import domain.repository.ServiceRepositoryInterface;
import infrastructure.persistence.ConnectionFactory;
import infrastructure.persistence.sqlite.mapper.AppointmentMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteAppointmentRepository implements AppointmentRepositoryInterface 
{
    private final ConnectionFactory connectionFactory;
    private final CustomerRepositoryInterface customerRepository;
    private final ServiceRepositoryInterface serviceRepository;

    public SqliteAppointmentRepository(
        ConnectionFactory connectionFactory,
        CustomerRepositoryInterface customerRepository,
        ServiceRepositoryInterface serviceRepository
    ) {
        this.connectionFactory = connectionFactory;
        this.customerRepository = customerRepository;
        this.serviceRepository = serviceRepository;
    }

    @Override
    public void save(Appointment appointment) 
    {
        boolean isInsert = (appointment.getId() == null);
        
        String sqlAppointment = isInsert 
            ? "INSERT INTO appointments (customer_id, date_time, status, description, total_price) VALUES (?, ?, ?, ?, ?)"
            : "UPDATE appointments SET customer_id = ?, date_time = ?, status = ?, description = ?, total_price = ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = connectionFactory.getConnection();
            conn.setAutoCommit(false);

            // 1. Save the main data from the Schedule
            try (PreparedStatement pstmt = isInsert 
                    ? conn.prepareStatement(sqlAppointment, Statement.RETURN_GENERATED_KEYS)
                    : conn.prepareStatement(sqlAppointment)) 
            {
                pstmt.setLong(1, appointment.getCustomer().getId());
                pstmt.setString(2, appointment.getDateTime().toString()); 
                pstmt.setString(3, appointment.getStatus().name()); 
                pstmt.setString(4, appointment.getDescription().toString()); 
                pstmt.setBigDecimal(5, appointment.getTotalPrice());

                if (!isInsert) 
                {
                    pstmt.setLong(6, appointment.getId());
                }

                pstmt.executeUpdate();

                if (isInsert) 
                {
                    try (ResultSet keys = pstmt.getGeneratedKeys()) 
                    {
                        AppointmentMapper.injectGeneratedId(appointment, keys);
                    }
                }
            }

            // 2. Clears old services in the associative table if it is an UPDATE
            if (!isInsert) 
            {
                String sqlDeleteServices = "DELETE FROM appointment_services WHERE appointment_id = ?";
                try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteServices)) 
                {
                    pstmtDelete.setLong(1, appointment.getId());
                    pstmtDelete.executeUpdate();
                }
            }

            // 3. Inserts the updated list of services into the associative table (N:N)
            String sqlInsertService = "INSERT INTO appointment_services (appointment_id, service_id) VALUES (?, ?)";
            try (PreparedStatement pstmtServices = conn.prepareStatement(sqlInsertService)) 
            {
                for (Service service : appointment.getServices()) 
                {
                    pstmtServices.setLong(1, appointment.getId());
                    pstmtServices.setLong(2, service.getId());
                    pstmtServices.addBatch(); 
                }
                pstmtServices.executeBatch();
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* Ignore rollback failure */ }
            }
            throw new RuntimeException("Error saving schedule: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) { System.err.println("Error closing connection."); }
            }
        }
    }

    @Override
    public Optional<Appointment> findById(Long id) 
    {
        String sql = "SELECT * FROM appointments WHERE id = ?";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    // 1. Find out who the customer is and search for the injected repository
                    Long customerId = rs.getLong("customer_id");
                    Customer customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found for Scheduling: " + id));

                    List<Service> services = findServicesByAppointmentId(id);

                    return Optional.of(AppointmentMapper.toDomain(rs, customer, services));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error when searching for appointment by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findByCustomerId(Long customerId) 
    {
        String sql = "SELECT * FROM appointments WHERE customer_id = ? ORDER BY date_time DESC";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, customerId);
            return executeAndMapAppointments(pstmt);
        } catch (SQLException e) {
            System.err.println("Error retrieving customer appointments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Appointment> findByPeriod(LocalDateTime start, LocalDateTime end) 
    {
        // SQLite can compare dates perfectly if they are in ISO text format. (ex: 2026-03-04T10:00:00)
        String sql = "SELECT * FROM appointments WHERE date_time >= ? AND date_time <= ? ORDER BY date_time ASC";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            return executeAndMapAppointments(pstmt);
        } catch (SQLException e) {
            System.err.println("Error when searching for appointments by period: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) 
    {
        String sql = "SELECT * FROM appointments WHERE status = ? ORDER BY date_time DESC";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, status.name());
            return executeAndMapAppointments(pstmt);
        } catch (SQLException e) {
            System.err.println("Error when searching for appointments by status: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Appointment> findAll(Boolean activeFilter) 
    {
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM appointments");

        // Applies the dynamic agenda lifecycle filter
        if (activeFilter != null) 
        {
            if (activeFilter) 
            {
                // Active appointments (occupy the schedule)
                sqlBuilder.append(" WHERE status IN ('SCHEDULED', 'IN_PROGRESS')");
            } 
            else 
            {
                // Past or canceled history
                sqlBuilder.append(" WHERE status IN ('FINISHED', 'CANCELLED')");
            }
        }

        sqlBuilder.append(" ORDER BY date_time ASC");

        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())
        ) {
            return executeAndMapAppointments(pstmt);
        } catch (SQLException e) {
            System.err.println("Error listing appointments: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Executes the prepared query and orchestrates the reassembly of the Schedule list,
     * searching for dependencies (Client and Services) for each line found.
     */
    private List<Appointment> executeAndMapAppointments(PreparedStatement pstmt) throws SQLException 
    {
        List<Appointment> appointments = new ArrayList<>();
        
        try (ResultSet rs = pstmt.executeQuery()) 
        {
            while (rs.next()) 
            {
                Long appointmentId = rs.getLong("id");
                Long customerId = rs.getLong("customer_id");
                
                Customer customer = customerRepository.findById(customerId).orElse(null);
                
                List<Service> services = findServicesByAppointmentId(appointmentId);

                if (customer != null) 
                {
                    appointments.add(AppointmentMapper.toDomain(rs, customer, services));
                }
            }
        }
        return appointments;
    }

    /**
     * Searches the associative table for all service IDs linked to this schedule,
     * and then use ServiceRepository to load the complete entities.
     */
    private List<Service> findServicesByAppointmentId(Long appointmentId) 
    {
        List<Service> services = new ArrayList<>();
        String sql = "SELECT service_id FROM appointment_services WHERE appointment_id = ?";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                while (rs.next()) 
                {
                    Long serviceId = rs.getLong("service_id");
                    serviceRepository.findById(serviceId).ifPresent(services::add);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving scheduling services: " + e.getMessage());
        }
        return services;
    }
}