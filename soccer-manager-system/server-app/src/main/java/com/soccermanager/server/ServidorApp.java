package com.soccermanager.server;

import com.soccermanager.server.api.ServidorHttp;

import java.io.IOException;

/**
 * Punto de entrada ejecutable para el Servidor Central (Authoritative Server).
 */
public class ServidorApp {

    public static void main(String[] args) {
        int puerto = 8080;
        if (args.length > 0) {
            try {
                puerto = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println(">> Puerto inválido. Usando puerto por defecto: 8080");
            }
        }

        try {
            ServidorHttp servidor = new ServidorHttp(puerto);
            servidor.iniciar();

            System.out.println("============================================================");
            System.out.println("      SERVIDOR 'SOCCER MANAGER' INICIADO CORRECTAMENTE       ");
            System.out.println("============================================================");
            System.out.println(">> Modo: Authoritative Server (Headless)");
            System.out.println(">> Escuchando peticiones en: http://localhost:" + puerto);
            System.out.println(">> Directorio de perfiles: data/perfiles/");
            System.out.println(">> API Endpoints:");
            System.out.println("   * POST /api/sesion/login-o-registro");
            System.out.println("   * POST /api/partida/modo");
            System.out.println("   * GET  /api/partida/sala-estado");
            System.out.println("   * GET  /api/club/{id}/plantilla");
            System.out.println("   * GET  /api/scouting/{id}");
            System.out.println("   * POST /api/jornada/plan-juego");
            System.out.println("   * POST /api/jornada/simular");
            System.out.println("   * GET  /api/torneo/tabla-y-resultados");
            System.out.println("============================================================");
            System.out.println("Presione Ctrl+C en esta terminal para detener el servidor.\n");

            // Hook de apagado ordenado
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n>> Deteniendo Servidor 'Soccer Manager' ordenadamente...");
                servidor.detener();
                System.out.println(">> Servidor detenido con éxito.");
            }));

        } catch (IOException e) {
            System.err.println(">> Error crítico al iniciar el servidor en el puerto " + puerto + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
