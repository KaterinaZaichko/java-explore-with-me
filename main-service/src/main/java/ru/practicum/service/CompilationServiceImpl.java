package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CompilationDto> getAll(Boolean pinned, int from, int size) {
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        List<CompilationDto> compilations;
        if (pinned != null) {
            compilations = compilationRepository.findAllByPinned(pinned, pageWithSomeElements).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        } else {
            compilations = compilationRepository.findAll(pageWithSomeElements).stream()
                    .map(CompilationMapper::toCompilationDto)
                    .collect(Collectors.toList());
        }
        return compilations;
    }

    @Override
    public CompilationDto getById(long compId) {
        return CompilationMapper.toCompilationDto(compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(
                        "Compilation with id=%d was not found", compId))));
    }

    @Override
    public CompilationDto save(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto);
        if (newCompilationDto.getEvents() != null) {
            compilation.setEvents(eventRepository.findByIdIn(newCompilationDto.getEvents()));
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void delete(long compId) {
        if (compilationRepository.existsById(compId)) {
            compilationRepository.deleteById(compId);
        } else {
            throw new EntityNotFoundException(String.format("Compilation with id=%d was not found", compId));
        }
    }

    @Override
    public CompilationDto update(long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation updateCompilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Compilation with id=%d was not found", compId)));
        if (updateCompilationRequest.getEvents() != null) {
            updateCompilation.setEvents(eventRepository.findByIdIn(updateCompilationRequest.getEvents()));
        }
        if (updateCompilationRequest.getPinned() != null) {
            updateCompilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null && !updateCompilationRequest.getTitle().isBlank()) {
            updateCompilation.setTitle(updateCompilationRequest.getTitle());
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(updateCompilation));
    }
}
