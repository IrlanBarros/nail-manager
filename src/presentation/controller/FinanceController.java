package presentation.controller;

import domain.entity.Transaction;
import domain.enums.TransactionType;
import application.usecase.transaction.GetCashFlowUseCase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class FinanceController {

    @FXML private TableView<Transaction> tableFinance;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, String> colData;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colOrigin;
    @FXML private TableColumn<Transaction, String> colValue;

    @FXML private Label lblTotalEntry;
    @FXML private Label lblTotalExpense;
    @FXML private Label lblTotal;

    @FXML private ComboBox<String> comboPeriod;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;

    private final GetCashFlowUseCase getCashFlowUseCase;
    private final Callback<Class<?>, Object> controllerFactory;

    public FinanceController(GetCashFlowUseCase getCashFlowUseCase, Callback<Class<?>, Object> controllerFactory) {
        this.getCashFlowUseCase = getCashFlowUseCase;
        this.controllerFactory = controllerFactory;
    }

    @FXML
    public void initialize() {
        configureTableColumns();
        configureFilters();
        handleFilter(null);
    }

    private void configureFilters() {
        comboPeriod.setItems(FXCollections.observableArrayList("Mês Atual", "Mês Passado", "Últimos 7 dias", "Personalizado"));
        comboPeriod.setValue("Mês Atual");
        comboPeriod.setOnAction(e -> applySelectedFilter());
        comboPeriod.getOnAction().handle(null);
    }

    private void applySelectedFilter() {
        LocalDate now = LocalDate.now();
        switch (comboPeriod.getValue()) {
            case "Mês Atual":
                dateStart.setValue(now.withDayOfMonth(1));
                dateEnd.setValue(now.withDayOfMonth(now.lengthOfMonth()));
                break;
            case "Mês Passado":
                LocalDate lastMonth = now.minusMonths(1);
                dateStart.setValue(lastMonth.withDayOfMonth(1));
                dateEnd.setValue(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
                break;
            case "Últimos 7 dias":
                dateStart.setValue(now.minusDays(7));
                dateEnd.setValue(now);
                break;
            case "Personalizado":
                break;
        }
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        refreshData();
    }

    private void refreshData() {
        if (dateStart.getValue() != null && dateEnd.getValue() != null) {
            LocalDateTime start = dateStart.getValue().atStartOfDay();
            LocalDateTime end = dateEnd.getValue().atTime(LocalTime.MAX);
            loadFinancialData(start, end);
        }
    }

    private void configureTableColumns() {
        setupTypeColumn();
        setupDateColumn();
        setupDescriptionColumn();
        setupOriginColumn();
        setupValueColumn();
    }

    private void setupTypeColumn() {
        colType.setCellValueFactory(cellData -> {
            TransactionType type = cellData.getValue().getType();
            if (type == null) return new SimpleStringProperty("DESCONHECIDO");
            return new SimpleStringProperty(type == TransactionType.INCOME ? "ENTRADA" : "SAÍDA");
        });

        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("ENTRADA") ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: red; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void setupDateColumn() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colData.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getDate();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "Sem data");
        });
    }

    private void setupDescriptionColumn() {
        colDescription.setCellValueFactory(cellData -> {
            var desc = cellData.getValue().getDescription();
            return new SimpleStringProperty((desc != null && desc.getValue() != null) ? desc.getValue() : "");
        });
    }

    private void setupOriginColumn() {
        colOrigin.setCellValueFactory(cellData -> {
            try {
                Object appId = cellData.getValue().getAppointmentId();
                if (appId == null) return new SimpleStringProperty("Avulso");
                
                if (appId instanceof Optional) {
                    Optional<?> opt = (Optional<?>) appId;
                    return new SimpleStringProperty(opt.isPresent() ? String.valueOf(opt.get()) : "Avulso");
                }
                return new SimpleStringProperty(String.valueOf(appId));
            } catch (Exception e) {
                return new SimpleStringProperty("Avulso");
            }
        });
    }

    private void setupValueColumn() {
        colValue.setCellValueFactory(cellData -> {
            BigDecimal amount = cellData.getValue().getAmount();
            return new SimpleStringProperty(amount != null ? String.format("R$ %.2f", amount) : "R$ 0,00");
        });
    }

    private void loadFinancialData(LocalDateTime start, LocalDateTime end) {
        try {
            GetCashFlowUseCase.CashFlowReport report = getCashFlowUseCase.execute(start, end);
            List<Transaction> transactions = report.getTransactions();
            tableFinance.setItems(FXCollections.observableArrayList(transactions));
            calculateAndDisplayTotals(transactions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateAndDisplayTotals(List<Transaction> transactions) {
        BigDecimal totalIn = BigDecimal.ZERO;
        BigDecimal totalOut = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t == null || !t.isActive()) continue;
            
            BigDecimal amount = t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO;

            if (t.getType() == TransactionType.INCOME) {
                totalIn = totalIn.add(amount);
            } else {
                totalOut = totalOut.add(amount);
            }
        }

        BigDecimal balance = totalIn.subtract(totalOut);

        lblTotalEntry.setText(String.format("R$ %.2f", totalIn));
        lblTotalExpense.setText(String.format("R$ %.2f", totalOut));
        lblTotal.setText(String.format("R$ %.2f", balance));
    }

    @FXML
    public void handleNewTransaction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/components/TransactionForm.fxml"));
            loader.setControllerFactory(controllerFactory);

            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Nova Transação");
            stage.initModality(Modality.APPLICATION_MODAL);
            
            if (tableFinance.getScene() != null && tableFinance.getScene().getWindow() != null) {
                stage.initOwner(tableFinance.getScene().getWindow());
            }

            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}