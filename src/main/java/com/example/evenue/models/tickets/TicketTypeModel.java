package com.example.evenue.models.tickets;

import com.example.evenue.models.events.EventModel;
import jakarta.persistence.*;

@Entity
@Table(name = "ticket_types")
public class TicketTypeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_type_id")
    private Long ticketTypeId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private EventModel event;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_name", nullable = false)
    private TicketTypeName typeName; // Use an enum for ticket types like VIP, General Admission, etc.

    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Total number of tickets available for this type

    @Column(name = "remaining_quantity", nullable = false)
    private Integer remainingQuantity; // Number of tickets still available

    // Getters and Setters

    public Long getTicketTypeId() {
        return ticketTypeId;
    }

    public void setTicketTypeId(Long ticketTypeId) {
        this.ticketTypeId = ticketTypeId;
    }

    public EventModel getEvent() {
        return event;
    }

    public void setEvent(EventModel event) {
        this.event = event;
    }

    public TicketTypeName getTypeName() {
        return typeName;
    }

    public void setTypeName(TicketTypeName typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}
