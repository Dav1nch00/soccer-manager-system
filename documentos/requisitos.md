# Especificación de Requisitos del Software: Simulador Táctico "Soccer Manager"

## 1. Introducción y Propósito

El presente documento define la especificación formal de requisitos funcionales y no funcionales para el sistema **Simulador Táctico "Soccer Manager"**. 

El sistema es una plataforma de gestión deportiva y estratégica inspirada en títulos como *Football Manager*, orientada a una arquitectura distribuida **Cliente-Servidor**. El usuario asume el rol de Director Técnico (DT) de un club de fútbol profesional, compitiendo en una liga regular frente a otros clubes.

El sistema contempla dos modalidades de juego:
1. **Modo Individual (Simular Solo):** Un único usuario DT compite contra equipos gestionados automáticamente por Inteligencia Artificial.
2. **Modo Dúo / Multijugador (Simular con otra persona):** Dos usuarios DT inician sesión obligatoriamente con sus propios perfiles, gestionan sus respectivos clubes y compiten en la misma liga.

El modelo operativo del sistema se fundamenta en la planificación estratégica previa por fechas o jornadas y la simulación matemática en lote (*batch processing*), desacoplando la interfaz de usuario de los motores de cálculo concurrentes y de persistencia basada en archivos individuales por perfil.

---

## 2. Objetivos del Sistema

* **Objetivo General:** Desarrollar un simulador táctico de fútbol multiusuario y concurrente bajo una arquitectura Cliente-Servidor robusta, modular y extensible con soporte para perfiles individuales persistentes y simulación en solitario o cooperativa/competitiva de 2 jugadores.
* **Objetivos Específicos:**
  1. Proveer al Director Técnico un sistema de autenticación rápida que genere automáticamente un archivo de perfil para almacenar su progreso y estadísticas.
  2. Implementar los modos de juego **Individual (vs IA)** y **Multijugador (2 Jugadores)**, requiriendo el inicio de sesión y sincronización de ambos usuarios en el modo compartido.
  3. Proveer herramientas de análisis de plantilla y scouting inteligente de rivales.
  4. Implementar un generador y validador de estrategias y alineaciones tácticas en el cliente (Patrón *Builder*).
  5. Ejecutar simulaciones simultáneas de todos los encuentros de una jornada mediante programación multihilo en el servidor.
  6. Garantizar la coherencia e integridad de los datos compartidos (tabla de posiciones, estadísticas, goleadores) evitando condiciones de carrera (*race conditions*).
  7. Asegurar alta modularidad mediante la aplicación de patrones de diseño GoF y el patrón arquitectónico de Tuberías y Filtros (*Pipes and Filters*).

---

## 3. Actores del Sistema

| Actor | Tipo | Descripción |
| :--- | :--- | :--- |
| **Director Técnico 1 (Usuario Principal)** | Humano / Frontend | Usuario final que inicia sesión, gestiona su perfil y club, y define las tácticas de su equipo. |
| **Director Técnico 2 (Segundo Usuario)** | Humano / Frontend | Segundo usuario que en modo 2 jugadores inicia sesión con su propio perfil y compite en la misma liga. |
| **Servidor Central (Authoritative Server)** | Backend / Proceso | Servidor que gestiona los perfiles, coordina los modos de juego, valida las alineaciones, orquesta la simulación paralela y actualiza la persistencia. |
| **Agente de IA (Mánager Automatizado)** | Componente Servidor | Algoritmo que asume el rol de DT para los equipos no controlados por humanos, generando alineaciones válidas. |

---

## 4. Requisitos Funcionales (RF)

### 4.1. Módulo de Autenticación, Perfiles y Modos de Juego

* **RF-01: Autenticación y Generación Automática de Archivo de Perfil**
  * **Descripción:** Al iniciar la aplicación, el usuario debe ingresar un nombre de usuario y una contraseña.
  * **Comportamiento dinámico:**
    * Si el perfil **no existe**, el sistema lo crea inmediatamente y genera de forma automática un archivo físico persistente exclusivo para dicho perfil (ej. `perfiles/<nombre_usuario>.json`).
    * Si el perfil **ya existe**, el sistema valida la contraseña ingresada y carga la información persistida (historial, estadísticas, club asignado).
  * **Datos del Archivo de Perfil:**
    * Identificador del usuario, nombre y credencial cifrada/hasheada.
    * Club asignado en la competición.
    * Historial de partidos (victorias, empates, derrotas).
    * Puntos acumulados, títulos y fecha de última conexión.

