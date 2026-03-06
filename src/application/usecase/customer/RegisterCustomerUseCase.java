package application.usecase.customer;

import domain.entity.Customer;
import domain.repository.CustomerRepositoryInterface;
import domain.valueobject.Email;
import domain.valueobject.Name;
import domain.valueobject.Phone;

public class RegisterCustomerUseCase 
{

    private final CustomerRepositoryInterface repository;

    public RegisterCustomerUseCase(CustomerRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    public Customer execute(String nameText, String phoneText, String emailText) 
    {
        Name name = new Name(nameText);
        Phone phone = new Phone(phoneText);
        Email email = new Email(emailText);

        if (repository.findByEmail(email).isPresent()) 
            throw new IllegalArgumentException("There is already a customer registered with this email address.");

        if (repository.findByPhone(phone).isPresent()) 
            throw new IllegalArgumentException("There is already a customer registered with this phone number.");

        Customer customer = new Customer(name, phone, email);

        repository.save(customer);

        return customer;
    }
}