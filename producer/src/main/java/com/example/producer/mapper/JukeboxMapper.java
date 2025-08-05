package com.example.producer.mapper;

import com.example.producer.model.Jukebox;
import com.example.producer.model.dto.JukeboxRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface JukeboxMapper {
    Jukebox toEntity(JukeboxRequest dto);
    JukeboxRequest toDto(Jukebox entity);
}
