package presentation.controller.components;

import application.usecase.appointment.CreateAppointmentUseCase;
import application.usecase.customer.ListCustomersUseCase;
import application.usecase.service.ListServicesUseCase;
import domain.entity.Appointment;
import domain.entity.Customer;
import domain.entity.Service;
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
    @FXML private ListView<Service> servicesListView;

    public AppointmentFormController(
        CreateAppointmentUseCase createAppointmentUseCase,
        ListCustomersUseCase listCustomersUseCase,
        ListServicesUseCase listServicesUseCase
    ) {
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.listCustomersUseCase = listCustomersUseCase;
        this.listServicesUseCase = listServicesUseCase;
    }

    @FXML
    public void initialize() {
        setupCustomerComboBox();
        setupServicesListView();
    }

    private void setupCustomerComboBox() {
        ObservableList<Customer> customers = FXCollections.observableArrayList(listCustomersUseCase.execute(true));
        customerComboBox.setItems(customers);
        customerComboBox.setPromptText("Selecione um cliente");

        customerComboBox.setConverter(new StringConverter<Customer>() {
            @Override
            public String toString(Customer customer) {
                return (customer == null) ? "" : customer.getName().getValue();
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });
    }

    private void setupServicesListView() {
        servicesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        servicesListView.setItems(FXCollections.observableArrayList(listServicesUseCase.execute()));

        servicesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Service item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName().getValue() + " - R$ " + item.getPrice());
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

            validateInputs(selectedCustomer, date, timeText);

            ObservableList<Service> selectedServices = servicesListView.getSelectionModel().getSelectedItems();
            if (selectedServices.isEmpty()) {
                throw new IllegalArgumentException("At least one service must be selected.");
            }

            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(timeText));

            Appointment appointment = createAppointmentUseCase.execute(
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

    private void validateInputs(Customer customer, LocalDate date, String timeText) {
        if (customer == null || date == null || timeText.isBlank()) {
            throw new IllegalArgumentException("Customer, Date and Time are mandatory.");
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