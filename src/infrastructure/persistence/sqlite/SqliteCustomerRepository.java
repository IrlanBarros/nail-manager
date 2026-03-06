package infrastructure.persistence.sqlite;

import domain.entity.Customer;
import domain.repository.CustomerRepositoryInterface;
import domain.valueobject.Email;
import domain.valueobject.Phone;
import infrastructure.persistence.ConnectionFactory;
import infrastructure.persistence.sqlite.mapper.CustomerMapper; 

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteCustomerRepository implements CustomerRepositoryInterface 
{
    private final ConnectionFactory connectionFactory;

    public SqliteCustomerRepository(ConnectionFactory connectionFactory) 
    {
        this.connectionFactory = connectionFactory;
    }

    private Connection getConnection() throws SQLException 
    {
        return connectionFactory.getConnection();
    }

    @Override
    public void save(Customer customer) 
    {
        boolean isInsert = (customer.getId() == null);
        
        String sql = isInsert 
            ? "INSERT INTO customers (name, phone, email, active) VALUES (?, ?, ?, ?)"
            : "UPDATE customers SET name = ?, phone = ?, email = ?, active = ? WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = isInsert 
                ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : conn.prepareStatement(sql)
        ) {

            pstmt.setString(1, customer.getName().getValue()); 
            pstmt.setString(2, customer.getPhone().getValue());
            pstmt.setString(3, customer.getEmail().getValue());
            pstmt.setInt(4, customer.isActive() ? 1 : 0);

            if (!isInsert) 
            {
                pstmt.setLong(5, customer.getId());
            }

            pstmt.executeUpdate();

            if (isInsert) 
            {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) 
                {
                    CustomerMapper.injectGeneratedId(customer, generatedKeys);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving customer to database: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Customer> findByEmail(Email email) 
    {
        String sql = "SELECT * FROM customers WHERE email = ?";
        return findCustomerByUniqueString(sql, email.getValue()); 
    }

    @Override
    public Optional<Customer> findByPhone(Phone phone) 
    {
        String sql = "SELECT * FROM customers WHERE phone = ?";
        return findCustomerByUniqueString(sql, phone.getValue()); 
    }

    @Override
    public Optional<Customer> findById(Long id) 
    {
        String sql = "SELECT * FROM customers WHERE id = ?";
        
        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return Optional.of(CustomerMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error when searching for customer by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<Customer> findAll(Boolean activeFilter) 
    {
        List<Customer> customers = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM customers");
        
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
                    customers.add(CustomerMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error listing clients in the database: " + e.getMessage());
        }
        
        return customers;
    }

    private Optional<Customer> findCustomerByUniqueString(String sql, String value) 
    {
        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, value);
            
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return Optional.of(CustomerMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in customer search: " + e.getMessage());
        }

        return Optional.empty();
    }
}