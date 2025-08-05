package com.example.producer.mapper;

import com.example.producer.model.Jukebox;
import com.example.producer.model.dto.JukeboxRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class JukeboxMapperTest {

    private JukeboxMapper jukeboxMapper;

    @BeforeEach
    void setUp() {
        // ⚠️ Instancie le mapper MapStruct
        jukeboxMapper = Mappers.getMapper(JukeboxMapper.class);
    }

  /*  @Test
    void testToEntity() {
        JukeboxRequest dto = new JukeboxRequest();
        dto.setSerialNumber("SN-123");
        dto.setModel("Model-X");

        Jukebox entity = jukeboxMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("SN-123", entity.getSerialNumber());
        assertEquals("Model-X", entity.getModel());
    }

    @Test
    void testToDto() {
        Jukebox entity = new Jukebox();
        entity.setSerialNumber("SN-456");
        entity.setModel("Model-Y");

        JukeboxRequest dto = jukeboxMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals("SN-456", dto.getSerialNumber());
        assertEquals("Model-Y", dto.getModel());
    }
*/
    @Test
    void testNullInput() {
        assertNull(jukeboxMapper.toEntity(null));
        assertNull(jukeboxMapper.toDto(null));
    }
}
