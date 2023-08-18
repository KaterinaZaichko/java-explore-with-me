package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentsCount;
import ru.practicum.model.Event;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEvent(Event event, Pageable pageable);

    @Query("select new ru.practicum.model.CommentsCount(c.event, count(c.id)) " +
            "from Comment c " +
            "where c.event IN ?1 " +
            "group by c.event")
    List<CommentsCount> findAllByEventIn(List<Event> events);
}
