package presentation.controller;

import java.io.IOException;

import domain.entity.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import presentation.Main;
import presentation.util.SceneManager;

public class DashboardController 
{

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private void loadView(String fxmlPath) {
        try {
            // 1. Carrega o arquivo FXML da sub-tela
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // 2. Limpa o que estiver no centro e coloca a nova tela
            contentArea.getChildren().setAll(view);
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela: " + fxmlPath);
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
        // Para o logout, retornamos à tela de login.
        // Como o LoginUseCase e repositórios são instanciados na Main, 
        // o ideal em uma arquitetura robusta é ter um AppContainer ou Factory central,
        // mas para simplificar aqui, voltaremos à tela inicial.
       
        try {
        System.out.println("Realizando logout...");
        
        // 1. Voltamos para a tela de login. 
        // Geralmente, a LoginController não precisa de factory complexa, 
        // então passamos null ou uma factory simples se o SceneManager exigir.
       SceneManager.changeScreen("/presentation/view/Login.fxml", Main::makeController);
        
    } catch (RuntimeException e) {
        System.err.println("Erro ao tentar deslogar: " + e.getMessage());
        e.printStackTrace();
    }
        
        // Exemplo simplificado de retorno (idealmente você repassaria a factory do Main)
        // SceneManager.changeScreen("src/presentation/view/Login.fxml", ...);
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