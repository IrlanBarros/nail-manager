package presentation.controller;
import presentation.controller.FinanceRecord;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.util.ResourceBundle;

public class FinanceController implements Initializable {

    @FXML private TableView<FinanceRecord> tableFinance;
    @FXML private TableColumn<FinanceRecord, String> colType;
    @FXML private TableColumn<FinanceRecord, String> colData;
    @FXML private TableColumn<FinanceRecord, String> colDescription;
    @FXML private TableColumn<FinanceRecord, String> colOrigin;
    @FXML private TableColumn<FinanceRecord, Double> colValue;

    @FXML private Label lblTotalBalance, lblTotalIncomes, lblTotalExpenses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Bind columns to the FinanceRecord model
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));

        // 2. Customize the "Type" column to show the colored circle
        colType.setCellFactory(column -> new TableCell<FinanceRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    //  Create the circle 
                    Circle circle = new Circle(5); 
                    
                    
                    Label label = new Label(item);
                    label.setStyle("-fx-font-weight: bold; -fx-text-fill: #2b2b2b;");

                    // Define colors based on "E" or "S" value
                    if (item.equals("E")) {
                        circle.setFill(Color.web("#4CAF50")); 
                    } else {
                        circle.setFill(Color.web("#EF233C")); 
                    }

                    //  Create the HBox to place the circle and letter side by side
                    HBox container = new HBox(10); 
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    container.getChildren().addAll(circle, label);

                  
                    setGraphic(container);
                    
                   
                    setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 0 0 0 10;");
                }
            }
        });

        // Dummy data according to your prototype
        ObservableList<FinanceRecord> data = FXCollections.observableArrayList(
            new FinanceRecord("E", "12/02", "Manicure Ana", "Atendimento", 35.00),
            new FinanceRecord("S", "13/02", "Compra esmalte", "Manual", 120.00),
            new FinanceRecord("E", "14/02", "Pedicure Maria", "Atendimento", 40.00)
        );

        tableFinance.setItems(data);
    }
}