* **RF-02: Selección y Configuración del Modo de Simulación**
  * **Descripción:** El sistema debe permitir elegir entre dos modalidades operativas antes de iniciar el ciclo de liga:
    * **Modo 1 - Simular Solo (Individual):**
      * Únicamente requiere el inicio de sesión de un usuario.
      * El usuario escoge su club y el resto de los clubes de la liga son administrados por agentes de IA.
      * Al confirmar su alineación, el usuario puede detonar la simulación de la jornada de forma directa.
    * **Modo 2 - Simular con Otra Persona (Multijugador / 2 Jugadores):**
      * Requiere obligatoriamente que **ambos usuarios inicien sesión** con sus respectivos nombres y contraseñas.
      * Cada usuario genera/carga su propio archivo de perfil independiente.
      * Cada usuario elige o tiene asignado un club diferente dentro de la misma liga.
      * **Edición Paralela:** Ambos jugadores pueden editar y configurar sus alineaciones y tácticas simultáneamente en paralelo.
      * **Mecanismo de Temporizador de 60 Segundos al Confirmar:** Cuando uno de los dos jugadores termina de definir su estrategia y presiona el botón "Simular", se dispara automáticamente en el servidor un temporizador de cuenta regresiva de **60 segundos** para el otro jugador.
      * **Resolución de la Jornada:**
        * Si el segundo jugador confirma su estrategia antes de agotarse los 60 segundos, la simulación se ejecuta de inmediato.
        * Si expira el tiempo (llega a 0 segundos), la jornada se simula forzosamente con la estrategia tal y como la tenga armada el segundo jugador en ese momento (aplicando auto-completado por IA si no alcanzó a llenar los 11 titulares).

### 4.2. Módulo de Gestión de Plantilla y Scouting (Pre-Jornada)

* **RF-03: Consulta de Plantilla de Jugadores**
  * **Descripción:** El sistema debe permitir consultar la lista completa de futbolistas pertenecientes al club asignado.
  * **Datos requeridos:**
    * Atributos base (escala 1 a 100): Velocidad, Pase, Tiro, Defensa, Físico.
    * Estado físico: Nivel de energía/fatiga (0% - 100%).
    * Estado de disponibilidad: Óptimo, Cansado, Lesionado, Sancionado (Patrón *State*).
    * Estadísticas acumuladas: Partidos jugados, goles anotados, asistencias, tarjetas amarillas/rojas y calificación promedio.
  * **Salidas:** Lista estructurada de jugadores (`List<JugadorDTO>`).

* **RF-04: Análisis de Inteligencia del Rival (Scouting)**
  * **Descripción:** El DT debe poder solicitar un informe de scouting detallado del rival de la próxima jornada.
  * **Funcionalidad subyacente:** El servidor utilizará una fachada (`ScoutingFacade`) que consolida:
    * Formación táctica habitual del oponente (ej. 4-4-2, 4-3-3).
    * Estilo de juego predominante (ej. Posesión, Contraataque, Presión Alta, Bloque Bajo).
    * Racha de los últimos 5 partidos (Victorias, Empates, Derrotas).
    * Identificación de jugadores más peligrosos / jugador estrella.
  * **Salidas:** `ReporteRivalDTO` con métricas comparativas y resumen de inteligencia.

### 4.3. Módulo de Planificación Táctica (Cliente)

* **RF-05: Selección de Formación y Alineación Titular**
  * **Descripción:** El DT debe poder elegir un esquema de juego táctico (ej. 4-4-2, 4-3-3, 3-5-2, 5-3-2) y asignar exactamente once (11) jugadores titulares respetando sus roles de campo.

* **RF-06: Selección de Estrategia de Partido**
  * **Descripción:** El DT debe poder configurar la filosofía de juego para el encuentro: *Posesión*, *Contraataque*, *Presión Alta* o *Bloque Bajo* (Patrón *Strategy*).

* **RF-07: Validación Local de Plan de Juego (Patrón Builder)**
  * **Descripción:** La aplicación cliente debe validar estrictamente las reglas tácticas antes de transmitir la configuración al servidor mediante el `PlanDeJuegoBuilder`:
    * Que la alineación contenga exactamente 11 jugadores.
    * Que no existan jugadores repetidos en la alineación.
    * Que no haya posiciones tácticas vacías.
    * Que ningún jugador seleccionado esté en estado **Lesionado** o **Sancionado**.
  * **Salidas:** `PlanDeJuegoDTO` validado y serializado a JSON para envío por red.

### 4.4. Módulo de Sincronización y Simulación de Jornada (Servidor)

