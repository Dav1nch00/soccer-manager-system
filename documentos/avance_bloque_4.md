# Avance y Bitácora de Desarrollo: Bloque 4

**Módulo:** `server-app` (Motor de Simulación y Concurrencia)  
**Nombre del Bloque:** Motor de Simulación Pipes & Filters y Concurrencia Multihilo  
**Fecha de Ejecución:** 17 de noviembre de 2026  
**Estado:** Completado con Éxito  

---

## 1. ¿Qué se hizo?

Se desarrolló el motor de simulación matemática deportiva del servidor dentro de `server-app/src/main/java/com/soccermanager/server/engine`, implementando el patrón arquitectónico de **Tuberías y Filtros (Pipes & Filters)** y la infraestructura de **concurrencia multihilo** con exclusión mutua para la tabla de posiciones:

### 1.1. Arquitectura de Tuberías y Filtros (`engine/pipeline`)
Cada partido individual se desacopló en una cadena de 5 filtros autónomos unidos por el orquestador `PipelinePartidos.java`:
1. **`ContextoPartido.java`:** Objeto mutable que viaja por la tubería transportando los clubes, tácticas, modificadores, goles, tiros, faltas y notas.
2. **`FiltroValidacion.java` (Filtro 1):** Verifica que ambos clubes tengan 11 futbolistas hábiles (`puedeJugar() == true`). Remueve automáticamente a jugadores lesionados/sancionados y auto-completa puestos vacíos a partir de los suplentes de la plantilla o canteranos de emergencia.
3. **`FiltroCalculoTactico.java` (Filtro 2):** Consulta los poderes ofensivos, defensivos y de control del Patrón Composite y aplica los modificadores del Patrón Strategy (ej. ventaja del Contraataque frente a Posesión). Calcula la posesión relativa de balón (acotada entre 30% y 70%).
4. **`FiltroSimulacionEventos.java` (Filtro 3):** Simula cronológicamente los 90 minutos de juego divididos en 6 bloques de 15 minutos:
   * Generación de ocasiones de gol y tiros al arco en función del ratio Ataque/Defensa.
   * Asignación estocástica de anotadores y asistentes ponderada por atributos de tiro y velocidad.
   * Evaluación de infracciones, amonestaciones y expulsiones según el factor de faltas de la estrategia.
   * Asignación dinámica de notas de rendimiento individual (1.0 a 10.0).
5. **`FiltroDesgaste.java` (Filtro 4):** Descuenta la energía física de los futbolistas según el esfuerzo base y extra de la estrategia. Evalúa la probabilidad estocástica de lesiones musculares (transición a `EstadoLesionado` de 1 a 3 semanas e inserción de la incidencia médica).
6. **`FiltroConsolidacion.java` (Filtro 5):** Empaqueta formalmente el `ResultadoPartidoDTO` consolidado listo para ser persistido y distribuido a los clientes.

### 1.2. Infraestructura de Concurrencia Multihilo (`engine`)
* **`TareaPartido.java` (`Callable<ResultadoPartidoDTO>`):** Tarea concurrente independiente que ejecuta un partido completo a través del pipeline.
* **`SimuladorJornada.java`:** Administra un pool de hilos de tamaño fijo (`Executors.newFixedThreadPool`). Recibe los $N/2$ partidos de la fecha y utiliza la sincronización de barrera `executorService.invokeAll(tareas)` para esperar a que todos los encuentros terminen antes de dar por cerrada la fecha.

### 1.3. Clasificación Thread-Safe (`services/TablaPosicionesService.java`)
* Mantiene la tabla de posiciones y el registro histórico de goleadores.
* **Thread-Safety:** Protegido mediante un `ReentrantLock` explícito para evitar condiciones de carrera (*Race Conditions*) o pérdida de puntos cuando múltiples hilos de partidos terminan al mismo microsegundo.
* Aplica criterios oficiales de desempate: 1. Puntos, 2. Diferencia de Gol, 3. Goles a Favor, 4. Orden Alfabético.

### 1.4. Pruebas Automatizadas (`SimuladorJornadaTest.java`)
* Validación del pipeline secuencial individual con 100% de coherencia en posesión y tiros.
* Validación de simulación paralela de 4 partidos simultáneos (8 equipos) con barrera de sincronización y consolidación matemática en la tabla de posiciones.

---

## 2. ¿Qué se usó?

* **Concurrencia en Java 17:** `java.util.concurrent.ExecutorService`, `Executors`, `Callable`, `Future`, `ThreadFactory`.
* **Sincronización y Exclusión Mutua:** `java.util.concurrent.locks.ReentrantLock`, `ConcurrentHashMap`.
* **Patrones Arquitectónicos y de Diseño:**
  * *Pipes and Filters Pattern:* Separación de etapas de cálculo deportivo.
  * *Thread Pool Pattern:* Reutilización eficiente de hilos del sistema operativo.
  * *Mutex Pattern:* Bloqueo explícito atómico sobre la tabla de posiciones.
* **Testing:** JUnit 5.

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

1. **Auto-completado de plantilla por bajas disciplinarias o médicas:**
   * *Problema potencial:* Si un usuario enviaba una alineación con jugadores lesionados o no alineaba 11 futbolistas, el pipeline arrojaba error de partido inválido, interrumpiendo la jornada de los demás clubes.
   * *Solución implementada:* En `FiltroValidacion`, si faltan futbolistas, el sistema extrae automáticamente suplentes aptos de la plantilla y, en caso de déficit extremo, genera canteranos de emergencia con media base (55), garantizando que el partido siempre se dispute válidamente.

2. **Invariante deportivo de tiros y goles:**
   * *Ajuste estadístico:* En ocasiones de alta efectividad, el generador estocástico podía producir más goles que remates registrados. Se aplicó la corrección: `contexto.setTirosLocal(Math.max(tirosLocal, contexto.getGolesLocal()))`, manteniendo coherencia visual para el usuario.

3. **Normalización de Posesión de Balón:**
   * *Ajuste:* La posesión se computó de manera relativa entre ambos equipos para forzar que `posesionLocal + posesionVisitante == 100%`, acotándola en el rango creíble de 30% a 70%.
