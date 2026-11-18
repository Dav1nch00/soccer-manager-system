# Documentación del Proyecto: Simulador Táctico "Soccer Manager"

Bienvenido a la carpeta de especificaciones técnicas y diseño de arquitectura del proyecto **Simulador Táctico "Soccer Manager"** para la asignatura de **Arquitectura Cliente-Servidor**.

El sistema permite la simulación de jornadas de fútbol en dos modos: **Modo Individual (Solo vs IA)** y **Modo Multijugador (2 Jugadores en red)** con edición en paralelo y un **temporizador competitivo de 60 segundos** que se activa en cuanto el primer jugador confirma su táctica, además de autenticación mediante nombre y contraseña con **generación automática de archivo individual de perfil** (`data/perfiles/<usuario>.json`) para persistencia de datos.

---

## Índice de Documentos

1. [Especificación de Requisitos del Sistema (requisitos.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/requisitos.md)
   * Autenticación, creación automática de perfil en archivo y persistencia.
   * Modos de simulación: Individual (Solo vs IA) y Multijugador (2 Jugadores con edición simultánea y temporizador de 60s).
   * Requisitos Funcionales (RF-01 a RF-14).
   * Requisitos No Funcionales (RNF-01 a RNF-09: concurrencia, multithreading, persistencia en archivos, thread-safety, patrones GoF).
   * Restricciones técnicas y matriz de trazabilidad.

2. [Diseño de Arquitectura y Software (diseno.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/diseno.md)
   * Subsistema de perfiles y persistencia desacoplada en archivos JSON individuales.
   * Arquitectura de modos de juego: Modo 1 (Solo) y Modo 2 (2 Jugadores con `SalaEsperaMultiplayer`, temporizador de 60s con `ScheduledExecutorService` y fallback por timeout).
   * Arquitectura Micro: Patrón *Pipes & Filters* (Validación, Cálculo Táctico, Eventos 90 min, Desgaste y Estado, Consolidación y Persistencia).
   * Patrones de Diseño GoF:
     * **Builder:** `PlanDeJuegoBuilder` (Cliente).
     * **Facade:** `ScoutingFacade` (Servidor).
     * **Composite:** `Jugador` $\rightarrow$ `LineaTactica` $\rightarrow$ `Equipo` (Servidor).
     * **Strategy:** `EstrategiaTactica` (*Posesión*, *Contraataque*, *Presión Alta*, *Bloque Bajo* con contrarrestos).
     * **State:** `EstadoJugador` (*Óptimo*, *Cansado*, *Lesionado*, *Sancionado*).
   * Programación concurrente: `ExecutorService` + `Callable`, barreras de sincronización y exclusión mutua con `ReentrantLock`.
   * Protocolo de comunicación JSON y endpoints de login, modos, sala y simulación.
   * Estructura de paquetes multi-módulo incluyendo el directorio de datos `/data/perfiles/`.
   * Vistas del MVP ampliado (Login, Selector de Modo, Plantilla, Scouting, Pizarra Táctica con countdown, Resultados).

3. [Diagramas de Flujo y Procesos (diagrama_de_flujo.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/diagrama_de_flujo.md)
   * **Diagrama de Inicio de Sesión:** Autenticación y creación automática de archivo `<usuario>.json`.
   * **Diagrama de Selección de Modos y Sala:** Flujo con temporizador de 60 segundos y resolución por confirmación o timeout.
   * **Diagrama de Flujo Principal (Game Loop):** Ciclo de 4 fases con actualización final de perfiles en disco.
   * **Diagrama de Tuberías y Filtros:** Ejecución secuencial de los 5 filtros por partido.
   * **Diagrama de Secuencia 2 Jugadores:** Coordinación completa entre DT 1, DT 2, temporizador de 60s, archivos de perfil y pool de simulación.
   * **Diagrama de Transición de Estados:** Ciclo de vida y disponibilidad del jugador (*State*).
   * **Diagrama del Patrón Builder:** Validaciones tácticas locales en el cliente.

4. [Plan y Bloques de Desarrollo (bloques_de_desarrollo.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/bloques_de_desarrollo.md)
   * **Bloque 1:** Contratos y DTOs Compartidos (`common-dto`).
   * **Bloque 2:** Persistencia y Gestión de Perfiles en Archivo (`server-app/persistence`).
   * **Bloque 3:** Modelo de Dominio Deportivo y Patrones GoF (`Composite`, `Strategy`, `State`).
   * **Bloque 4:** Motor de Simulación `Pipes & Filters` y Concurrencia con `ExecutorService`.
   * **Bloque 5:** Coordinador de Sala, Modo 2P, Temporizador de 60s y `ScoutingFacade`.
   * **Bloque 6:** Capa de Red y API de Comunicación (Controladores REST/Sockets y Cliente de Red).
   * **Bloque 7:** Cliente Táctico, Patrón `Builder` y Vistas de Interfaz (MVP con widget de cuenta regresiva).
   * **Bloque 8:** Pruebas de Integración, Concurrencia, Persistencia y Calidad (QA).
   * Matriz de dependencias y ruta crítica secuencial de implementación.

---

## Bitácoras de Avance de Desarrollo

* [Avance Bloque 1: Contratos y DTOs Compartidos (avance_bloque_1.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/avance_bloque_1.md)
  * Qué se hizo (DTOs, Enums de formación, estrategia, estado, sala y modos).
  * Qué se usó (Java 17, Jackson JSR-310, Maven multi-módulo).
  * Errores y ajustes técnicos resueltos (evitar nulls, anticipar campos del timer de 60s).

* [Avance Bloque 2: Persistencia y Gestión de Perfiles en Archivo (avance_bloque_2.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/avance_bloque_2.md)
  * Qué se hizo (Entidad `PerfilUsuario`, `PerfilArchivoRepository`, `PerfilService`, `SeguridadUtil` y tests unitarios).
  * Qué se usó (Java NIO, Jackson pretty print, SHA-256 + Salt aleatorio, `ReentrantReadWriteLock`).
  * Errores y ajustes técnicos resueltos (sanitización de nombres de archivo y exclusión mutua de archivos).

* [Avance Bloque 3: Modelo de Dominio Deportivo y Patrones GoF (avance_bloque_3.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/avance_bloque_3.md)
  * Qué se hizo (Patrón Composite con `Jugador`, `LineaTactica`, `Equipo`, Patrón Strategy con matriz de contrarrestos, Patrón State para disponibilidad física y disciplinaria, y tests unitarios).
  * Qué se usó (Java 17 LTS, Composite, Strategy, State, Factory, JUnit 5).
  * Errores y ajustes técnicos resueltos (prevención de `NaN` en líneas vacías, blindaje de jugadores no aptos y automatización de suspensiones).

* [Avance Bloque 4: Motor de Simulación Pipes & Filters y Concurrencia (avance_bloque_4.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/avance_bloque_4.md)
  * Qué se hizo (Cadena de 5 filtros secuenciales en `PipelinePartidos`, `SimuladorJornada` con `ExecutorService`, `TareaPartido` Callable y tabla de posiciones thread-safe con `ReentrantLock`).
  * Qué se usó (Java 17 Concurrency, Thread Pool, Pipes & Filters, ReentrantLock, JUnit 5).
  * Errores y ajustes técnicos resueltos (auto-completado de alineaciones incompletas con canteranos y normalización de posesión de balón).

* [Avance Bloque 5: Coordinador de Sala, Modo 2P, Timer de 60s y ScoutingFacade (avance_bloque_5.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/avance_bloque_5.md)
  * Qué se hizo (`GestorModoYSala` con edición paralela, `TemporizadorJornada` concurrente de 60 segundos con resolución anticipada o timeout forzado, `ScoutingFacade` y tests unitarios).
  * Qué se usó (Java 17 Concurrency, ScheduledExecutorService, ReentrantLock, Patrón Facade, JUnit 5).
  * Errores y ajustes técnicos resueltos (prevención de doble simulación por colisión entre confirmación y timeout, y threads daemon para cierre limpio de JVM).

* [Avance Bloque 6: Capa de Red y API de Comunicación (avance_bloque_6.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/avance_bloque_6.md)
  * Qué se hizo (Servidor HTTP ligero multihilo con 8 endpoints REST JSON, `GestorLiga`, `MotorConexion` en el cliente con `HttpClient` y tests de integración end-to-end).
  * Qué se usó (Java 17 HttpServer, Java 11+ HttpClient, Jackson Databind, UTF-8, CORS, JUnit 5).
  * Errores y ajustes técnicos resueltos (asignación dinámica de puertos efímeros para evitar `BindException` y headers UTF-8 para nombres colombianos).

* [Avance Bloque 7: Cliente Táctico, Patrón Builder y Vistas UI (avance_bloque_7.md)](file:///c:/Users/davin/Desktop/estudio/semestre%2010/arq.%20clie.%20ser/proyecto_final/documentos/avance_bloque_7.md)
  * Qué se hizo (`PlanDeJuegoBuilder` con validaciones ergonómicas estrictas, 6 vistas de interfaz interactiva, widget de cuenta regresiva de 60s en tiempo real, app ejecutable `ClienteTacticoApp` y tests unitarios).
  * Qué se usó (Java 17 LTS, Patrón Builder, MotorConexion HTTP, Formato tabular con printf, JUnit 5).
  * Errores y ajustes técnicos resueltos (sondeo no invasivo en consola con retorno de carro `\r` para el temporizador y sincronización de IDs de titulares).
