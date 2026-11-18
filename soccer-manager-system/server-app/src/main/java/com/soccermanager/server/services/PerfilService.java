package com.soccermanager.server.services;

import com.soccermanager.common.dto.PerfilDTO;
import com.soccermanager.common.dto.ResultadoPartidoDTO;
import com.soccermanager.common.dto.SesionDTO;
import com.soccermanager.server.persistence.PerfilArchivoRepository;
import com.soccermanager.server.persistence.PerfilUsuario;
import com.soccermanager.server.persistence.SeguridadUtil;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de negocio encargado de la gestión de perfiles, autenticación rápida y
 * actualización del rendimiento en disco tras los encuentros de liga.
 */
public class PerfilService {

    private final PerfilArchivoRepository repository;

    public PerfilService() {
        this(new PerfilArchivoRepository());
    }

    public PerfilService(PerfilArchivoRepository repository) {
        this.repository = repository;
    }

    /**
     * Autentica al mánager si su perfil ya existe, o genera automáticamente un nuevo
     * archivo físico ('data/perfiles/<usuario>.json') si es la primera vez que ingresa.
     *
     * @param usuario Nombre de usuario ingresado.
     * @param password Contraseña en texto plano.
     * @param clubIdDefault Identificador del club a asignar si es nuevo perfil.
     * @param nombreClubDefault Nombre del club a asignar si es nuevo perfil.
     * @return SesionDTO con los datos de sesión activa.
     */
    public SesionDTO autenticarOCrearPerfil(String usuario, String password, String clubIdDefault, String nombreClubDefault) {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña es obligatoria.");
        }

        String usuarioNormalizado = usuario.trim();

        // 1. Caso Usuario Nuevo: Creación automática del archivo de perfil
        if (!repository.existePerfil(usuarioNormalizado)) {
            String idPerfil = "usr-" + UUID.randomUUID().toString().substring(0, 8);
            String passHash = SeguridadUtil.hashearPassword(password);

            PerfilUsuario nuevoPerfil = new PerfilUsuario(
                    idPerfil,
                    usuarioNormalizado,
                    passHash,
                    clubIdDefault != null ? clubIdDefault : "club-auto",
                    nombreClubDefault != null ? nombreClubDefault : "Club Deportivo"
            );

            repository.guardar(nuevoPerfil);

            String idSesion = "ses-" + UUID.randomUUID().toString();
            return new SesionDTO(
                    idSesion,
                    nuevoPerfil.getIdPerfil(),
                    nuevoPerfil.getNombreUsuario(),
                    nuevoPerfil.getClubId(),
                    nuevoPerfil.getNombreClub(),
                    true // perfilNuevoCreado = true
            );
        }

        // 2. Caso Usuario Existente: Carga y verificación de credenciales
        Optional<PerfilUsuario> optPerfil = repository.buscarPorNombreUsuario(usuarioNormalizado);
        if (optPerfil.isEmpty()) {
            throw new IllegalStateException("Error al leer el perfil existente de: " + usuarioNormalizado);
        }

        PerfilUsuario perfil = optPerfil.get();

        if (!SeguridadUtil.verificarPassword(password, perfil.getPasswordHash())) {
            throw new SecurityException("Contraseña incorrecta para el usuario: " + usuarioNormalizado);
        }

        if (clubIdDefault != null
                && clubIdDefault.equals(perfil.getClubId())
                && nombreClubDefault != null
                && !nombreClubDefault.equals(perfil.getNombreClub())) {
            perfil.setNombreClub(nombreClubDefault);
        }

        perfil.setUltimaConexion(LocalDateTime.now());
        repository.guardar(perfil);

        String idSesion = "ses-" + UUID.randomUUID().toString();
        return new SesionDTO(
                idSesion,
                perfil.getIdPerfil(),
                perfil.getNombreUsuario(),
                perfil.getClubId(),
                perfil.getNombreClub(),
                false // perfilNuevoCreado = false
        );
    }

    /**
     * Consulta el perfil de un usuario y lo retorna como DTO.
     */
    public Optional<PerfilDTO> obtenerPerfil(String usuario) {
        return repository.buscarPorNombreUsuario(usuario).map(PerfilUsuario::toDTO);
    }

    /**
     * Actualiza el histórico de partidos, goles y puntos en el archivo del mánager tras disputar una jornada.
     */
    public void registrarResultadoPartido(String usuario, ResultadoPartidoDTO resultado, boolean esLocal) {
        Optional<PerfilUsuario> optPerfil = repository.buscarPorNombreUsuario(usuario);
        if (optPerfil.isEmpty()) {
            return;
        }

        PerfilUsuario perfil = optPerfil.get();
        int golesPropios = esLocal ? resultado.getGolesLocal() : resultado.getGolesVisitante();
        int golesRival = esLocal ? resultado.getGolesVisitante() : resultado.getGolesLocal();

        if (golesPropios > golesRival) {
            perfil.registrarVictoria(golesPropios, golesRival);
        } else if (golesPropios == golesRival) {
            perfil.registrarEmpate(golesPropios, golesRival);
        } else {
            perfil.registrarDerrota(golesPropios, golesRival);
        }

        repository.guardar(perfil);
    }

    public PerfilArchivoRepository getRepository() {
        return repository;
    }
}
