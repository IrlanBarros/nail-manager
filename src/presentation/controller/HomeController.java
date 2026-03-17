package presentation.controller;

import application.usecase.appointment.CancelAppointmentUseCase;
import application.usecase.appointment.CreateAppointmentUseCase;
import application.usecase.appointment.FinishAppointmentUseCase;
import application.usecase.appointment.GetAllAppointmentsUseCase;
import application.usecase.customer.ListCustomersUseCase;
import application.usecase.service.ListServicesUseCase;
import domain.entity.Appointment;
import domain.entity.User;
import domain.repository.CustomerRepositoryInterface;
import domain.repository.ServiceRepositoryInterface;
import infrastructure.persistence.sqlite.SqliteCustomerRepository;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.TableCell;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class HomeController {

    private final User loggedUser;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;

    // Colunas de Ação (Verifique se os IDs no FXML batem com esses nomes)
    @FXML private TableColumn<Appointment, Void> colAcaoAgendamento;

    // --- Botões das Abas ---
    @FXML private Button btnTabTodos;
    @FXML private Button btnTabAgendamentos;
    @FXML private Button btnTabAtendimentos;

    // --- Tabela 1: Serve para "Todos" e "Agendamentos" ---
    @FXML private TableView<Appointment> tabelaAgendamentos;
    @FXML private TableColumn<Appointment, String> colAgendamentoCliente;
    @FXML private TableColumn<Appointment, String> colAgendamentoData;
    @FXML private TableColumn<Appointment, String> colAgendamentoServicos;
    @FXML private TableColumn<Appointment, String> colAgendamentoStatus;

    // --- Tabela 2: Serve apenas para "Atendimentos" ---
    @FXML private TableView<Appointment> tabelaAtendimentos;
    @FXML private TableColumn<Appointment, String> colAtendimentoCliente;
    @FXML private TableColumn<Appointment, String> colAtendimentoData;
    @FXML private TableColumn<Appointment, String> colAtendimentoServicos;
    @FXML private TableColumn<Appointment, String> colAtendimentoValor; 
    @FXML private TableColumn<Appointment, String> colAtendimentoPagamento;

    // --- Listas em memória ---
    private final ObservableList<Appointment> listaTodos = FXCollections.observableArrayList();
    private final ObservableList<Appointment> listaAgendados = FXCollections.observableArrayList();
    private final ObservableList<Appointment> listaAtendidos = FXCollections.observableArrayList();

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final FinishAppointmentUseCase finishAppointmentUseCase;
    private final GetAllAppointmentsUseCase getAllAppointmentsUseCase;
    private final CustomerRepositoryInterface customerRepository; // Adicione este campo
    private final ServiceRepositoryInterface serviceRepository;

    public HomeController(
        User loggedUser, 
        GetAllAppointmentsUseCase getAllAppointmentsUseCase,
        CreateAppointmentUseCase createAppointmentUseCase,
        FinishAppointmentUseCase finishAppointmentUseCase,
        CustomerRepositoryInterface customerRepository,
        CancelAppointmentUseCase cancelAppointmentUseCase,
        ServiceRepositoryInterface serviceRepository
    ) {
        this.loggedUser = loggedUser;
        this.getAllAppointmentsUseCase = getAllAppointmentsUseCase;
        this.createAppointmentUseCase = createAppointmentUseCase;
        this.finishAppointmentUseCase = finishAppointmentUseCase;
        this.customerRepository = customerRepository;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
        this.serviceRepository = serviceRepository;
    }

    @FXML
    public void initialize() {
        System.out.println("A configurar colunas da tabela...");
        configurarColunas(); 
        configurarColunaCancelamento(colAcaoAgendamento);
        
        carregarTodosOsDados();
        showTodos(null); 
    }

    private void configurarColunaCancelamento(TableColumn<Appointment, Void> column) {
        column.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Appointment, Void> call(final TableColumn<Appointment, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Cancel");
                    {
                        btn.getStyleClass().add("action-btn");
                        btn.setStyle("-fx-background-color: #e74c3c;");
                        btn.setOnAction(event -> {
                            Appointment appointment = getTableView().getItems().get(getIndex());
                            handleCancelAppointment(appointment);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Appointment app = getTableView().getItems().get(getIndex());
                            // ESCONDE o botão se já estiver cancelado ou finalizado
                            if (app.getStatus().name().equals("CANCELLED") || app.getStatus().name().equals("FINISHED")) {
                                setGraphic(null);
                            } else {
                                setGraphic(btn);
                            }
                        }
                    }
                };
            }
        });
    }

    private void handleCancelAppointment(Appointment appointment) {
        try {
            // Chama o UseCase enviado
            cancelAppointmentUseCase.execute(appointment.getId());
            
            // O segredo da atualização automática:
            // Como o carregarTodosOsDados() filtra apenas por SCHEDULED e IN_PROGRESS,
            // ao mudar para CANCELLED, o item desaparece na próxima leitura.
            carregarTodosOsDados();
            
            mostrarAlerta(Alert.AlertType.INFORMATION, "Success", "Appointment cancelled successfully.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void configurarColunas() {
        // ==========================================
        // TABELA 1: AGENDAMENTOS (E "TODOS")
        // ==========================================
        
        colAgendamentoCliente.setCellValueFactory(cellData -> {
            // Nota: Se o Name for um Value Object com .getValue(), ajuste aqui:
            String nomeCliente = cellData.getValue().getCustomer().getName().getValue();
            return new SimpleStringProperty(nomeCliente);
        });

        colAgendamentoStatus.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().name())
        );

        colAgendamentoData.setCellValueFactory(cellData -> {
            LocalDateTime dataHora = cellData.getValue().getDateTime();
            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
            return new SimpleStringProperty(dataHora.format(formatador));
        });

        colAgendamentoServicos.setCellValueFactory(cellData -> {
            String servicos = cellData.getValue().getServices().stream()
                    // Extraindo a String de dentro do Value Object Name:
                    .map(servico -> servico.getName().getValue()) 
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(servicos);
        });

// Dentro do seu configurarColunas()
        colAgendamentoStatus.setCellFactory(column -> new TableCell<Appointment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Se for cancelado, pintamos de vermelho
                    if (item.equals("CANCELLED")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (item.equals("FINISHED")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: white;");
                    }
                }
            }
        });

        // ==========================================
        // TABELA 2: ATENDIMENTOS (FINALIZADOS)
        // ==========================================
        
        colAtendimentoCliente.setCellValueFactory(colAgendamentoCliente.getCellValueFactory());
        colAtendimentoData.setCellValueFactory(colAgendamentoData.getCellValueFactory());
        colAtendimentoServicos.setCellValueFactory(colAgendamentoServicos.getCellValueFactory());

        colAtendimentoValor.setCellValueFactory(cellData -> {
            BigDecimal total = cellData.getValue().getTotalPrice();
            return new SimpleStringProperty(String.format("R$ %.2f", total));
        });

        colAtendimentoPagamento.setCellValueFactory(cellData -> {
            // Valor provisório. Atualize quando a entidade tiver a forma de pagamento real.
            return new SimpleStringProperty("Pix"); 
        });
    }

    private void carregarTodosOsDados() {
        List<Appointment> todosOsDados = getAllAppointmentsUseCase.execute();

        listaTodos.clear();
        listaAgendados.clear();
        listaAtendidos.clear();

        // 1. Adiciona absolutamente TODOS à lista principal (inclusive CANCELLED)
        listaTodos.addAll(todosOsDados);

        // 2. Filtra para as abas específicas apenas o que for pertinente
        listaAgendados.addAll(todosOsDados.stream()
                .filter(a -> a.getStatus().name().equals("SCHEDULED")) 
                .collect(Collectors.toList()));

        listaAtendidos.addAll(todosOsDados.stream()
                .filter(a -> a.getStatus().name().equals("FINISHED"))
                .collect(Collectors.toList()));
    }

    // --- Lógica de Troca de Abas ---

    @FXML
    public void showTodos(ActionEvent event) {
        tabelaAgendamentos.setItems(listaTodos); 
        tabelaAgendamentos.setVisible(true);
        tabelaAtendimentos.setVisible(false);
        atualizarEstiloBotoes(btnTabTodos);
    }

    @FXML
    public void showAgendamentos(ActionEvent event) {
        tabelaAgendamentos.setItems(listaAgendados); 
        tabelaAgendamentos.setVisible(true);
        tabelaAtendimentos.setVisible(false);
        atualizarEstiloBotoes(btnTabAgendamentos);
    }

    @FXML
    public void showAtendimentos(ActionEvent event) {
        tabelaAtendimentos.setItems(listaAtendidos); 
        tabelaAgendamentos.setVisible(false);
        tabelaAtendimentos.setVisible(true);
        atualizarEstiloBotoes(btnTabAtendimentos);
    }

    private void atualizarEstiloBotoes(Button botaoAtivo) {
        String estiloInativo = "-fx-text-fill: white; -fx-font-weight: normal;";
        String estiloAtivo = "-fx-text-fill: #3498db; -fx-font-weight: bold;";

        if (btnTabTodos != null) btnTabTodos.setStyle(estiloInativo);
        if (btnTabAgendamentos != null) btnTabAgendamentos.setStyle(estiloInativo);
        if (btnTabAtendimentos != null) btnTabAtendimentos.setStyle(estiloInativo);

        if (botaoAtivo != null) {
            botaoAtivo.setStyle(estiloAtivo);
        }
    }

    // --- Lógica dos Botões de Ação Inferiores ---

    @FXML
    public void handleNewAppointment(ActionEvent event) {
        try {
            // 1. Verifique se o caminho está idêntico ao nome do arquivo (case-sensitive)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/presentation/view/AppointmentForm.fxml"));
            
            loader.setControllerFactory(controllerClass -> {
                if (controllerClass == AppointmentFormController.class) {
                    // Agora 'this.customerRepository' possui a instância vinda do construtor
                    ListCustomersUseCase listCustomers = new ListCustomersUseCase(this.customerRepository);
                    ListServicesUseCase listServices = new ListServicesUseCase(this.serviceRepository);
                    return new AppointmentFormController(createAppointmentUseCase, listCustomers, listServices);
                }
                return null;
            });

            Parent root = loader.load();
            Stage modalStage = new Stage();
            modalStage.setTitle("New Appointment");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            
            // Define o dono do modal para centralizar corretamente
            modalStage.initOwner(btnTabTodos.getScene().getWindow());
            
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            
            modalStage.showAndWait();

            // Refresh automático após fechar
            carregarTodosOsDados();

        } catch (Exception e) {
            // IMPRIMA O ERRO NO CONSOLE para saber a causa real (ex: FXML path, NullPointer)
            e.printStackTrace(); 
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Could not open appointment form: " + e.getMessage());
        }
    }

    @FXML
    public void handleRegisterAppointment(ActionEvent event) {
        // 1. Pega o item que o usuário selecionou na tabela que está visível
        Appointment agendamentoSelecionado = tabelaAgendamentos.isVisible() 
            ? tabelaAgendamentos.getSelectionModel().getSelectedItem()
            : tabelaAtendimentos.getSelectionModel().getSelectedItem();

        // 2. Verifica se ele realmente clicou em alguma linha
        if (agendamentoSelecionado == null) {
            mostrarAlerta(AlertType.WARNING, "Aviso", "Por favor, selecione um agendamento na lista primeiro.");
            return;
        }

        try {
            // 3. Tenta finalizar o agendamento no banco
            BigDecimal valorFinal = finishAppointmentUseCase.execute(agendamentoSelecionado.getId());
            
            // 4. Se deu certo, atualiza as tabelas!
            carregarTodosOsDados();
            
            mostrarAlerta(AlertType.INFORMATION, "Atendimento Finalizado", 
                "Atendimento registrado com sucesso!\nValor Total: R$ " + valorFinal);

        } catch (IllegalStateException | IllegalArgumentException e) {
            // AQUI CAPTURAMOS AS REGRAS DA SUA ENTIDADE (Ex: não está IN_PROGRESS, sem serviços)
            mostrarAlerta(AlertType.ERROR, "Ação não permitida", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(AlertType.ERROR, "Erro no Sistema", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método utilitário para mostrar pop-ups bonitos na tela
    private void mostrarAlerta(AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        // Opcional: Adicionar estilo dark mode no alerta se você quiser depois
        alert.showAndWait();
    }
}