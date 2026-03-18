package presentation.controller;

import application.usecase.appointment.CancelAppointmentUseCase;
import application.usecase.appointment.FinishAppointmentUseCase;
import application.usecase.appointment.GetAllAppointmentsUseCase;
import domain.entity.Appointment;
import domain.entity.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController {

    private final User loggedUser;
    private final Callback<Class<?>, Object> controllerFactory;
    
    @FXML private TableColumn<Appointment, Void> colAcaoAgendamento;

    @FXML private Button btnTabTodos;
    @FXML private Button btnTabAgendamentos;
    @FXML private Button btnTabAtendimentos;

    @FXML private TableView<Appointment> tabelaAgendamentos;
    @FXML private TableColumn<Appointment, String> colAgendamentoCliente;
    @FXML private TableColumn<Appointment, String> colAgendamentoData;
    @FXML private TableColumn<Appointment, String> colAgendamentoServicos;
    @FXML private TableColumn<Appointment, String> colAgendamentoStatus;

    @FXML private TableView<Appointment> tabelaAtendimentos;
    @FXML private TableColumn<Appointment, String> colAtendimentoCliente;
    @FXML private TableColumn<Appointment, String> colAtendimentoData;
    @FXML private TableColumn<Appointment, String> colAtendimentoServicos;
    @FXML private TableColumn<Appointment, String> colAtendimentoValor; 
    @FXML private TableColumn<Appointment, String> colAtendimentoPagamento;

    private final ObservableList<Appointment> listaTodos = FXCollections.observableArrayList();
    private final ObservableList<Appointment> listaAgendados = FXCollections.observableArrayList();
    private final ObservableList<Appointment> listaAtendidos = FXCollections.observableArrayList();

    private final GetAllAppointmentsUseCase getAllAppointmentsUseCase;
    private final FinishAppointmentUseCase finishAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;

    public HomeController(
        User loggedUser, 
        GetAllAppointmentsUseCase getAllAppointmentsUseCase,
        FinishAppointmentUseCase finishAppointmentUseCase,
        CancelAppointmentUseCase cancelAppointmentUseCase,
        Callback<Class<?>, Object> controllerFactory
    ) {
        this.loggedUser = loggedUser;
        this.getAllAppointmentsUseCase = getAllAppointmentsUseCase;
        this.finishAppointmentUseCase = finishAppointmentUseCase;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
        this.controllerFactory = controllerFactory;
    }

    @FXML
    public void initialize() {
        setupTables();
        loadAllData();
        showTodos(null); 
    }

    private void setupTables() {
        setupAppointmentColumns();
        setupAttendanceColumns();
        setupStatusColumnColoring();
        setupActionColumn();
    }

    private void setupAppointmentColumns() {
        colAgendamentoCliente.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCustomer().getName().getValue()));

        colAgendamentoStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().name()));

        colAgendamentoData.setCellValueFactory(cellData -> {
            LocalDateTime dataHora = cellData.getValue().getDateTime();
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            return new SimpleStringProperty(dataHora.format(formatador));
        });

        colAgendamentoServicos.setCellValueFactory(cellData -> {
            String servicos = cellData.getValue().getServices().stream()
                    .map(servico -> servico.getName().getValue()) 
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(servicos);
        });
    }

    private void setupAttendanceColumns() {
        colAtendimentoCliente.setCellValueFactory(colAgendamentoCliente.getCellValueFactory());
        colAtendimentoData.setCellValueFactory(colAgendamentoData.getCellValueFactory());
        colAtendimentoServicos.setCellValueFactory(colAgendamentoServicos.getCellValueFactory());

        colAtendimentoValor.setCellValueFactory(cellData -> {
            BigDecimal total = cellData.getValue().getTotalPrice();
            return new SimpleStringProperty(String.format("R$ %.2f", total));
        });

        colAtendimentoPagamento.setCellValueFactory(cellData -> new SimpleStringProperty("Pix"));
    }

    private void setupStatusColumnColoring() {
        colAgendamentoStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    applyStatusStyle(item);
                }
            }

            private void applyStatusStyle(String status) {
                if (status.equals("CANCELLED")) {
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else if (status.equals("FINISHED")) {
                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: black;");
                }
            }
        });
    }

    private void setupActionColumn() {
        colAcaoAgendamento.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Appointment, Void> call(final TableColumn<Appointment, Void> param) {
                return new TableCell<>() {
                    private final Button btn = createCancelButton();

                    private Button createCancelButton() {
                        Button button = new Button("Cancel");
                        button.getStyleClass().add("action-btn");
                        button.setStyle("-fx-background-color: #e74c3c;");
                        button.setOnAction(event -> handleCancelAppointment(getTableView().getItems().get(getIndex())));
                        return button;
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || isAppointmentFinishedOrCancelled()) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }

                    private boolean isAppointmentFinishedOrCancelled() {
                        Appointment app = getTableView().getItems().get(getIndex());
                        String status = app.getStatus().name();
                        return status.equals("CANCELLED") || status.equals("FINISHED");
                    }
                };
            }
        });
    }

    private void loadAllData() {
        List<Appointment> todosOsDados = getAllAppointmentsUseCase.execute();

        listaTodos.clear();
        listaAgendados.clear();
        listaAtendidos.clear();

        listaTodos.addAll(todosOsDados);
        
        listaAgendados.addAll(todosOsDados.stream()
                .filter(a -> a.getStatus().name().equals("SCHEDULED")) 
                .collect(Collectors.toList()));

        listaAtendidos.addAll(todosOsDados.stream()
                .filter(a -> a.getStatus().name().equals("FINISHED"))
                .collect(Collectors.toList()));
    }

    @FXML
    public void showTodos(ActionEvent event) {
        switchTab(tabelaAgendamentos, listaTodos, btnTabTodos);
    }

    @FXML
    public void showAgendamentos(ActionEvent event) {
        switchTab(tabelaAgendamentos, listaAgendados, btnTabAgendamentos);
    }

    @FXML
    public void showAtendimentos(ActionEvent event) {
        switchTab(tabelaAtendimentos, listaAtendidos, btnTabAtendimentos);
    }

    private void switchTab(TableView<Appointment> tableToShow, ObservableList<Appointment> listToBind, Button activeBtn) {
        tabelaAgendamentos.setVisible(tableToShow == tabelaAgendamentos);
        tabelaAtendimentos.setVisible(tableToShow == tabelaAtendimentos);
        
        tableToShow.setItems(listToBind);
        updateButtonStyles(activeBtn);
    }

    private void updateButtonStyles(Button botaoAtivo) {
        String estiloInativo = "-fx-text-fill: white; -fx-font-weight: normal;";
        String estiloAtivo = "-fx-text-fill: #3498db; -fx-font-weight: bold;";

        if (btnTabTodos != null) btnTabTodos.setStyle(estiloInativo);
        if (btnTabAgendamentos != null) btnTabAgendamentos.setStyle(estiloInativo);
        if (btnTabAtendimentos != null) btnTabAtendimentos.setStyle(estiloInativo);

        if (botaoAtivo != null) {
            botaoAtivo.setStyle(estiloAtivo);
        }
    }

    @FXML
    public void handleNewAppointment(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/components/AppointmentForm.fxml"));

            loader.setControllerFactory(controllerFactory);

            Parent root = loader.load();
            Stage modalStage = new Stage();
            modalStage.setTitle("Novo Agendamento");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(btnTabTodos.getScene().getWindow());
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            
            modalStage.showAndWait();

            loadAllData();

        } catch (Exception e) {
            e.printStackTrace(); 
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open appointment form: " + e.getMessage());
        }
    }

    @FXML
    public void handleRegisterAppointment(ActionEvent event) {
        Appointment agendamentoSelecionado = getSelectedAppointment();

        if (agendamentoSelecionado == null) {
            showAlert(AlertType.WARNING, "Aviso", "Por favor, selecione um agendamento na lista primeiro.");
            return;
        }

        try {
            BigDecimal valorFinal = finishAppointmentUseCase.execute(agendamentoSelecionado.getId());
            loadAllData();
            showAlert(AlertType.INFORMATION, "Atendimento Finalizado", "Atendimento registrado com sucesso!\nValor Total: R$ " + valorFinal);
        } catch (IllegalStateException | IllegalArgumentException e) {
            showAlert(AlertType.ERROR, "Ação não permitida", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erro no Sistema", "Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    private void handleCancelAppointment(Appointment appointment) {
        try {
            cancelAppointmentUseCase.execute(appointment.getId());
            loadAllData();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Appointment cancelled successfully.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private Appointment getSelectedAppointment() {
        if (tabelaAgendamentos.isVisible()) {
            return tabelaAgendamentos.getSelectionModel().getSelectedItem();
        }
        return tabelaAtendimentos.getSelectionModel().getSelectedItem();
    }

    private void showAlert(AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}