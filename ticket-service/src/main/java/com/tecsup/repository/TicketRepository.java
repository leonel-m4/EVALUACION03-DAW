package com.tecsup.repository;

import com.tecsup.model.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEstado(EstadoTicket estado, Sort sort);
}
