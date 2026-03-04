package application.usecase.transaction;

import domain.entity.Transaction;
import domain.repository.TransactionRepositoryInterface;

public class CancelTransactionUseCase 
{

    private final TransactionRepositoryInterface repository;

    public CancelTransactionUseCase(TransactionRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    public void execute(Long transactionId) 
    {
        if (transactionId == null) 
        {
            throw new IllegalArgumentException("The transaction ID is required.");
        }

        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        transaction.cancel();

        repository.save(transaction);
    }
}