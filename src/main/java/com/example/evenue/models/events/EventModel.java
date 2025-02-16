package com.example.evenue.models.events;

import com.example.evenue.models.tickets.TicketTypeModel;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;

@Entity
@Table(name = "events")
public class EventModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id") // Match with your database column name
    private Long id;

    @Column(name = "organizer_id", nullable = false)
    private Integer organizerId; // Organizer's ID from users table

    private String organizerName; // Name of the organization organizing the event

    @Column(name = "event_name", nullable = false)
    private String eventName; // Name of the event

    private String description; // Description of the event

    private String location; // Location of the event

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate; // Date of the event

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // Start time of the event

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime; // End time of the event

    @Column(name = "ticket_price", nullable = false)
    private Double ticketPrice; // Price of the ticket

    @Column(name = "event_image")
    private String eventImage; // URL or path to the event image

    @Column(nullable = false)
    private String venue; // Venue of the event

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Creation timestamp

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(); // Last update timestamp

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private EventCategory eventCategory; // Reference to EventCategory entity

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketTypeModel> ticketTypes;

    @Transient
    private boolean isLiked; // Transient field, not stored in the database

    // Check if the event is upcoming
    public boolean isUpcoming() {
        return eventDate.isAfter(ChronoLocalDate.from(LocalDateTime.now()));
    }

    // Constructors
    public EventModel() {}

    public EventModel(Integer organizerId, String organizerName, String eventName, String description, String location,
                      LocalDate eventDate, LocalTime startTime, LocalTime endTime, Double ticketPrice, String eventImage, String venue, EventCategory eventCategory) {
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.eventName = eventName;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ticketPrice = ticketPrice;
        this.eventImage = eventImage;
        this.venue = venue;
        this.eventCategory = eventCategory;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Integer organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Double getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    // Getters and setters for all fields, including ticketTypes
    public List<TicketTypeModel> getTicketTypes() {
        return ticketTypes;
    }

    public void setTicketTypes(List<TicketTypeModel> ticketTypes) {
        this.ticketTypes = ticketTypes;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
