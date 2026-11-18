package com.soccermanager.server.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidad criptográfica para el almacenamiento seguro de contraseñas en los archivos de perfil.
 * Utiliza SHA-256 con Salt aleatorio y codificación Base64.
 */
public final class SeguridadUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_BYTES = 16;

    private SeguridadUtil() {
    }

    /**
     * Genera un hash seguro en formato 'salt:hash' para una contraseña en texto plano.
     */
    public static String hashearPassword(String passwordPlana) {
        if (passwordPlana == null || passwordPlana.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] hash = calcularHash(passwordPlana, salt);

        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        return saltBase64 + ":" + hashBase64;
    }

    /**
     * Valida si una contraseña en texto plano coincide con el hash almacenado.
     */
    public static boolean verificarPassword(String passwordPlana, String hashAlmacenado) {
        if (passwordPlana == null || hashAlmacenado == null || !hashAlmacenado.contains(":")) {
            return false;
        }

        String[] partes = hashAlmacenado.split(":");
        if (partes.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(partes[0]);
        byte[] hashEsperado = Base64.getDecoder().decode(partes[1]);

        byte[] hashCalculado = calcularHash(passwordPlana, salt);

        return MessageDigest.isEqual(hashEsperado, hashCalculado);
    }

    private static byte[] calcularHash(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error crítico: Algoritmo SHA-256 no disponible en la JVM.", e);
        }
    }
}
