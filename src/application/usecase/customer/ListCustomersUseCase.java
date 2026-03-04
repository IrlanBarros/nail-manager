package application.usecase.customer;

import domain.entity.Customer;
import domain.repository.CustomerRepositoryInterface;
import java.util.List;

public class ListCustomersUseCase 
{

    private final CustomerRepositoryInterface repository;

    public ListCustomersUseCase(CustomerRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    /**
     * @param activeFilter true (active only), false (inactive only), null (all)
     */
    public List<Customer> execute(Boolean activeFilter) 
    {
        return repository.findAll(activeFilter);
    }

    public List<Customer> execute() {
        return this.execute(true);
    }
}