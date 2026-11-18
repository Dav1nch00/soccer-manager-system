# Avance y Bitácora de Desarrollo: Bloque 2

**Módulo:** `server-app` (Persistencia y Servicios)  
**Nombre del Bloque:** Persistencia y Gestión de Perfiles en Archivo  
**Fecha de Ejecución:** 17 de noviembre de 2026  
**Estado:** Completado con Éxito  

---

## 1. ¿Qué se hizo?

Se implementó el subsistema completo de almacenamiento desacoplado y persistencia basado en archivos individuales JSON por mánager en el servidor, ubicado en el directorio `data/perfiles/`:

1. **Entidad de Persistencia (`PerfilUsuario.java`):**
   * Modela el documento JSON individual que se almacena en disco para cada mánager (`data/perfiles/<usuario>.json`).
   * Almacena identificadores únicos (`idPerfil` generado con UUID), nombre de usuario, contraseña hasheada, club asignado, marcas temporales de creación y última conexión, y estadísticas deportivas acumuladas (partidos jugados, ganados, empatados, perdidos, goles a favor, goles en contra, puntos y títulos).
   * Incorpora métodos de lógica de negocio para actualizar récords deportivos de forma cohesiva (`registrarVictoria`, `registrarEmpate`, `registrarDerrota`).
   * Provee conversión bidireccional limpia hacia el DTO transferible `PerfilDTO`.

2. **Mecanismo de Seguridad Criptográfica (`SeguridadUtil.java`):**
   * Garantiza que las contraseñas nunca se almacenen en texto plano dentro de los archivos JSON.
   * Aplica un algoritmo robusto con `SHA-256` y salting aleatorio criptográfico de 16 bytes (`java.security.SecureRandom`).
   * Codifica el par `Salt:Hash` en Base64 para una serialización segura en formato de cadena.
   * Utiliza comparación de tiempo constante (`MessageDigest.isEqual`) en la verificación de credenciales para mitigar ataques de temporización (*timing attacks*).

3. **Repositorio de Archivos JSON (`PerfilArchivoRepository.java`):**
   * Encapsula todas las operaciones de entrada/salida (I/O) sobre el sistema de archivos del servidor.
   * Inicializa y asegura automáticamente la existencia de la carpeta `data/perfiles/`.
   * Método `existePerfil(usuario)`: Comprueba la presencia física del archivo `.json`.
   * Método `guardar(perfil)`: Escribe y sobrescribe el archivo en disco de forma formateada (*pretty print* indentado).
   * Método `buscarPorNombreUsuario(usuario)`: Carga y deserializa el contenido JSON a la entidad `PerfilUsuario`.
   * Método `listarTodos()`: Escanea el directorio retornando todos los perfiles registrados.
   * Método `eliminarPerfil(usuario)`: Permite eliminar archivos de prueba o reseteos.
   * Implementa concurrencia segura por archivo mediante un mapa concurrente de bloqueos de lectura/escritura (`ConcurrentHashMap<String, ReentrantReadWriteLock>`), asegurando lecturas simultáneas no bloqueantes y escrituras atómicas exclusivas.

4. **Servicio de Negocio de Perfiles (`PerfilService.java`):**
   * Orquesta el flujo de inicio de sesión:
     * **Caso Usuario Nuevo:** Si el archivo no existe, lo crea automáticamente en disco asignándole su club, contraseña cifrada y estadísticas en cero, retornando un `SesionDTO` con `perfilNuevoCreado = true`.
     * **Caso Usuario Existente:** Si el archivo ya existe, valida la contraseña ingresada; si es correcta, actualiza la fecha de última conexión y retorna `SesionDTO` con `perfilNuevoCreado = false`. Si la clave es errónea, lanza una excepción de seguridad `SecurityException`.
   * Provee el método `registrarResultadoPartido(usuario, resultado, esLocal)` para impactar automáticamente en disco los puntos, goles y balance tras cada fecha simulada.

