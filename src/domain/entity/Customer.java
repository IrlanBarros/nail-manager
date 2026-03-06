package domain.entity;

import domain.valueobject.Name;
import domain.valueobject.Email;
import domain.valueobject.Phone;
import java.util.Objects;

public class Customer 
{

    private final Long id;
    private Name name;
    private Email email;
    private Phone phone;
    private int totalAppointments;
    private boolean active;

    public Customer(Name name, Phone phone, Email email) 
    {
        this(null, name, phone, email, 0, true);
    }

    public Customer
    (
        Long id, Name name, Phone phone, 
        Email email, int totalAppointments, Boolean active
    ) {
        validateCustomer(name, email, phone);

        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.totalAppointments = totalAppointments;
        this.active = active;
    }

    public void registerAppointment() 
    {
        validateActiveState(); 
        this.totalAppointments++;
    }

    public void deactivate() 
    {
        if (!this.active) 
            throw new IllegalStateException("Customer is already inactive.");

        this.active = false;
    }

    public void activate() 
    {
        if (this.active) 
            throw new IllegalStateException("Customer is already active.");

        this.active = true;
    }

    private void validateCustomer(Name name, Email email, Phone phone)
    {
        if (name == null) 
            throw new IllegalArgumentException("Name cannot be null.");

        if (phone == null) 
            throw new IllegalArgumentException("Phone cannot be null.");

        if (email == null) 
            throw new IllegalArgumentException("Email cannot be null.");
    }
    
    public void updateName(Name newName) 
    {
        if (newName == null) 
            throw new IllegalArgumentException("Name cannot be null.");
        
        this.name = newName;
    }

    public void updatePhone(Phone newPhone) 
    {
        if (newPhone == null) 
            throw new IllegalArgumentException("Phone cannot be null.");
        
        this.phone = newPhone;
    }
    
    public void updateEmail(Email newEmail) 
    {
        if (newEmail == null) 
            throw new IllegalArgumentException("Email cannot be null.");

        this.email = newEmail;
    }
    
    private void validateActiveState() 
    {
        if (!this.active) {
            throw new IllegalStateException("Cannot modify an inactive customer.");
        }
    }
    
    public boolean isActive() { return active; }

    public Long getId() { return id; }

    public Name getName() { return name; }

    public Phone getPhone() { return phone; }

    public Email getEmail() { return email; }

    public int getTotalAppointments() { return totalAppointments; }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;

        Customer customer = (Customer) o;

        if (this.id == null) return false; 

        return Objects.equals(this.id, customer.getId());
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}