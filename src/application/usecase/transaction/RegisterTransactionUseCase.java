package application.usecase.transaction;

import domain.entity.Transaction;
import domain.enums.TransactionType;
import domain.repository.TransactionRepositoryInterface;
import domain.repository.AppointmentRepositoryInterface; 
import domain.valueobject.Description;

import java.math.BigDecimal;

public class RegisterTransactionUseCase 
{

    private final TransactionRepositoryInterface transactionRepository;
    private final AppointmentRepositoryInterface appointmentRepository;

    public RegisterTransactionUseCase
    (
        TransactionRepositoryInterface transactionRepository,
        AppointmentRepositoryInterface appointmentRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public Transaction execute
    (
        TransactionType type, 
        BigDecimal amount, 
        String descriptionText, 
        Long appointmentId
    ) {
        Description description = new Description(descriptionText);

        if (appointmentId != null) 
        {
            appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Linked appointment not found."));
            
            if (type == TransactionType.EXPENSE) 
            {
                throw new IllegalArgumentException("Is not permitted to record an expense linked to a appointment.");
            }
        }

        Transaction transaction = new Transaction(type, amount, description, appointmentId);
        
        transactionRepository.save(transaction);
        
        return transaction;
    }
}