package infrastructure.database;

import domain.service.PasswordHasher;
import infrastructure.persistence.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseSeeder 
{

    private final ConnectionFactory connectionFactory;
    private final PasswordHasher passwordHasher;

    public DatabaseSeeder(ConnectionFactory connectionFactory, PasswordHasher passwordHasher) 
    {
        this.connectionFactory = connectionFactory;
        this.passwordHasher = passwordHasher;
    }

    public void seed() 
    {
        try (Connection conn = connectionFactory.getConnection()) 
        {
            seedUser(conn);
            seedClients(conn);
            seedServices(conn);
            seedAppointments(conn);
            seedTransactions(conn);
            System.out.println("🌱 Database successfully populated (Users, Customers, Services, Appointments and Transactions)!");
        } catch (SQLException e) {
            System.err.println("Error feeding the database: " + e.getMessage());
        }
    }

    private void seedUser(Connection conn) throws SQLException 
    {
        // Check if there is already a registered user
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) AS total FROM users")
        ) {
            if (rs.getInt("total") > 0) return; 
        }

        // If it does not exist, insert the administrator user.
        String sql = "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, "Administrador");
            pstmt.setString(2, "admin@manager.com");

            // Encrypt the password “admin123.”
            pstmt.setString(3, passwordHasher.hash("admin123")); 
            pstmt.executeUpdate();
            System.out.println("✅ User admin@manager.com created.");
        }
    }

    private void seedClients(Connection conn) throws SQLException 
    {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) AS total FROM customers")
        ) {
            if (rs.getInt("total") > 0) return;
        }

        // If the table is empty, insert the initial 10 customers
        String sql = "INSERT INTO customers (name, phone, email) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            String[] nomes = {"Ana Silva", "Beatriz Costa", "Carla Souza", "Daniela Lima", 
                              "Fernanda Alves", "Gabriela Rocha", "Helena Dias", 
                              "Isabela Ribeiro", "Juliana Carvalho", "Larissa Mendes"};

            for (int i = 0; i < nomes.length; i++) 
            {
                pstmt.setString(1, nomes[i]);
                pstmt.setString(2, "(11) 99999-00" + String.format("%02d", i));
                pstmt.setString(3, nomes[i].split(" ")[0].toLowerCase() + "@email.com");
                pstmt.addBatch();
            }
            
            pstmt.executeBatch(); // Fire all 10 inserts to SQLite at once
            System.out.println("✅ 10 initial customers added.");
        }
    }

    private void seedServices(Connection conn) throws SQLException 
    {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) AS total FROM services")
        ) {
            if (rs.getInt("total") > 0) return;
        }

        // Columns: name, description, price
        String sql = "INSERT INTO services (name, description, price) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            Object[][] servicos = {
                {
                    "Manicure Clássica", 
                    "Cuidado essencial para as mãos: modelagem, cutícula, esfoliação leve e esmaltação com brilho duradouro.", 
                    35.00
                },
                {
                    "Pedicure Spa", 
                    "Tratamento relaxante: escalda-pés com sais, esfoliação profunda, máscara hidratante e massagem.", 
                    45.00
                },
                {
                    "Alongamento em Gel", 
                    "Técnica avançada de reconstrução e extensão com gel fotopolimerizável. Aspecto natural e alta resistência.", 
                    120.00
                }
            };

            for (Object[] servico : servicos) 
            {
                pstmt.setString(1, (String) servico[0]);
                pstmt.setString(2, (String) servico[1]);
                pstmt.setDouble(3, (Double) servico[2]);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            System.out.println("✅ 3 services registered with descriptions.");
        }
    }

    private void seedAppointments(Connection conn) throws SQLException 
    {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) AS total FROM appointments")
        ) {
            if (rs.getInt("total") > 0) return;
        }

        // 1. SQL for the main table (Note that we use customer_id and total_price according to your schema)
        String sqlApp = "INSERT INTO appointments (customer_id, date_time, status, total_price) VALUES (?, ?, ?, ?)";

        // 2. SQL for the associative table
        String sqlPivot = "INSERT INTO appointment_services (appointment_id, service_id) VALUES (?, ?)";

        try (
            PreparedStatement pstmtApp = conn.prepareStatement(sqlApp, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement pstmtPivot = conn.prepareStatement(sqlPivot)
        ) {
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime agora = LocalDateTime.now();

            // Data: {customer_id, data, status, total_price, service_id}
            Object[][] dados = {
                {1, agora.plusDays(1).withHour(10).format(formatter), "SCHEDULED", 45.0, 1},
                {2, agora.plusDays(1).withHour(14).format(formatter), "SCHEDULED", 120.0, 3},
                {3, agora.plusDays(2).withHour(9).format(formatter), "SCHEDULED", 60.0, 2},
                {4, agora.plusDays(2).withHour(16).format(formatter), "SCHEDULED", 45.0, 1},
                {5, agora.plusDays(3).withHour(11).format(formatter), "SCHEDULED", 120.0, 3}
            };

            for (Object[] d : dados) {
                // Insert the Appointment
                pstmtApp.setInt(1, (Integer) d[0]);
                pstmtApp.setString(2, (String) d[1]);
                pstmtApp.setString(3, (String) d[2]);
                pstmtApp.setDouble(4, (Double) d[3]);
                pstmtApp.executeUpdate();

                // Get the ID that SQLite just generated for this schedule
                try (ResultSet generatedKeys = pstmtApp.getGeneratedKeys()) 
                {
                    if (generatedKeys.next()) 
                    {
                        int newAppId = generatedKeys.getInt(1);
                        
                        // Insert into the associative table linking to the service
                        pstmtPivot.setInt(1, newAppId);
                        pstmtPivot.setInt(2, (Integer) d[4]);
                        pstmtPivot.executeUpdate();
                    }
                }
            }
            System.out.println("✅ Five appointments and their service links have been created.");
        }
    }

    private void seedTransactions(Connection conn) throws SQLException 
    {
        try (
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT count(*) AS total FROM transactions")
        ) {
            if (rs.getInt("total") > 0) return;
        }

        // Columns: type, amount, description, appointment_id, date
        String sql = "INSERT INTO transactions (type, amount, description, appointment_id, date) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String agora = LocalDateTime.now().format(formatter);

            // Data: {type, amount, description, appointment_id, date}
            Object[][] transacoes = {
                {"INCOME", 45.00, "Pagamento - Manicure Clássica", 1, agora},
                {"INCOME", 120.00, "Pagamento - Alongamento em Gel", 2, agora},
                {"EXPENSE", 50.00, "Compra de material (Esmaltes)", null, agora}, // Transação avulsa/saída
                {"INCOME", 60.00, "Pagamento - Pedicure Spa", 3, agora},
                {"EXPENSE", 200.00, "Aluguel da sala", null, agora}             // Transação avulsa/saída
            };

            for (Object[] tr : transacoes) {
                pstmt.setString(1, (String) tr[0]);
                pstmt.setDouble(2, (Double) tr[1]);
                pstmt.setString(3, (String) tr[2]);
                
                if (tr[3] == null) {
                    pstmt.setNull(4, java.sql.Types.INTEGER);
                } else {
                    pstmt.setInt(4, (Integer) tr[3]);
                }
                
                pstmt.setString(5, (String) tr[4]);
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            System.out.println("✅ 5 transactions (incoming and outgoing) recorded.");
        }
    }
}