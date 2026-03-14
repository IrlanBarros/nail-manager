package presentation.controller;

import java.io.IOException;

import domain.entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import presentation.Main;
import presentation.util.SceneManager;
import java.net.URL;

public class DashboardController 
{

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private void loadView(String fxmlPath) {
    try {
        // Locates the file
        URL url = getClass().getResource(fxmlPath);
        if (url == null) {
            System.err.println("FXML file not found: " + fxmlPath);
            return;
        }

        // Loads the new screen
        FXMLLoader loader = new FXMLLoader(url);
        Parent view = loader.load();

        // Clears the content area BEFORE adding the new one
        // setAll removes all old children at once
        contentArea.getChildren().setAll(view);

        // Makes the new screen fill the available space
        VBox.setVgrow(view, Priority.ALWAYS);
        
        if (view instanceof javafx.scene.layout.Region) {
            ((javafx.scene.layout.Region) view).setMaxWidth(Double.MAX_VALUE);
            ((javafx.scene.layout.Region) view).setMaxHeight(Double.MAX_VALUE);
        }

        System.out.println("Screen loaded successfully: " + fxmlPath);

    } catch (IOException e) {
        System.err.println("FXML LOADING ERROR: " + fxmlPath);
        // This will show if the error is due to FinanceRecord not being found
        e.printStackTrace(); 
    }
}

    private final User loggedUser;

    public DashboardController(User loggedUser) 
    {
        this.loggedUser = loggedUser;
    }

    @FXML
    public void initialize() {
        if (loggedUser != null && loggedUser.getName() != null) {
            welcomeLabel.setText("Bem-vindo(a), " + loggedUser.getName().getValue());
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) 
    {
        // For logout, we return to the login screen.
        // Since LoginUseCase and repositories are instantiated in Main, 
        // the ideal in a robust architecture is to have a central AppContainer or Factory,
        // but to simplify here, we will return to the initial screen.
       
        try {
        System.out.println("Performing logout...");
        
        
       SceneManager.changeScreen("/presentation/view/Login.fxml", Main::makeController);
        
    } catch (RuntimeException e) {
        System.err.println("Error while trying to logout: " + e.getMessage());
        e.printStackTrace();
    }
        

    }

    @FXML
    public void goToCustomers(ActionEvent event) {
        welcomeLabel.setText("Clientes");
        loadView("/presentation/view/Customers.fxml");
        
    }

    @FXML
    public void goToFinance(ActionEvent event) {
        welcomeLabel.setText("Financeiro");
        loadView("/presentation/view/Finance.fxml");
    }

    @FXML
    public void goToReports(ActionEvent event) {
        welcomeLabel.setText("Relatórios");
        loadView("/presentation/view/Reports.fxml");
    }

    @FXML
    public void goToServices(ActionEvent event) {
        welcomeLabel.setText("Serviços");
        loadView("/presentation/view/Services.fxml");
    }

    @FXML
    public void goToHome(ActionEvent event) {
        welcomeLabel.setText("Bem-vindo(a), Administrador");
        loadView("/presentation/view/Home.fxml");
    }

}