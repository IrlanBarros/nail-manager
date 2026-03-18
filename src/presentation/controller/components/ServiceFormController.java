package presentation.controller.components;

import application.usecase.service.CreateServiceUseCase;
import application.usecase.service.UpdateServiceUseCase;
import domain.entity.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class ServiceFormController {

    private final CreateServiceUseCase createUseCase;
    private final UpdateServiceUseCase updateUseCase;
    private Service currentService;

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField priceField;

    public ServiceFormController(CreateServiceUseCase createUseCase, UpdateServiceUseCase updateUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
    }

    public void setService(Service service) {
        this.currentService = service;
        if (service != null) {
            nameField.setText(service.getName().getValue());
            descriptionField.setText(service.getDescription() != null ? service.getDescription().getValue() : "");
            priceField.setText(service.getPrice().toString());
        }
    }

    @FXML
    public void initialize() {
        priceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]\\d*)?")) {
                priceField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText();
            String description = descriptionField.getText();
            String priceText = priceField.getText().replace(",", ".");

            if (name.isBlank() || priceText.isBlank()) {
                throw new IllegalArgumentException("Name and Price are mandatory.");
            }

            BigDecimal price = new BigDecimal(priceText);

            if (currentService == null) {
                createUseCase.execute(name, description, price);
            } else {
                updateUseCase.execute(currentService.getId(), name, description, price);
            }

            closeStage();

        } catch (NumberFormatException e) {
            showErrorAlert("Invalid price format. Use numbers only.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            showErrorAlert(e.getMessage());
        } catch (Exception e) {
            showErrorAlert("An unexpected error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    private void showErrorAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}