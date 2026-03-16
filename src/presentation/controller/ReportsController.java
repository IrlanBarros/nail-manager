package presentation.controller;

import application.usecase.transaction.GetCashFlowUseCase;
import domain.entity.Transaction;
import domain.enums.TransactionType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import presentation.controller.FinanceController.ClientRanking;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

public class ReportsController implements Initializable {

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblAverageTicket;
    @FXML private Label lblTotalCustomers; 
    @FXML private LineChart<String, Number> chartRevenue;
    @FXML private BarChart<String, Number> chartServices;
    @FXML private TableView<ClientRanking> tableTopClients;
    @FXML private TableColumn<ClientRanking, String> colName;
    @FXML private TableColumn<ClientRanking, Double> colTotalSpent;

    private final GetCashFlowUseCase getCashFlowUseCase;

    public ReportsController(GetCashFlowUseCase getCashFlowUseCase) {
        this.getCashFlowUseCase = getCashFlowUseCase;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chartRevenue.setCreateSymbols(false);

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTotalSpent.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));

        colTotalSpent.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("R$ %.2f", value));
            }
        });

        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        try {
            LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
            LocalDateTime end = LocalDateTime.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59);

            GetCashFlowUseCase.CashFlowReport report = getCashFlowUseCase.execute(start, end);
            List<Transaction> transactions = report.getTransactions();

            // Total de Receita
            BigDecimal totalIn = report.getTotalIncome();
            lblTotalRevenue.setText(String.format("R$ %.2f", totalIn));
            
            // Contagem de atendimentos (Filtrando por INCOME com segurança)
            long countIn = transactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .count();
            
            double avg = countIn > 0 ? totalIn.doubleValue() / countIn : 0.0;
            lblAverageTicket.setText(String.format("R$ %.2f", avg));
            lblTotalCustomers.setText(String.valueOf(countIn));

            updateLineChart(transactions);
            updateBarChart(transactions);
            updateServiceRankingTable(transactions);

            applyStyling();

        } catch (Exception e) {
            System.err.println("Erro ao carregar relatórios: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    private void updateLineChart(List<Transaction> transactions) {
        chartRevenue.getData().clear();
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Receita Diária");

        // Solução para o erro de Cast: Forçamos o dia a ser Integer e o valor a ser Double
        Map<Integer, Double> dailySum = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .collect(Collectors.groupingBy(
                t -> Integer.valueOf(t.getDate().getDayOfMonth()), 
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));

        int lastDay = java.time.LocalDate.now().lengthOfMonth();

        for (int day = 1; day <= lastDay; day++) {
            Double amount = dailySum.getOrDefault(day, 0.0);
            revenueSeries.getData().add(new XYChart.Data<>(String.valueOf(day), amount));
        }

        chartRevenue.getData().add(revenueSeries);
    }

    private void updateBarChart(List<Transaction> transactions) {
        chartServices.getData().clear();
        XYChart.Series<String, Number> serviceSeries = new XYChart.Series<>();
        serviceSeries.setName("Faturamento por Serviço");

        Map<String, Double> categorySum = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .collect(Collectors.groupingBy(
                t -> String.valueOf(t.getDescription().getValue()),
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));

        categorySum.forEach((name, sum) -> {
            serviceSeries.getData().add(new XYChart.Data<>(name, sum));
        });

        chartServices.getData().add(serviceSeries);
    }

    private void updateServiceRankingTable(List<Transaction> transactions) {
        Map<String, Double> rankingMap = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .collect(Collectors.groupingBy(
                t -> String.valueOf(t.getDescription().getValue()),
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));

        List<ClientRanking> tableData = rankingMap.entrySet().stream()
            .map(entry -> new ClientRanking(entry.getKey(), entry.getValue()))
            .sorted((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()))
            .collect(Collectors.toList());

        tableTopClients.getItems().setAll(tableData);
    }

   private void applyStyling() {
    javafx.application.Platform.runLater(() -> {
        // 1. Estilo do LineChart (Receita Diária) - Mantendo o que já estava
        chartRevenue.lookupAll(".chart-series-line").forEach(line -> 
            line.setStyle("-fx-stroke: #EF233C;"));
        chartRevenue.lookupAll(".chart-line-symbol").forEach(symbol -> 
            symbol.setStyle("-fx-background-color: #EF233C, white;"));

        // 2. Estilo das BARRAS do BarChart
        for (XYChart.Series<String, Number> series : chartServices.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    data.getNode().setStyle("-fx-bar-fill: #EF233C;");
                }
            }
        }

        // 3. O SEGREDO: Mudar a cor do quadradinho da LEGENDA apenas do BarChart
        // Procuramos apenas dentro do chartServices por símbolos de legenda
        chartServices.lookupAll(".chart-legend-item-symbol").forEach(symbol -> {
            symbol.setStyle("-fx-background-color: #EF233C;");
        });
    });
}

}