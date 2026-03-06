package domain.entity;

import domain.valueobject.Name;
import domain.valueobject.Email;
import domain.valueobject.Phone;
import java.util.Objects;
import java.util.Optional;

public class User 
{

    private final Long id;
    private Name name;
    private Phone phone;
    private Email email;
    private String passwordHash;

    public User(Name name, Email email, String passwordHash) 
    {
        this(null, name, null, email, passwordHash); 
    }

    public User(Name name, Phone phone, Email email, String passwordHash) 
    {
        this(null, name, phone, email, passwordHash);
    }

    public User(Long id, Name name, Phone phone, Email email, String passwordHash) 
    {
        validateUser(name, email, passwordHash);

        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public void changePassword(String newPasswordHash) 
    {
        validatePassword(newPasswordHash);
        this.passwordHash = newPasswordHash;
    }

    public void updateName(Name newName) 
    {
        if (newName == null) 
            throw new IllegalArgumentException("Name cannot be null.");

        this.name = newName;
    }

    private void validateUser(Name name, Email email, String passwordHash)
    {
        if (name == null) throw new IllegalArgumentException("Name cannot be null.");
        if (email == null) throw new IllegalArgumentException("Email cannot be null.");
        validatePassword(passwordHash);
    }

    public void updatePhone(Phone newPhone) { this.phone = newPhone; }

    public void updateEmail(Email newEmail) 
    {
        if (newEmail == null) 
            throw new IllegalArgumentException("Email cannot be null.");

        this.email = newEmail;
    }

    private void validatePassword(String passwordHash) 
    {
        if (passwordHash == null || passwordHash.isBlank()) 
            throw new IllegalArgumentException("Password cannot be empty.");
    }

    public Long getId() { return id; }

    public Name getName() { return name; }

    public Optional<Phone> getPhone() { return Optional.ofNullable(phone); }

    public Email getEmail() { return email; }

    public String getPasswordHash() { return passwordHash; }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User that = (User) o;

        if (this.id == null) return false;

        return Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}