5. **Suite de Pruebas Automatizadas (`PerfilServiceTest.java`):**
   * Valida la auto-creación del archivo `.json` en disco ante nuevos usuarios.
   * Valida el login exitoso de usuarios existentes.
   * Valida el rechazo y lanzamiento de `SecurityException` ante contraseñas incorrectas.
   * Valida la persistencia correcta de estadísticas tras una victoria simulada (3 a 1).

---

## 2. ¿Qué se usó?

* **Tecnología Base:** Java 17 LTS (Java NIO: `java.nio.file.Files`, `java.nio.file.Path`, `java.nio.file.Paths`).
* **Librería de Serialización:** Jackson Databind (`com.fasterxml.jackson.databind.ObjectMapper`) con módulo JSR-310 (`JavaTimeModule`) y formateo indentado (`SerializationFeature.INDENT_OUTPUT`).
* **Seguridad y Criptografía:** `java.security.MessageDigest` (algoritmo SHA-256), `java.security.SecureRandom` y `java.util.Base64`.
* **Programación Concurrente:** `java.util.concurrent.ConcurrentHashMap` y `java.util.concurrent.locks.ReentrantReadWriteLock` para exclusión mutua de archivos.
* **Testing:** JUnit 5 (`org.junit.jupiter.api.*`) con limpieza y aislamiento de carpetas temporales de prueba (`@BeforeEach`, `@AfterEach`).
* **Patrones de Diseño:**
  * **Repository Pattern:** Desacopla la lógica de negocio de los detalles de almacenamiento en archivos JSON.
  * **Service Pattern:** Orquesta las reglas de negocio de autenticación y actualización deportiva.
  * **Read-Write Lock Pattern:** Permite concurrencia de lectura masiva sin condiciones de carrera en escritura.

---

## 3. ¿Se generó algún cambio por algún error o ajuste en el proceso?

Durante la implementación del Bloque 2 se identificaron y solventaron los siguientes detalles técnicos:

1. **Sanitización de nombres de archivo ante caracteres especiales en nombres de usuario:**
   * *Problema potencial:* En Windows y Linux, si un usuario ingresa caracteres reservados en nombres de archivo (tales como `\`, `/`, `:`, `*`, `?`, `"`, `<`, `>`, `|` o espacios), la llamada a `Files.createFile` o `objectMapper.writeValue` arrojaría una excepción del sistema operativo (`InvalidPathException` o `IOException`).
   * *Solución implementada:* Se añadió el método `normalizarNombre(String nombreUsuario)` en `PerfilArchivoRepository`, el cual aplica `trim()`, pasa a minúsculas y reemplaza cualquier caracter no alfanumérico por un guión bajo `_`. De esta forma, el usuario `"Carlos DT #1"` genera de forma segura y válida el archivo `carlos_dt__1.json`.

2. **Prevención de condiciones de carrera en disco:**
   * *Problema potencial:* En el modo de 2 jugadores, ambos clientes pueden consultar o actualizar sus perfiles simultáneamente, o el servidor podría intentar leer un perfil mientras el hilo de consolidación escribe el resultado del partido.
   * *Solución implementada:* Se asoció un `ReentrantReadWriteLock` independiente a cada nombre de usuario normalizado mediante un `ConcurrentHashMap`. De este modo:
     * Múltiples hilos pueden leer el perfil al mismo tiempo (`readLock().lock()`).
     * Solo un hilo puede escribir o actualizar el archivo en disco a la vez (`writeLock().lock()`), garantizando integridad referencial y cero corrupción de datos.

3. **Legibilidad y auditoría humana de los perfiles:**
   * *Ajuste:* Por defecto, Jackson genera cadenas JSON compactas en una sola línea. Se activó explícitamente `SerializationFeature.INDENT_OUTPUT` para que los archivos generados en `data/perfiles/` cuenten con formato tabular legible por cualquier desarrollador o evaluador al abrir el archivo directamente en el explorador.
