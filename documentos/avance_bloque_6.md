# Avance y Bitácora de Desarrollo: Bloque 6

**Módulos:** `server-app/api` y `client-app/network`  
**Nombre del Bloque:** Capa de Red y API de Comunicación Cliente-Servidor  
**Fecha de Ejecución:** 17 de noviembre de 2026  
**Estado:** Completado con Éxito  

---

## 1. ¿Qué se hizo?

Se desarrolló la capa de comunicación distribuida y los protocolos de red que permiten enlazar la interfaz del mánager (`client-app`) con el motor central del servidor (`server-app`):

### 1.1. Servidor HTTP / REST Ligero (`server-app/api/ServidorHttp.java`)
Se construyó un servidor HTTP multihilo nativo utilizando `com.sun.net.httpserver.HttpServer` sobre Java 17, sin dependencias pesadas de frameworks externos, optimizado para alto rendimiento y bajo consumo de memoria:
* **Pool de Concurrencia:** Configurado con `Executors.newCachedThreadPool()` para atender peticiones concurrentes de múltiples clientes sin bloquearse.
* **Catálogo de Endpoints JSON Implementados:**
  1. `POST /api/sesion/login-o-registro`: Autentica al mánager o genera automáticamente su archivo físico `data/perfiles/<usuario>.json` y conecta al DT en la sala.
  2. `POST /api/partida/modo`: Permite seleccionar entre `SOLO` (1P vs IA) y `DOS_JUGADORES`.
  3. `GET /api/partida/sala-estado`: Provee el estado en vivo de la sala y los segundos restantes del temporizador de 60s.
  4. `GET /api/club/{id}/plantilla`: Retorna la lista de futbolistas con atributos base, energía y estados.
  5. `GET /api/scouting/{rivalId}`: Retorna el informe consolidado por la `ScoutingFacade`.
  6. `POST /api/jornada/plan-juego`: Recibe y registra la táctica configurada por el mánager.
  7. `POST /api/jornada/simular`: Solicita la simulación de la jornada (inmediata en modo Solo, o activación de la cuenta regresiva de 60s / resolución en modo 2P).
  8. `GET /api/torneo/tabla-y-resultados`: Retorna la clasificación general actualizada y los marcadores de la última fecha.
* **`GestorLiga.java` (`services`):** Inicializa una liga representativa de 4 clubes (Barcelona, Millonarios FC, Junior FC, América de Cali) con 13 futbolistas por plantel para alimentar los servicios de plantilla y partidos.

### 1.2. Cliente de Red del Frontend (`client-app/network/MotorConexion.java`)
Se implementó el cliente de red basado en `java.net.http.HttpClient` de Java 11+, proveyendo métodos tipados y síncronos/asíncronos para la interfaz gráfica:
* `loginORegistro(usuario, password, clubId)`
* `configurarModo(modo)`
* `consultarEstadoSala()`
* `obtenerPlantilla(clubId)`
* `obtenerScouting(rivalId)`
* `enviarPlanJuego(usuario, plan)`
* `solicitarSimulacion(usuario)`
* `obtenerTablaYResultados()`

### 1.3. Suite de Pruebas de Integración de Red (`ClienteServidorApiTest.java`)
Prueba end-to-end con JUnit 5 que levanta el `ServidorHttp` en un puerto dinámico y valida la interacción completa mediante `MotorConexion`:
* Registro automático de perfil vía red.
* Configuración de modo `SOLO`.
* Consulta de nómina y scouting del rival.
* Solicitud de simulación y lectura de resultados con tabla de posiciones.

---

## 2. ¿Qué se usó?

* **Tecnología Servidor:** `com.sun.net.httpserver.HttpServer` con `InetSocketAddress` y `Executors.newCachedThreadPool()`.
* **Tecnología Cliente:** `java.net.http.HttpClient`, `HttpRequest`, `HttpResponse` de Java 11+.
* **Serialización:** Jackson Databind (`ObjectMapper`, `JavaTimeModule`) con codificación UTF-8.
* **Testing:** JUnit 5 con asignación dinámica de puertos efímeros (`puerto = 0`).

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

1. **Prevención de conflictos de puerto (`BindException: Address already in use`):**
   * *Problema potencial:* En entornos de pruebas o si el puerto por defecto `8080` estuviese ocupado por otro servicio del sistema, el servidor fallaría al iniciar.
   * *Solución implementada:* Se añadió el método `getPuertoAsignado()`. Al pasar `puerto = 0` en las pruebas de integración, el sistema operativo asigna dinámicamente cualquier puerto libre disponible y se lo entrega automáticamente al `MotorConexion`.

2. **Garantía de codificación de caracteres especiales y CORS:**
   * *Ajuste:* Para asegurar que tildes y nombres propios (como *Medellín*, *El Campín* o *Camp Nou*) se transfieran sin corrupción de texto (*mojibake*), se forzó el encabezado `Content-Type: application/json; charset=UTF-8` y se añadió `Access-Control-Allow-Origin: *` para permitir futuros clientes web o navegadores.