* **RF-08: Sincronización de Mánagers y Temporizador de 60s en Modo 2 Jugadores**
  * **Descripción:** En modo de dos jugadores, el servidor mantiene el estado de sala (*Lobby/Matchmaking*) y coordina la cuenta regresiva competitiva:
    * Estado de inicio de sesión de DT 1 y DT 2.
    * Recepción y validación del `PlanDeJuegoDTO` de DT 1 o DT 2.
    * **Disparo del Temporizador:** Tan pronto el primer mánager envía su plan y presiona "Simular", el servidor transiciona la sala a estado `CUENTA_REGRESIVA` e inicializa un temporizador de 60 segundos gestionado con `ScheduledExecutorService`.
    * **Notificación al Segundo DT:** El cliente del segundo mánager recibe la alerta en tiempo real con los segundos restantes para finalizar su táctica.
    * **Cierre Anticipado:** Si el segundo DT confirma su plan antes de que el contador llegue a cero, el temporizador se cancela y se inicia la simulación paralela de forma instantánea.
    * **Cierre por Timeout (Expiración de los 60s):** Si el contador expira, el servidor toma la alineación actual del segundo DT; si faltan posiciones por asignar o no hay 11 jugadores habilitados, el servidor auto-completa la alineación mediante un servicio de respaldo (*fallback*) y lanza la simulación sin retrasar la fecha.

* **RF-09: Ejecución Paralela de la Jornada**
  * **Descripción:** Una vez cumplida la condición del modo (1 DT en solitario o 2 DTs en modo dúo), el servidor simula todos los partidos de la jornada ($N/2$ encuentros para una liga de $N$ equipos) de manera simultánea e independiente utilizando un pool de hilos (`ExecutorService`).

* **RF-10: Pipeline de Simulación por Partido (Pipes and Filters)**
  * **Descripción:** La simulación matemática de cada encuentro individual debe procesarse a través de una cadena secuencial de 5 filtros:
    1. **Filtro de Validación:** Re-verifica la integridad de las alineaciones de ambos clubes en el servidor.
    2. **Filtro de Cálculo Táctico:** Evalúa el enfrentamiento entre las estrategias (ej. Contraataque con bonificación ante Posesión, cálculo de poder ofensivo/defensivo mediante Composite).
    3. **Filtro de Simulación de Eventos:** Genera cronológicamente los 90 minutos de juego (goles, tiros, faltas, tarjetas, asistencias, calificaciones).
    4. **Filtro de Desgaste y Estado:** Ajusta el porcentaje de fatiga/energía y calcula eventos de lesión según la intensidad del juego.
    5. **Filtro de Consolidación:** Persiste los marcadores, actualiza estadísticas y recalcula posiciones.

* **RF-11: Sincronización y Barrera de Cierre de Jornada**
  * **Descripción:** El servidor no debe declarar concluida la jornada ni habilitar los resultados hasta que la totalidad de los partidos simulados en paralelo hayan finalizado (`CompletableFuture.allOf` o `invokeAll`).

### 4.5. Módulo de Consolidación, Resultados y Persistencia

* **RF-12: Actualización Segura de Tablas y Goleadores (Thread-Safety)**
  * **Descripción:** El servidor debe actualizar el `GestorTablaPosiciones` y el `RegistroGoleadores` aplicando mecanismos de exclusión mutua (`ReentrantLock`) para evitar pérdida de datos o inconsistencias cuando múltiples hilos de partidos intenten escribir a la vez.

* **RF-13: Actualización y Persistencia de Archivos de Perfil**
  * **Descripción:** Al concluir la jornada, el servidor debe reescribir/actualizar automáticamente los archivos de perfil de los mánagers involucrados (`perfiles/<usuario>.json`), reflejando el nuevo resultado obtenido (victoria, empate o derrota), puntos sumados y estadísticas actualizadas.

* **RF-14: Consulta de Marcadores y Tabla de Clasificación**
  * **Descripción:** Ambos clientes deben poder consultar y visualizar los marcadores finales de la fecha, el detalle de incidencias de su partido, la tabla general de posiciones actualizada y la tabla de goleadores.

---

## 5. Requisitos No Funcionales (RNF)

