# Bitácora de Avance - Bloque 8: Pruebas de Integración, Concurrencia y Calidad (QA)

**Proyecto:** Simulador Táctico "Soccer Manager"  
**Módulo:** `server-app` / Suite de Pruebas Integradas y Punto de Entrada Ejecutable  
**Fecha de Ejecución:** Noviembre 2026  
**Estado:** Completado (8 de 8 Bloques Finalizados al 100%)

---

## 1. ¿Qué se hizo?

En este octavo y último bloque se implementó la capa de empaquetado ejecutable y la suite de aseguramiento de calidad (QA) e integración continua del sistema completo, validando la interacción armónica entre todos los módulos desarrollados en los bloques 1 al 7:

1. **Punto de Entrada Ejecutable del Servidor (`ServidorApp.java`):**
   * Se desarrolló la clase principal ejecutable (`public static void main`) para el servidor central autoritativo (`com.soccermanager.server.ServidorApp`).
   * Soporta parametrización de puerto mediante argumentos de línea de comandos (por defecto `8080`).
   * Inicializa el servidor HTTP embebido multihilo (`ServidorHttp`) exponiendo los 8 endpoints REST JSON.
   * Incorpora un `ShutdownHook` de la JVM (`Runtime.getRuntime().addShutdownHook`) para garantizar el cierre ordenado de los pools de hilos (`ExecutorService`), temporizadores y canales de red ante interrupciones (Ctrl+C).

2. **Suite de Integración y Pruebas de Estrés (`SimuladorCompletoIntegrationTest.java`):**
   * **Simulación Concurrente Masiva (Estrés de Multithreading):** Se diseñó una prueba con 10 partidos simultáneos (20 clubes en competencia paralela) a través de `SimuladorJornada` con `ExecutorService`. Se comprobó que el tiempo de ejecución es sub-segundo y que no se presentan condiciones de carrera (*race conditions*).
   * **Actualización Concurrente de Clasificación con Barreras:** Se utilizó un grupo de 8 hilos de escritura simultáneos coordinados por un `CountDownLatch` para impactar la tabla de posiciones protegida por `ReentrantLock` en `TablaPosicionesService`.
   * **Validación de Invariantes Deportivos y Matemáticos:** Se verificó automáticamente que la sumatoria total de goles a favor en la liga coincida exactamente con la sumatoria de goles en contra ($\sum GolesFavor = \sum GolesContra$), y que cada club registre rigurosamente un partido jugado.
   * **Pruebas de Concurrencia sobre el Sistema de Archivos JSON:** Se ejecutaron 10 hilos simultáneos creando perfiles y persistiendo resultados en `data/perfiles_qa_test/` con `PerfilService` y `PerfilArchivoRepository`. Se comprobó la integridad de los archivos JSON mediante bloqueos de lectura/escritura (`ReentrantReadWriteLock`).
   * **Validación de Carrera y Cancelación del Temporizador de 60s:** Se simuló el escenario competitivo donde dos DTs entran a la sala multijugador, el primer DT dispara la cuenta regresiva de 60 segundos y el segundo confirma a tiempo, evaluando la cancelación limpia del `ScheduledFuture` y evitando ejecuciones dobles.
   * **Prueba de Persistencia Histórica Multi-Jornada:** Se evaluó el ciclo de vida de un mánager a lo largo de 3 jornadas consecutivas (victoria, empate y derrota), confirmando la consistencia de estadísticas acumuladas (puntos, goles y récord) escritas físicamente en su archivo `.json`.

---

## 2. ¿Qué se usó?

* **Java 17 LTS:**
  * Primitivas de concurrencia avanzada: `CountDownLatch`, `AtomicBoolean`, `Executors.newFixedThreadPool`, `ScheduledExecutorService`.
  * Bloqueos explícitos: `ReentrantLock` para la tabla de posiciones y `ReentrantReadWriteLock` para los archivos de perfil en disco.
  * APIs modernas de archivos: `java.nio.file.Files`, `java.nio.file.Path`, `java.nio.file.Paths` con limpieza recursiva de directorios de prueba temporales.
