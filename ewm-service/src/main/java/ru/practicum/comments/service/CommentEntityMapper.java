package ru.practicum.comments.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentShortDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.dto.UpdateCommentDto;
import ru.practicum.comments.entity.CommentEntity;
import ru.practicum.events.service.EventEntityMapper;
import ru.practicum.users.service.UserEntityMapper;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserEntityMapper.class, EventEntityMapper.class})

public interface CommentEntityMapper {

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "event.id", source = "eventId")
    CommentEntity toEntity(NewCommentDto dto);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "event", ignore = true)
    CommentEntity toEntity(UpdateCommentDto dto);

    CommentDto toDto(CommentEntity entity);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "authorId", source = "author.id")
    CommentShortDto toShortDto(CommentEntity entity);
}
