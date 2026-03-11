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

    // Tornamos o loginUseCase acessível para a Factory
    private static LoginUseCase loginUseCase;

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
        
        loginUseCase = new LoginUseCase(userRepository, passwordHasher);

        // No seu Main.java, dentro do método start()
        System.out.println("Caminho do Login: " + SceneManager.class.getResource("/presentation/view/Login.fxml"));

         SceneManager.changeScreen("/presentation/view/Login.fxml", Main::makeController);
    }
       
    // ESSA É A "RECEITA" CENTRALIZADA: Agora qualquer tela sabe como criar as outras
    public static Object makeController(Class<?> controllerClass) {
        if (controllerClass == LoginController.class) {
            return new LoginController(loginUseCase);
        }
        
        // Se você adicionar outros controllers com injeção (ex: Finance), coloque aqui
        
        // Fallback para controllers simples
        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao instanciar: " + controllerClass, e);
        }
    }

    public static void main(String[] args) 
    {
        launch(args);
    }
}