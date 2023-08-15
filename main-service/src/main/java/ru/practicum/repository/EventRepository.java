package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByInitiator(User initiator, Pageable pageable);

    Event findByIdAndInitiator(long eventId, User initiator);

    @Query(value = "select * from events as e " +
            "where (?1 is null or initiator_id in ?1) " +
            "and (?2 is null or state in ?2) " +
            "and (?3 is null or category_id in ?3) " +
            "and event_date between ?4 and ?5", nativeQuery = true)
    List<Event> findAllByInitiatorInAndStateInAndCategoryInAndEventDateBetween(List<User> users,
                                                                               List<String> states,
                                                                               List<Category> categories,
                                                                               LocalDateTime rangeStart,
                                                                               LocalDateTime rangeEnd,
                                                                               Pageable pageWithSomeElements);

    @Query(value = "select * from events as e " +
            "where (?1 is null or (LOWER(e.annotation) like LOWER(concat('%', ?1,'%')) or LOWER(e.description) like LOWER(concat('%', ?1,'%')))) " +
            "and (?2 is null or e.category_id in ?2) " +
            "and (?3 is null or e.paid = ?3) " +
            "and e.event_date between ?4 and ?5", nativeQuery = true)
    List<Event> findAllByAnnotationOrDescriptionAndCategoryInAndPaidAndEventDateBetween(String text,
                                                                                        Set<Category> categories,
                                                                                        Boolean paid,
                                                                                        LocalDateTime rangeStart,
                                                                                        LocalDateTime rangeEnd,
                                                                                        Pageable pageWithSomeElements);

    Set<Event> findByIdIn(Set<Long> events);

    @Query("select case when count(e)> 0 then true else false end " +
            "from Event e " +
            "join e.category cat " +
            "where cat.id = ?1")
    boolean existsByCategory(long catId);
}