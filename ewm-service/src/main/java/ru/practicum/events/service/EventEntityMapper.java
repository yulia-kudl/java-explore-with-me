package ru.practicum.events.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.categories.service.CategoryEntityMapper;
import ru.practicum.events.dto.*;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.events.entity.LocationEntity;
import ru.practicum.users.service.UserEntityMapper;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserEntityMapper.class, CategoryEntityMapper.class})
public interface EventEntityMapper {
    EventFullDto toFullDto(EventEntity entity);
    EventShortDto toShortDto(EventEntity entity);

    @Mapping(target = "category", ignore = true)
    EventEntity toEntity(NewEventDto dto);
    @Mapping(target = "category", ignore = true)
    EventEntity toEntity(UpdateEventUserRequest dto);
    @Mapping(target = "category", ignore = true)
    EventEntity toEntity(UpdateEventAdminRequest dto);


    // Location mapping
    Location toLocation(LocationEntity entity);

    LocationEntity toLocationEntity(Location dto);


}
