package domain.repository;

import domain.entity.Service;
import domain.valueobject.Name;
import java.util.List;
import java.util.Optional;

public interface ServiceRepositoryInterface 
{
    /**
     * Persists or updates a service in the data store.
     * @param service The service entity to be saved.
     */
    void save(Service service);

    /**
     * Retrieves a service by its unique identifier.
     * @param id The ID of the service.
     * @return An Optional containing the service if found, or empty otherwise.
     */
    Optional<Service> findById(Long id);

    /**
     * Finds a service by its unique name.
     * Useful for preventing duplicate services in the catalog.
     * @param name The name Value Object.
     * @return An Optional containing the service if found.
     */
    Optional<Service> findByName(Name name);

    /**
     * Lists services based on their status in the catalog.
     * @param activeFilter true: active only, false: inactive only, null: all services.
     * @return A list of services sorted alphabetically by name.
     */
    List<Service> findAll(Boolean activeFilter);
}