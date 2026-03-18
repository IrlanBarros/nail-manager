package presentation.controller;

import application.usecase.transaction.GetCashFlowUseCase;
import domain.entity.Transaction;
import domain.enums.TransactionType;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportsController {

    @FXML private ComboBox<String> comboPeriod;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblAverageTicket;
    @FXML private Label lblTotalCustomers; 
    
    @FXML private LineChart<String, Number> chartRevenue;
    @FXML private BarChart<String, Number> chartServices;

    @FXML private TableView<ServiceMetric> tableServiceRanking;
    @FXML private TableColumn<ServiceMetric, String> colName;
    @FXML private TableColumn<ServiceMetric, Number> colQuantity;
    @FXML private TableColumn<ServiceMetric, String> colTotalSpent;
    @FXML private VBox reportContainer;

    private final GetCashFlowUseCase getCashFlowUseCase;

    public ReportsController(GetCashFlowUseCase getCashFlowUseCase) {
        this.getCashFlowUseCase = getCashFlowUseCase;
    }

    @FXML
    public void initialize() {
        chartRevenue.setCreateSymbols(false);
        configureFilters();
        configureTable();
        handleFilter(null);
    }

    private void configureFilters() {
        comboPeriod.setItems(FXCollections.observableArrayList("Mês Atual", "Mês Passado", "Últimos 7 dias", "Todo o Período"));
        comboPeriod.setValue("Mês Atual");
        comboPeriod.setOnAction(e -> applyDateFilter());
        comboPeriod.getOnAction().handle(null);
    }

    private void applyDateFilter() {
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
            case "Todo o Período":
                dateStart.setValue(now.minusYears(10));
                dateEnd.setValue(now);
                break;
        }
    }

    private void configureTable() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colQuantity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()));
        colTotalSpent.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("R$ %.2f", data.getValue().getTotalRevenue()))
        );
    }

    @FXML
    public void handleFilter(ActionEvent event) {
        if (dateStart.getValue() == null || dateEnd.getValue() == null) return;

        LocalDateTime start = dateStart.getValue().atStartOfDay();
        LocalDateTime end = dateEnd.getValue().atTime(LocalTime.MAX);
        loadDataFromDatabase(start, end);
    }

    private void loadDataFromDatabase(LocalDateTime start, LocalDateTime end) {
        try {
            GetCashFlowUseCase.CashFlowReport report = getCashFlowUseCase.execute(start, end);
            List<Transaction> transactions = report.getTransactions();

            List<Transaction> incomes = transactions.stream()
                    .filter(t -> t != null && t.isActive() && t.getType() == TransactionType.INCOME)
                    .collect(Collectors.toList());

            updateSummaryCards(report.getTotalIncome(), incomes.size());
            updateLineChart(incomes);
            
            List<ServiceMetric> metrics = generateServiceMetrics(incomes);
            updateBarChart(metrics);
            updateServiceRankingTable(metrics);
            applyStyling();

        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

    private void updateSummaryCards(BigDecimal totalIn, long countIn) {
        lblTotalRevenue.setText(String.format("R$ %.2f", totalIn));
        double avg = countIn > 0 ? totalIn.doubleValue() / countIn : 0.0;
        lblAverageTicket.setText(String.format("R$ %.2f", avg));
        lblTotalCustomers.setText(String.valueOf(countIn));
    }

    private void updateLineChart(List<Transaction> incomes) {
        chartRevenue.getData().clear();
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Receita Diária");

        Map<LocalDate, Double> dailySum = incomes.stream()
            .collect(Collectors.groupingBy(
                t -> t.getDate().toLocalDate(), 
                Collectors.summingDouble(t -> t.getAmount().doubleValue())
            ));

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM");
        
        dailySum.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> revenueSeries.getData().add(new XYChart.Data<>(entry.getKey().format(format), entry.getValue())));

        chartRevenue.getData().add(revenueSeries);
    }

    private List<ServiceMetric> generateServiceMetrics(List<Transaction> incomes) {
        Map<String, List<Transaction>> groupedByService = incomes.stream()
            .filter(t -> t.getDescription() != null && t.getDescription().getValue() != null)
            .collect(Collectors.groupingBy(t -> t.getDescription().getValue()));

        List<ServiceMetric> metrics = new ArrayList<>();
        
        for (Map.Entry<String, List<Transaction>> entry : groupedByService.entrySet()) {
            int qtd = entry.getValue().size();
            BigDecimal totalRev = entry.getValue().stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            metrics.add(new ServiceMetric(entry.getKey(), qtd, totalRev));
        }
        
        return metrics;
    }

    private void updateBarChart(List<ServiceMetric> metrics) {
        chartServices.getData().clear();
        XYChart.Series<String, Number> serviceSeries = new XYChart.Series<>();
        serviceSeries.setName("Faturamento (R$)");

        metrics.stream()
            .sorted((m1, m2) -> m2.getTotalRevenue().compareTo(m1.getTotalRevenue()))
            .limit(5)
            .forEach(metric -> serviceSeries.getData().add(new XYChart.Data<>(metric.getName(), metric.getTotalRevenue().doubleValue())));

        chartServices.getData().add(serviceSeries);
    }

    private void updateServiceRankingTable(List<ServiceMetric> metrics) {
        metrics.sort((m1, m2) -> m2.getTotalRevenue().compareTo(m1.getTotalRevenue()));
        tableServiceRanking.setItems(FXCollections.observableArrayList(metrics));
    }

    private void applyStyling() {
        javafx.application.Platform.runLater(() -> {
            chartRevenue.lookupAll(".chart-series-line").forEach(line -> line.setStyle("-fx-stroke: #EF233C;"));
            chartRevenue.lookupAll(".chart-line-symbol").forEach(symbol -> symbol.setStyle("-fx-background-color: #EF233C, white;"));

            for (XYChart.Series<String, Number> series : chartServices.getData()) {
                for (XYChart.Data<String, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-bar-fill: #EF233C;");
                    }
                }
            }
            chartServices.lookupAll(".chart-legend-item-symbol").forEach(symbol -> symbol.setStyle("-fx-background-color: #EF233C;"));
        });
    }

    @FXML
    public void handleExportPDF(ActionEvent event) {
        File file = promptSaveLocation();
        if (file != null) {
            try {
                generatePDF(file);
                showNotification("Sucesso", "Relatório exportado em PDF com sucesso!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                showNotification("Erro", "Não foi possível exportar o PDF: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private File promptSaveLocation() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo PDF", "*.pdf"));
        fileChooser.setInitialFileName("Relatorio_NailManager.pdf");
        return fileChooser.showSaveDialog(reportContainer.getScene().getWindow());
    }

    private void generatePDF(File file) throws Exception {
        WritableImage snapshot = reportContainer.snapshot(new javafx.scene.SnapshotParameters(), null);
        BufferedImage bImage = SwingFXUtils.fromFXImage(snapshot, null);

        try (PDDocument doc = new PDDocument()) {
            PDRectangle pageSize = new PDRectangle(bImage.getWidth(), bImage.getHeight());
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);

            PDImageXObject pdImage = LosslessFactory.createFromImage(doc, bImage);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            doc.save(file);
        }
    }

    private void showNotification(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ServiceMetric {
        private final String name;
        private final int quantity;
        private final BigDecimal totalRevenue;

        public ServiceMetric(String name, int quantity, BigDecimal totalRevenue) {
            this.name = name;
            this.quantity = quantity;
            this.totalRevenue = totalRevenue;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }
}