package domain.repository;

import domain.entity.User;
import domain.valueobject.Email;
import domain.valueobject.Phone;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryInterface 
{
    /**
     * Persists or updates a system user in the data store.
     * @param user The user entity to be saved.
     */
    void save(User user);

    /**
     * Retrieves a user by its unique identifier.
     * @param id The ID of the user.
     * @return An Optional containing the user if found, or empty otherwise.
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by their unique email address.
     * Essential for authentication and preventing duplicate accounts.
     * @param email The email Value Object.
     * @return An Optional containing the user if found.
     */
    Optional<User> findByEmail(Email email);

    /**
     * Finds a user by their unique phone number.
     * @param phone The phone Value Object.
     * @return An Optional containing the user if found.
     */
    Optional<User> findByPhone(Phone phone);

    /**
     * Lists all registered users in the system.
     * @return A list containing all system users.
     */
    List<User> findAll();
}