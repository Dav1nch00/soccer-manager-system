package com.soccermanager.server.engine.pipeline;

import com.soccermanager.common.dto.IncidenciaDTO;
import com.soccermanager.common.enums.TipoIncidenciaEnum;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.model.Jugador;

import java.util.List;
import java.util.Random;

/**
 * Filtro 3: Ejecuta la simulación cronológica de los 90 minutos de juego,
 * generando goles, asistencias, tiros, faltas, tarjetas y calificaciones individuales.
 */
public class FiltroSimulacionEventos implements FiltroPartido {

    private final Random random;

    public FiltroSimulacionEventos() {
        this(new Random());
    }

    public FiltroSimulacionEventos(Random random) {
        this.random = random;
    }

    @Override
    public ContextoPartido procesar(ContextoPartido contexto) {
        if (contexto == null || !contexto.isValido()) return contexto;

        // Potenciales ofensivos y defensivos efectivos
        double ataLocal = contexto.getEquipoLocal().calcularPoderOfensivo() * (1.0 + contexto.getModLocal().getBonoOfensivo()) * 1.05;
        double defVisitante = contexto.getEquipoVisitante().calcularPoderDefensivo() * (1.0 + contexto.getModVisitante().getBonoDefensivo());

        double ataVisitante = contexto.getEquipoVisitante().calcularPoderOfensivo() * (1.0 + contexto.getModVisitante().getBonoOfensivo());
        double defLocal = contexto.getEquipoLocal().calcularPoderDefensivo() * (1.0 + contexto.getModLocal().getBonoDefensivo());

        int tirosLocal = 0;
        int tirosVisitante = 0;

        // Simulación en 6 bloques cronológicos de 15 minutos (Min 1 a 90)
        for (int bloque = 1; bloque <= 6; bloque++) {
            int minutoBase = (bloque - 1) * 15;

            // 1. Ocasiones del Local
            double ratioLocal = Math.max(0.1, ataLocal / Math.max(1.0, defVisitante));
            if (random.nextDouble() < (0.35 * ratioLocal)) {
                tirosLocal++;
                int minutoExacto = minutoBase + 1 + random.nextInt(14);
                if (random.nextDouble() < (0.28 * ratioLocal)) {
                    // ¡Gol Local!
                    registrarGol(contexto, contexto.getEquipoLocal(), contexto.getClubLocal().getNombre(), minutoExacto, true);
                }
            }

            // 2. Ocasiones del Visitante
            double ratioVisitante = Math.max(0.1, ataVisitante / Math.max(1.0, defLocal));
            if (random.nextDouble() < (0.30 * ratioVisitante)) {
                tirosVisitante++;
                int minutoExacto = minutoBase + 1 + random.nextInt(14);
                if (random.nextDouble() < (0.28 * ratioVisitante)) {
                    // ¡Gol Visitante!
                    registrarGol(contexto, contexto.getEquipoVisitante(), contexto.getClubVisitante().getNombre(), minutoExacto, false);
                }
            }

            // 3. Faltas y Disciplina
            evaluarDisciplina(contexto, contexto.getEquipoLocal(), contexto.getClubLocal().getNombre(),
                    minutoBase + 1 + random.nextInt(14), contexto.getModLocal().getFactorFaltas());
            evaluarDisciplina(contexto, contexto.getEquipoVisitante(), contexto.getClubVisitante().getNombre(),
                    minutoBase + 1 + random.nextInt(14), contexto.getModVisitante().getFactorFaltas());
        }

        contexto.setTirosLocal(Math.max(tirosLocal, contexto.getGolesLocal()));
        contexto.setTirosVisitante(Math.max(tirosVisitante, contexto.getGolesVisitante()));

        // Asignar calificaciones a todos los jugadores participantes (escala 5.0 a 10.0)
        calcularCalificaciones(contexto);

        return contexto;
    }