* **JUnit 5 (Jupiter):**
  * Anotaciones `@Test`, `@BeforeEach`, `@AfterEach` para aislamiento riguroso de pruebas.
  * Aserciones avanzadas (`assertEquals`, `assertTrue`, `assertFalse`, `Optional.isPresent`).
* **Jackson Databind:**
  * Serialización/deserialización de contratos DTO y perfiles de usuario.
* **Patrones de Arquitectura y Diseño Consolidados:**
  * **Authoritative Server:** Control centralizado de reglas, tiempo y simulación.
  * **Pipes & Filters:** Validación $\rightarrow$ Cálculo Táctico $\rightarrow$ Eventos Minuto a Minuto $\rightarrow$ Desgaste $\rightarrow$ Consolidación.
  * **GoF Patterns:** Builder (`PlanDeJuegoBuilder`), Facade (`ScoutingFacade`), Composite (`Jugador` / `LineaTactica` / `Equipo`), Strategy (`Posesion`, `Contraataque`, etc.) y State (`Optimo`, `Cansado`, `Lesionado`, `Sancionado`).

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

Durante la estructuración y validación del Bloque 8 se identificaron y solventaron los siguientes escenarios técnicos:

1. **Aislamiento de Archivos de Prueba en Disco:**
   * *Problema:* Al ejecutar pruebas de persistencia masiva concurrente directamente en el directorio por defecto (`data/perfiles/`), los archivos de tests contaminaban los perfiles reales de usuario y podían provocar colisiones con usuarios existentes.
   * *Solución:* Se parametrizó el repositorio (`PerfilArchivoRepository`) para aceptar una ruta de directorio configurable (`data/perfiles_qa_test`). Se implementaron métodos `@BeforeEach` y `@AfterEach` que eliminan recursivamente el directorio de prueba al iniciar y al terminar, garantizando pruebas reproducibles e inocuas.

2. **Invariante Matemático en la Tabla de Posiciones:**
   * *Problema:* En simulaciones concurrentes intensivas con múltiples hilos calculando y consolidando resultados a la vez, existía el riesgo teórico de que un gol se contabilizara en el club ganador pero se perdiera en el club perdedor si fallaba la atomicidad.
   * *Solución:* Se incorporó un test específico de invariantes donde se totalizan los goles de todos los clubes de la liga. Se corroboró que el `ReentrantLock` de `TablaPosicionesService` garantiza atomicidad absoluta: la sumatoria de goles a favor es matemáticamente idéntica a la sumatoria de goles en contra en el 100% de los casos.

3. **Cierre Limpio de Recursos del Servidor (*Shutdown Hook*):**
   * *Ajuste:* Al ejecutar el servidor en consola de forma interactiva y detenerlo con `Ctrl+C`, los hilos del `ExecutorService` o del servidor HTTP podían quedar bloqueando el puerto brevemente. Se incorporó `Runtime.getRuntime().addShutdownHook(...)` en `ServidorApp.java`, asegurando la liberación inmediata del puerto y el apagado seguro de los servicios.

---

## 4. Estado del Proyecto

Con la finalización de este bloque, **se completan los 8 bloques de desarrollo planificados (100% del sistema implementado)**:
* Bloque 1: Contratos y DTOs Comunes (`common-dto`) ✅
* Bloque 2: Persistencia y Perfiles en Archivo (`server-app/persistence`) ✅
* Bloque 3: Dominio Deportivo y Patrones GoF (`server-app/domain`) ✅
* Bloque 4: Motor Pipes & Filters y Concurrencia (`server-app/engine`) ✅
* Bloque 5: Coordinador de Sala, Modo 2P y Timer 60s (`server-app/lobby`) ✅
* Bloque 6: Capa de Red y API HTTP/JSON (`server-app/api` & `client-app/network`) ✅
* Bloque 7: Cliente Táctico, Builder y Vistas UI (`client-app/ui`) ✅
* Bloque 8: Pruebas de Integración, Concurrencia y Calidad (QA) ✅
