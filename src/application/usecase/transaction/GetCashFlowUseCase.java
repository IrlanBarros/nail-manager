package application.usecase.transaction;

import domain.entity.Transaction;
import domain.enums.TransactionType;
import domain.repository.TransactionRepositoryInterface;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class GetCashFlowUseCase 
{

    private final TransactionRepositoryInterface repository;

    public GetCashFlowUseCase(TransactionRepositoryInterface repository) 
    {
        this.repository = repository;
    }

    public CashFlowReport execute(LocalDateTime startDate, LocalDateTime endDate) 
    {
        if (startDate == null || endDate == null) 
            throw new IllegalArgumentException("The start and end dates are mandatory.");

        if (startDate.isAfter(endDate)) 
            throw new IllegalArgumentException("The start date cannot be later than the end date.");

        List<Transaction> allTransactions = repository.findByPeriod(startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction t : allTransactions) 
        {
            if (!t.isActive()) { continue; }

            if (t.getType() == TransactionType.INCOME) 
            {
                totalIncome = totalIncome.add(t.getAmount());
            } 
            else if (t.getType() == TransactionType.EXPENSE) 
            {
                totalExpense = totalExpense.add(t.getAmount());
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return new CashFlowReport(totalIncome, totalExpense, balance, allTransactions);
    }

    /**
     * DTO
     */
    public static class CashFlowReport 
    {
        private final BigDecimal totalIncome;
        private final BigDecimal totalExpense;
        private final BigDecimal balance;
        private final List<Transaction> transactions;

        public CashFlowReport(BigDecimal totalIncome, BigDecimal totalExpense, 
                              BigDecimal balance, List<Transaction> transactions) {
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.balance = balance;
            this.transactions = transactions;
        }

        public BigDecimal getTotalIncome() { return totalIncome; }
        public BigDecimal getTotalExpense() { return totalExpense; }
        public BigDecimal getBalance() { return balance; }
        public List<Transaction> getTransactions() { return List.copyOf(transactions); }
    }
}