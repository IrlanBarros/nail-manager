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

import infrastructure.persistence.sqlite.SqliteTransactionRepository;
import application.usecase.transaction.GetCashFlowUseCase;
import domain.repository.TransactionRepositoryInterface;

public class DashboardController 
{

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;

    private void loadView(String fxmlPath) {
    loadView(fxmlPath, null); // Apenas repassa para a versão B com null
}

    private void loadView(String fxmlPath, javafx.util.Callback<Class<?>, Object> factory) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        if (factory != null) {
            loader.setControllerFactory(factory);
        }
        
        Parent view = loader.load();
        
        // Em vez de mudar a cena inteira, muda só o miolo da Dashboard
        contentArea.getChildren().clear();
        contentArea.getChildren().add(view);
        
    } catch (IOException e) {
        System.err.println("Erro ao carregar a sub-view: " + e.getMessage());
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

    // Prepara a "receita"
    var connectionFactory = new infrastructure.persistence.sqlite.SqliteConnectionFactory();
    var repository = new SqliteTransactionRepository(connectionFactory);
    var useCase = new GetCashFlowUseCase(repository);

    // Chama o loadView passando a factory
    loadView("/presentation/view/Finance.fxml", type -> {
        if (type == FinanceController.class) {
            return new FinanceController(useCase);
        }
        return null; 
    });
}


    @FXML
public void goToReports(ActionEvent event) {
    welcomeLabel.setText("Relatórios");

    // Reutilizamos a mesma estrutura de dependências
    var factory = new infrastructure.persistence.sqlite.SqliteConnectionFactory();
    var repository = new SqliteTransactionRepository(factory);
    var useCase = new GetCashFlowUseCase(repository);

    // Carregamos a nova view (certifique-se de que o arquivo existe)
    loadView("/presentation/view/Reports.fxml", type -> {
        if (type == ReportsController.class) {
            return new ReportsController(useCase);
        }
        return null;
    });
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