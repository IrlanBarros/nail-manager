package presentation.controller;

import java.io.IOException;

import domain.entity.User;
import domain.policy.DiscountPolicy;
import domain.policy.LoyaltyDiscountPolicy;
import domain.repository.AppointmentRepositoryInterface;
import domain.repository.CustomerRepositoryInterface;
import domain.repository.ServiceRepositoryInterface;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import presentation.Main;
import presentation.util.SceneManager;

import infrastructure.persistence.sqlite.SqliteTransactionRepository;
import infrastructure.persistence.ConnectionFactory;
import infrastructure.persistence.sqlite.SqliteAppointmentRepository;
import infrastructure.persistence.sqlite.SqliteConnectionFactory;
import infrastructure.persistence.sqlite.SqliteCustomerRepository;
import infrastructure.persistence.sqlite.SqliteServiceRepository;
import application.usecase.appointment.CancelAppointmentUseCase;
import application.usecase.appointment.CreateAppointmentUseCase;
import application.usecase.appointment.FinishAppointmentUseCase;
import application.usecase.appointment.GetAllAppointmentsUseCase;
import application.usecase.appointment.GetDailyScheduleUseCase;
import application.usecase.transaction.GetCashFlowUseCase;
public class DashboardController 
{

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox contentArea;
    
    private final User loggedUser;
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


    public DashboardController(User loggedUser) 
    {
        this.loggedUser = loggedUser;
    }

    @FXML
    public void initialize() {
        if (loggedUser != null && loggedUser.getName() != null) {
            welcomeLabel.setText("Bem-vindo(a), " + loggedUser.getName().getValue());
        }

        goToHome(null);
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
        
        // Chamando o loadView passando a fábrica que injeta o usuário!
        loadView("/presentation/view/Home.fxml", controllerClass -> {
            
        // Já tínhamos esses:
        ConnectionFactory connectionFactory = new SqliteConnectionFactory();
        CustomerRepositoryInterface customerRepo = new SqliteCustomerRepository(connectionFactory);
        ServiceRepositoryInterface serviceRepo = new SqliteServiceRepository(connectionFactory);
        AppointmentRepositoryInterface appointmentRepo = new SqliteAppointmentRepository(connectionFactory, customerRepo, serviceRepo);

        // A Política de Desconto (necessária para o FinishAppointmentUseCase)
        // Supondo que você tenha uma classe concreta ou instancie a padrão:
        DiscountPolicy discountPolicy = new LoyaltyDiscountPolicy(); // Substitua pela sua implementação real

        // Os UseCases
        GetAllAppointmentsUseCase getAllUseCase = new GetAllAppointmentsUseCase(appointmentRepo);
        CreateAppointmentUseCase createUseCase = new CreateAppointmentUseCase(appointmentRepo, customerRepo);
        CancelAppointmentUseCase cancelAppointmentUseCase = new CancelAppointmentUseCase(appointmentRepo);
        FinishAppointmentUseCase finishUseCase = new FinishAppointmentUseCase(appointmentRepo, customerRepo, discountPolicy);

        // Passando tudo para o Controller
        if (controllerClass == HomeController.class) {
            return new HomeController(this.loggedUser, getAllUseCase, createUseCase, finishUseCase, customerRepo, cancelAppointmentUseCase, serviceRepo);
        }
            
            try {
                return controllerClass.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException("Falha ao instanciar: " + controllerClass, ex);
            }
        });
    }

}