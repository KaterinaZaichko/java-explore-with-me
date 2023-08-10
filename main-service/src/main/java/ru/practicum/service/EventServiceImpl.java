package ru.practicum.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.StatsDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.exception.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;

    @Override
    public List<EventShortDto> getEventsByUserId(long userId, int from, int size) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        List<EventShortDto> events = new ArrayList<>();
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        for (Event event : eventRepository.findAllByInitiator(initiator, pageWithSomeElements)) {
            EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
            eventShortDto.setConfirmedRequests(getConfirmedRequestsCount(event));
            eventShortDto.setViews(getViews(event));
            events.add(eventShortDto);
        }
        return events;
    }

    @Override
    public EventFullDto save(long userId, NewEventDto newEventDto) {
        Event event = EventMapper.toEvent(newEventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException(String.format("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value: %s", event.getEventDate()));
        }
        event.setCategory(categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(
                        String.format("Category with id=%d was not found", newEventDto.getCategory()))));
        event.setInitiator(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId))));
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(long userId, long eventId) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        if (!eventRepository.existsById(eventId)
                || !eventRepository.findById(eventId).get().getInitiator().getId().equals(userId)) {
            throw new EventNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        Event event = eventRepository.findByIdAndInitiator(eventId, initiator);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setConfirmedRequests(getConfirmedRequestsCount(event));
        eventFullDto.setViews(getViews(event));
        return eventFullDto;
    }

    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        Event updatedEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        if (!updatedEvent.getInitiator().equals(initiator)) {
            throw new EventNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        if (updatedEvent.getState().equals(State.PUBLISHED)) {
            throw new ForbiddenStateException("Only pending or canceled events can be changed");
        }
        if (updateEventUserRequest.getEventDate() != null && LocalDateTime.parse(updateEventUserRequest.getEventDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                .isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException("The date and time for which the event is scheduled cannot be earlier " +
                    "than two hours from the current moment");
        }
        if (updateEventUserRequest.getAnnotation() != null) {
            updatedEvent.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            if (categoryRepository.existsById(updateEventUserRequest.getCategory())) {
                updatedEvent.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory()).get());
            }
        }
        if (updateEventUserRequest.getDescription() != null) {
            updatedEvent.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            updatedEvent.setEventDate(LocalDateTime.parse(updateEventUserRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (updateEventUserRequest.getLocation() != null) {
            updatedEvent.setLocation(updateEventUserRequest.getLocation().toString());
        }
        if (updateEventUserRequest.getPaid() != null) {
            updatedEvent.setPaid(updateEventUserRequest.getPaid());
        }
        if (updateEventUserRequest.getParticipantLimit() != null) {
            updatedEvent.setParticipantLimit(updateEventUserRequest.getParticipantLimit());
        }
        if (updateEventUserRequest.getRequestModeration() != null) {
            updatedEvent.setRequestModeration(updateEventUserRequest.getRequestModeration());
        }
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                updatedEvent.setState(State.PENDING);
            }
            if (updateEventUserRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                updatedEvent.setState(State.CANCELED);
            }
        }
        if (updateEventUserRequest.getTitle() != null) {
            updatedEvent.setTitle(updateEventUserRequest.getTitle());
        }
        return EventMapper.toEventFullDto(eventRepository.save(updatedEvent));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserIdAndEventId(long userId, long eventId) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        if (!eventRepository.existsById(eventId)
                || !eventRepository.findById(eventId).get().getInitiator().getId().equals(userId)) {
            throw new EventNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        Event event = eventRepository.findByIdAndInitiator(eventId, initiator);
        List<ParticipationRequestDto> participationRequests = new ArrayList<>();
        for (ParticipationRequest participationRequest : participationRequestRepository.findAllByEvent(event)) {
            participationRequests.add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
        }
        return participationRequests;
    }

    @Override
    public EventRequestStatusUpdateResult updateRequests(long userId, long eventId,
                                                         EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(
                String.format("User with id=%d was not found", userId)));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        List<ParticipationRequest> participationRequests;
        if (event.getInitiator().equals(initiator)) {
            participationRequests = participationRequestRepository.findAllByIdIn(
                    eventRequestStatusUpdateRequest.getRequestIds());
        } else {
            throw new ForbiddenRequesterException(String.format("The user with id %d is not the initiator " +
                    "of the event with id %d", userId, eventId));
        }
        if (event.getParticipantLimit() == 0 || event.getRequestModeration().equals(false)) {
            for (ParticipationRequest participationRequest : participationRequests) {
                if (participationRequest.getStatus().equals(State.PENDING)) {
                    participationRequest.setStatus(State.CONFIRMED);
                    participationRequestRepository.save(participationRequest);
                } else {
                    throw new ForbiddenStateException("Status can only be changed for applications " +
                            "that are in the pending state");
                }
            }
        }
        if (event.getParticipantLimit() != 0
                && event.getParticipantLimit() == participationRequestRepository.findAllByEvent(event).size()) {
            throw new ForbiddenParticipantsCountException("The participant limit has been reached");
        } else {
            for (ParticipationRequest participationRequest : participationRequests) {
                if (!participationRequest.getStatus().equals(State.PENDING)) {
                    throw new ForbiddenStateException("Status can only be changed for applications " +
                            "that are in the pending state");
                }
                if (event.getParticipantLimit() != participationRequestRepository.findAllByEvent(event).size()) {
                    participationRequest.setStatus(eventRequestStatusUpdateRequest.getStatus());
                    participationRequestRepository.save(participationRequest);
                } else {
                    participationRequest.setStatus(State.REJECTED);
                    participationRequestRepository.save(participationRequest);
                }
            }
        }
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        for (ParticipationRequest participationRequest : participationRequests) {
            if (participationRequest.getStatus().equals(State.CONFIRMED)) {
                confirmedRequests.add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
            } else {
                rejectedRequests.add(ParticipationRequestMapper.toParticipationRequestDto(participationRequest));
            }
        }
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    public List<EventFullDto> getEventsByUsersAndStatesAndCategoriesAndRanges(long[] users,
                                                                              String[] states,
                                                                              long[] categories,
                                                                              LocalDateTime rangeStart,
                                                                              LocalDateTime rangeEnd,
                                                                              int from,
                                                                              int size) {
        List<EventFullDto> events = new ArrayList<>();
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        List<User> usersList = new ArrayList<>();
        if (users != null) {
            for (Long id : users) {
                if (userRepository.existsById(id)) {
                    usersList.add(userRepository.findById(id).get());
                }
            }
        }
        List<String> statesList = new ArrayList<>();
        if (states != null) {
            statesList = List.of(states);
        }
        List<Category> categoriesList = new ArrayList<>();
        if (categories != null) {
            for (Long id : categories) {
                if (categoryRepository.existsById(id)) {
                    categoriesList.add(categoryRepository.findById(id).get());
                }
            }
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        for (Event event : eventRepository.findAllByInitiatorInAndStateInAndCategoryInAndEventDateBetween(
                usersList, statesList, categoriesList, rangeStart, rangeEnd, pageWithSomeElements)) {
            EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
            eventFullDto.setConfirmedRequests(getConfirmedRequestsCount(event));
            eventFullDto.setViews(getViews(event));
            events.add(eventFullDto);
        }
        return events;
    }

    @Override
    public EventFullDto update(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event updatedEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        if (updatedEvent.getPublishedOn() != null && updatedEvent.getEventDate().isBefore(
                updatedEvent.getPublishedOn().plusHours(1))) {
            throw new ForbiddenDateException("The start date of the event to be changed must be no earlier " +
                    "than one hour from the publication date");
        }
        if (!updatedEvent.getState().equals(State.PENDING)) {
            throw new ForbiddenStateException("An event can only be published if it is in the publish pending state");
        }
        if (updateEventAdminRequest.getAnnotation() != null) {
            updatedEvent.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            if (categoryRepository.existsById(updateEventAdminRequest.getCategory())) {
                updatedEvent.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory()).get());
            }
            updatedEvent.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(String.format("Category with id=%d was not found",
                            updateEventAdminRequest.getCategory()))));
        }
        if (updateEventAdminRequest.getDescription() != null) {
            updatedEvent.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            if (LocalDateTime.parse(updateEventAdminRequest.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isAfter(LocalDateTime.now().plusHours(2))) {
                updatedEvent.setEventDate(LocalDateTime.parse(updateEventAdminRequest.getEventDate(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                throw new EventDateException(String.format("Field: eventDate. Error: должно содержать дату, " +
                        "которая еще не наступила. Value: %s", updateEventAdminRequest.getEventDate()));
            }
        }
        if (updateEventAdminRequest.getLocation() != null) {
            updatedEvent.setLocation(updateEventAdminRequest.getLocation().toString());
        }
        if (updateEventAdminRequest.getPaid() != null) {
            updatedEvent.setPaid(updateEventAdminRequest.getPaid());
        }
        if (updateEventAdminRequest.getParticipantLimit() != null) {
            updatedEvent.setParticipantLimit(updateEventAdminRequest.getParticipantLimit());
        }
        if (updateEventAdminRequest.getRequestModeration() != null) {
            updatedEvent.setRequestModeration(updateEventAdminRequest.getRequestModeration());
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                updatedEvent.setState(State.PUBLISHED);
                updatedEvent.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequest.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                updatedEvent.setState(State.CANCELED);
            }
        }
        if (updateEventAdminRequest.getTitle() != null) {
            updatedEvent.setTitle(updateEventAdminRequest.getTitle());
        }
        return EventMapper.toEventFullDto(eventRepository.save(updatedEvent));
    }

    @Override
    public List<EventShortDto> getAllPublishedFilterableEvents(String text,
                                                               long[] categories,
                                                               Boolean paid,
                                                               LocalDateTime rangeStart,
                                                               LocalDateTime rangeEnd,
                                                               Boolean onlyAvailable,
                                                               String sort,
                                                               int from,
                                                               int size) {
        List<EventShortDto> events = new ArrayList<>();
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Category> categoriesList = new ArrayList<>();
        if (categories != null) {
            for (Long id : categories) {
                if (categoryRepository.existsById(id)) {
                    Category category = categoryRepository.findById(id).get();
                    categoriesList.add(category);
                }
            }
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        if (rangeEnd.isBefore(rangeStart)) {
            throw new EventDateException("The beginning of the period cannot be later than its end");
        }
        if (onlyAvailable.equals(true)) {
            for (Event event : eventRepository.findAllByAnnotationOrDescriptionAndCategoryInAndPaidAndEventDateBetween(
                    text, categoriesList, paid, rangeStart, rangeEnd, pageWithSomeElements)) {
                if (event.getParticipantLimit() < participationRequestRepository.findAllByEvent(event).size()) {
                    EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
                    eventShortDto.setConfirmedRequests(getConfirmedRequestsCount(event));
                    eventShortDto.setViews(getViews(event));
                    events.add(eventShortDto);
                }
            }
        } else {
            for (Event event : eventRepository.findAllByAnnotationOrDescriptionAndCategoryInAndPaidAndEventDateBetween(
                    text, categoriesList, paid, rangeStart, rangeEnd, pageWithSomeElements)) {
                EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
                eventShortDto.setConfirmedRequests(getConfirmedRequestsCount(event));
                eventShortDto.setViews(getViews(event));
                events.add(eventShortDto);
            }
        }
        if (sort != null && sort.equalsIgnoreCase("event_date")) {
            events.sort(Comparator.comparing(e -> LocalDateTime.parse(e.getEventDate(), DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))));
        }
        if (sort != null && sort.equalsIgnoreCase("views")) {
            events.sort((e1, e2) -> (int) (e1.getViews() - e2.getViews()));
        }
        return events;
    }

    @Override
    public EventFullDto getEventById(long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(
                String.format("Event with id=%d was not found", id)));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new EventNotFoundException(String.format("Event with id=%d was not found", id));
        }
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setConfirmedRequests(getConfirmedRequestsCount(event));
        eventFullDto.setViews(getViews(event));
        return eventFullDto;
    }

    @Override
    public long getConfirmedRequestsCount(Event event) {
        return participationRequestRepository.findAllByEventAndStatus(
                event, State.CONFIRMED).size();
    }

    @Override
    public long getViews(Event event) {
        Object object = statsClient.getStatsWithUris(event.getCreatedOn(), LocalDateTime.now(),
                new String[]{"/events/" + event.getId()}, true).getBody();
        List<StatsDto> stats = new ObjectMapper().convertValue(object, new TypeReference<>() {
        });
        if (!stats.isEmpty()) {
            return stats.get(0).getHits();
        }
        return 0;
    }
}
