package com.example.evenue.service;

import com.example.evenue.models.tickets.TicketTypeDao;
import com.example.evenue.models.tickets.TicketTypeModel;
import com.example.evenue.models.tickets.TicketTypeName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketTypeService {

    @Autowired
    private TicketTypeDao ticketTypeDao;

    // Method to save a new or updated ticket type
    public void saveTicketType(TicketTypeModel ticketType) {
        ticketTypeDao.save(ticketType); // This should save the ticketType along with its event association
    }

    // Method to retrieve a ticket type by its ID
    public TicketTypeModel getTicketTypeById(Long ticketTypeId) {
        return ticketTypeDao.findById(ticketTypeId).orElse(null);
    }

    // Method to retrieve all ticket types for a specific event by event ID
    public List<TicketTypeModel> getTicketTypesByEventId(Long eventId) {
        return ticketTypeDao.findByEventId(eventId);
    }



    // Method to delete a ticket type by its ID
    public void deleteTicketTypeById(Long ticketTypeId) {
        ticketTypeDao.deleteById(ticketTypeId);
    }

    // Method to find ticket type by event ID and type name
    public TicketTypeModel findTicketTypeByEventIdAndType(Long eventId, TicketTypeName typeName) {
        Optional<TicketTypeModel> ticketType = ticketTypeDao.findByEventIdAndTypeName(eventId, typeName);
        return ticketType.orElse(null);
    }
}
