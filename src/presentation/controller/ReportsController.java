package presentation.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.TableColumn;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableView;

public class ReportsController implements Initializable {

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblAverageTicket;
    @FXML private Label lblTotalClients;
    @FXML private ComboBox<String> comboPeriod;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;
    @FXML private LineChart<String, Number> chartRevenue;
    @FXML private BarChart<String, Number> chartServices;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    // LINE CHART 
    // 1. Line Chart Data
    XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
    revenueSeries.setName("Receita");
    revenueSeries.getData().add(new XYChart.Data<>("Semana 1", 1200));
    revenueSeries.getData().add(new XYChart.Data<>("Semana 2", 1800));
    revenueSeries.getData().add(new XYChart.Data<>("Semana 3", 1500));
    revenueSeries.getData().add(new XYChart.Data<>("Semana 4", 2100));
    chartRevenue.getData().add(revenueSeries);

    // Bar Chart Data
    XYChart.Series<String, Number> serviceSeries = new XYChart.Series<>();
    serviceSeries.setName("Quantidade");
    serviceSeries.getData().add(new XYChart.Data<>("Pé e Mão", 45));
    serviceSeries.getData().add(new XYChart.Data<>("Gel", 30));
    serviceSeries.getData().add(new XYChart.Data<>("Fibra", 15));
    chartServices.getData().add(serviceSeries);

    
    javafx.application.Platform.runLater(() -> {
        
    
        chartRevenue.lookupAll(".chart-series-line").forEach(line -> 
            line.setStyle("-fx-stroke: #EF233C;"));
        
        chartRevenue.lookupAll(".chart-line-symbol").forEach(symbol -> 
            symbol.setStyle("-fx-background-color: #EF233C, white;"));

        // Bar colors
        for (XYChart.Data<String, Number> data : serviceSeries.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-bar-fill: #EF233C;");
            }
        }

        
        chartRevenue.lookupAll(".chart-legend-item-symbol").forEach(s -> 
            s.setStyle("-fx-background-color: #EF233C, white;"));
            
        chartServices.lookupAll(".chart-legend-item-symbol").forEach(s -> 
            s.setStyle("-fx-background-color: #EF233C;"));
    });
  }
}