package com.soccermanager.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlanDeJuegoDTOTest {

    @Test
    void serializesOnlyContractFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PlanDeJuegoDTO plan = new PlanDeJuegoDTO();

        String json = mapper.writeValueAsString(plan);

        assertTrue(json.contains("\"titularesIds\""));
        assertTrue(json.contains("\"titulares\""));
        assertFalse(json.contains("\"cantidadTitulares\""));
    }
}