| Identificador | Categoría | Requisito | Criterio de Aceptación |
| :--- | :--- | :--- | :--- |
| **RNF-01** | **Arquitectura** | **Cliente-Servidor Desacoplado** | El servidor (*Authoritative Server*) no posee interfaz gráfica. Toda la comunicación se efectúa mediante mensajes estructurados en formato JSON (REST o Sockets TCP). |
| **RNF-02** | **Persistencia** | **Persistencia Basada en Archivos por Perfil** | Cada perfil se guarda en un archivo individual independiente (JSON). La creación del archivo debe ser automática e instantánea al registrarse por primera vez. |
| **RNF-03** | **Concurrencia** | **Sincronización de Sesiones Múltiples** | El servidor debe soportar sesiones concurrentes (1 o 2 usuarios humanos simultáneos) y sincronizar la barrera de juego sin bloqueos activos que saturen la CPU. |
| **RNF-04** | **Rendimiento** | **Simulación Concurrente y Paralela** | La simulación de los $N/2$ partidos debe ejecutarse en paralelo mediante un pool de hilos (`Executors.newFixedThreadPool`). |
| **RNF-05** | **Integridad** | **Seguridad en Hilos (Thread-Safety)** | Cero condiciones de carrera (*Race Conditions*) en los recursos compartidos. El acceso a la tabla de posiciones y archivos de perfil debe protegerse con `ReentrantLock` y estructuras concurrentes. |
| **RNF-06** | **Mantenibilidad** | **Patrones de Diseño GoF** | Se debe implementar rigurosamente la separación de responsabilidades a través de: `Builder` (cliente), `Facade` (servidor), `Composite` (servidor), `Strategy` (servidor) y `State` (servidor). |
| **RNF-07** | **Arquitectura Interna**| **Tuberías y Filtros (Pipes & Filters)** | El motor de simulación de partido debe desacoplar sus etapas en filtros autónomos e intercambiables con interfaces uniformes de entrada/salida. |
| **RNF-08** | **Modularidad** | **Estructura Multi-Módulo** | El proyecto debe organizarse en al menos tres módulos independientes: `common-dto` (contratos compartidos), `server-app` (motor backend y persistencia) y `client-app` (interfaz cliente). |
| **RNF-09** | **Usabilidad** | **Interfaz de Usuario Táctica y Login** | El cliente debe disponer de vista de Login/Registro de perfil, selector de modo (Solo vs 2 Jugadores), y las 4 vistas del MVP táctico con feedback visual de preparación del rival. |

---

## 6. Restricciones del Sistema

1. **Persistencia en Archivos Locales:** No se requiere una base de datos relacional externa (como MySQL o PostgreSQL); la persistencia se gestiona a través del sistema de archivos local del servidor mediante archivos serializados JSON en un directorio dedicado (`/data/perfiles/`).
2. **Requisito de Concurrencia de 2 Jugadores:** En el modo de simulación con otra persona, la jornada permanece en pausa hasta que ambos jugadores hayan enviado un plan de juego válido.
3. **Autoridad del Servidor (*Server Authoritative*):** Ningún resultado ni actualización de perfil se calcula en el cliente; el servidor valida, simula y persiste.

---

## 7. Matriz de Trazabilidad de Requisitos vs Arquitectura

| Requisito Funcional | Componente de Arquitectura | Patrón de Diseño / Mecanismo | Módulo |
| :--- | :--- | :--- | :--- |
| **RF-01, RF-13** (Perfiles en Archivo) | `services.PerfilService`, `repository.PerfilArchivoRepository` | DAO / File Repository (JSON) | `server-app` |
| **RF-02, RF-08** (Modos y Sala 2P) | `services.SalaEsperaService`, `api.JornadaController` | Sincronización de Sesiones / Monitor | `server-app` |
| **RF-03** (Plantilla y Estados) | `domain.model.Jugador`, `domain.state` | Patrón **State** | `server-app`, `common-dto` |
| **RF-04** (Scouting Rival) | `services.ScoutingFacade` | Patrón **Facade** | `server-app` |
| **RF-05, RF-06, RF-07** (Plan Táctico) | `builders.PlanDeJuegoBuilder` | Patrón **Builder** | `client-app` |
| **RF-06, RF-10** (Estrategia de Juego) | `domain.strategy.EstrategiaTactica` | Patrón **Strategy** | `server-app` |
| **RF-10** (Cálculo de Potencial) | `domain.model.LineaTactica`, `Equipo` | Patrón **Composite** | `server-app` |
| **RF-09, RF-11** (Simulación Paralela) | `engine.SimuladorJornada` | `ExecutorService`, `Callable`, Barrera | `server-app` |
| **RF-10** (Motor de Simulación) | `engine.PipelinePartidos` | Patrón **Pipes and Filters** | `server-app` |
| **RF-12** (Consolidación Segura) | `services.TablaPosicionesService` | `ReentrantLock`, `ConcurrentHashMap` | `server-app` |
