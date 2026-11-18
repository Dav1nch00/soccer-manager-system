package com.soccermanager.common.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResultadoPartidoDTOTest {

    @Test
    void acceptsSerializedComputedScoreAndRecomputesIt() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> partido = Map.of(
                "nombreLocal", "Nacional",
                "golesLocal", 2,
                "nombreVisitante", "Millonarios",
                "golesVisitante", 1,
                "marcadorTexto", "stale server value"
        );

        List<ResultadoPartidoDTO> resultados = mapper.convertValue(
                List.of(partido), new TypeReference<List<ResultadoPartidoDTO>>() {});
        ResultadoPartidoDTO resultado = resultados.get(0);

        assertEquals("Nacional 2 - 1 Millonarios", resultado.getMarcadorTexto());
        assertEquals("Nacional 2 - 1 Millonarios",
                mapper.readTree(mapper.writeValueAsString(resultado)).get("marcadorTexto").asText());
    }
}
