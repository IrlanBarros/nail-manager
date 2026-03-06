package infrastructure.persistence.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection 
{
    private static final String FOLDER_PATH = "data";
    private static final String URL = "jdbc:sqlite:" + FOLDER_PATH + "/nail-manager.db";

    public static Connection getConnection() throws SQLException 
    {
        File directory = new File(FOLDER_PATH);
        
        if (!directory.exists()) {
            directory.mkdirs(); 
        }
        
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite driver not found! Check the Referenced Libraries in VS Code.", e);
        }
        
        return DriverManager.getConnection(URL);
    }

    
    public static void initDatabase() 
    {
        // Arrays of strings containing the queries for creating the tables
        String[] createTableQueries = {
            // Customer Table
            """
            CREATE TABLE IF NOT EXISTS customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                active INTEGER DEFAULT 1
            )
            """,
            // Service Table
            """
            CREATE TABLE IF NOT EXISTS services (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT UNIQUE NOT NULL,
                description TEXT,
                price REAL NOT NULL,
                active INTEGER DEFAULT 1
            )
            """,
            // User Table
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT UNIQUE,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL
            )
            """,
            // Appointment Table
            """
            CREATE TABLE IF NOT EXISTS appointments (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                date_time TEXT NOT NULL, 
                status TEXT NOT NULL, 
                total_price REAL NOT NULL,
                FOREIGN KEY (customer_id) REFERENCES customers(id)
            )
            """,
            // Associative Table (To store List<Service>)
            """
            CREATE TABLE IF NOT EXISTS appointment_services (
                appointment_id INTEGER NOT NULL,
                service_id INTEGER NOT NULL,
                PRIMARY KEY (appointment_id, service_id),
                FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
                FOREIGN KEY (service_id) REFERENCES services(id)
            )
            """,
            // Transaction Table
            """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                amount REAL NOT NULL,
                description TEXT NOT NULL,
                appointment_id INTEGER,
                date TEXT NOT NULL,
                active INTEGER DEFAULT 1,
                canceled_at TEXT
            )
            """
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) 
        {
            for (String query : createTableQueries) 
            {
                stmt.execute(query);
            }

            System.out.println("Tables successfully verified/created in the nail-manager.db database!");
        } 
        catch (SQLException e) 
        {
            System.err.println("Error initializing tables: " + e.getMessage());
        }
    }

    public static void main(String[] args) 
    {
        initDatabase();
    }
}