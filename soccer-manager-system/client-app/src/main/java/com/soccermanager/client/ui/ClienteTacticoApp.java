package com.soccermanager.client.ui;

import com.soccermanager.client.network.MotorConexion;
import com.soccermanager.common.dto.JugadorDTO;
import com.soccermanager.common.dto.ReporteRivalDTO;
import com.soccermanager.common.dto.SesionDTO;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Aplicación de entrada interactiva para el Director Técnico (MVP de Usuario Final).
 */
public class ClienteTacticoApp {

    public static void main(String[] args) {
        String urlServidor = args.length > 0 ? args[0] : "http://localhost:8080";
        MotorConexion conexion = new MotorConexion(urlServidor);
        Scanner scanner = new Scanner(System.in);

        // 1. Vista de Login / Auto-creación de perfil
        VistaLogin vistaLogin = new VistaLogin(conexion);
        SesionDTO sesion = vistaLogin.ejecutar(scanner);

        // 2. Vista de Selección de Modo (Solo vs 2 Jugadores)
        VistaSelectorModo vistaModo = new VistaSelectorModo(conexion);
        vistaModo.ejecutar(scanner);

        // Vistas funcionales del MVP
        VistaMiPlantilla vistaPlantilla = new VistaMiPlantilla();
        VistaCentroScouting vistaScouting = new VistaCentroScouting();
        VistaPizarraTactica vistaPizarra = new VistaPizarraTactica(conexion);
        VistaPanelResultados vistaResultados = new VistaPanelResultados();

        // Cargar plantilla inicial y pre-alinear 11 titulares
        try {
            List<JugadorDTO> plantilla = conexion.obtenerPlantilla(sesion.getClubId());
            vistaPizarra.inicializarAlineacionPredeterminada(sesion.getClubId(), sesion.getNombreClub(), plantilla);
        } catch (Exception e) {
            System.out.println(">> Advertencia al cargar plantilla inicial: " + e.getMessage());
        }

        // Bucle Principal del Mánager
        boolean ejecutando = true;
        while (ejecutando) {
            System.out.println("============================================================");
            System.out.println("           MENÚ PRINCIPAL DEL DIRECTOR TÉCNICO              ");
            System.out.println("   DT: " + sesion.getNombreUsuario() + " | Club: " + sesion.getNombreClub());
            System.out.println("============================================================");
            System.out.println("1. Mi Plantilla (Atributos, Energía y Disponibilidad)");
            System.out.println("2. Centro de Scouting (Informe de Inteligencia del Rival)");
            System.out.println("3. Pizarra Táctica (Alineación, Estrategia y SIMULAR)");
            System.out.println("4. Tabla de Posiciones y Resultados");
            System.out.println("5. Salir del Simulador");
            System.out.print("\nSeleccione una opción (1-5): ");

            String opcion = scanner.nextLine().trim();

            switch (opcion) {
                case "1":
                    try {
                        List<JugadorDTO> plantilla = conexion.obtenerPlantilla(sesion.getClubId());
                        vistaPlantilla.mostrar(plantilla, sesion.getNombreClub());
                    } catch (Exception e) {
                        System.out.println(">> Error al consultar plantilla: " + e.getMessage() + "\n");
                    }
                    break;
                case "2":
                    try {
                        // En la liga predeterminada, si somos club-nal el rival es club-mil
                        String rivalId = sesion.getClubId().equalsIgnoreCase("club-nal") ? "club-mil" : "club-nal";
                        ReporteRivalDTO scouting = conexion.obtenerScouting(rivalId);
                        vistaScouting.mostrar(scouting);
                    } catch (Exception e) {
                        System.out.println(">> Error al consultar scouting: " + e.getMessage() + "\n");
                    }
                    break;
                case "3":
                    boolean simulado = vistaPizarra.gestionarPizarra(scanner, sesion.getNombreUsuario());
                    if (simulado) {
                        try {
                            Map<String, Object> torneo = conexion.obtenerTablaYResultados();
                            vistaResultados.mostrar(torneo);
                        } catch (Exception e) {
                            System.out.println(">> Error al cargar resultados: " + e.getMessage() + "\n");
                        }
                    }
                    break;
                case "4":
                    try {
                        Map<String, Object> torneo = conexion.obtenerTablaYResultados();
                        vistaResultados.mostrar(torneo);
                    } catch (Exception e) {
                        System.out.println(">> Error al consultar torneo: " + e.getMessage() + "\n");
                    }
                    break;
                case "5":
                    System.out.println("\n>> Cerrando sesión del DT " + sesion.getNombreUsuario() + ". ¡Hasta pronto!");
                    ejecutando = false;
                    break;
                default:
                    System.out.println(">> Opción inválida. Ingrese un número del 1 al 5.\n");
                    break;
            }
        }
    }
}
