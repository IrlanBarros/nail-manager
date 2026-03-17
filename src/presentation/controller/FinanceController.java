package presentation.controller;

import domain.entity.Transaction;
import domain.enums.TransactionType;
import application.usecase.transaction.GetCashFlowUseCase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label; // IMPORTANTE ADICIONAR
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FinanceController {

    @FXML
    private TableView<Transaction> tableFinance;

    @FXML
    private TableColumn<Transaction, String> colType;
    @FXML
    private TableColumn<Transaction, String> colData;
    @FXML
    private TableColumn<Transaction, String> colDescription;
    @FXML
    private TableColumn<Transaction, String> colOrigin;
    @FXML
    private TableColumn<Transaction, String> colValue;

    // DECLARAÇÃO DOS LABELS (Verifique se os fx:id no FXML são esses)
    @FXML
    private Label lblTotalEntry;
    @FXML
    private Label lblTotalExpense;
    @FXML
    private Label lblTotal;

    private final GetCashFlowUseCase getCashFlowUseCase;

    

    public FinanceController(GetCashFlowUseCase getCashFlowUseCase) {
        this.getCashFlowUseCase = getCashFlowUseCase;
    }

    @FXML
    public void initialize() {
        configureTableColumns();
        loadFinancialData();
    }

    private void configureTableColumns() {
        // Tipo: Traduz e coloca cor
        colType.setCellValueFactory(cellData -> {
            TransactionType type = cellData.getValue().getType();
            return new SimpleStringProperty(type.name().equals("INCOME") ? "ENTRADA" : "SAÍDA");
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
                    if (item.equals("ENTRADA")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Data: Formato brasileiro
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colData.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().format(formatter))
        );

        // Descrição
        colDescription.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDescription().getValue())
        );

        // Origem
        colOrigin.setCellValueFactory(cellData -> {
            var appId = cellData.getValue().getAppointmentId();
            return new SimpleStringProperty(appId.isPresent() ? String.valueOf(appId.get()) : "Avulso");
        });

        // Valor
        colValue.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getAmount()))
        );
    }

    private void loadFinancialData() {
    try {
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = LocalDateTime.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59);

        GetCashFlowUseCase.CashFlowReport report = getCashFlowUseCase.execute(start, end);
        List<Transaction> transactions = report.getTransactions();
        
        tableFinance.setItems(FXCollections.observableArrayList(transactions));

        // Inicializamos os somadores usando BigDecimal para precisão total
        java.math.BigDecimal totalIn = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalOut = java.math.BigDecimal.ZERO;

        System.out.println("--- financial calculation ---");
        for (Transaction t : transactions) {
          
            if (t.isActive()) {
                if (t.getType().name().equals("INCOME")) {
                    totalIn = totalIn.add(t.getAmount());
                } else {
                    totalOut = totalOut.add(t.getAmount());
                }
            } 
        }

        java.math.BigDecimal balance = totalIn.subtract(totalOut);

        // Atualiza os labels com os valores EXATOS da soma acima
        lblTotalEntry.setText(String.format("R$ %.2f", totalIn));
        lblTotalExpense.setText(String.format("R$ %.2f", totalOut));
        lblTotal.setText(String.format("R$ %.2f", balance));

        System.out.println("RESULTS -> Entrys: " + totalIn + " | Expenses: " + totalOut + " | balance: " + balance);
        System.out.println("------------------------");

    } catch (Exception e) {
        e.printStackTrace();
    }
}



public static class ClientRanking {
    private final String name;
    private final Double totalSpent;

    public ClientRanking(String name, Double totalSpent) {
        this.name = name;
        this.totalSpent = totalSpent;
    }

    public String getName() { return name; }
    public Double getTotalSpent() { return totalSpent; }
}



}