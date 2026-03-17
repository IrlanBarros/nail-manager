package application.usecase.appointment;

import domain.entity.Appointment;
import domain.repository.AppointmentRepositoryInterface;

import java.util.List;

public class GetAllAppointmentsUseCase {
    
    private final AppointmentRepositoryInterface appointmentRepository;

    public GetAllAppointmentsUseCase(AppointmentRepositoryInterface appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Busca todos os agendamentos do sistema.
     * Passamos 'null' para o seu repositório não aplicar o filtro de ativo/inativo,
     * trazendo assim tanto os SCHEDULED quanto os FINISHED.
     */
    public List<Appointment> execute() {
        return appointmentRepository.findAll(null);
    }
}