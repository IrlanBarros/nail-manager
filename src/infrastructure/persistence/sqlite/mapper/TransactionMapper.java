package infrastructure.persistence.sqlite.mapper;

import domain.entity.Transaction;
import domain.valueobject.Description;
import domain.enums.TransactionType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.lang.reflect.Field;

public class TransactionMapper 
{
    public static Transaction toDomain(ResultSet rs) throws SQLException 
    {
        Long id = rs.getLong("id");
        
        String typeString = rs.getString("type");
        TransactionType type = TransactionType.valueOf(typeString);
        
        BigDecimal amount = rs.getBigDecimal("amount");
        Description description = new Description(rs.getString("description"));
        
        Long appointmentId = (Long) rs.getObject("appointment_id");
        
        LocalDateTime date = LocalDateTime.parse(rs.getString("date"));
        boolean active = rs.getInt("active") == 1;
        
        String canceledAtString = rs.getString("canceled_at");
        LocalDateTime canceledAt = null;
        if (canceledAtString != null && !canceledAtString.trim().isEmpty()) 
        {
            canceledAt = LocalDateTime.parse(canceledAtString);
        }

        return new Transaction(id, type, amount, description, appointmentId, date, active, canceledAt);
    }

    public static void injectGeneratedId(Transaction transaction, ResultSet generatedKeys) throws SQLException 
    {
        if (generatedKeys.next()) 
        {
            long generatedId = generatedKeys.getLong(1);
            try {
                Field idField = transaction.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(transaction, generatedId);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Reflection error when setting the ID generated in the transaction: " + e.getMessage());
            }
        }
    }
}