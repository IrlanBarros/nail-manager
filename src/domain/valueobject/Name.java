package domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public class Name 
{
    private final String value;

    // Names cannot contain special characters or numbers.
    private static final Pattern PATTERN = Pattern.compile("^[A-Za-zÀ-ÖØ-öø-ÿ\\s]+$");

    public Name(String value) 
    {
        if (value == null || value.isBlank()) 
            throw new IllegalArgumentException("The name cannot be empty.");
    
        String trimmedValue = value.trim();
        
        if (!PATTERN.matcher(trimmedValue).matches()) 
            throw new IllegalArgumentException("The name can only contain letters and spaces, without numbers or special characters.");
        
        this.value = trimmedValue;
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Name)) return false;
        Name name = (Name) o;
        return Objects.equals(value, name.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
}