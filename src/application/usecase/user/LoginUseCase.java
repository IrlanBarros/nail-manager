package application.usecase.user;

import domain.entity.User;
import domain.repository.UserRepositoryInterface;
import domain.service.PasswordHasher;
import domain.valueobject.Email;

public class LoginUseCase 
{

    private final UserRepositoryInterface repository;
    private final PasswordHasher hasher;

    public LoginUseCase(UserRepositoryInterface repository, PasswordHasher hasher) 
    {
        this.repository = repository;
        this.hasher = hasher;
    }

    public User execute(String emailText, String rawPassword) 
    {
        Email email = new Email(emailText);

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!hasher.check(rawPassword, user.getPasswordHash())) 
        {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }
}