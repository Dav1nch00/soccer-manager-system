package com.soccermanager.server.engine.pipeline;

import com.soccermanager.common.dto.IncidenciaDTO;
import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.server.domain.model.Club;
import com.soccermanager.server.domain.model.Equipo;
import com.soccermanager.server.domain.strategy.EstrategiaTactica;
import com.soccermanager.server.domain.strategy.ModificadorTactico;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Objeto de contexto que fluye a través de la tubería de filtros (Pipes & Filters).
 * Almacena el estado intermedio y mutable de la simulación de un partido.
 */
public class ContextoPartido implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idPartido;
    private int jornadaNumero;

    // Equipos y Dominio
    private Club clubLocal;
    private Club clubVisitante;
    private Equipo equipoLocal;
    private Equipo equipoVisitante;
    private EstrategiaTactica estrategiaLocal;
    private EstrategiaTactica estrategiaVisitante;

    // Modificadores calculados
    private ModificadorTactico modLocal;
    private ModificadorTactico modVisitante;

    // Métricas del partido
    private int golesLocal;
    private int golesVisitante;
    private int tirosLocal;
    private int tirosVisitante;
    private int posesionLocal;
    private int posesionVisitante;

    // Eventos y notas
    private List<IncidenciaDTO> incidencias;
    private Map<String, Double> calificaciones; // jugadorId -> nota (1.0 a 10.0)

    // Estado del pipeline
    private boolean valido;
    private String mensajeValidacion;
    private ResultadoPartidoDTO resultadoFinal;

    public ContextoPartido(String idPartido, int jornadaNumero, Club clubLocal, Club clubVisitante,
                           Equipo equipoLocal, Equipo equipoVisitante,
                           EstrategiaTactica estrategiaLocal, EstrategiaTactica estrategiaVisitante) {
        this.idPartido = idPartido;
        this.jornadaNumero = jornadaNumero;
        this.clubLocal = clubLocal;
        this.clubVisitante = clubVisitante;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
        this.estrategiaLocal = estrategiaLocal;
        this.estrategiaVisitante = estrategiaVisitante;

        this.incidencias = new ArrayList<>();
        this.calificaciones = new HashMap<>();
        this.valido = true;
        this.posesionLocal = 50;
        this.posesionVisitante = 50;
    }

    public void agregarGolLocal() {
        this.golesLocal++;
    }

    public void agregarGolVisitante() {
        this.golesVisitante++;
    }

    public void agregarIncidencia(IncidenciaDTO inc) {
        this.incidencias.add(inc);
    }

    public void registrarCalificacion(String jugadorId, double nota) {
        this.calificaciones.put(jugadorId, Math.max(1.0, Math.min(10.0, nota)));
    }

    // Getters y Setters
    public String getIdPartido() {
        return idPartido;
    }

    public int getJornadaNumero() {
        return jornadaNumero;
    }

    public Club getClubLocal() {
        return clubLocal;
    }

    public Club getClubVisitante() {
        return clubVisitante;
    }

    public Equipo getEquipoLocal() {
        return equipoLocal;
    }

    public Equipo getEquipoVisitante() {
        return equipoVisitante;
    }

    public EstrategiaTactica getEstrategiaLocal() {
        return estrategiaLocal;
    }

    public void setEstrategiaLocal(EstrategiaTactica estrategiaLocal) {
        this.estrategiaLocal = estrategiaLocal;
    }

    public EstrategiaTactica getEstrategiaVisitante() {
        return estrategiaVisitante;
    }

    public void setEstrategiaVisitante(EstrategiaTactica estrategiaVisitante) {
        this.estrategiaVisitante = estrategiaVisitante;
    }

    public ModificadorTactico getModLocal() {
        return modLocal;
    }

    public void setModLocal(ModificadorTactico modLocal) {
        this.modLocal = modLocal;
    }

    public ModificadorTactico getModVisitante() {
        return modVisitante;
    }

    public void setModVisitante(ModificadorTactico modVisitante) {
        this.modVisitante = modVisitante;
    }

    public int getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(int golesLocal) {
        this.golesLocal = golesLocal;
    }

    public int getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(int golesVisitante) {
        this.golesVisitante = golesVisitante;
    }

    public int getTirosLocal() {
        return tirosLocal;
    }

    public void setTirosLocal(int tirosLocal) {
        this.tirosLocal = tirosLocal;
    }

    public int getTirosVisitante() {
        return tirosVisitante;
    }

    public void setTirosVisitante(int tirosVisitante) {
        this.tirosVisitante = tirosVisitante;
    }

    public int getPosesionLocal() {
        return posesionLocal;
    }

    public void setPosesionLocal(int posesionLocal) {
        this.posesionLocal = posesionLocal;
    }

    public int getPosesionVisitante() {
        return posesionVisitante;
    }

    public void setPosesionVisitante(int posesionVisitante) {
        this.posesionVisitante = posesionVisitante;
    }

    public List<IncidenciaDTO> getIncidencias() {
        return incidencias;
    }

    public Map<String, Double> getCalificaciones() {
        return calificaciones;
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getMensajeValidacion() {
        return mensajeValidacion;
    }

    public void setMensajeValidacion(String mensajeValidacion) {
        this.mensajeValidacion = mensajeValidacion;
    }

    public ResultadoPartidoDTO getResultadoFinal() {
        return resultadoFinal;
    }

    public void setResultadoFinal(ResultadoPartidoDTO resultadoFinal) {
        this.resultadoFinal = resultadoFinal;
    }
}
