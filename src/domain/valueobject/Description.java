package domain.valueobject;

import java.util.Objects;

public class Description 
{

    private final String value;

    public Description(String value) 
    {
        if (value == null || value.trim().isEmpty()) 
            throw new IllegalArgumentException("Description cannot be null.");
        
        if (value.length() > 500) 
            throw new IllegalArgumentException("Description cannot exceed 500 characters.");

        this.value = value.trim(); 
    }

    public String getValue() { return value; }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Description)) return false;
        Description that = (Description) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() { return Objects.hash(value); }
    
    @Override
    public String toString() { return value; }
}