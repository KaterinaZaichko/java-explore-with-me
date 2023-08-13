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
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    ObjectMapper objectMapper;

    @Override
    public List<EventShortDto> getEventsByUserId(long userId, int from, int size) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId)));
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        List<Event> events = eventRepository.findAllByInitiator(initiator, pageWithSomeElements);
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        Map<Event, Long> confirmedRequests = getConfirmedRequestsCount(events);
        Map<String, Long> views = getViews(events);
        for (Event event : events) {
            EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
            eventShortDto.setConfirmedRequests(
                    confirmedRequests.get(event) != null ? confirmedRequests.get(event) : 0);
            eventShortDto.setViews(views.get("/events/" + event.getId()));
            eventShortDtos.add(eventShortDto);
        }
        return eventShortDtos;
    }

    @Override
    public EventFullDto save(long userId, NewEventDto newEventDto) {
        Event event = EventMapper.toEvent(newEventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException(String.format("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value: %s", event.getEventDate()));
        }
        event.setCategory(categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Category with id=%d was not found", newEventDto.getCategory()))));
        event.setInitiator(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId))));
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByUserIdAndEventId(long userId, long eventId) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId)));
        if (!eventRepository.existsById(eventId)
                || !eventRepository.findById(eventId).get().getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        Event event = eventRepository.findByIdAndInitiator(eventId, initiator);
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setConfirmedRequests(getConfirmedRequestsCount(List.of(event)).get(event) != null
                ? getConfirmedRequestsCount(List.of(event)).get(event) : 0);
        eventFullDto.setViews(getViews(List.of(event)).get("/events/" + event.getId()));
        return eventFullDto;
    }

    @Override
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId)));
        Event updatedEvent = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        if (!updatedEvent.getInitiator().equals(initiator)) {
            throw new EntityNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        if (updatedEvent.getState().equals(State.PUBLISHED)) {
            throw new ForbiddenStateException("Only pending or canceled events can be changed");
        }
        if (updateEventUserRequest.getEventDate() != null && updateEventUserRequest.getEventDate()
                .isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException("The date and time for which the event is scheduled cannot be earlier " +
                    "than two hours from the current moment");
        }
        if (updateEventUserRequest.getAnnotation() != null
                && !updateEventUserRequest.getAnnotation().isBlank()) {
            updatedEvent.setAnnotation(updateEventUserRequest.getAnnotation());
        }
        if (updateEventUserRequest.getCategory() != null) {
            if (categoryRepository.existsById(updateEventUserRequest.getCategory())) {
                updatedEvent.setCategory(categoryRepository.findById(updateEventUserRequest.getCategory()).get());
            }
        }
        if (updateEventUserRequest.getDescription() != null
                && !updateEventUserRequest.getDescription().isBlank()) {
            updatedEvent.setDescription(updateEventUserRequest.getDescription());
        }
        if (updateEventUserRequest.getEventDate() != null) {
            updatedEvent.setEventDate(updateEventUserRequest.getEventDate());
        }
        if (updateEventUserRequest.getLocation() != null) {
            Location location = LocationMapper.toLocation(updateEventUserRequest.getLocation());
            updatedEvent.setLocation(location);
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
            if (updateEventUserRequest.getStateAction().equals(UpdateEventUserRequest.StateAction.SEND_TO_REVIEW)) {
                updatedEvent.setState(State.PENDING);
            }
            if (updateEventUserRequest.getStateAction().equals(UpdateEventUserRequest.StateAction.CANCEL_REVIEW)) {
                updatedEvent.setState(State.CANCELED);
            }
        }
        if (updateEventUserRequest.getTitle() != null
                && !updateEventUserRequest.getTitle().isBlank()) {
            updatedEvent.setTitle(updateEventUserRequest.getTitle());
        }
        return EventMapper.toEventFullDto(eventRepository.save(updatedEvent));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByUserIdAndEventId(long userId, long eventId) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId)));
        if (!eventRepository.existsById(eventId)
                || !eventRepository.findById(eventId).get().getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        Event event = eventRepository.findByIdAndInitiator(eventId, initiator);
        return participationRequestRepository.findAllByEvent(event).stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateRequests(long userId, long eventId,
                                                         EventRequestStatusUpdateRequest
                                                                 eventRequestStatusUpdateRequest) {
        User initiator = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(
                String.format("User with id=%d was not found", userId)));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
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
                && event.getParticipantLimit() == participationRequestRepository.findAllByEventAndStatus(
                event, State.CONFIRMED).size()) {
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
        List<Event> events = eventRepository.findAllByInitiatorInAndStateInAndCategoryInAndEventDateBetween(
                usersList, statesList, categoriesList, rangeStart, rangeEnd, pageWithSomeElements);
        List<EventFullDto> eventFullDtos = new ArrayList<>();
        Map<Event, Long> confirmedRequests = getConfirmedRequestsCount(events);
        Map<String, Long> views = getViews(events);
        for (Event event : events) {
            EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
            eventFullDto.setConfirmedRequests(
                    confirmedRequests.get(event) != null ? confirmedRequests.get(event) : 0);
            eventFullDto.setViews(views.get("/events/" + event.getId()));
            eventFullDtos.add(eventFullDto);
        }
        return eventFullDtos;
    }

    @Override
    public EventFullDto update(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event updatedEvent = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Event with id=%d was not found", eventId)));
        if (updatedEvent.getPublishedOn() != null && updatedEvent.getEventDate().isBefore(
                updatedEvent.getPublishedOn().plusHours(1))) {
            throw new ForbiddenDateException("The start date of the event to be changed must be no earlier " +
                    "than one hour from the publication date");
        }
        if (!updatedEvent.getState().equals(State.PENDING)) {
            throw new ForbiddenStateException("An event can only be published if it is in the publish pending state");
        }
        if (updateEventAdminRequest.getAnnotation() != null
                && !updateEventAdminRequest.getAnnotation().isBlank()) {
            updatedEvent.setAnnotation(updateEventAdminRequest.getAnnotation());
        }
        if (updateEventAdminRequest.getCategory() != null) {
            if (categoryRepository.existsById(updateEventAdminRequest.getCategory())) {
                updatedEvent.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory()).get());
            }
            updatedEvent.setCategory(categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new EntityNotFoundException(String.format("Category with id=%d was not found",
                            updateEventAdminRequest.getCategory()))));
        }
        if (updateEventAdminRequest.getDescription() != null
                && !updateEventAdminRequest.getDescription().isBlank()) {
            updatedEvent.setDescription(updateEventAdminRequest.getDescription());
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            if (updateEventAdminRequest.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
                updatedEvent.setEventDate(updateEventAdminRequest.getEventDate());
            } else {
                throw new EventDateException(String.format("Field: eventDate. Error: должно содержать дату, " +
                        "которая еще не наступила. Value: %s", updateEventAdminRequest.getEventDate()));
            }
        }
        if (updateEventAdminRequest.getLocation() != null) {
            Location location = LocationMapper.toLocation(updateEventAdminRequest.getLocation());
            updatedEvent.setLocation(location);
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
            if (updateEventAdminRequest.getStateAction().equals(UpdateEventAdminRequest.StateAction.PUBLISH_EVENT)) {
                updatedEvent.setState(State.PUBLISHED);
                updatedEvent.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequest.getStateAction().equals(UpdateEventAdminRequest.StateAction.REJECT_EVENT)) {
                updatedEvent.setState(State.CANCELED);
            }
        }
        if (updateEventAdminRequest.getTitle() != null
                && !updateEventAdminRequest.getTitle().isBlank()) {
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
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        Set<Category> categoriesList = new HashSet<>();
        if (categories != null) {
            categoriesList = categoryRepository.findByIdIn(categories);
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
        List<Event> events = eventRepository.findAllByAnnotationOrDescriptionAndCategoryInAndPaidAndEventDateBetween(
                text, categoriesList, paid, rangeStart, rangeEnd, pageWithSomeElements);
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        Map<Event, Long> confirmedRequests = getConfirmedRequestsCount(events);
        Map<String, Long> views = getViews(events);
        if (onlyAvailable.equals(true)) {
            for (Event event : events) {
                if (event.getParticipantLimit() < confirmedRequests.get(event)) {
                    EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
                    eventShortDto.setConfirmedRequests(
                            confirmedRequests.get(event) != null ? confirmedRequests.get(event) : 0);
                    eventShortDto.setViews(views.get("/events/" + event.getId()));
                    eventShortDtos.add(eventShortDto);
                }
            }
        } else {
            for (Event event : events) {
                EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
                eventShortDto.setConfirmedRequests(
                        confirmedRequests.get(event) != null ? confirmedRequests.get(event) : 0);
                eventShortDto.setViews(views.get("/events/" + event.getId()));
                eventShortDtos.add(eventShortDto);
            }
        }
        if (sort != null && sort.equalsIgnoreCase("event_date")) {
            eventShortDtos.sort(Comparator.comparing(EventShortDto::getEventDate));
        }
        if (sort != null && sort.equalsIgnoreCase("views")) {
            eventShortDtos.sort((e1, e2) -> (int) (e1.getViews() - e2.getViews()));
        }
        return eventShortDtos;
    }

    @Override
    public EventFullDto getEventById(long id) {
        Event event = eventRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                String.format("Event with id=%d was not found", id)));
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new EntityNotFoundException(String.format("Event with id=%d was not found", id));
        }
        EventFullDto eventFullDto = EventMapper.toEventFullDto(event);
        eventFullDto.setConfirmedRequests(getConfirmedRequestsCount(List.of(event)).get(event) != null
                ? getConfirmedRequestsCount(List.of(event)).get(event) : 0);
        eventFullDto.setViews(getViews(List.of(event)).get("/events/" + event.getId()));
        return eventFullDto;
    }

    private Map<Event, Long> getConfirmedRequestsCount(List<Event> events) {
        return participationRequestRepository.findAllByEventInAndStatus(events, State.CONFIRMED).stream()
                .collect(Collectors.toMap(
                        ConfirmedRequestsCount::getEvent, ConfirmedRequestsCount::getCountConfirmedRequests));
    }

    private Map<String, Long> getViews(List<Event> events) {
        List<Event> publishEvents = events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .sorted(Comparator.comparing(Event::getPublishedOn))
                .collect(Collectors.toList());
        List<String> eventsUris = new ArrayList<>();
        if (publishEvents.size() != 0) {
            for (Event event : publishEvents) {
                eventsUris.add("/events/" + event.getId());
            }
            Object object = statsClient.getStatsWithUris(
                    publishEvents.get(0).getPublishedOn(), LocalDateTime.now(),
                    eventsUris.toArray(new String[0]), true).getBody();
            List<StatsDto> stats = objectMapper.convertValue(object, new TypeReference<>() {
            });
            return stats.stream().collect(Collectors.toMap(StatsDto::getUri, StatsDto::getHits));
        }
        return new HashMap<>();
    }
}
