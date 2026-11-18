package com.soccermanager.server.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.soccermanager.common.dto.*;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.ModoJuegoEnum;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;
import com.soccermanager.server.domain.strategy.EstrategiaFactory;
import com.soccermanager.server.engine.SimuladorJornada;
import com.soccermanager.server.engine.pipeline.ContextoPartido;
import com.soccermanager.server.lobby.GestorModoYSala;
import com.soccermanager.server.services.GestorLiga;
import com.soccermanager.server.services.PerfilService;
import com.soccermanager.server.services.ScoutingFacade;
import com.soccermanager.server.services.TablaPosicionesService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Servidor HTTP REST ligero basado en com.sun.net.httpserver.
 * Expone la API JSON para autenticación, gestión de sala, envío de tácticas y simulación de jornadas.
 */
public class ServidorHttp {

    private final int puerto;
    private HttpServer server;
    private final ObjectMapper mapper;

    // Servicios del backend
    private final PerfilService perfilService;
    private final GestorModoYSala gestorSala;
    private final GestorLiga gestorLiga;
    private final ScoutingFacade scoutingFacade;
    private final SimuladorJornada simuladorJornada;
    private final TablaPosicionesService tablaService;

    private List<ResultadoPartidoDTO> ultimosResultados;

    public ServidorHttp(int puerto) {
        this(puerto, new PerfilService(), new GestorModoYSala(), new GestorLiga(),
                new ScoutingFacade(), new SimuladorJornada(), new TablaPosicionesService());
    }

    public ServidorHttp(int puerto, PerfilService perfilService, GestorModoYSala gestorSala,
                        GestorLiga gestorLiga, ScoutingFacade scoutingFacade,
                        SimuladorJornada simuladorJornada, TablaPosicionesService tablaService) {
        this.puerto = puerto;
        this.perfilService = perfilService;
        this.gestorSala = gestorSala;
        this.gestorLiga = gestorLiga;
        this.scoutingFacade = scoutingFacade;
        this.simuladorJornada = simuladorJornada;
        this.tablaService = tablaService;
        this.ultimosResultados = new ArrayList<>();

        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Registrar clubes en la tabla de posiciones inicial
        for (Club c : gestorLiga.listarClubes()) {
            this.tablaService.registrarClub(c.getIdClub(), c.getNombre());
        }
    }

    public void iniciar() throws IOException {
        server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // Mapeo de rutas
        server.createContext("/api/sesion/login-o-registro", this::handleLoginORegistro);
        server.createContext("/api/partida/modo", this::handleConfigurarModo);
        server.createContext("/api/partida/sala-estado", this::handleConsultarSala);
        server.createContext("/api/club/", this::handleClub); // /api/club/{id}/plantilla
        server.createContext("/api/scouting/", this::handleScouting); // /api/scouting/{id}
        server.createContext("/api/jornada/plan-juego", this::handlePlanJuego);
        server.createContext("/api/jornada/simular", this::handleSimularJornada);
        server.createContext("/api/torneo/tabla-y-resultados", this::handleTablaYResultados);

        server.start();
    }

    public void detener() {
        if (server != null) {
            server.stop(0);
        }
        if (simuladorJornada != null) {
            simuladorJornada.apagar();
        }
    }

    public int getPuertoAsignado() {
        return server != null ? server.getAddress().getPort() : puerto;
    }

    // --- Controladores de Endpoints ---

    private void handleLoginORegistro(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarRespuesta(exchange, 405, Map.of("error", "Método no permitido"));
            return;
        }

        try {
            JsonNode body = mapper.readTree(exchange.getRequestBody());
            String usuario = body.path("usuario").asText();
            String password = body.path("password").asText();
            String clubId = body.path("clubId").asText(gestorLiga.obtenerPrimerClub().getIdClub());

            Club club = gestorLiga.obtenerClubPorId(clubId).orElse(gestorLiga.obtenerPrimerClub());

            SesionDTO sesion = perfilService.autenticarOCrearPerfil(usuario, password, club.getIdClub(), club.getNombre());

            // Registrar al DT en la sala
            SalaEstadoDTO sala = gestorSala.getEstadoSala();
            if (!sala.isDt1Conectado()) {
                gestorSala.conectarDT(1, usuario, club.getIdClub(), club.getNombre());
            } else if (!sala.isDt2Conectado() && !usuario.equalsIgnoreCase(sala.getNombreDt1())) {
                gestorSala.conectarDT(2, usuario, club.getIdClub(), club.getNombre());
            }

            enviarRespuesta(exchange, 200, sesion);
        } catch (SecurityException e) {
            enviarRespuesta(exchange, 401, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            enviarRespuesta(exchange, 400, Map.of("error", e.getMessage()));
        }
    }

    private void handleConfigurarModo(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarRespuesta(exchange, 405, Map.of("error", "Método no permitido"));
            return;
        }

