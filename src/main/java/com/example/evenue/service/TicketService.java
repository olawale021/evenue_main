package com.example.evenue.service;

import com.example.evenue.models.tickets.TicketDao;
import com.example.evenue.models.tickets.TicketModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketDao ticketDao;

    // Method to save a new or updated ticket
    public TicketModel saveTicket(TicketModel ticket) {
        return ticketDao.save(ticket);
    }

    // Method to retrieve a ticket by its ID
    public TicketModel getTicketById(Long ticketId) {
        return ticketDao.findById(ticketId).orElse(null);
    }

    // Method to retrieve all tickets for a specific event by event ID
    public List<TicketModel> getTicketsByEventId(Long eventId) {
        return ticketDao.findByEventId(eventId);
    }

    // Method to retrieve all tickets purchased by a specific user by user ID
    public List<TicketModel> getTicketsByUserId(int userId) {
        return ticketDao.findByUserId(userId);
    }

    // Method to delete a ticket by its ID
    public void deleteTicketById(Long ticketId) {
        ticketDao.deleteById(ticketId);
    }

    // Method to get ticket by ticketCode
    public TicketModel getTicketByTicketCode(String ticketCode) {
        return ticketDao.findByTicketCode(ticketCode);
    }



}
