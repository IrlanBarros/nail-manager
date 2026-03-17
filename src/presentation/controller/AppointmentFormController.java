package presentation.controller;

import application.usecase.appointment.CreateAppointmentUseCase;
import application.usecase.customer.ListCustomersUseCase;
import application.usecase.service.ListServicesUseCase;
import domain.entity.Appointment;
import domain.entity.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class AppointmentFormController {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final ListServicesUseCase listServicesUseCase;

    @FXML private ComboBox<Customer> customerComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField descriptionField;
    @FXML private ListView<domain.entity.Service> servicesListView;

    // No construtor, adicione o novo UseCase
    public AppointmentFormController(
        CreateAppointmentUseCase createAppointmentUseCase,
        ListCustomersUseCase listCustomersUseCase,
        ListServicesUseCase listServicesUseCase // Novo
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.listServicesUseCase = listServicesUseCase;
    }

    @FXML
    public void initialize() {
        // 1. Carrega apenas clientes ativos para o agendamento
        ObservableList<Customer> customers = FXCollections.observableArrayList(listCustomersUseCase.execute(true));
        customerComboBox.setItems(customers);

        // 2. Define como o objeto Customer será exibido no ComboBox
        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return (customer == null) ? "" : customer.getName().getValue();
            }

            @Override
            public Customer fromString(String string) {
                return null; // Não necessário para seleção
            }
        });
        
        // Garante que o prompt text apareça corretamente
        customerComboBox.setPromptText("Select a customer");

        // 1. Carregar serviços e permitir seleção múltipla
        servicesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        servicesListView.setItems(FXCollections.observableArrayList(listServicesUseCase.execute()));

        // 2. Formatar exibição dos serviços na lista
        servicesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(domain.entity.Service item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName() + " - R$ " + item.getPrice());
            }
        });
    }

    @FXML
    public void handleSave(ActionEvent event) {
        try {
            Customer selectedCustomer = customerComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String timeText = timeField.getText();
            String description = descriptionField.getText();

            // Validação simples de campos obrigatórios
            if (selectedCustomer == null || date == null || timeText.isBlank()) {
                throw new IllegalArgumentException("Customer, Date and Time are mandatory.");
            }

            ObservableList<domain.entity.Service> selectedServices = servicesListView.getSelectionModel().getSelectedItems();
            if (selectedServices.isEmpty()) {
                throw new IllegalArgumentException("At least one service must be selected.");
            }

            // Criar o Appointment
            Appointment appointment = createAppointmentUseCase.execute(
                customerComboBox.getValue().getId(),
                LocalDateTime.of(datePicker.getValue(), LocalTime.parse(timeField.getText())),
                descriptionField.getText()
            );

            LocalTime time = LocalTime.parse(timeText);
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            // Executa a criação do agendamento
            createAppointmentUseCase.execute(
                selectedCustomer.getId(), 
                dateTime, 
                description
            );

            selectedServices.forEach(appointment::addService);

            closeStage();

        } catch (DateTimeParseException e) {
            showAlert("Invalid time format. Please use HH:mm.");
        } catch (Exception e) {
            showAlert(e.getMessage());
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) customerComboBox.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}