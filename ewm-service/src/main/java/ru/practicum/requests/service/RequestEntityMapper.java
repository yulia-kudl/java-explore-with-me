package ru.practicum.requests.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.categories.service.CategoryEntityMapper;
import ru.practicum.events.service.EventEntityMapper;
import ru.practicum.requests.dto.ParticipationRequestDto;
import ru.practicum.requests.entity.RequestEntity;
import ru.practicum.users.service.UserEntityMapper;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserEntityMapper.class, CategoryEntityMapper.class, EventEntityMapper.class})
public interface RequestEntityMapper {

    @Mapping( target = "event", ignore = true)
    RequestEntity toEntity(ParticipationRequestDto dto);

    @Mapping( target = "event", ignore = true)
    ParticipationRequestDto toDto(RequestEntity entity);
}
