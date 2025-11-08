package ru.practicum.service;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.HitRequestDTO;
import ru.practicum.entity.HitEntity;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HitEntityMapper {

    //HitRequestDTO toDTO(HitEntity hitEntity);

    HitEntity toEntity(HitRequestDTO requestDTO);


}
