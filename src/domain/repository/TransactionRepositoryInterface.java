package domain.repository;

import domain.entity.Transaction;
import domain.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepositoryInterface
{
    /**
     * Persists a transaction in the financial records.
     * @param transaction The transaction entity to be saved.
     */
    void save(Transaction transaction);

    /**
     * Retrieves a transaction by its unique identifier.
     * @param id The ID of the transaction.
     * @return An Optional containing the transaction if found.
     */
    Optional<Transaction> findById(Long id);

    /**
     * Retrieves all transactions within a specific timeframe.
     * Essential for cash flow reports and financial auditing.
     * @param start The beginning of the period.
     * @param end The end of the period.
     * @return A list of transactions within the specified period.
     */
    List<Transaction> findByPeriod(LocalDateTime start, LocalDateTime end);

    /**
     * Finds all transactions linked to a specific appointment.
     * Useful for tracking payments for services rendered.
     * @param appointmentId The unique identifier of the appointment.
     * @return A list of transactions associated with the given appointment.
     */
    List<Transaction> findByAppointmentId(Long appointmentId);

    /**
     * Retrieves transactions filtered by type within a specific period.
     * Useful for isolated analysis of Incomes or Expenses.
     * @param type The type of transaction (INCOME or EXPENSE).
     * @param start The beginning of the period.
     * @param end The end of the period.
     * @return A list of matching transactions.
     */
    List<Transaction> findByTypeAndPeriod(TransactionType type, LocalDateTime start, LocalDateTime end);
}