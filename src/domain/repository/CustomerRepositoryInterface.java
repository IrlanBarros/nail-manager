package domain.repository;

import domain.entity.Customer;
import domain.valueobject.Email;
import domain.valueobject.Phone;

import java.util.List;
import java.util.Optional;

public interface CustomerRepositoryInterface 
{
    /**
     * Persists or updates a customer in the data store.
     * @param customer The customer entity to be saved.
     */
    void save(Customer customer);

    /**
     * Retrieves a customer by its unique identifier.
     * @param id The ID of the customer.
     * @return An Optional containing the customer if found, or empty otherwise.
     */
    Optional<Customer> findById(Long id);

    /**
     * Finds a customer by their unique email address.
     * Useful for preventing duplicate registrations.
     * @param email The email Value Object.
     * @return An Optional containing the customer if found.
     */
    Optional<Customer> findByEmail(Email email);

    /**
     * Finds a customer by their unique phone number.
     * @param phone The phone Value Object.
     * @return An Optional containing the customer if found.
     */
    Optional<Customer> findByPhone(Phone phone);

    /**
     * Lists customers based on their account status.
     * @param activeFilter true: active only, false: inactive only, null: all customers.
     * @return A list of customers sorted alphabetically by name.
     */
    List<Customer> findAll(Boolean activeFilter);
}