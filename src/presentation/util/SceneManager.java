package presentation.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;

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
     * * @param fxmlPath File path .fxml (ex: "/presentation/view/Login.fxml")
     * @param controllerFactory Factory that teaches JavaFX to instantiate the Controller
     */
    public static void changeScreen(String fxmlPath, Callback<Class<?>, Object> controllerFactory) 
    {
        if (primaryStage == null) 
        {
            throw new IllegalStateException("SceneManager was not initialized with the main Stage.");
        }

        // 1. Resolvemos a URL antes para validar
        java.net.URL fxmlLocation = SceneManager.class.getResource(fxmlPath);

        // 2. Se for null, o Java não achou o arquivo na pasta compilada!
        if (fxmlLocation == null) 
        {
            throw new IllegalStateException("🚨 ERRO: Arquivo FXML não encontrado: " + fxmlPath + 
                                            "\nVerifique a ortografia ou se o VS Code copiou o arquivo para a pasta de build (bin/).");
        }

        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);

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