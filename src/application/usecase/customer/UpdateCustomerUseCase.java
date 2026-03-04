package application.usecase.customer;

import domain.entity.Customer;
import domain.repository.CustomerRepositoryInterface;
import domain.valueobject.Email;
import domain.valueobject.Name;
import domain.valueobject.Phone;

public class UpdateCustomerUseCase 
{

    private final CustomerRepositoryInterface repository;

    public UpdateCustomerUseCase(CustomerRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    public void execute(Long id, String newName, String newEmail, String newPhone) 
    {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found."));

        if (!customer.isActive()) 
        {
            throw new IllegalStateException("Is impossible to update an inactive customer. Reactivate them first.");
        }

        if (newName != null && !newName.isBlank()) 
        {
            customer.updateName(new Name(newName));
        }

        if (newEmail != null && !newEmail.isBlank()) 
        {
            Email emailVo = new Email(newEmail);
            
            if (!customer.getEmail().equals(emailVo)) 
            {
                repository.findByEmail(emailVo).ifPresent(other -> {
                    throw new IllegalArgumentException("This email address is already in use by another customer.");
                });

                customer.updateEmail(emailVo);
            }
        }

        if (newPhone != null && !newPhone.isBlank()) 
        {
            Phone phoneVo = new Phone(newPhone);
            
            if (!customer.getPhone().equals(phoneVo)) 
            {
                repository.findByPhone(phoneVo).ifPresent(other -> {
                    throw new IllegalArgumentException("This phone is already in use by another customer.");
                });

                customer.updatePhone(phoneVo);
            }
        }

        repository.save(customer);
    }
}