package presentation.controller;

import application.usecase.user.LoginUseCase;
import domain.entity.User;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import presentation.util.SceneManager;

public class LoginController {

    // Mapped visual components from your Login.fxml
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final LoginUseCase loginUseCase;

    /**
     * Dependency Injection Builder.
     * This constructor is called by the factory in Main.java (via SceneManager).
     */
    public LoginController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // 1. Purely visual validation (Front-end)
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert("Aviso", "Por favor, preencha o e-mail e a senha.", AlertType.WARNING);
            return;
        }

        try {
            // 2. Action: Delegates the business rule to the Application layer
            User loggedUser = loginUseCase.execute(email, password);

            showAlert("✅ Login bem-sucedido", "Bem-vindo(a), " + loggedUser.getName().getValue(),
                    AlertType.CONFIRMATION);

            // 3. Sucesso: Limpa os campos e vai para a próxima tela
            emailField.clear();
            passwordField.clear();

            // Transição para o Dashboard injetando o usuário logado
            SceneManager.changeScreen("src/presentation/view/Dashboard.fxml", controllerClass -> {
                if (controllerClass == DashboardController.class) {
                    return new DashboardController(loggedUser);
                }

                // Fallback padrão
                try {
                    return controllerClass.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException("Falha ao instanciar controller: " + controllerClass, ex);
                }
            });

        } catch (IllegalArgumentException e) {
            // 4. Failure: Captures the domain exception (“Invalid email or password.”)
            showAlert("Erro de Autenticação", e.getMessage(), AlertType.ERROR);
            passwordField.clear(); // It is good practice to clear the password when you make a mistake.
        }
    }

    /**
     * Utility method to display native JavaFX pop-ups.
     */
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}