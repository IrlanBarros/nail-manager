package presentation.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;

public class SceneManager 
{

    private static Stage primaryStage;

    public static void setStage(Stage stage) 
    {
        primaryStage = stage;
    }

    /**
     * Replace the current screen with a new one, applying Dependency Injection.
     * * @param fxmlPath File path .fxml (ex: "src/presentation/view/Login.fxml" - relative to project root)
     * @param controllerFactory Factory that teaches JavaFX to instantiate the Controller
     */
    public static void changeScreen(String fxmlPath, Callback<Class<?>, Object> controllerFactory) 
    {
        if (primaryStage == null) 
        {
            throw new IllegalStateException("SceneManager was not initialized with the main Stage.");
        }

        try {
            // Use file path relative to project root instead of classpath
            String projectPath = System.getProperty("user.dir");
            File fxmlFile = new File(projectPath, fxmlPath);
            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());

            if (controllerFactory != null) 
            {
                loader.setControllerFactory(controllerFactory);
            }

            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);

            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            throw new RuntimeException("Critical error loading screen: " + fxmlPath, e);
        }
    }
}