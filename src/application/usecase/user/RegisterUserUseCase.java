package application.usecase.user;

import domain.entity.User;
import domain.repository.UserRepositoryInterface;
import domain.service.PasswordHasher;
import domain.valueobject.Email;
import domain.valueobject.Name;

public class RegisterUserUseCase 
{

    private final UserRepositoryInterface repository;
    private final PasswordHasher hasher;

    public RegisterUserUseCase(UserRepositoryInterface repository, PasswordHasher hasher) 
    {
        this.repository = repository;
        this.hasher = hasher;
    }

    public User execute(String nameText, String emailText, String rawPassword) 
    {
        Name userName = new Name(nameText);
        Email userEmail = new Email(emailText);

        if (rawPassword == null || rawPassword.length() < 8) 
            throw new IllegalArgumentException("Password must be at least 8 characters long.");

        if (repository.findByEmail(userEmail).isPresent()) 
            throw new IllegalArgumentException("There is already a user registered with this email address.");

        String passwordHash = hasher.hash(rawPassword);
        
        User user = new User(userName, userEmail, passwordHash);
        
        repository.save(user);
        
        return user;
    }
}