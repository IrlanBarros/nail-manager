package infrastructure.persistence.sqlite;

import domain.entity.Transaction;
import domain.repository.TransactionRepositoryInterface;
import domain.enums.TransactionType;
import infrastructure.persistence.ConnectionFactory;
import infrastructure.persistence.sqlite.mapper.TransactionMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteTransactionRepository implements TransactionRepositoryInterface 
{
    private final ConnectionFactory connectionFactory;

    public SqliteTransactionRepository(ConnectionFactory connectionFactory) 
    {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void save(Transaction transaction) 
    {
        boolean isInsert = (transaction.getId() == null);
        
        String sql = isInsert 
            ? "INSERT INTO transactions (type, amount, description, appointment_id, date, active, canceled_at) VALUES (?, ?, ?, ?, ?, ?, ?)"
            : "UPDATE transactions SET type = ?, amount = ?, description = ?, appointment_id = ?, date = ?, active = ?, canceled_at = ? WHERE id = ?";

        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = isInsert 
                ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, transaction.getType().name());
            pstmt.setBigDecimal(2, transaction.getAmount());
            pstmt.setString(3, transaction.getDescription().getValue());
            
            if (transaction.getAppointmentId().isPresent()) 
            {
                pstmt.setLong(4, transaction.getAppointmentId().get());
            } 
            else 
            {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.setString(5, transaction.getDate().toString());
            pstmt.setInt(6, transaction.isActive() ? 1 : 0);
            
            if (transaction.getCanceledAt() != null) 
            {
                pstmt.setString(7, transaction.getCanceledAt().toString());
            } 
            else 
            {
                pstmt.setNull(7, Types.VARCHAR);
            }

            if (!isInsert) 
            {
                pstmt.setLong(8, transaction.getId());
            }

            pstmt.executeUpdate();

            if (isInsert) 
            {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) 
                {
                    TransactionMapper.injectGeneratedId(transaction, generatedKeys);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving transaction to database: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) 
    {
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return Optional.of(TransactionMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transaction by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<Transaction> findByPeriod(LocalDateTime start, LocalDateTime end) 
    {
        String sql = "SELECT * FROM transactions WHERE date >= ? AND date <= ? ORDER BY date ASC";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, start.toString());
            pstmt.setString(2, end.toString());
            return executeAndMapTransactions(pstmt);
        } catch (SQLException e) {
            System.err.println("Error when searching for transactions by period: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Transaction> findByAppointmentId(Long appointmentId) 
    {
        String sql = "SELECT * FROM transactions WHERE appointment_id = ? ORDER BY date DESC";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, appointmentId);
            return executeAndMapTransactions(pstmt);
        } catch (SQLException e) {
            System.err.println("Error retrieving scheduled transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Transaction> findByTypeAndPeriod(TransactionType type, LocalDateTime start, LocalDateTime end) 
    {
        String sql = "SELECT * FROM transactions WHERE type = ? AND date >= ? AND date <= ? ORDER BY date ASC";
        
        try (
            Connection conn = connectionFactory.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, type.name());
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            return executeAndMapTransactions(pstmt);
        } catch (SQLException e) {
            System.err.println("Error retrieving filtered transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Executes the prepared query and orchestrates the reassembly of the Transactions list.
     */
    private List<Transaction> executeAndMapTransactions(PreparedStatement pstmt) throws SQLException 
    {
        List<Transaction> transactions = new ArrayList<>();
        
        try (ResultSet rs = pstmt.executeQuery()) 
        {
            while (rs.next()) 
            {
                transactions.add(TransactionMapper.toDomain(rs));
            }
        }
        return transactions;
    }
}