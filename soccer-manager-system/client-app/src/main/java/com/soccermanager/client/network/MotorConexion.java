package com.soccermanager.client.network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.soccermanager.common.dto.*;
import com.soccermanager.common.enums.ModoJuegoEnum;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP para la aplicación de escritorio del Mánager.
 * Consume los servicios REST del servidor para login, sala, scouting y simulación.
 */
public class MotorConexion {

    private final String urlBase;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public MotorConexion(String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    public SesionDTO loginORegistro(String usuario, String password, String clubId) throws IOException, InterruptedException {
        Map<String, String> body = Map.of(
                "usuario", usuario,
                "password", password,
                "clubId", clubId != null ? clubId : ""
        );
        String json = mapper.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/sesion/login-o-registro"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return mapper.readValue(resp.body(), SesionDTO.class);
        } else {
            throw new IOException("Error en autenticación (" + resp.statusCode() + "): " + resp.body());
        }
    }

    public SalaEstadoDTO configurarModo(ModoJuegoEnum modo) throws IOException, InterruptedException {
        Map<String, String> body = Map.of("modo", modo.name());
        String json = mapper.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/partida/modo"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), SalaEstadoDTO.class);
    }

    public SalaEstadoDTO consultarEstadoSala() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/partida/sala-estado"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), SalaEstadoDTO.class);
    }

    public List<JugadorDTO> obtenerPlantilla(String clubId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/club/" + clubId + "/plantilla"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), new TypeReference<List<JugadorDTO>>() {});
    }

    public ReporteRivalDTO obtenerScouting(String rivalId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/scouting/" + rivalId))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), ReporteRivalDTO.class);
    }

    public void enviarPlanJuego(String usuario, PlanDeJuegoDTO plan) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(plan);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/jornada/plan-juego?usuario=" + usuario))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Error al enviar plan de juego: " + resp.body());
        }
    }

    public SalaEstadoDTO solicitarSimulacion(String usuario) throws IOException, InterruptedException {
        Map<String, String> body = Map.of("usuario", usuario);
        String json = mapper.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/jornada/simular"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() == 200) {
            return mapper.readValue(resp.body(), SalaEstadoDTO.class);
        } else {
            throw new IOException("Error al simular (" + resp.statusCode() + "): " + resp.body());
        }
    }

    public Map<String, Object> obtenerTablaYResultados() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(urlBase + "/api/torneo/tabla-y-resultados"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
    }

    public String getUrlBase() {
        return urlBase;
    }
}
