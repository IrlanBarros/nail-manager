package presentation;

import application.usecase.user.LoginUseCase;
import infrastructure.persistence.sqlite.SqliteUserRepository;
import infrastructure.security.PBKDF2PasswordHasher;
import infrastructure.database.DatabaseSeeder;
import infrastructure.persistence.sqlite.SqliteConnectionFactory;
import presentation.controller.LoginController;
import presentation.util.SceneManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    // Making the loginUseCase accessible for the Factory
    private static LoginUseCase loginUseCase;

    @Override
    public void start(Stage primaryStage) throws Exception 
    {
        
        primaryStage.setTitle("Management System - Nail Manager");
        
        SceneManager.setStage(primaryStage);

        SqliteConnectionFactory connectionFactory = new SqliteConnectionFactory();
        SqliteUserRepository userRepository = new SqliteUserRepository(connectionFactory);
        PBKDF2PasswordHasher passwordHasher = new PBKDF2PasswordHasher();

        DatabaseSeeder seeder = new DatabaseSeeder(connectionFactory, passwordHasher);
        seeder.seed();
        
        loginUseCase = new LoginUseCase(userRepository, passwordHasher);

        // In your Main.java, inside the start() method
        System.out.println("Login Path: " + SceneManager.class.getResource("/presentation/view/Login.fxml"));

        SceneManager.changeScreen("/presentation/view/Login.fxml", Main::makeController);
    }
       
    // Now any screen knows how to instantiate the others
    public static Object makeController(Class<?> controllerClass) {
        if (controllerClass == LoginController.class) {
            return new LoginController(loginUseCase);
        }
        
        // Fallback for simple controllers without dependencies
        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate: " + controllerClass, e);
        }
    }

    public static void main(String[] args) 
    {
        launch(args);
    }
}