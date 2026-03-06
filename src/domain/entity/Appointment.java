package domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import domain.enums.AppointmentStatus;
import domain.policy.DiscountPolicy;
import domain.valueobject.Description;

public class Appointment 
{

    private final Long id;
    private LocalDateTime dateTime;
    private final Customer customer;
    private AppointmentStatus status;
    private Description description;
    private final List<Service> services;
    private BigDecimal totalPrice;

    public Appointment
    (
        Customer customer, LocalDateTime dateTime, 
        Description description
    ) {
        this (
            null, customer, dateTime, AppointmentStatus.SCHEDULED, 
            new ArrayList<>(), description, BigDecimal.ZERO
        );
    }

    public Appointment
    (
        Long id, Customer customer, LocalDateTime dateTime, AppointmentStatus status, 
        List<Service> services, Description description, BigDecimal totalPrice
    ) {
        validateAppointment(customer, status, dateTime);

        this.id = id;
        this.customer = customer;
        this.dateTime = dateTime;
        this.status = status;
        this.services = services == null ? new ArrayList<>() : new ArrayList<>(services);
        this.description = description;
        this.totalPrice = totalPrice;
    }

    public void addService(Service service) 
    {
        if (isFinalized())
            throw new IllegalStateException("Cannot add services to a finished or cancelled appointment.");

        services.add(service);
    }

    public BigDecimal calculateTotal(DiscountPolicy discountPolicy) 
    {
        if (discountPolicy == null) 
            throw new IllegalArgumentException("Discount policy cannot be null.");

        BigDecimal subtotal = services.stream()
                .map(Service::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = discountPolicy.calculateDiscount(this.customer, subtotal);

        BigDecimal total = subtotal.subtract(discount);

        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    public void start() 
    {
        if (status != AppointmentStatus.SCHEDULED)
            throw new IllegalStateException("Only scheduled appointments can be started.");

        this.status = AppointmentStatus.IN_PROGRESS;
    }

    public void finish() 
    {
        if (status != AppointmentStatus.IN_PROGRESS)
            throw new IllegalStateException("Only appointments in progress can be finished.");

        if (services.isEmpty())
            throw new IllegalStateException("Cannot finish appointment without services.");

        this.status = AppointmentStatus.FINISHED;
    }

    public void cancel() 
    {
        if (status == AppointmentStatus.FINISHED)
            throw new IllegalStateException("Cannot cancel a finished appointment.");

        this.status = AppointmentStatus.CANCELLED;
    }

    public void reschedule(LocalDateTime newDateTime) 
    {
        if (isFinalized()) 
            throw new IllegalStateException("Is impossible to reschedule a completed or canceled appointment.");

        if (newDateTime.isBefore(LocalDateTime.now())) 
            throw new IllegalArgumentException("New appointment date cannot be in the past.");

        this.dateTime = newDateTime;
        
        if (this.status == AppointmentStatus.IN_PROGRESS) 
        {
            this.status = AppointmentStatus.SCHEDULED;
        }
    }

    private boolean isFinalized() 
    {
        return this.status == AppointmentStatus.FINISHED || this.status == AppointmentStatus.CANCELLED;
    }

    public void updateServices(List<Service> newServices) 
    {
        if (newServices == null || newServices.isEmpty()) 
            throw new IllegalArgumentException("An appointment must contain at least one service.");

        if (isFinalized()) 
            throw new IllegalStateException("Is impossible to change services for a completed or canceled order.");

        this.services.clear();
        this.services.addAll(newServices);
    }

    public void updateDescription(Description newDescription) 
    {
        if (isFinalized()) 
            throw new IllegalStateException("Cannot update description of a finished or cancelled appointment.");

        this.description = newDescription; 
    }

    private void validateAppointment(Customer c, AppointmentStatus s, LocalDateTime dTime)
    {
        if (c == null)
            throw new IllegalArgumentException("Customer cannot be null.");

        if (s == null)
            throw new IllegalArgumentException("Status cannot be null.");

        if (dTime == null)
            throw new IllegalArgumentException("DateTime cannot be null.");
    }

    public void lockFinalPrice(DiscountPolicy discountPolicy) 
    {
        this.totalPrice = calculateTotal(discountPolicy);
    }

    public Long getId() { return id; }

    public LocalDateTime getDateTime() { return dateTime; }

    public Customer getCustomer() { return customer; }

    public AppointmentStatus getStatus() { return status; }

    public List<Service> getServices() { return List.copyOf(services); }

    public Optional<Description> getDescription() { return Optional.ofNullable(description); }

    public BigDecimal getTotalPrice() { return this.totalPrice; }

    @Override
    public boolean equals(Object o) 
    {
        if (this == o) return true;
        if (!(o instanceof Appointment)) return false;

        Appointment that = (Appointment) o;

        if (this.id == null) return false;

        return Objects.equals(this.id, that.getId());
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }
}