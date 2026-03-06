package application.usecase.service;

import domain.entity.Service;
import domain.enums.AppointmentStatus;
import domain.repository.ServiceRepositoryInterface;
import domain.repository.AppointmentRepositoryInterface;

public class DeleteServiceUseCase 
{

    private final ServiceRepositoryInterface serviceRepository;
    private final AppointmentRepositoryInterface appointmentRepository;

    public DeleteServiceUseCase
    (
        ServiceRepositoryInterface serviceRepository,
        AppointmentRepositoryInterface appointmentRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public void execute(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found."));

        boolean isBeingUsed = appointmentRepository.findAll(true).stream()
                .anyMatch(app -> (app.getStatus() == AppointmentStatus.SCHEDULED || app.getStatus() == AppointmentStatus.IN_PROGRESS)
                              && app.getServices().contains(service));

        if (isBeingUsed) 
            throw new IllegalStateException("Is impossible to deactivate a service that has pending or ongoing appointments.");

        service.deactivate();

        serviceRepository.save(service);
    }
}