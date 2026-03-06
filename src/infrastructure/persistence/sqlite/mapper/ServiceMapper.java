package infrastructure.persistence.sqlite.mapper;

import domain.entity.Service;
import domain.valueobject.Description;
import domain.valueobject.Name;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Field;

public class ServiceMapper 
{
    public static Service toDomain(ResultSet rs) throws SQLException 
    {
        Long id = rs.getLong("id");
        
        Name name = new Name(rs.getString("name"));
        Description description = new Description(rs.getString("description"));
        
        BigDecimal price = rs.getBigDecimal("price");
        
        boolean isActive = rs.getInt("active") == 1;

        return new Service(id, name, description, price, isActive);
    }

    public static void injectGeneratedId(Service service, ResultSet generatedKeys) throws SQLException 
    {
        if (generatedKeys.next()) 
        {
            long generatedId = generatedKeys.getLong(1);
            
            try {
                Field idField = service.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(service, generatedId);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Reflection error when setting the generated ID: " + e.getMessage());
            }
        }
    }
}