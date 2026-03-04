package application.usecase.service;

import domain.entity.Service;
import domain.repository.ServiceRepositoryInterface;

import java.util.List;

public class ListServicesUseCase 
{

    private final ServiceRepositoryInterface repository;

    public ListServicesUseCase(ServiceRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    /**
     * List services based on status.
     * @param activeFilter true (active only), false (inactive only), null (all)
     * @return List of services sorted by name from the repository.
     */
    public List<Service> execute(Boolean activeFilter) 
    {
        return repository.findAll(activeFilter);
    }

    public List<Service> execute() {
        return this.execute(true);
    }
}