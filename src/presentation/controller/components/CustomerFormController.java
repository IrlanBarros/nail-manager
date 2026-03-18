package presentation.controller.components;

import java.util.regex.Pattern;

import application.usecase.customer.RegisterCustomerUseCase;
import application.usecase.customer.UpdateCustomerUseCase;
import domain.entity.Customer;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CustomerFormController {

    private final RegisterCustomerUseCase registerUseCase;
    private final UpdateCustomerUseCase updateUseCase;
    private Customer currentCustomer;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    public CustomerFormController(RegisterCustomerUseCase registerUseCase, UpdateCustomerUseCase updateUseCase) {
        this.registerUseCase = registerUseCase;
        this.updateUseCase = updateUseCase;
    }

    public void setCustomer(Customer customer) {
        this.currentCustomer = customer;
        if (customer != null) {
            nameField.setText(customer.getName().getValue());
            phoneField.setText(customer.getPhone().getValue());
            emailField.setText(customer.getEmail().getValue());
        }
    }

    @FXML
    public void initialize() {
        phoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[\\d\\s\\+\\-\\(\\)]*")) {
                phoneField.setText(oldValue);
            }
            if (newValue == null || newValue.isEmpty()) return;
                    
            String digits = newValue.replaceAll("\\D", "");
            StringBuilder masked = new StringBuilder();
            
            if (digits.length() > 11) {
                digits = digits.substring(0, 11);
            }
            
            int len = digits.length();
            if (len > 0) {
                masked.append("(").append(digits.substring(0, Math.min(len, 2)));
                if (len > 2) {
                    masked.append(") ").append(digits.substring(2, Math.min(len, 3)));
                    if (len > 3) {
                        masked.append(" ").append(digits.substring(3, Math.min(len, 7)));
                        if (len > 7) {
                            masked.append("-").append(digits.substring(7, len));
                        }
                    }
                }
            }
            
            String finalMasked = masked.toString();
            if (!newValue.equals(finalMasked)) {
                phoneField.setText(finalMasked);
                
                javafx.application.Platform.runLater(() -> {
                    phoneField.positionCaret(finalMasked.length());
                });
            }
        });
    }

    @FXML
    private void handleSave() {
        try {
            String name = nameField.getText();
            String email = emailField.getText();
            String phoneRaw = phoneField.getText().replaceAll("\\D", "");

            if (name.isBlank() || email.isBlank() || phoneRaw.isBlank()) {
                throw new IllegalArgumentException("All fields are mandatory.");
            }

            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("The email format is invalid.");
            }

            if (phoneRaw.length() < 10 || phoneRaw.length() > 11) {
                throw new IllegalArgumentException("The phone must have 10 or 11 digits.");
            }

            if (currentCustomer == null) {
                registerUseCase.execute(name, phoneRaw, email);
            } else {
                updateUseCase.execute(currentCustomer.getId(), name, email, phoneRaw);
            }

            closeStage();

        } catch (IllegalArgumentException | IllegalStateException e) {
            showErrorAlert(e.getMessage());
        } catch (Exception e) {
            showErrorAlert("An unexpected error occurred.");
            e.printStackTrace();
        }
    }
    
    private void showErrorAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }
}