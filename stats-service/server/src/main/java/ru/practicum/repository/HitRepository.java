package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Hit;
import ru.practicum.model.Stats;

import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {
    @Query("select new ru.practicum.model.Stats(h.app, h.uri, count(h.id)) " +
            "from Hit as h " +
            "where ((cast(h.timestamp as date)) between (cast(?1 as date)) and (cast(?2 as date))) " +
            "and h.uri IN (?3) " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<Stats> findAllNonUniqueVisitsWithUris(String start, String end, String[] uris);

    @Query("select new ru.practicum.model.Stats(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit as h " +
            "where ((cast(h.timestamp as date)) between (cast(?1 as date)) and (cast(?2 as date))) " +
            "and h.uri IN (?3) " +
            "group by h.ip, h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<Stats> findAllUniqueVisitsWithUris(String start, String end, String[] uris);

    @Query("select new ru.practicum.model.Stats(h.app, h.uri, count(h.id)) " +
            "from Hit as h " +
            "where ((cast(h.timestamp as date)) between (cast(?1 as date)) and (cast(?2 as date))) " +
            "group by h.app, h.uri " +
            "order by count(h.id) desc")
    List<Stats> findAllNonUniqueVisitsWithoutUris(String start, String end);

    @Query("select new ru.practicum.model.Stats(h.app, h.uri, count(distinct h.ip)) " +
            "from Hit as h " +
            "where ((cast(h.timestamp as date)) between (cast(?1 as date)) and (cast(?2 as date))) " +
            "group by h.ip, h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<Stats> findAllUniqueVisitsWithoutUris(String start, String end);
}
