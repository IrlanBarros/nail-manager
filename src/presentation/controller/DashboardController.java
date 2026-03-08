package presentation.controller;

import domain.entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    private final User loggedUser;

    /**
     * Injeção de Dependência via construtor.
     * Recebe o usuário autenticado da tela anterior.
     */
    public DashboardController(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    /**
     * Método chamado automaticamente pelo JavaFX após carregar o FXML.
     */
    @FXML
    public void initialize() {
        if (loggedUser != null && loggedUser.getName() != null) {
            welcomeLabel.setText("Bem-vindo(a), " + loggedUser.getName().getValue());
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        // Para o logout, retornamos à tela de login.
        // Como o LoginUseCase e repositórios são instanciados na Main, 
        // o ideal em uma arquitetura robusta é ter um AppContainer ou Factory central,
        // mas para simplificar aqui, voltaremos à tela inicial.
        System.out.println("Saindo do sistema...");
        
        // Exemplo simplificado de retorno (idealmente você repassaria a factory do Main)
        // SceneManager.changeScreen("src/presentation/view/Login.fxml", ...);
    }
}