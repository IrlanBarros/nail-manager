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
public class LoginController 
{

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
    public void handleLogin(ActionEvent event) 
    {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.trim().isEmpty() || password.trim().isEmpty()) 
        {
            showAlert("Aviso", "Por favor, preencha o e-mail e a senha.", AlertType.WARNING);
            return;
        }

        try {
            User loggedUser = loginUseCase.execute(email, password);

            showAlert("✅ Login bem-sucedido", "Bem-vindo(a), " + loggedUser.getName().getValue(),
                    AlertType.CONFIRMATION);

            emailField.clear();
            passwordField.clear();

            try {
                System.out.println("[DEBUG] Iniciando tentativa de troca de tela...");

                SceneManager.changeScreen("/presentation/view/Dashboard.fxml", controllerClass -> {
                    System.out.println("[DEBUG] FXMLLoader pediu para criar o controller: " + controllerClass.getName());
                    
                    if (controllerClass == DashboardController.class) {
                        System.out.println("[DEBUG] Injetando usuário no DashboardController...");
                        return new DashboardController(loggedUser);
                    }

                    try {
                        return controllerClass.getDeclaredConstructor().newInstance();
                    } catch (Exception ex) {
                        throw new RuntimeException("Falha ao instanciar controller: " + controllerClass, ex);
                    }
                });

                System.out.println("[DEBUG] Troca de tela finalizada com sucesso!");

            } catch (Throwable t) {
                System.err.println("🚨 ERRO CRÍTICO NA TROCA DE TELA 🚨");
                t.printStackTrace();
                
                showAlert("Erro Fatal", "Falha na transição de tela: " + t.getMessage(), AlertType.ERROR);
            }

        } catch (IllegalArgumentException e) 
        {
            showAlert("Erro de Autenticação", e.getMessage(), AlertType.ERROR);
            passwordField.clear(); 
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