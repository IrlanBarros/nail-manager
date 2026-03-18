package presentation.controller;

import application.usecase.customer.DeleteCustomerUseCase;
import application.usecase.customer.ListCustomersUseCase;
import domain.entity.Customer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import presentation.controller.components.CustomerFormController;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerController {

    private final ListCustomersUseCase listCustomersUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final Callback<Class<?>, Object> controllerFactory;

    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, Void> colActions;
    @FXML private TextField searchField;

    public CustomerController(
        ListCustomersUseCase listCustomersUseCase,
        DeleteCustomerUseCase deleteCustomerUseCase,
        Callback<Class<?>, Object> controllerFactory
    ) {
        this.listCustomersUseCase = listCustomersUseCase;
        this.deleteCustomerUseCase = deleteCustomerUseCase;
        this.controllerFactory = controllerFactory;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchFilter();
        loadCustomerData();
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName().getValue()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone().getValue()));
        configureActionButtons();
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch(null));
    }

    private void loadCustomerData() {
        customerList.setAll(listCustomersUseCase.execute(true));
        customerTable.setItems(customerList);
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String searchTerm = searchField.getText().toLowerCase().trim();

        if (searchTerm.isEmpty()) {
            loadCustomerData();
            return;
        }

        List<Customer> filteredList = listCustomersUseCase.execute(true).stream()
            .filter(c -> c.getName().getValue().toLowerCase().contains(searchTerm) || 
                         c.getPhone().getValue().contains(searchTerm))
            .collect(Collectors.toList());

        customerList.setAll(filteredList);
    }

    private void configureActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = createActionButton("✎", "#3498db", e -> handleEdit(getTableView().getItems().get(getIndex())));
            private final Button btnDelete = createActionButton("🗑", "#e74c3c", e -> handleDelete(getTableView().getItems().get(getIndex())));
            private final HBox container = new HBox(10, btnEdit, btnDelete);

            private Button createActionButton(String text, String color, javafx.event.EventHandler<ActionEvent> action) {
                Button btn = new Button(text);
                btn.getStyleClass().add("action-btn");
                btn.setStyle("-fx-background-color: " + color + ";");
                btn.setOnAction(action);
                return btn;
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML
    public void handleNewCustomer(ActionEvent event) {
        openCustomerForm(null);
    }

    private void handleEdit(Customer customer) {
        openCustomerForm(customer);
    }

    private void handleDelete(Customer customer) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deactivation");
        confirm.setHeaderText("Deactivate customer: " + customer.getName().getValue());
        confirm.setContentText("Are you sure? This will not delete history, but they will be inactive.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    deleteCustomerUseCase.execute(customer.getId());
                    loadCustomerData();
                } catch (Exception e) {
                    showError("Error", e.getMessage());
                }
            }
        });
    }

    private void openCustomerForm(Customer customer) {
        try {
            URL fxmlLocation = getClass().getResource("/presentation/view/components/CustomerForm.fxml");
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            
            loader.setControllerFactory(controllerFactory);

            Parent root = loader.load();
            
            CustomerFormController formController = loader.getController();
            formController.setCustomer(customer);

            Stage stage = new Stage();
            stage.setTitle(customer == null ? "Novo Cliente" : "Editar Cliente");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            if (customerTable.getScene() != null && customerTable.getScene().getWindow() != null) {
                stage.initOwner(customerTable.getScene().getWindow());
            }

            stage.setScene(new Scene(root));
            stage.setResizable(false);
            
            stage.showAndWait();

            loadCustomerData(); 

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erro Crítico", "Falha ao abrir formulário: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}