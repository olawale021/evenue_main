package com.example.evenue.models.userBehaviour;

import com.example.evenue.models.events.EventCategory;
import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.users.UserModel;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_behaviour")
public class UserBehaviourModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private EventModel event;

    @Column(name = "interaction_type", nullable = false)
    private String interactionType;

    @Column(name = "interaction_timestamp", nullable = false)
    private LocalDateTime interactionTimestamp;

    @ManyToOne
    @JoinColumn(name = "event_category_id")
    private EventCategory eventCategory;

    @Column(name = "event_location")
    private String eventLocation;

    @Column(name = "date_filter")
    private String dateFilter;

    @Column(name = "price_filter")
    private String priceFilter;

    @Column(name = "location_filter")
    private String locationFilter;

    @Column(name = "session_length")
    private Double sessionLength;

    @Column(name = "user_location")
    private String userLocation;

    @Column(name = "preferred_category1")
    private Long preferredCategory1;

    @Column(name = "preferred_category2")
    private Long preferredCategory2;

    @Column(name = "preferred_category3")
    private Long preferredCategory3;


    @ManyToOne
    @JoinColumn(name = "friend_id")
    private UserModel friend;

    @Column(name = "ticket_id")
    private Integer ticketId;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public EventModel getEvent() {
        return event;
    }

    public void setEvent(EventModel event) {
        this.event = event;
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public LocalDateTime getInteractionTimestamp() {
        return interactionTimestamp;
    }

    public void setInteractionTimestamp(LocalDateTime interactionTimestamp) {
        this.interactionTimestamp = interactionTimestamp;
    }

    public EventCategory getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(String dateFilter) {
        this.dateFilter = dateFilter;
    }

    public String getPriceFilter() {
        return priceFilter;
    }

    public void setPriceFilter(String priceFilter) {
        this.priceFilter = priceFilter;
    }

    public String getLocationFilter() {
        return locationFilter;
    }

    public void setLocationFilter(String locationFilter) {
        this.locationFilter = locationFilter;
    }

    public Double getSessionLength() {
        return sessionLength;
    }

    public void setSessionLength(Double sessionLength) {
        this.sessionLength = sessionLength;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Long getPreferredCategory1() {
        return preferredCategory1;
    }

    public void setPreferredCategory1(Long preferredCategory1) {
        this.preferredCategory1 = preferredCategory1;
    }

    public Long getPreferredCategory2() {
        return preferredCategory2;
    }

    public void setPreferredCategory2(Long preferredCategory2) {
        this.preferredCategory2 = preferredCategory2;
    }

    public Long getPreferredCategory3() {
        return preferredCategory3;
    }

    public void setPreferredCategory3(Long preferredCategory3) {
        this.preferredCategory3 = preferredCategory3;
    }

    public UserModel getFriend() {
        return friend;
    }

    public void setFriend(UserModel friend) {
        this.friend = friend;
    }

    public Integer getTicketId() {
        return ticketId;
    }

    public void setTicketId(Integer ticketId) {
        this.ticketId = ticketId;
    }

}
