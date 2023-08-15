package ru.practicum.service;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getAll(Boolean pinned, int from, int size);

    CompilationDto getById(long compId);

    CompilationDto save(NewCompilationDto newCompilationDto);

    void delete(long compId);

    CompilationDto update(long compId, UpdateCompilationRequest updateCompilationRequest);
}
