package infrastructure.persistence.sqlite;

import infrastructure.persistence.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class SqliteConnectionFactory implements ConnectionFactory 
{
    
    @Override
    public Connection getConnection() throws SQLException 
    {
        return DatabaseConnection.getConnection();
    }
}