package application.usecase.service;

import domain.entity.Service;
import domain.repository.ServiceRepositoryInterface;
import domain.valueobject.Description;
import domain.valueobject.Name;

import java.math.BigDecimal;

public class UpdateServiceUseCase 
{

    private final ServiceRepositoryInterface repository;

    public UpdateServiceUseCase(ServiceRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    public void execute(Long id, String newNameText, String newDescriptionText, BigDecimal newPrice) 
    {
        Service service = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found."));

        if (!service.isActive()) 
        {
            throw new IllegalStateException("Is impossible to update an inactive service. Reactivate it first.");
        }

        if (newNameText != null && !newNameText.isBlank()) 
        {
            Name newName = new Name(newNameText);
            
            if (!service.getName().equals(newName)) 
            {
                repository.findByName(newName).ifPresent(other -> {
                    throw new IllegalArgumentException("There is already another service registered with this name.");
                });

                service.updateName(newName);
            }
        }

        if (newDescriptionText != null && !newDescriptionText.isBlank()) 
        { 
            service.updateDescription(new Description(newDescriptionText)); 
        }

        if (newPrice != null) 
        { 
            service.updatePrice(newPrice); 
        }

        repository.save(service);
    }
}