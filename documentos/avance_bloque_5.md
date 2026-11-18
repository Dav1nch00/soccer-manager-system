# Avance y Bitácora de Desarrollo: Bloque 5

**Módulo:** `server-app` (Lobby, Concurrencia y Servicios)  
**Nombre del Bloque:** Coordinador de Sala, Modo 2P, Temporizador de 60s y ScoutingFacade  
**Fecha de Ejecución:** 17 de noviembre de 2026  
**Estado:** Completado con Éxito (Hito MVP de Backend alcanzado)  

---

## 1. ¿Qué se hizo?

Se desarrolló la capa de orquestación competitiva y la fachada de inteligencia deportiva en `server-app`, completando el **Hito 1: MVP de Backend / Servidor**:

### 1.1. Temporizador Concurrente de 60 Segundos (`lobby/TemporizadorJornada.java`)
* Gestiona la cuenta regresiva competitiva mediante un ejecutor programado (`ScheduledExecutorService`).
* Emite un pulso cada segundo decrementando el contador de forma atómica (`AtomicInteger`), permitiendo que los clientes conectados consulten los `segundosRestantes` en tiempo real.
* Ofrece cancelación inmediata (`cancelar()`) si el rival confirma antes del límite, y ejecución de un callback en caso de expiración del tiempo (`timeout`).
* Utiliza hilos marcados como *Daemon* para no retener el proceso de la JVM al cerrar la aplicación.

### 1.2. Coordinador de Modos y Sala Multijugador (`lobby/GestorModoYSala.java`)
* **Modo Individual (`SOLO`):** Permite simular inmediatamente la jornada tan pronto el único mánager confirma su alineación.
* **Modo 2 Jugadores (`DOS_JUGADORES`):**
  * Requiere obligatoriamente que ambos directores técnicos hayan iniciado sesión con sus respectivos perfiles.
  * Habilita la **edición táctica en paralelo**: ambos mánagers pueden modificar sus 11 titulares y estrategias de forma simultánea.
  * **Regla del Contador de 60 Segundos:**
    * Cuando el **primer jugador** confirma su estrategia y presiona "Simular", la sala transiciona a `CUENTA_REGRESIVA`, registra su plan táctico y activa el temporizador de 60 segundos.
    * El cliente del segundo jugador recibe la alerta y el contador regresivo en su interfaz.
    * **Resolución Anticipada:** Si el segundo DT confirma su táctica antes de que el contador llegue a cero, el temporizador se cancela al instante y se lanza la simulación de la fecha.
    * **Resolución por Timeout:** Si el temporizador llega a 0 segundos, el servidor congela la edición del segundo mánager, toma su configuración actual (asignando un plan por defecto si no envió nada) y dispara la simulación forzada.
* Toda la sincronización de estados está blindada mediante un `ReentrantLock` para evitar colisiones entre el hilo de red del cliente y el hilo del temporizador.

### 1.3. Patrón Estructural Facade (`services/ScoutingFacade.java`)
* Simplifica y centraliza la consulta de inteligencia del rival de turno para el mánager.
* Orquesta múltiples subsistemas:
  * Lee la formación y estrategia predominante del club oponente.
  * Utiliza el Patrón Composite (`Equipo` y `LineaTactica`) para calcular las medias de rendimiento de las líneas (*Defensa*, *Mediocampo*, *Delantera*).
  * Detecta automáticamente al jugador estrella del rival a neutralizar.
  * Analiza el historial reciente para construir la racha de los últimos 5 encuentros ("V", "E", "D") y el promedio de goles.
  * Retorna un objeto unificado y limpio `ReporteRivalDTO`.

### 1.4. Suite de Pruebas Automatizadas (`LobbyYScoutingTest.java`)
Se implementaron pruebas con JUnit 5 validando:
1. Simulación inmediata en Modo Individual.
2. Bloqueo de simulación en Modo 2P si falta un DT conectado.
3. Activación correcta del temporizador de 60s en el primer envío.
4. Cancelación del temporizador y simulación inmediata cuando el segundo DT confirma a tiempo.
5. Generación completa y exacta del `ReporteRivalDTO` por parte de la `ScoutingFacade`.

---

## 2. ¿Qué se usó?

* **Concurrencia Programada:** `java.util.concurrent.ScheduledExecutorService`, `ScheduledFuture`, `AtomicInteger`, `AtomicBoolean`.
* **Sincronización:** `java.util.concurrent.locks.ReentrantLock`.
* **Patrones GoF y de Arquitectura:**
  * *Facade Pattern:* `ScoutingFacade` desacopla al cliente de las entidades internas del rival.
  * *State / Observer Callback Pattern:* Callbacks de Tick y Timeout en el temporizador.
* **Testing:** JUnit 5.

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

1. **Prevención de doble simulación por colisión entre confirmación y timeout:**
   * *Problema potencial:* Si el segundo jugador confirma en el segundo 59 justo en el instante en que el hilo del temporizador despierta para ejecutar el timeout, ambos hilos podrían intentar disparar la simulación en paralelo.
   * *Solución implementada:* Se sincronizó `solicitarSimulacion()` y `gestionarTimeout()` bajo el mismo `ReentrantLock`. En `gestionarTimeout()` se condicionó `if (estadoSala == EstadoSalaEnum.CUENTA_REGRESIVA)`, asegurando que si el usuario confirmó fracciones de segundo antes, el timeout se descarte atómicamente.

2. **Apagado limpio de la JVM con hilos Daemon:**
   * *Problema potencial:* Los `ScheduledExecutorService` por defecto crean hilos de usuario que mantienen viva la máquina virtual de Java incluso si el proceso principal finaliza.
   * *Solución implementada:* Se añadió un `ThreadFactory` personalizado con `t.setDaemon(true)` en `TemporizadorJornada`, permitiendo que el servidor o cliente cierre sus procesos sin hilos zombies.
