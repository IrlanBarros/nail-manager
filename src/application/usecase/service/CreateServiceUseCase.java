package application.usecase.service;

import domain.entity.Service;
import domain.repository.ServiceRepositoryInterface;
import domain.valueobject.Description;
import domain.valueobject.Name;

import java.math.BigDecimal;
import java.util.Optional;

public class CreateServiceUseCase 
{

    private final ServiceRepositoryInterface repository;

    public CreateServiceUseCase(ServiceRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    public Service execute(String nameText, String descriptionText, BigDecimal price) 
    {
        Name name = new Name(nameText);
        Description description = new Description(descriptionText);

        Optional<Service> existingService = repository.findByName(name);

        if (existingService.isPresent()) 
            throw new IllegalArgumentException("There is already a service registered with this name in the catalog.");

        Service service = new Service(name, description, price);
        
        repository.save(service);
        
        return service;
    }
}