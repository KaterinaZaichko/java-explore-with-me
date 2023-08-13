package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.*;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    ParticipationRequest findByRequesterAndEvent(User requester, Event event);

    List<ParticipationRequest> findAllByEvent(Event event);

    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);

    List<ParticipationRequest> findAllByEventAndStatus(Event event, State status);

    List<ParticipationRequest> findAllByRequester(User requester);

    @Query("select new ru.practicum.model.ConfirmedRequestsCount(pr.event, count(pr.id)) " +
            "from ParticipationRequest pr " +
            "where pr.event IN ?1 and pr.status = ?2 " +
            "group by pr.event")
    List<ConfirmedRequestsCount> findAllByEventInAndStatus(List<Event> events, State status);
}
