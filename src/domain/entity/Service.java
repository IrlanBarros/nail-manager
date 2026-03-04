package domain.entity;

import domain.valueobject.Name;
import domain.valueobject.Description;

import java.math.BigDecimal;
import java.util.Objects;

public class Service 
{

    private final Long id;
    private Name name;
    private Description description;
    private BigDecimal price;
    private boolean active;

    public Service(Name name, Description description, BigDecimal price) 
    {
        this(null, name, description, price, true);
    }

    public Service(Long id, Name name, Description description, BigDecimal price, boolean active) 
    {
        validateService(name, description, price);

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = active;
    }

    public void deactivate() 
    {
        if (!this.active) 
        {
            throw new IllegalStateException("This service is currently inactive.");
        }

        this.active = false;
    }

    public void reactivate() 
    {
        if (this.active) 
        {
            throw new IllegalStateException("This service is already active.");
        }

        this.active = true;
    }

    private void validateService(Name name, Description description, BigDecimal price)
    {
        if (name == null) 
            throw new IllegalArgumentException("Name cannot be null.");

        if (description == null) 
            throw new IllegalArgumentException("Description cannot be null.");
        
        validatePrice(price);
    }

    public void updateName(Name newName) 
    {
        validateActiveState();

        if (newName == null) 
            throw new IllegalArgumentException("Name cannot be null.");
        
        this.name = newName;
    }

    public void updateDescription(Description newDescription) 
    {
        validateActiveState();

        if (newDescription == null) 
            throw new IllegalArgumentException("Description cannot be null.");
        
        this.description = newDescription;
    }

    public void updatePrice(BigDecimal newPrice) 
    {
        validateActiveState();

        validatePrice(newPrice);
        this.price = newPrice;
    }

    private void validateActiveState() 
    {
        if (!this.active) 
        {
            throw new IllegalStateException("Cannot modify an inactive service.");
        }
    }

    private void validatePrice(BigDecimal price) 
    {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) 
        {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
    }

    public boolean isActive() { return active; }
    
    public Long getId() { return id; }

    public Name getName() { return name; }

    public Description getDescription() { return description; }

    public BigDecimal getPrice() { return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;

        Service service = (Service) o;

        if (this.id == null) return false;

        return Objects.equals(this.id, service.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}