package domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email 
{
    private final String address;
    
    // Requires at least one character at the beginning, “@” is mandatory, and a domain name.
    private static final Pattern PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");

    public Email(String address) 
    {
        if (address == null || address.isBlank()) 
            throw new IllegalArgumentException("Email cannot be null.");

        if (!PATTERN.matcher(address).matches()) 
            throw new IllegalArgumentException("Invalid email format.");

        this.address = address;
    }

    public String getValue() { return address; }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email = (Email) o;
        return Objects.equals(address, email.address);
    }

    @Override
    public int hashCode() { return Objects.hash(address); }
}