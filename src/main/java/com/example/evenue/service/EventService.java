package com.example.evenue.service;

import com.example.evenue.models.events.EventDao;
import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.tickets.TicketDao;
import com.example.evenue.models.tickets.TicketTypeDao;
import com.example.evenue.models.users.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventService {

    @Autowired
    private EventDao eventDao;

    @Autowired
    private TicketDao ticketDao;

    @Autowired
    private TicketTypeDao ticketTypeDao;

    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    // Method to add an event
    public EventModel addEvent(EventModel event) {
        return eventDao.save(event);
    }

    public Page<EventModel> getAllEvents(Pageable pageable) {
        Pageable sortedByLatest = PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        return eventDao.findAll(sortedByLatest);
    }

    public Optional<EventModel> getEventById(Long eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID must not be null");
        }
        return eventDao.findById(eventId);
    }


    // Implement filter logic with search functionality
    public Page<EventModel> filterEvents(Long categoryId, String search, String location, Pageable pageable) {
        if (categoryId != null && categoryId > 0) {
            return eventDao.findByCategoryAndSearch(categoryId, search, pageable);
        }
        return eventDao.findBySearchAndLocation(search, location, pageable);
    }


    // Method to find an event by name
    public Optional<EventModel> findByEventName(String eventName) {
        return eventDao.findByEventName(eventName); // Use Optional to avoid returning null
    }

    // Fetch events by tickets the user has
    public List<EventModel> getEventsByUser(UserModel user) {
        return ticketDao.findEventsByUser(user);
    }

    public List<EventModel> searchEvents(String searchQuery, String location) {
        return eventDao.findBySearchAndLocation(searchQuery, location, Pageable.unpaged()).getContent();
    }

    // Method to get all events for dropdown
    public List<EventModel> getAllEventsForDropdown() {
        return eventDao.findAllEvents();
    }

    public Page<EventModel> getFilteredEvents(
            List<Long> categories,
            String dateFilter,
            String priceFilter,
            String searchQuery,
            String location,
            Pageable pageable) {

        // Normalize categories list
        categories = (categories != null && categories.isEmpty()) ? null : categories;

        // Date filter logic
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (dateFilter != null) {
            switch (dateFilter) {
                case "today":
                    startDate = LocalDate.now();
                    endDate = LocalDate.now();
                    break;
                case "this-week":
                    startDate = LocalDate.now();
                    endDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    break;
                case "this-month":
                    startDate = LocalDate.now();
                    endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
                    break;
                case "within-2-weeks":
                    startDate = LocalDate.now();
                    endDate = LocalDate.now().plusWeeks(2);
                    break;
                case "within-1-month":
                    startDate = LocalDate.now();
                    endDate = LocalDate.now().plusMonths(1);
                    break;
                default:
                    break;
            }
        }

        // Price filter logic
        Double minPrice = null;
        Double maxPrice = null;
        if (priceFilter != null) {
            switch (priceFilter) {
                case "free":
                    minPrice = 0.0;
                    maxPrice = 0.0;
                    break;
                case "under-30":
                    maxPrice = 30.0;
                    break;
                case "between-30-and-100":
                    minPrice = 30.0;
                    maxPrice = 100.0;
                    break;
                case "over-100":
                    minPrice = 100.0;
                    break;
                default:
                    break;
            }
        }

        // Log the calculated filter values (if logging is needed)
//    logger.info("Categories: {}", categories);
//    logger.info("Date range: {} to {}", startDate, endDate);
//    logger.info("Price range: {} to {}", minPrice, maxPrice);
//    logger.info("Search Query: {}", searchQuery);
//    logger.info("Location: {}", location);
        // Fetch events using DAO method with all filters applied
        return eventDao.findByFilters(
                categories,
                searchQuery,
                startDate,
                endDate,
                minPrice,
                maxPrice,
                location,
                pageable);
    }

}
