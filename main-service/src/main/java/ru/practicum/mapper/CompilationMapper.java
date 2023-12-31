package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.model.Compilation;

import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {
    public Compilation toCompilation(NewCompilationDto newCompilationDto) {
        return Compilation.builder()
                .pinned(newCompilationDto.isPinned())
                .title(newCompilationDto.getTitle())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents() != null
                        ? compilation.getEvents().stream()
                        .map(EventMapper::toEventShortDto)
                        .collect(Collectors.toSet())
                        : null)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
