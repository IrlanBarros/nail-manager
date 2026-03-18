package presentation.util;

import domain.entity.User;
import domain.policy.DiscountPolicy;
import domain.policy.LoyaltyDiscountPolicy;
import domain.repository.*;
import infrastructure.persistence.ConnectionFactory;
import infrastructure.persistence.sqlite.*;
import application.usecase.appointment.*;
import application.usecase.customer.*;
import application.usecase.dashboard.*;
import application.usecase.service.*;
import application.usecase.transaction.*;
import application.usecase.user.*;
import presentation.controller.*;
import presentation.controller.components.AppointmentFormController;
import presentation.controller.components.CustomerFormController;
import presentation.controller.components.ServiceFormController;
import presentation.controller.components.TransactionFormController;
import javafx.util.Callback;

public class AppControllerFactory implements Callback<Class<?>, Object> {

    private final User loggedUser;

    private final ConnectionFactory connectionFactory;
    private final CustomerRepositoryInterface customerRepo;
    private final ServiceRepositoryInterface serviceRepo;
    private final AppointmentRepositoryInterface appointmentRepo;
    private final TransactionRepositoryInterface transactionRepo;
    private final DiscountPolicy discountPolicy;

    public AppControllerFactory(User loggedUser) {
        this.loggedUser = loggedUser;
        
        this.connectionFactory = new SqliteConnectionFactory();
        this.customerRepo = new SqliteCustomerRepository(connectionFactory);
        this.serviceRepo = new SqliteServiceRepository(connectionFactory);
        this.appointmentRepo = new SqliteAppointmentRepository(connectionFactory, customerRepo, serviceRepo);
        this.transactionRepo = new SqliteTransactionRepository(connectionFactory);
        this.discountPolicy = new LoyaltyDiscountPolicy();
    }

    @Override
    public Object call(Class<?> controllerClass) {
        try {
            if (controllerClass == SidebarController.class) {
                return new SidebarController(loggedUser, this);
            }
            if (controllerClass == HomeController.class) {
                return new HomeController(
                    loggedUser,
                    new GetAllAppointmentsUseCase(appointmentRepo),
                    new FinishAppointmentUseCase(appointmentRepo, customerRepo, discountPolicy),
                    new CancelAppointmentUseCase(appointmentRepo),
                    this
                );
            }
            if (controllerClass == AppointmentFormController.class) {
                return new AppointmentFormController(
                    new CreateAppointmentUseCase(appointmentRepo, customerRepo),
                    new ListCustomersUseCase(customerRepo),
                    new ListServicesUseCase(serviceRepo)
                );
            }
            if (controllerClass == CustomerController.class) {
                return new CustomerController(
                    new ListCustomersUseCase(customerRepo),
                    new DeleteCustomerUseCase(customerRepo, appointmentRepo),
                    this
                );
            }
            if (controllerClass == CustomerFormController.class) {
                return new CustomerFormController(
                    new RegisterCustomerUseCase(customerRepo),
                    new UpdateCustomerUseCase(customerRepo)
                );
            }
            if (controllerClass == ServiceController.class) {
                return new ServiceController(
                    new ListServicesUseCase(serviceRepo),
                    new DeleteServiceUseCase(serviceRepo, appointmentRepo),
                    this // <- A fábrica entra aqui
                );
            }
            if (controllerClass == ServiceFormController.class) {
                return new ServiceFormController(
                    new CreateServiceUseCase(serviceRepo),
                    new UpdateServiceUseCase(serviceRepo)
                );
            }
            if (controllerClass == FinanceController.class) {
                return new FinanceController(
                    new GetCashFlowUseCase(transactionRepo),
                    this // <- A fábrica é passada aqui
                );
            }
            if (controllerClass == TransactionFormController.class) {
                return new TransactionFormController(
                    new RegisterTransactionUseCase(transactionRepo, appointmentRepo)
                );
            }
            if (controllerClass == ReportsController.class) {
                return new ReportsController(
                    new GetCashFlowUseCase(transactionRepo)
                );
            }

            return controllerClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao instanciar o controller: " + controllerClass.getName(), e);
        }
    }
}