package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.CompilationNotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CompilationDto> compilations = new ArrayList<>();
        List<EventShortDto> events = new ArrayList<>();
        if (pinned != null) {
            for (Compilation compilation : compilationRepository.findAllByPinned(pinned, pageWithSomeElements)) {
                for (Long id : eventRepository.findByCompilationId(compilation.getId())) {
                    if (eventRepository.existsById(id)) {
                        Event event = eventRepository.findById(id).get();
                        EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
                        eventShortDto.setConfirmedRequests(eventService.getConfirmedRequestsCount(event));
                        eventShortDto.setViews(eventService.getViews(event));
                        events.add(eventShortDto);
                    }
                }
                CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);
                compilationDto.setEvents(events);
                compilations.add(compilationDto);
            }
        } else {
            for (Compilation compilation : compilationRepository.findAll(pageWithSomeElements)) {
                for (Long id : eventRepository.findByCompilationId(compilation.getId())) {
                    if (eventRepository.existsById(id)) {
                        Event event = eventRepository.findById(id).get();
                        EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
                        eventShortDto.setConfirmedRequests(eventService.getConfirmedRequestsCount(event));
                        eventShortDto.setViews(eventService.getViews(event));
                        events.add(eventShortDto);
                    }
                }
                CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilation);
                compilationDto.setEvents(events);
                compilations.add(compilationDto);
            }
        }
        return compilations;
    }

    @Override
    public CompilationDto getById(long compId) {
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(
                        String.format("Compilation with id=%d was not found", compId))));
        List<EventShortDto> events = new ArrayList<>();
        for (Long id : eventRepository.findByCompilationId(compId)) {
            if (eventRepository.existsById(id)) {
                Event event = eventRepository.findById(id).get();
                EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
                eventShortDto.setConfirmedRequests(eventService.getConfirmedRequestsCount(event));
                eventShortDto.setViews(eventService.getViews(event));
                events.add(eventShortDto);
            }
        }
        compilationDto.setEvents(events);
        return compilationDto;
    }

    @Override
    public CompilationDto save(NewCompilationDto newCompilationDto) {
        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null) {
            for (Long id : newCompilationDto.getEvents()) {
                if (eventRepository.existsById(id)) {
                    events.add(eventRepository.findById(id).get());
                }
            }
        }
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        compilation.setEvents(events);
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        for (Event event : events) {
            EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
            eventShortDto.setConfirmedRequests(eventService.getConfirmedRequestsCount(event));
            eventShortDto.setViews(eventService.getViews(event));
            eventShortDtos.add(eventShortDto);
        }
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
        compilationDto.setEvents(eventShortDtos);
        return compilationDto;
    }

    @Override
    public void delete(long compId) {
        if (compilationRepository.existsById(compId)) {
            compilationRepository.deleteById(compId);
        } else {
            throw new CompilationNotFoundException(
                    String.format("Compilation with id=%d was not found", compId));
        }
    }

    @Override
    public CompilationDto update(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation updateCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new CompilationNotFoundException(
                        String.format("Compilation with id=%d was not found", compId)));
        List<Event> events = new ArrayList<>();
        if (updateCompilationRequest.getEvents() != null) {
            events.addAll(eventRepository.findByIdIn(updateCompilationRequest.getEvents()));
            updateCompilation.setEvents(events);
        }
        if (updateCompilationRequest.getPinned() != null) {
            updateCompilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            updateCompilation.setTitle(updateCompilationRequest.getTitle());
        }
        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationRepository.save(updateCompilation));
        List<EventShortDto> eventShortDtos = new ArrayList<>();
        for (Event event : events) {
            EventShortDto eventShortDto = EventMapper.toEventShortDto(event);
            eventShortDto.setConfirmedRequests(eventService.getConfirmedRequestsCount(event));
            eventShortDto.setViews(eventService.getViews(event));
            eventShortDtos.add(eventShortDto);
        }
        compilationDto.setEvents(eventShortDtos);
        return compilationDto;
    }
}