        try {
            JsonNode body = mapper.readTree(exchange.getRequestBody());
            String modoStr = body.path("modo").asText("SOLO");
            ModoJuegoEnum modo = ModoJuegoEnum.valueOf(modoStr.toUpperCase());
            gestorSala.configurarModo(modo);

            enviarRespuesta(exchange, 200, gestorSala.getEstadoSala());
        } catch (Exception e) {
            enviarRespuesta(exchange, 400, Map.of("error", "Modo inválido: " + e.getMessage()));
        }
    }

    private void handleConsultarSala(HttpExchange exchange) throws IOException {
        enviarRespuesta(exchange, 200, gestorSala.getEstadoSala());
    }

    private void handleClub(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath(); // /api/club/{id}/plantilla
        String[] partes = path.split("/");
        if (partes.length >= 5 && "plantilla".equalsIgnoreCase(partes[4])) {
            String clubId = partes[3];
            Optional<Club> optClub = gestorLiga.obtenerClubPorId(clubId);
            if (optClub.isPresent()) {
                List<JugadorDTO> lista = optClub.get().getPlantilla().stream()
                        .map(Jugador::toDTO)
                        .collect(Collectors.toList());
                enviarRespuesta(exchange, 200, lista);
                return;
            }
        }
        enviarRespuesta(exchange, 404, Map.of("error", "Club no encontrado"));
    }

    private void handleScouting(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath(); // /api/scouting/{id}
        String[] partes = path.split("/");
        if (partes.length >= 4) {
            String rivalId = partes[3];
            Optional<Club> optRival = gestorLiga.obtenerClubPorId(rivalId);
            if (optRival.isPresent()) {
                ReporteRivalDTO rep = scoutingFacade.generarReporteRival(optRival.get(), ultimosResultados);
                enviarRespuesta(exchange, 200, rep);
                return;
            }
        }
        enviarRespuesta(exchange, 404, Map.of("error", "Rival no encontrado"));
    }

    private void handlePlanJuego(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarRespuesta(exchange, 405, Map.of("error", "Método no permitido"));
            return;
        }

        try {
            PlanDeJuegoDTO plan = mapper.readValue(exchange.getRequestBody(), PlanDeJuegoDTO.class);
            String query = exchange.getRequestURI().getQuery();
            String usuario = extraerParametro(query, "usuario");
            if (usuario == null) usuario = plan.getNombreClub();

            gestorSala.guardarPlanTactico(usuario, plan);
            enviarRespuesta(exchange, 200, Map.of("estado", "REGISTRADO", "mensaje", "Plan táctico recibido"));
        } catch (Exception e) {
            enviarRespuesta(exchange, 400, Map.of("error", e.getMessage()));
        }
    }

    private void handleSimularJornada(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            enviarRespuesta(exchange, 405, Map.of("error", "Método no permitido"));
            return;
        }

        try {
            JsonNode body = mapper.readTree(exchange.getRequestBody());
            String usuario = body.path("usuario").asText();

            gestorSala.solicitarSimulacion(usuario, this::ejecutarSimulacionCompleta);

            enviarRespuesta(exchange, 200, gestorSala.getEstadoSala());
        } catch (IllegalStateException e) {
            enviarRespuesta(exchange, 409, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            enviarRespuesta(exchange, 400, Map.of("error", e.getMessage()));
        }
    }

    private void ejecutarSimulacionCompleta() {
        try {
            List<Club> clubes = gestorLiga.listarClubes();
            List<ContextoPartido> partidos = new ArrayList<>();

            // Organizar 2 partidos (4 equipos)
            if (clubes.size() >= 4) {
                Club c1 = clubes.get(0);
                Club c2 = clubes.get(1);
                Club c3 = clubes.get(2);
                Club c4 = clubes.get(3);

                partidos.add(crearContextoDesdePlanes(c1, c2, 1));
                partidos.add(crearContextoDesdePlanes(c3, c4, 1));
            }

            ultimosResultados = simuladorJornada.simularJornada(partidos);

            // Actualizar tabla y perfiles
            for (ResultadoPartidoDTO res : ultimosResultados) {
                tablaService.actualizarResultado(res);

                // Actualizar perfiles de DTs
                SalaEstadoDTO sala = gestorSala.getEstadoSala();
                if (sala.getNombreDt1() != null) {
                    boolean esLocal = sala.getClubDt1() != null && sala.getClubDt1().equalsIgnoreCase(res.getNombreLocal());
                    perfilService.registrarResultadoPartido(sala.getNombreDt1(), res, esLocal);
                }
                if (sala.getNombreDt2() != null) {
                    boolean esLocal = sala.getClubDt2() != null && sala.getClubDt2().equalsIgnoreCase(res.getNombreLocal());
                    perfilService.registrarResultadoPartido(sala.getNombreDt2(), res, esLocal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ContextoPartido crearContextoDesdePlanes(Club local, Club visitante, int jornada) {
        Equipo eqLoc = new Equipo(local.getIdClub(), local.getNombre(), local.getFormacionPredeterminada());
        for (Jugador j : local.getPlantilla()) eqLoc.agregarTitular(j);

        Equipo eqVis = new Equipo(visitante.getIdClub(), visitante.getNombre(), visitante.getFormacionPredeterminada());
        for (Jugador j : visitante.getPlantilla()) eqVis.agregarTitular(j);

        return new ContextoPartido("part-" + UUID.randomUUID().toString().substring(0, 8),
                jornada, local, visitante, eqLoc, eqVis,
                EstrategiaFactory.obtenerEstrategia(local.getEstrategiaPredeterminada()),
                EstrategiaFactory.obtenerEstrategia(visitante.getEstrategiaPredeterminada()));
    }

    private void handleTablaYResultados(HttpExchange exchange) throws IOException {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("tabla", tablaService.obtenerTablaOrdenada());
        respuesta.put("resultadosUltimaFecha", ultimosResultados);
        respuesta.put("goleadores", tablaService.obtenerGoleadores());

        enviarRespuesta(exchange, 200, respuesta);
    }

    private void enviarRespuesta(HttpExchange exchange, int statusCode, Object data) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(data);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String extraerParametro(String query, String clave) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] par = param.split("=");
            if (par.length == 2 && par[0].equalsIgnoreCase(clave)) {
                return par[1];
            }
        }
        return null;
    }
}
