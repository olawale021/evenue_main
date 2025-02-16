package com.example.evenue.models.tickets;



import com.example.evenue.models.events.EventModel;
import com.example.evenue.models.tickets.TicketModel;
import com.example.evenue.models.users.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketDao extends JpaRepository<TicketModel, Long> {


    List<TicketModel> findByEventId(Long eventId);

    TicketModel findByTicketCode(String ticketCode);

    List<TicketModel> findByUserId(int user_id);


    @Query("SELECT t FROM TicketModel t WHERE t.event.id = :eventId AND t.ticketType.ticketTypeId = :ticketTypeId")
    List<TicketModel> findByEventIdAndTicketTypeId(@Param("eventId") Long eventId, @Param("ticketTypeId") Long ticketTypeId);

    // Query to find events a user has tickets for
    @Query("SELECT t.event FROM TicketModel t WHERE t.user = :user")
    List<EventModel> findEventsByUser(@Param("user") UserModel user);

}
