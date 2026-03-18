package presentation.controller.components;

import application.usecase.transaction.RegisterTransactionUseCase;
import domain.enums.TransactionType;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class TransactionFormController {

    private final RegisterTransactionUseCase registerTransactionUseCase;

    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;

    public TransactionFormController(RegisterTransactionUseCase registerTransactionUseCase) {
        this.registerTransactionUseCase = registerTransactionUseCase;
    }

    @FXML
    public void initialize() {
        setupComboBox();
        setupAmountFieldValidation();
    }

    private void setupComboBox() {
        typeComboBox.setItems(FXCollections.observableArrayList("ENTRADA", "SAÍDA"));
        typeComboBox.setValue("ENTRADA");
    }

    private void setupAmountFieldValidation() {
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*([\\.,]\\d*)?")) {
                amountField.setText(oldValue);
            }
        });
    }

    @FXML
    public void handleSave(ActionEvent event) {
        try {
            String description = descriptionField.getText();
            String amountText = amountField.getText().replace(",", ".");
            
            validateInputs(description, amountText);

            BigDecimal amount = new BigDecimal(amountText);
            TransactionType type = determineTransactionType();

            registerTransactionUseCase.execute(type, amount, description, null);

            closeStage();

        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        } catch (Exception e) {
            showAlert("An unexpected error occurred: " + e.getMessage());
        }
    }

    private void validateInputs(String description, String amountText) {
        if (description.isBlank() || amountText.isBlank()) {
            throw new IllegalArgumentException("Description and Amount are mandatory.");
        }
    }

    private TransactionType determineTransactionType() {
        return typeComboBox.getValue().equals("ENTRADA") ? TransactionType.INCOME : TransactionType.EXPENSE;
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeStage();
    }

    private void closeStage() {
        Stage stage = (Stage) descriptionField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}