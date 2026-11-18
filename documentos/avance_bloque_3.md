# Avance y Bitácora de Desarrollo: Bloque 3

**Módulo:** `server-app` (Dominio y Lógica Deportiva)  
**Nombre del Bloque:** Modelo de Dominio Deportivo y Patrones GoF (Composite, Strategy, State)  
**Fecha de Ejecución:** 17 de noviembre de 2026  
**Estado:** Completado con Éxito  

---

## 1. ¿Qué se hizo?

Se desarrolló el núcleo futbolístico y estratégico del simulador dentro de `server-app/src/main/java/com/soccermanager/server/domain`, aplicando con rigor tres patrones de diseño clásicos de la banda de los cuatro (GoF):

### 1.1. Patrón Estructural Composite (`domain/model`)
Permite calcular métricas y potenciales de juego de manera uniforme a través de una jerarquía de árbol:
* **`ComponenteTactico.java` (Interfaz Component):** Declara los métodos `calcularPoderOfensivo()`, `calcularPoderDefensivo()`, `calcularPoderControl()` y `getCantidadElementos()`.
* **`Jugador.java` (Hoja / Leaf):** Modela al futbolista individual. Computa sus poderes efectivos combinando sus atributos base (velocidad, tiro, pase, defensa, físico), ponderados por su nivel de energía y el multiplicador de su estado actual (`EstadoJugador`).
* **`LineaTactica.java` (Compuesto Intermedio / Composite):** Agrupa los jugadores de una demarcación específica (*Portería*, *Defensa*, *Mediocampo*, *Delantera*) y calcula los promedios de la línea.
* **`Equipo.java` (Compuesto Raíz / Root Composite):** Agrupa las 4 líneas tácticas y calcula el potencial global del club aplicando ponderaciones tácticas realistas:
  * *Poder Ofensivo:* Delantera (50%), Mediocampo (35%), Defensa (15%).
  * *Poder Defensivo:* Defensa (50%), Portería (25%), Mediocampo (25%).
  * *Poder de Control / Posesión:* Mediocampo (60%), Defensa (20%), Delantera (20%).
* **`Club.java`:** Entidad institucional que gestiona la plantilla completa de futbolistas, jugadores disponibles y detección algorítmica del jugador estrella.

### 1.2. Patrón de Comportamiento Strategy (`domain/strategy`)
Encapsula los algoritmos tácticos y la matriz de contrarrestos e intensidades:
* **`EstrategiaTactica.java` (Interfaz Strategy):** Define el contrato con `aplicarModificadores(Equipo, EstrategiaTactica)` y `getDesgasteBase()`.
* **`ModificadorTactico.java`:** Objeto de valor que transporta bonos ofensivos, defensivos, de control, factores de faltas/tarjetas y desgaste físico adicional.
* **Estrategias Concretas:**
  * `PosesionStrategy`: +25% control de balón, +10% ataque paciente, desgaste base de 15 puntos. Vulnerable ante equipos de contraataque (-15% defensa por espacios a la espalda).
  * `ContraataqueStrategy`: +15% ataque base, desgaste de 22 puntos. **Contrarresto clave:** Obtiene un masivo +35% de efectividad ofensiva cuando enfrenta a un rival con estrategia de *Posesión*.
  * `PresionAltaStrategy`: +20% control y recuperación en campo rival, +20% ocasiones generadas, pero eleva las infracciones a un factor de 1.45 (+45% probabilidad de tarjetas) y exige 28 puntos de energía base.
  * `BloqueBajoStrategy`: Muro defensivo de +35%, reduce la generación ofensiva propia (-25%) y minimiza el desgaste físico a solo 12 puntos de energía base.
* **`EstrategiaFactory.java`:** Fábrica estática que desacopla la selección de estrategia desde el DTO hacia la instancia de algoritmo adecuada.

### 1.3. Patrón de Comportamiento State (`domain/state`)
Modela el ciclo de vida, salud y habilitación disciplinaria del futbolista:
* **`EstadoJugador.java` (Interfaz State):** Métodos `puedeJugar()`, `obtenerFactorRendimiento()` y `avanzarJornada(Jugador)`.
* **Estados Concretos:**
  * `EstadoOptimo`: Habilitado para jugar al 100% de atributos.
  * `EstadoCansado`: Habilitado, pero con factor de rendimiento de 0.75 (-25% de penalización en velocidad y físico).
  * `EstadoLesionado`: Inhabilitado (`puedeJugar() == false`, rendimiento 0.0). Administra un contador regresivo de semanas; al llegar a cero, otorga el alta médica y transiciona automáticamente a `EstadoOptimo`.
  * `EstadoSancionado`: Inhabilitado reglamentariamente (`puedeJugar() == false`, rendimiento 0.0) por tarjeta roja o ciclo de amonestaciones. Al cumplir las fechas de suspensión, transiciona automáticamente a `EstadoOptimo`.

### 1.4. Suite de Pruebas Automatizadas (`DominioPatronesTest.java`)
Se implementaron tests unitarios con JUnit 5 validando:
1. Cálculo uniforme del Composite a nivel de Jugador, Línea y Equipo completo.
2. Contrarrestos del Strategy (+35% de Contraataque ante Posesión, faltas de Presión Alta y bonos defensivos de Bloque Bajo).
3. Transiciones del State (Óptimo a Cansado por fatiga, inhabilitación y alta médica por lesión, y suspensión por tarjeta roja).

---

## 2. ¿Qué se usó?

* **Lenguaje:** Java 17 LTS.
* **Patrones GoF:**
  * *Composite Pattern:* Jerarquía Hoja/Compuesto uniforme.
  * *Strategy Pattern:* Intercambiabilidad de algoritmos de juego en tiempo de ejecución.
  * *State Pattern:* Cambio dinámico de comportamiento según el estado fisiológico/disciplinario.
  * *Factory Pattern:* Creación centralizada de estrategias.
* **Testing:** JUnit 5 (`@Test`, aserciones de igualdad con delta flotante y verificación de instancias polimórficas).

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

Durante el desarrollo del Bloque 3 se detectaron y corrigieron los siguientes aspectos clave:

1. **Prevención de indeterminación numérica (`NaN`) en líneas vacías del Composite:**
   * *Problema potencial:* Si una `LineaTactica` se instanciaba pero aún no tenía jugadores asignados (por ejemplo, durante la construcción paso a paso de la alineación), la operación de promedio `total / elementos.size()` producía una división por cero (`0.0 / 0 = NaN`).
   * *Solución implementada:* Se añadió una guarda explícita en todos los métodos de `LineaTactica`: `if (elementos.isEmpty()) return 0.0;`, garantizando solidez numérica sin importar el orden de llenado.

2. **Blindaje de jugadores inhabilitados en los cálculos matemáticos del partido:**
   * *Ajuste:* Se aseguró que cuando el estado de un futbolista retorna `puedeJugar() == false` (como en `EstadoLesionado` o `EstadoSancionado`), las llamadas a `calcularPoderOfensivo()`, `calcularPoderDefensivo()` y `calcularPoderControl()` devuelvan inmediatamente `0.0`. De este modo, si por error un jugador no apto quedase en la alineación, el Composite no aportará poder alguno al club.

3. **Automatización disciplinaria por acumulación de tarjetas:**
   * *Mejora implementada:* En `Jugador.java`, se ligó el método `registrarTarjetaAmarilla()` con la regla deportiva estándar: cada 5 amarillas acumuladas (`tarjetasAmarillas % 5 == 0`), el jugador pasa de forma automática a `EstadoSancionado(1)`, reduciendo la complejidad del motor de simulación.
