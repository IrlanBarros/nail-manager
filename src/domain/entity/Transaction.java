package domain.entity;

import domain.enums.TransactionType;
import domain.valueobject.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Transaction 
{

    private final Long id;
    private final TransactionType type;
    private final BigDecimal amount;
    private final Description description;
    private final LocalDateTime date;
    private boolean active;
    private LocalDateTime canceledAt;
    private final Long appointmentId;

    public Transaction
    (
        TransactionType type, BigDecimal amount, 
        Description description, Long appointmentId
    ) {
        this(null, type, amount, description, appointmentId, LocalDateTime.now(), true, null);
    }

    public Transaction
    (
        Long id, TransactionType type, BigDecimal amount, Description description, 
        Long appointmentId, LocalDateTime date, boolean active, LocalDateTime canceledAt
    ) {
        validateTransaction(type, amount, description, date);

        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.appointmentId = appointmentId;
        this.date = date;
        this.active = active;
        this.canceledAt = canceledAt;
    }

    public void cancel() 
    {
        if (!this.active) 
            throw new IllegalStateException("This transaction has already been canceled.");

        this.active = false;
        this.canceledAt = LocalDateTime.now();
    }

    private void validateTransaction
    (
        TransactionType type, BigDecimal amount, 
        Description description, LocalDateTime date
    ) {
        if (type == null) 
            throw new IllegalArgumentException("Transaction type cannot be null.");

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) 
            throw new IllegalArgumentException("The transaction amount must be greater than zero.");

        if (description == null) 
            throw new IllegalArgumentException("Description cannot be null.");

        if (date == null) 
            throw new IllegalArgumentException("Transaction date cannot be null.");
    }

    public Long getId() { return id; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Description getDescription() { return description; }
    public LocalDateTime getDate() { return date; }
    public boolean isActive() { return active; }

    public Optional<Long> getAppointmentId() { return Optional.ofNullable(appointmentId); }

    public Optional<LocalDateTime> getCanceledAt() { return Optional.ofNullable(canceledAt); }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;

        Transaction that = (Transaction) o;

        if (this.id == null) return false;

        return Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}