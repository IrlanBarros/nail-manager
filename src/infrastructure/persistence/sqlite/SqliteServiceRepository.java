package infrastructure.persistence.sqlite;

import domain.entity.Service;
import domain.valueobject.Name;
import domain.repository.ServiceRepositoryInterface;
import infrastructure.persistence.ConnectionFactory;
import infrastructure.persistence.sqlite.mapper.ServiceMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteServiceRepository implements ServiceRepositoryInterface 
{
    private final ConnectionFactory connectionFactory;

    public SqliteServiceRepository(ConnectionFactory connectionFactory) 
    {
        this.connectionFactory = connectionFactory;
    }

    private Connection getConnection() throws SQLException 
    {
        return connectionFactory.getConnection();
    }

    @Override
    public void save(Service service) 
    {
        boolean isInsert = (service.getId() == null);
        
        String sql = isInsert 
            ? "INSERT INTO services (name, description, price, active) VALUES (?, ?, ?, ?)"
            : "UPDATE services SET name = ?, description = ?, price = ?, active = ? WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = isInsert 
                ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, service.getName().getValue()); 
            pstmt.setString(2, service.getDescription().getValue()); 
            pstmt.setBigDecimal(3, service.getPrice());
            pstmt.setInt(4, service.isActive() ? 1 : 0);

            if (!isInsert) 
            {
                pstmt.setLong(5, service.getId());
            }

            pstmt.executeUpdate();

            if (isInsert) 
            {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) 
                {
                    ServiceMapper.injectGeneratedId(service, generatedKeys);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving service to database: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Service> findById(Long id) 
    {
        String sql = "SELECT * FROM services WHERE id = ?";
        
        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return Optional.of(ServiceMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error when searching for service by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<Service> findByName(Name name) 
    {
        String sql = "SELECT * FROM services WHERE name = ?";
        
        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, name.getValue()); 
            
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return Optional.of(ServiceMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error when searching for service by name: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<Service> findAll(Boolean activeFilter) 
    {
        List<Service> services = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM services");
        
        if (activeFilter != null) 
        {
            sqlBuilder.append(" WHERE active = ?");
        }
        
        sqlBuilder.append(" ORDER BY name ASC");
        String sql = sqlBuilder.toString();

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            if (activeFilter != null) 
            {
                pstmt.setInt(1, activeFilter ? 1 : 0);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                while (rs.next()) 
                {
                    services.add(ServiceMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listing services in the database: " + e.getMessage());
        }
        
        return services;
    }
}