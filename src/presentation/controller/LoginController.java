package presentation.controller;

import application.usecase.user.LoginUseCase;
import domain.entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import presentation.util.AppControllerFactory;
import presentation.util.SceneManager;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final LoginUseCase loginUseCase;

    public LoginController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (isInputInvalid(email, password)) {
            showAlert("Aviso", "Por favor, preencha o e-mail e a senha.", AlertType.WARNING);
            return;
        }

        try {
            User loggedUser = loginUseCase.execute(email, password);
            processSuccessfulLogin(loggedUser);
        } catch (IllegalArgumentException e) {
            handleLoginError(e.getMessage());
        }
    }

    private boolean isInputInvalid(String email, String password) {
        return email.trim().isEmpty() || password.trim().isEmpty();
    }

    private void processSuccessfulLogin(User loggedUser) {
        showAlert("Login bem-sucedido", "Bem-vindo(a), " + loggedUser.getName().getValue(), AlertType.CONFIRMATION);
        
        emailField.clear();
        passwordField.clear();
        
        navigateToSidebar(loggedUser);
    }

    private void handleLoginError(String errorMessage) {
        showAlert("Erro de Autenticação", errorMessage, AlertType.ERROR);
        passwordField.clear();
    }

    private void navigateToSidebar(User loggedUser) {
        try {
            AppControllerFactory appFactory = new AppControllerFactory(loggedUser);
            SceneManager.changeScreen("/presentation/view/Sidebar.fxml", appFactory);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erro Fatal", "Falha na transição de tela: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}