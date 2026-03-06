package infrastructure.persistence.sqlite;

import domain.entity.User;
import domain.repository.UserRepositoryInterface;
import domain.valueobject.Email;
import domain.valueobject.Phone;
import infrastructure.persistence.ConnectionFactory;
import infrastructure.persistence.sqlite.mapper.UserMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteUserRepository implements UserRepositoryInterface 
{
    private final ConnectionFactory connectionFactory;

    public SqliteUserRepository(ConnectionFactory connectionFactory) 
    {
        this.connectionFactory = connectionFactory;
    }

    private Connection getConnection() throws SQLException 
    {
        return connectionFactory.getConnection();
    }

    @Override
    public void save(User user) 
    {
        boolean isInsert = (user.getId() == null);
        
        String sql = isInsert 
            ? "INSERT INTO users (name, phone, email, password_hash) VALUES (?, ?, ?, ?)"
            : "UPDATE users SET name = ?, phone = ?, email = ?, password_hash = ? WHERE id = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = isInsert 
                ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, user.getName().getValue());
            
            if (user.getPhone().isPresent()) 
            {
                pstmt.setString(2, user.getPhone().get().getValue());
            } 
            else 
            {
                pstmt.setNull(2, Types.VARCHAR);
            }

            pstmt.setString(3, user.getEmail().getValue());
            pstmt.setString(4, user.getPasswordHash()); 

            if (!isInsert) 
            {
                pstmt.setLong(5, user.getId());
            }

            pstmt.executeUpdate();

            if (isInsert) 
            {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) 
                {
                    UserMapper.injectGeneratedId(user, generatedKeys);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user to database: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) 
    {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) 
            {
                if (rs.next()) 
                {
                    return Optional.of(UserMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error when searching for user by ID: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(Email email) 
    {
        String sql = "SELECT * FROM users WHERE email = ?";
        return findUserByUniqueString(sql, email.getValue());
    }

    @Override
    public Optional<User> findByPhone(Phone phone) 
    {
        String sql = "SELECT * FROM users WHERE phone = ?";
        return findUserByUniqueString(sql, phone.getValue());
    }

    @Override
    public List<User> findAll() 
    {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY name ASC";

        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) 
            {
                users.add(UserMapper.toDomain(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
        
        return users;
    }

    private Optional<User> findUserByUniqueString(String sql, String value) 
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
                    return Optional.of(UserMapper.toDomain(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro na busca de usuário: " + e.getMessage());
        }

        return Optional.empty();
    }
}