package com.example.evenue.models.tickets;



import com.example.evenue.models.tickets.TicketTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketTypeDao extends JpaRepository<TicketTypeModel, Long> {
    List<TicketTypeModel> findByEventId(Long eventId);

    // Find ticket type by event ID and ticket type name
    Optional<TicketTypeModel> findByEventIdAndTypeName(Long eventId, TicketTypeName typeName);
}
