package presentation.controller;

import application.usecase.service.DeleteServiceUseCase;
import application.usecase.service.ListServicesUseCase;
import domain.entity.Service;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import presentation.controller.components.ServiceFormController;

public class ServiceController {

    private final ListServicesUseCase listServicesUseCase;
    private final DeleteServiceUseCase deleteServiceUseCase;
    private final Callback<Class<?>, Object> controllerFactory;

    private final ObservableList<Service> serviceList = FXCollections.observableArrayList();

    @FXML private TableView<Service> serviceTable;
    @FXML private TableColumn<Service, String> colName;
    @FXML private TableColumn<Service, String> colPrice;
    @FXML private TableColumn<Service, String> colStatus;
    @FXML private TableColumn<Service, Void> colActions;

    public ServiceController(
        ListServicesUseCase listServicesUseCase,
        DeleteServiceUseCase deleteServiceUseCase,
        Callback<Class<?>, Object> controllerFactory
    ) {
        this.listServicesUseCase = listServicesUseCase;
        this.deleteServiceUseCase = deleteServiceUseCase;
        this.controllerFactory = controllerFactory;
    }

    @FXML
    public void initialize() {
        configureColumns();
        loadServiceData();
    }

    private void configureColumns() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName().getValue()));
        
        colPrice.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("R$ %.2f", data.getValue().getPrice()))
        );

        colStatus.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive")
        );

        configureActionButtons();
    }

    private void configureActionButtons() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = createActionButton("✎", "#3498db", e -> handleEdit(getTableView().getItems().get(getIndex())));
            private final Button btnDelete = createActionButton("🗑", "#e74c3c", e -> handleDelete(getTableView().getItems().get(getIndex())));
            private final HBox container = new HBox(10, btnEdit, btnDelete);

            private Button createActionButton(String text, String color, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
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

    private void loadServiceData() {
        serviceList.setAll(listServicesUseCase.execute());
        serviceTable.setItems(serviceList);
    }

    @FXML 
    private void handleNewService() { 
        openServiceForm(null); 
    }

    private void handleEdit(Service service) { 
        openServiceForm(service); 
    }

    private void handleDelete(Service service) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar desativação");
        confirm.setHeaderText("Desativar serviço: " + service.getName().getValue());
        confirm.setContentText("Tem certeza? Isso tornará o serviço indisponível para novas consultas.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    deleteServiceUseCase.execute(service.getId());
                    loadServiceData();
                } catch (IllegalStateException | IllegalArgumentException e) {
                    showError("Not Allowed", e.getMessage());
                } catch (Exception e) {
                    showError("Error", "An unexpected error occurred.");
                }
            }
        });
    }

    private void openServiceForm(Service service) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/components/ServiceForm.fxml"));
            
            loader.setControllerFactory(controllerFactory);

            Parent root = loader.load();
            
            ServiceFormController formController = loader.getController();
            formController.setService(service);

            Stage stage = new Stage();
            stage.setTitle(service == null ? "Novo Serviço" : "Editar Serviço");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            if (serviceTable.getScene() != null && serviceTable.getScene().getWindow() != null) {
                stage.initOwner(serviceTable.getScene().getWindow());
            }

            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            loadServiceData(); 
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not load the service form.");
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}