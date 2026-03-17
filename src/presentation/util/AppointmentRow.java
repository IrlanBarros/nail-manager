package presentation.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppointmentRow {
    private final StringProperty cliente;
    private final StringProperty horario;
    private final StringProperty servico;

    public AppointmentRow(String cliente, String horario, String servico) {
        this.cliente = new SimpleStringProperty(cliente);
        this.horario = new SimpleStringProperty(horario);
        this.servico = new SimpleStringProperty(servico);
    }

    public StringProperty clienteProperty() { return cliente; }
    public StringProperty horarioProperty() { return horario; }
    public StringProperty servicoProperty() { return servico; }
}