    private void registrarGol(ContextoPartido ctx, Equipo equipoAnotador, String nombreClub, int minuto, boolean esLocal) {
        List<Jugador> titulares = equipoAnotador.getTodosLosTitulares();
        if (titulares.isEmpty()) return;

        // Seleccionar anotador preferente (delanteros o volantes ofensivos)
        Jugador anotador = seleccionarAnotador(titulares);
        Jugador asistente = seleccionarAsistente(titulares, anotador);

        anotador.registrarGol();
        if (asistente != null) {
            asistente.registrarAsistencia();
        }

        if (esLocal) {
            ctx.agregarGolLocal();
        } else {
            ctx.agregarGolVisitante();
        }

        String desc = asistente != null
                ? "¡Gooool de " + anotador.getNombre() + "! Asistencia de " + asistente.getNombre()
                : "¡Gooool de " + anotador.getNombre() + " con remate soberbio!";

        IncidenciaDTO inc = new IncidenciaDTO(minuto, TipoIncidenciaEnum.GOL, equipoAnotador.getClubId(),
                nombreClub, anotador.getIdJugador(), anotador.getNombre(), desc);
        if (asistente != null) {
            inc.setAsistenteId(asistente.getIdJugador());
            inc.setNombreAsistente(asistente.getNombre());
        }

        ctx.agregarIncidencia(inc);
    }

    private void evaluarDisciplina(ContextoPartido ctx, Equipo equipo, String nombreClub, int minuto, double factorFaltas) {
        double prob = 0.12 * factorFaltas;
        if (random.nextDouble() < prob) {
            List<Jugador> titulares = equipo.getTodosLosTitulares();
            if (titulares.isEmpty()) return;

            Jugador infractor = titulares.get(random.nextInt(titulares.size()));

            if (random.nextDouble() < 0.05) { // 5% de que sea roja directa
                infractor.registrarTarjetaRoja();
                ctx.agregarIncidencia(new IncidenciaDTO(minuto, TipoIncidenciaEnum.TARJETA_ROJA, equipo.getClubId(),
                        nombreClub, infractor.getIdJugador(), infractor.getNombre(), "Expulsión directa por falta descalificadora"));
            } else {
                infractor.registrarTarjetaAmarilla();
                ctx.agregarIncidencia(new IncidenciaDTO(minuto, TipoIncidenciaEnum.TARJETA_AMARILLA, equipo.getClubId(),
                        nombreClub, infractor.getIdJugador(), infractor.getNombre(), "Tarjeta amarilla por infracción táctica"));
            }
        }
    }

    private Jugador seleccionarAnotador(List<Jugador> jugadores) {
        // Ponderar por atributos de tiro
        int totalPeso = 0;
        for (Jugador j : jugadores) {
            totalPeso += Math.max(5, j.getTiro());
        }
        int r = random.nextInt(Math.max(1, totalPeso));
        int acumulado = 0;
        for (Jugador j : jugadores) {
            acumulado += Math.max(5, j.getTiro());
            if (r <= acumulado) return j;
        }
        return jugadores.get(0);
    }

    private Jugador seleccionarAsistente(List<Jugador> jugadores, Jugador anotador) {
        if (random.nextDouble() < 0.25) return null; // 25% jugadas individuales
        for (Jugador j : jugadores) {
            if (!j.getIdJugador().equals(anotador.getIdJugador()) && random.nextDouble() < 0.4) {
                return j;
            }
        }
        return null;
    }

    private void calcularCalificaciones(ContextoPartido ctx) {
        asignarNotasEquipo(ctx, ctx.getEquipoLocal(), ctx.getGolesLocal() > ctx.getGolesVisitante(), ctx.getGolesLocal() == ctx.getGolesVisitante());
        asignarNotasEquipo(ctx, ctx.getEquipoVisitante(), ctx.getGolesVisitante() > ctx.getGolesLocal(), ctx.getGolesLocal() == ctx.getGolesVisitante());
    }

    private void asignarNotasEquipo(ContextoPartido ctx, Equipo equipo, boolean gano, boolean empato) {
        double bonoEquipo = gano ? 0.8 : (empato ? 0.3 : -0.4);
        for (Jugador j : equipo.getTodosLosTitulares()) {
            double nota = 6.0 + bonoEquipo + ((j.getGoles() * 1.5) + (j.getAsistencias() * 0.8) - (j.getTarjetasAmarillas() * 0.5) - (j.getTarjetasRojas() * 2.0));
            nota = Math.max(4.0, Math.min(10.0, Math.round(nota * 10.0) / 10.0));
            j.registrarParticipacion(nota);
            ctx.registrarCalificacion(j.getIdJugador(), nota);
        }
    }
}
