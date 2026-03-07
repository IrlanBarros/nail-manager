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

    @Override
    public void start(Stage primaryStage) throws Exception 
    {
        
        primaryStage.setTitle("Sistema de Gestão - Nail Manager");
        
        SceneManager.setStage(primaryStage);

        SqliteConnectionFactory connectionFactory = new SqliteConnectionFactory();
        SqliteUserRepository userRepository = new SqliteUserRepository(connectionFactory);
        PBKDF2PasswordHasher passwordHasher = new PBKDF2PasswordHasher();

        DatabaseSeeder seeder = new DatabaseSeeder(connectionFactory, passwordHasher);
        seeder.seed();
        
        LoginUseCase loginUseCase = new LoginUseCase(userRepository, passwordHasher);

        SceneManager.changeScreen("src/presentation/view/Login.fxml", controllerClass -> {
            
            if (controllerClass == LoginController.class) 
            {
                return new LoginController(loginUseCase);
            }

            // Fallback for controllers that do not require dependencies
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate controller: " + controllerClass, e);
            }
        });
    }

    public static void main(String[] args) 
    {
        launch(args);
    }
}