# Avance y Bitácora de Desarrollo: Bloque 7

**Módulo:** `client-app` (Frontend Táctico, Builder y Vistas)  
**Nombre del Bloque:** Cliente Táctico, Patrón Builder y Vistas de Interfaz (MVP de Usuario Final)  
**Fecha de Ejecución:** 17 de noviembre de 2026  
**Estado:** Completado con Éxito (Hito MVP de Usuario Final alcanzado)  

---

## 1. ¿Qué se hizo?

Se desarrolló la aplicación cliente interactiva para el Director Técnico, culminando el **Hito 2: Producto Mínimo Viable (MVP) Integral y Operativo para el Usuario Final**:

### 1.1. Patrón Creacional Builder (`builders/PlanDeJuegoBuilder.java`)
Permite construir paso a paso la configuración táctica en el cliente antes de transmitirla por la red, aplicando validaciones preventivas:
* **Construcción Fluida:** Métodos encadenados `setClub()`, `setFormacion()`, `setEstrategia()`, `agregarTitular()`, `removerTitular()`.
* **Validaciones Locales Estrictas:**
  1. *Regla de los 11:* Exige exactamente 11 jugadores titulares antes de permitir el `build()`.
  2. *Unicidad de Jugadores:* Bloquea e impide agregar un futbolista que ya haya sido alineado.
  3. *Habilitación Física y Disciplinaria (Patrón State):* Valida `jugador.isDisponible()`; si el futbolista está en `EstadoLesionado` o `EstadoSancionado`, rechaza la convocatoria de inmediato.
  4. *Cobertura de la Formación:* Exige la cantidad exacta de arqueros, defensas, mediocampistas y delanteros definida por el esquema seleccionado.
  5. *Límite de Cupo:* No permite convocar a un doceavo jugador.

### 1.2. Vistas Interactivas del Cliente (`client-app/ui`)
Se construyeron las 6 vistas especializadas requeridas para la experiencia de gestión táctica:
1. **`VistaLogin.java`:** Interfaz de acceso rápido con usuario y contraseña. Detecta y notifica al DT si es un usuario existente o si se acaba de crear automáticamente su archivo persistente `data/perfiles/<usuario>.json` en el servidor.
2. **`VistaSelectorModo.java`:** Permite alternar entre *Modo 1 (Solo vs IA)* y *Modo 2 (2 Jugadores en red con cuenta regresiva)*.
3. **`VistaMiPlantilla.java`:** Despliega la nómina de futbolistas en formato tabular con atributos individuales, media general, barras visuales de energía (`[########  ] 80%`) y estado médico/disciplinario.
4. **`VistaCentroScouting.java`:** Presenta el reporte de inteligencia de la `ScoutingFacade` con barras de potencial por líneas (Defensa, Medio, Ataque), racha histórica de 5 partidos y advertencia sobre el jugador estrella adversario.
5. **`VistaPizarraTactica.java`:** Permite alternar formaciones tácticas (4-4-2, 4-3-3, 5-3-2, 3-5-2), cambiar estrategias (Posesión, Contraataque, Presión Alta, Bloque Bajo), verificar los 11 convocados con el Builder y presionar el botón **SIMULAR JORNADA**.
   * **Widget de Cuenta Regresiva de 60s:** Si se juega en modo de 2 personas y el rival confirmó primero, despliega un cronómetro regresivo en vivo (`[TEMPORIZADOR EN VIVO] Xs restantes...`) con refresco dinámico en pantalla hasta que se confirme o expire el tiempo.
6. **`VistaPanelResultados.java`:** Presenta los marcadores de todos los partidos simulados en paralelo en el servidor, con posesión de balón, tiros, desglose de incidencias (goles, tarjetas, lesiones) y la tabla de posiciones general actualizada.
7. **`ClienteTacticoApp.java`:** Clase ejecutable con método `main(...)` que enlaza el menú principal del mánager con el backend a través de `MotorConexion`.

### 1.3. Suite de Pruebas Automatizadas (`PlanDeJuegoBuilderTest.java`)
Se implementaron tests unitarios con JUnit 5 validando:
* Construcción exitosa y completa de 11 titulares válidos.
* Rechazo por alineación incompleta (< 11 jugadores).
* Rechazo si la distribución por posiciones no coincide con la formación seleccionada.
* Rechazo inmediato ante intento de duplicar un jugador.
* Rechazo ante intento de alinear jugadores lesionados o sancionados.
* Rechazo por ausencia de arquero reglamentario.

---

## 2. ¿Qué se usó?

* **Lenguaje:** Java 17 LTS.
* **Patrón de Diseño:** Creacional Builder (GoF).
* **Capa de Red:** `MotorConexion` basado en `java.net.http.HttpClient` de Java 11+.
* **Interfaz de Usuario:** Interfaz de consola rica con formateo tabular con `printf`, barras de caracteres ASCII y refresco de línea con retorno de carro `\r`.
* **Testing:** JUnit 5.

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

1. **Sondeo sin saturación de consola en la cuenta regresiva:**
   * *Problema potencial:* En el modo de 2 jugadores, al entrar en cuenta regresiva de 60 segundos, imprimir continuamente líneas nuevas llenaría la pantalla de texto repetitivo.
   * *Solución implementada:* Se implementó un bucle con `Thread.sleep(1000)` utilizando el caracter de retorno de carro `\r` (`\r[TEMPORIZADOR EN VIVO] Xs restantes...`), refrescando el segundo exacto en la misma línea visual sin ensuciar la terminal.

2. **Sincronización atómica entre titulares e identificadores en el Builder:**
   * *Ajuste:* Para que el DTO final sea liviano para transferirse por JSON y al mismo tiempo contenga los datos completos de los futbolistas convocados, el `PlanDeJuegoBuilder` sincroniza de forma transparente tanto la lista de objetos `titulares` como la lista de IDs `titularesIds`, evitando inconsistencias si el usuario remueve o cambia jugadores dinámicamente.
