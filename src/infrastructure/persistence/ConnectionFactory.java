package infrastructure.persistence;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract contract for providing connections to the database.
 * (solve the dip - Dependency Inversion Principle)
 */
public interface ConnectionFactory 
{
    Connection getConnection() throws SQLException;
}