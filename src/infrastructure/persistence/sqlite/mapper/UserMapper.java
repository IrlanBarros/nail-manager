package infrastructure.persistence.sqlite.mapper;

import domain.entity.User;
import domain.valueobject.Email;
import domain.valueobject.Name;
import domain.valueobject.Phone;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Field;

public class UserMapper 
{

    public static User toDomain(ResultSet rs) throws SQLException 
    {
        Long id = rs.getLong("id");
        
        Name name = new Name(rs.getString("name"));
        Email email = new Email(rs.getString("email"));
        
        String phoneString = rs.getString("phone");
        Phone phone = null;
        if (phoneString != null && !phoneString.trim().isEmpty()) 
        {
            phone = new Phone(phoneString);
        }

        String passwordHash = rs.getString("password_hash");

        return new User(id, name, phone, email, passwordHash);
    }

    public static void injectGeneratedId(User user, ResultSet generatedKeys) throws SQLException 
    {
        if (generatedKeys.next()) 
        {
            long generatedId = generatedKeys.getLong(1);
            try {
                Field idField = user.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, generatedId);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Reflection error when setting the ID generated in User: " + e.getMessage());
            }
        }
    }
}