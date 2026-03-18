package presentation.controller;

import domain.entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import presentation.Main;
import presentation.util.SceneManager;

import java.io.IOException;

public class SidebarController {

    @FXML private Label welcomeLabel;
    @FXML private VBox contentArea;

    private final User loggedUser;
    private final Callback<Class<?>, Object> controllerFactory;

    public SidebarController(User loggedUser, Callback<Class<?>, Object> controllerFactory) {
        this.loggedUser = loggedUser;
        this.controllerFactory = controllerFactory;
    }

    @FXML
    public void initialize() {
        if (loggedUser != null && loggedUser.getName() != null) {
            welcomeLabel.setText("Bem-vindo(a), " + loggedUser.getName().getValue());
        }
        goToHome(null);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(controllerFactory);
            Parent view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SceneManager.changeScreen("/presentation/view/Login.fxml", Main::makeController);
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
        welcomeLabel.setText("Visão Geral");
        loadView("/presentation/view/Home.fxml");
    }
}