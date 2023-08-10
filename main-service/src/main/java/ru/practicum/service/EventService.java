package ru.practicum.service;

import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventShortDto> getEventsByUserId(long userId, int from, int size);

    EventFullDto save(long userId, NewEventDto newEventDto);

    EventFullDto getEventByUserIdAndEventId(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getRequestsByUserIdAndEventId(long userId, long eventId);

    EventRequestStatusUpdateResult updateRequests(long userId, long eventId,
                                                  EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    List<EventFullDto> getEventsByUsersAndStatesAndCategoriesAndRanges(long[] users,
                                                                       String[] states,
                                                                       long[] categories,
                                                                       LocalDateTime rangeStart,
                                                                       LocalDateTime rangeEnd,
                                                                       int from,
                                                                       int size);

    EventFullDto update(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getAllPublishedFilterableEvents(String text,
                                                        long[] categories,
                                                        Boolean paid,
                                                        LocalDateTime rangeStart,
                                                        LocalDateTime rangeEnd,
                                                        Boolean onlyAvailable,
                                                        String sort,
                                                        int from,
                                                        int size);

    EventFullDto getEventById(long id);

    long getConfirmedRequestsCount(Event event);

    long getViews(Event event);
}
