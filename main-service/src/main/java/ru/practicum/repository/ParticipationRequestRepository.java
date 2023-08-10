package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.State;
import ru.practicum.model.User;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    ParticipationRequest findByRequesterAndEvent(User requester, Event event);

    List<ParticipationRequest> findAllByEvent(Event event);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

    List<ParticipationRequest> findAllByEventAndStatus(Event event, State status);

    List<ParticipationRequest> findAllByRequester(User requester);
}
