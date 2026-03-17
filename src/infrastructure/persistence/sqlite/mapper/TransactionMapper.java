
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
        // 1. ID da Transação
        Long id = rs.getLong("id");
        
        // 2. Tipo da Transação
        String typeString = rs.getString("type");
        TransactionType type = TransactionType.valueOf(typeString);
        
        // 3. Valores Financeiros e Descrição
        BigDecimal amount = rs.getBigDecimal("amount");
        Description description = new Description(rs.getString("description"));
        
        // 4. CORREÇÃO DO CAST: appointment_id (Integer vs Long no SQLite)
        Object appointmentObj = rs.getObject("appointment_id");
        Long appointmentId = (appointmentObj != null) ? ((Number) appointmentObj).longValue() : null;
        
        // 5. CORREÇÃO DA DATA: Substituindo espaço por 'T' para o LocalDateTime.parse
        String dateString = rs.getString("date");
        LocalDateTime date = null;
        if (dateString != null) {
            date = LocalDateTime.parse(dateString.replace(" ", "T"));
        }
        
        // 6. Status Ativo
        boolean active = rs.getInt("active") == 1;
        
        // 7. Data de Cancelamento (também com correção de formato)
        String canceledAtString = rs.getString("canceled_at");
        LocalDateTime canceledAt = null;
        if (canceledAtString != null && !canceledAtString.trim().isEmpty()) 
        {
            canceledAt = LocalDateTime.parse(canceledAtString.replace(" ", "T"));
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