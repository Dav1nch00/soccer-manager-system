package com.soccermanager.server.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Repositorio de acceso a datos basado en el sistema de archivos del servidor.
 * Almacena cada perfil de mánager en su propio archivo físico JSON ('data/perfiles/<usuario>.json').
 * Implementa concurrencia segura mediante bloqueos por archivo (ReentrantReadWriteLock).
 */
public class PerfilArchivoRepository {

    private final Path directorioPerfiles;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> locksPorUsuario;

    public PerfilArchivoRepository() {
        this("data/perfiles");
    }

    public PerfilArchivoRepository(String rutaDirectorio) {
        this.directorioPerfiles = Paths.get(rutaDirectorio);
        this.locksPorUsuario = new ConcurrentHashMap<>();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        asegurarDirectorio();
    }

    private void asegurarDirectorio() {
        try {
            if (!Files.exists(directorioPerfiles)) {
                Files.createDirectories(directorioPerfiles);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo inicializar el directorio de perfiles: " + directorioPerfiles, e);
        }
    }

    private ReentrantReadWriteLock obtenerLock(String nombreUsuario) {
        String key = normalizarNombre(nombreUsuario);
        return locksPorUsuario.computeIfAbsent(key, k -> new ReentrantReadWriteLock());
    }

    private String normalizarNombre(String nombreUsuario) {
        if (nombreUsuario == null) return "";
        return nombreUsuario.trim().toLowerCase().replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private Path obtenerRutaArchivo(String nombreUsuario) {
        String archivo = normalizarNombre(nombreUsuario) + ".json";
        return directorioPerfiles.resolve(archivo);
    }

    /**
     * Verifica si existe físicamente el archivo del perfil en disco.
     */
    public boolean existePerfil(String nombreUsuario) {
        Path ruta = obtenerRutaArchivo(nombreUsuario);
        return Files.exists(ruta) && Files.isRegularFile(ruta);
    }

    /**
     * Guarda o actualiza atómicamente el archivo de perfil en formato JSON.
     */
    public void guardar(PerfilUsuario perfil) {
        if (perfil == null || perfil.getNombreUsuario() == null) {
            throw new IllegalArgumentException("El perfil o el nombre de usuario no pueden ser nulos.");
        }

        ReentrantReadWriteLock lock = obtenerLock(perfil.getNombreUsuario());
        lock.writeLock().lock();
        try {
            asegurarDirectorio();
            Path ruta = obtenerRutaArchivo(perfil.getNombreUsuario());
            objectMapper.writeValue(ruta.toFile(), perfil);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir el archivo de perfil: " + perfil.getNombreUsuario(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Carga y deserializa el perfil desde su archivo físico JSON.
     */
    public Optional<PerfilUsuario> buscarPorNombreUsuario(String nombreUsuario) {
        if (!existePerfil(nombreUsuario)) {
            return Optional.empty();
        }

        ReentrantReadWriteLock lock = obtenerLock(nombreUsuario);
        lock.readLock().lock();
        try {
            Path ruta = obtenerRutaArchivo(nombreUsuario);
            PerfilUsuario perfil = objectMapper.readValue(ruta.toFile(), PerfilUsuario.class);
            return Optional.ofNullable(perfil);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el archivo de perfil: " + nombreUsuario, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Lista todos los perfiles registrados escaneando el directorio de datos.
     */
    public List<PerfilUsuario> listarTodos() {
        List<PerfilUsuario> perfiles = new ArrayList<>();
        File carpeta = directorioPerfiles.toFile();
        File[] archivos = carpeta.listFiles((dir, name) -> name.endsWith(".json"));

        if (archivos != null) {
            for (File archivo : archivos) {
                try {
                    PerfilUsuario p = objectMapper.readValue(archivo, PerfilUsuario.class);
                    if (p != null) {
                        perfiles.add(p);
                    }
                } catch (IOException ignored) {
                    // Si algún archivo está corrupto o temporalmente bloqueado, se omite
                }
            }
        }
        return perfiles;
    }

    /**
     * Elimina el archivo de perfil (útil para pruebas y reseteo).
     */
    public boolean eliminarPerfil(String nombreUsuario) {
        ReentrantReadWriteLock lock = obtenerLock(nombreUsuario);
        lock.writeLock().lock();
        try {
            Path ruta = obtenerRutaArchivo(nombreUsuario);
            return Files.deleteIfExists(ruta);
        } catch (IOException e) {
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Path getDirectorioPerfiles() {
        return directorioPerfiles;
    }
}
