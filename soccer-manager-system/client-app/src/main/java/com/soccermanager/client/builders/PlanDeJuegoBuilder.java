package com.soccermanager.client.builders;

import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.PlanDeJuegoDTO;
import com.soccermanager.common.enums.EstrategiaEnum;
import com.soccermanager.common.enums.FormacionEnum;
import com.soccermanager.common.enums.PosicionEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Patrón Creacional Builder.
 * Construye y valida paso a paso la configuración táctica en el cliente
 * antes de transmitir el objeto PlanDeJuegoDTO por la red hacia el servidor.
 */
public class PlanDeJuegoBuilder {

    private String clubId;
    private String nombreClub;
    private FormacionEnum formacion;
    private EstrategiaEnum estrategia;
    private final List<JugadorDTO> titulares;

    public PlanDeJuegoBuilder() {
        this.titulares = new ArrayList<>();
        this.formacion = FormacionEnum.F_4_4_2;
        this.estrategia = EstrategiaEnum.POSESION;
    }

    public PlanDeJuegoBuilder setClub(String clubId, String nombreClub) {
        this.clubId = clubId;
        this.nombreClub = nombreClub;
        return this;
    }

    public PlanDeJuegoBuilder setFormacion(FormacionEnum formacion) {
        this.formacion = formacion != null ? formacion : FormacionEnum.F_4_4_2;
        return this;
    }

    public PlanDeJuegoBuilder setEstrategia(EstrategiaEnum estrategia) {
        this.estrategia = estrategia != null ? estrategia : EstrategiaEnum.POSESION;
        return this;
    }

    /**
     * Agrega un futbolista a la lista de 11 titulares verificando disponibilidad y no duplicidad.
     */
    public PlanDeJuegoBuilder agregarTitular(JugadorDTO jugador) {
        if (jugador == null) {
            throw new IllegalArgumentException("El jugador no puede ser nulo.");
        }

        // 1. Validar que no esté repetido
        for (JugadorDTO t : titulares) {
            if (t.getIdJugador().equalsIgnoreCase(jugador.getIdJugador())) {
                throw new IllegalStateException("El jugador '" + jugador.getNombre() + "' ya está alineado como titular.");
            }
        }

        // 2. Validar que esté habilitado física y disciplinariamente (Patrón State)
        if (!jugador.isDisponible()) {
            throw new IllegalStateException("El jugador '" + jugador.getNombre() + "' no está disponible (" + jugador.getEstado().getEtiqueta() + ").");
        }

        // 3. Validar límite máximo de 11 futbolistas
        if (titulares.size() >= 11) {
            throw new IllegalStateException("La alineación ya cuenta con los 11 titulares reglamentarios.");
        }

        titulares.add(jugador);
        return this;
    }

    public PlanDeJuegoBuilder removerTitular(String jugadorId) {
        titulares.removeIf(j -> j.getIdJugador().equalsIgnoreCase(jugadorId));
        return this;
    }

    public PlanDeJuegoBuilder limpiarTitulares() {
        titulares.clear();
        return this;
    }

    /**
     * Valida exhaustivamente las reglas tácticas del fútbol y construye el DTO definitivo.
     *
     * @return PlanDeJuegoDTO verificado y listo para envío.
     * @throws IllegalStateException Si se infringe alguna regla reglamentaria.
     */
    public PlanDeJuegoDTO build() {
        if (clubId == null || clubId.trim().isEmpty()) {
            throw new IllegalStateException("Debe asignar el club correspondiente a la alineación.");
        }

        // Regla 1: Exactamente 11 jugadores
        if (titulares.size() != 11) {
            throw new IllegalStateException("La alineación debe tener exactamente 11 jugadores titulares (actuales: " + titulares.size() + ").");
        }

        // Regla 2: Unicidad absoluta (sin duplicados)
        Set<String> idsUnicos = new HashSet<>();
        for (JugadorDTO j : titulares) {
            if (!idsUnicos.add(j.getIdJugador())) {
                throw new IllegalStateException("Se detectaron jugadores duplicados en la alineación titular: " + j.getNombre());
            }
        }

        // Regla 3: Verificar cobertura por posiciones según la Formación seleccionada
        int porteros = 0;
        int defensas = 0;
        int medios = 0;
        int delanteros = 0;

        for (JugadorDTO j : titulares) {
            if (j.getPosicion() == PosicionEnum.PORTERO) porteros++;
            else if (j.getPosicion() == PosicionEnum.DEFENSA) defensas++;
            else if (j.getPosicion() == PosicionEnum.MEDIOCAMPISTA) medios++;
            else if (j.getPosicion() == PosicionEnum.DELANTERO) delanteros++;
        }

        if (porteros != formacion.getPorteros()) {
            throw new IllegalStateException("La alineación debe contener exactamente "
                    + formacion.getPorteros() + " arquero (actuales: " + porteros + ").");
        }
        validarCantidadPosiciones("defensas", formacion.getDefensas(), defensas);
        validarCantidadPosiciones("mediocampistas", formacion.getMediocampistas(), medios);
        validarCantidadPosiciones("delanteros", formacion.getDelanteros(), delanteros);

        return new PlanDeJuegoDTO(clubId, nombreClub, formacion, estrategia, new ArrayList<>(titulares));
    }

    private void validarCantidadPosiciones(String posicion, int esperados, int actuales) {
        if (actuales != esperados) {
            throw new IllegalStateException("La formación " + formacion.getEtiqueta() + " requiere "
                    + esperados + " " + posicion + " (alineados: " + actuales + ").");
        }
    }

    public int getCantidadTitularesActual() {
        return titulares.size();
    }

    public List<JugadorDTO> getTitularesActuales() {
        return new ArrayList<>(titulares);
    }

    public FormacionEnum getFormacion() {
        return formacion;
    }

    public EstrategiaEnum getEstrategia() {
        return estrategia;
    }
}
