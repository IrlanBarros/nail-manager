package domain.valueobject;

import java.util.Objects;
import java.util.regex.Pattern;

public class Phone 
{
    private final String number;
    
    // Accepts +, numbers, spaces, parentheses, and hyphens. Between 7 and 15 digits.
    private static final Pattern PATTERN = Pattern.compile("^\\+?[0-9\\s\\-()]{7,15}$");

    public Phone(String number) 
    {
        if (number == null || number.isBlank()) 
            throw new IllegalArgumentException("The phone cannot be empty.");

        if (!PATTERN.matcher(number).matches()) 
            throw new IllegalArgumentException("Invalid phone format.");

        this.number = number;
    }

    public String getValue() { return number; }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Phone)) return false;
        Phone phone = (Phone) o;
        return Objects.equals(number, phone.number);
    }

    @Override
    public int hashCode() { return Objects.hash(number); }
}