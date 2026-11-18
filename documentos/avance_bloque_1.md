# Avance y Bitácora de Desarrollo: Bloque 1

**Módulo:** `common-dto`  
**Nombre del Bloque:** Contratos, Modelos y DTOs Compartidos  
**Fecha de Ejecución:** 17 de noviembre de 2026  
**Estado:** Completado con Éxito  

---

## 1. ¿Qué se hizo?

Se sentaron las bases arquitectónicas y contractuales del sistema implementando la librería común que compartirán tanto la aplicación cliente (`client-app`) como el servidor (`server-app`), garantizando un desacoplamiento estricto y eliminando cualquier dependencia circular:

1. **Configuración Multi-Módulo del Proyecto:**
   * Creación del archivo raíz `soccer-manager-system/pom.xml` con gestión centralizada de dependencias y versiones de Java (Java 17) y Jackson Databind.
   * Creación de los módulos subordinados: `common-dto/pom.xml`, `server-app/pom.xml` y `client-app/pom.xml`.

2. **Definición de Enumeraciones de Dominio (`com.soccermanager.common.enums`):**
   * `FormacionEnum`: Modela los esquemas tácticos permitidos (4-4-2, 4-3-3, 3-5-2, 5-3-2, 4-2-3-1), asignando formalmente el número obligatorio de futbolistas por línea de campo (1 arquero, defensas, medios y delanteros sumando 11 titulares).
   * `EstrategiaEnum`: Define las filosofías de juego del patrón *Strategy* (*Posesión*, *Contraataque*, *Presión Alta*, *Bloque Bajo*), con sus descripciones y factores de compensación.
   * `EstadoJugadorEnum`: Modela las condiciones físicas y disciplinarias del patrón *State* (*Óptimo*, *Cansado*, *Lesionado*, *Sancionado*), con banderas booleanas de habilitación y multiplicadores de rendimiento (1.0 a 0.0).
   * `ModoJuegoEnum`: Modela las dos variantes de simulación: `SOLO` (1 Jugador vs IA) y `DOS_JUGADORES` (Multijugador sincronizado).
   * `EstadoSalaEnum`: Controla las fases de sincronización del lobby multijugador (`ESPERANDO_JUGADORES`, `EN_PLANIFICACION`, `CUENTA_REGRESIVA`, `SIMULANDO`, `FINALIZADA`).
   * `TipoIncidenciaEnum`: Tipifica los sucesos de los 90 minutos de partido (*Gol*, *Tarjeta Amarilla*, *Tarjeta Roja*, *Lesión*, *Ajuste Táctico*).
   * `PosicionEnum`: Demarca los roles posicionales naturales (*Portero*, *Defensa*, *Mediocampista*, *Delantero*).

3. **Implementación de Data Transfer Objects (`com.soccermanager.common.dto`):**
   * `SesionDTO`: Transporta credenciales de sesión activa, identificador de club y bandera de creación de nuevo perfil.
   * `PerfilDTO`: Contiene el histórico de rendimiento del mánager (PJ, PG, PE, PP, goles, puntos, títulos y fechas de conexión).
   * `JugadorDTO`: Modela al futbolista individual con atributos cuantitativos (1 a 100 en Velocidad, Pase, Tiro, Defensa, Físico), nivel de energía (0 a 100%), cálculo automático de media general y estadísticas acumuladas.
   * `PlanDeJuegoDTO`: Estructura la alineación de 11 jugadores, formación y estrategia a transmitir al servidor.
   * `ReporteRivalDTO`: Estructura el informe consolidado por la fachada de scouting (*ScoutingFacade*) con medias por línea, racha y jugador insignia.
   * `IncidenciaDTO`: Representa cronológicamente un evento dentro de los 90 minutos de juego.
   * `ResultadoPartidoDTO`: Consolida marcadores finales, posesión, tiros, lista de incidencias y calificaciones individuales de rendimiento.
   * `SalaEstadoDTO`: Informa en tiempo real el estado de ambos jugadores en el modo dúo y reporta el contador regresivo de 60 segundos (`segundosRestantes`, `isCuentaRegresivaActiva`).

---

## 2. ¿Qué se usó?

* **Lenguaje y Plataforma:** Java 17 LTS.
* **Gestor de Dependencias y Construcción:** Apache Maven (estructura multi-módulo con módulo raíz agregador).
* **Serialización y Formato de Datos:** Jackson Databind (`com.fasterxml.jackson.core:jackson-databind`) y Jackson JSR-310 Datatype para manejo nativo de fechas y horas (`LocalDateTime`).
* **Estándar de Intercambio:** `java.io.Serializable` en todos los DTOs con `serialVersionUID` explícito para compatibilidad con transporte por red mediante sockets TCP o payloads JSON vía HTTP.
* **Patrones de Diseño de Software Aplicados:**
  * **Data Transfer Object (DTO):** Aislamiento entre las representaciones de red y las entidades de negocio.
  * **Typesafe Enum Pattern:** Tipado fuerte con lógica embebida (métodos de cálculo de totales en formaciones y factores en estados).

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

Durante el diseño y codificación del Bloque 1 se identificaron y solventaron los siguientes aspectos técnicos:

1. **Ajuste de dependencias circulares y redundancia en `PlanDeJuegoDTO`:**
   * *Situación:* Inicialmente se contemplaba enviar únicamente la lista de IDs de jugadores (`List<String> titularesIds`). Sin embargo, en el cliente se requería poder desplegar la lista con nombres y dorsales sin consultar nuevamente al servidor.
   * *Solución:* Se ajustó `PlanDeJuegoDTO` para admitir tanto `List<JugadorDTO> titulares` como la sincronización automática de `List<String> titularesIds`, inicializando las colecciones con `new ArrayList<>()` en los constructores para prevenir excepciones `NullPointerException` durante la serialización/deserialización JSON.

2. **Inclusión nativa del temporizador competitivo en `SalaEstadoDTO`:**
   * *Situación:* Originalmente el DTO de sala solo contemplaba banderas de conexión (`dt1Conectado`, `dt2Conectado`). Al definirse la regla del temporizador de 60 segundos detonado por el primer jugador en confirmar, el DTO hubiera requerido una refactorización tardía.
   * *Solución:* Se incorporaron desde este bloque los campos `segundosRestantes`, `quienDisparoTimer` y el método utilitario `isCuentaRegresivaActiva()`, garantizando que la capa de red nazca lista para el temporizador de 60s sin deuda técnica.

3. **Manejo de serialización temporal en Java:**
   * *Situación:* Los tipos `LocalDateTime` de Java 8+ pueden fallar al ser convertidos a JSON plano si no se dispone del módulo `jackson-datatype-jsr310`.
   * *Solución:* Se añadió `jackson-datatype-jsr310` al `pom.xml` padre y a los módulos hijos para que las marcas de tiempo (`fechaInicio`, `fechaCreacion`, `fechaEnvio`) se serialicen en formato estándar ISO-8601 sin errores de runtime.
