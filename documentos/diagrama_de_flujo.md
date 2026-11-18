# Diagramas de Flujo del Sistema: "Soccer Manager"

Este documento describe de forma gráfica y detallada los flujos de control, datos, persistencia en archivos, concurrencia, sincronización de dos jugadores y transiciones de estado del sistema **Simulador Táctico "Soccer Manager"**.

---

## 1. Diagrama de Flujo: Inicio de Sesión y Generación Automática de Archivo de Perfil

Flujo ejecutado al abrir la aplicación donde cada mánager se autentica y el sistema gestiona su archivo de perfil individual (`data/perfiles/<usuario>.json`):

```mermaid
flowchart TD
    InicioApp([Lanzar Aplicación Cliente]) --> FormLogin["Ingresar Nombre de Usuario y Contraseña"]
    FormLogin --> SendLogin["Enviar petición de autenticación al Servidor"]
    
    SendLogin --> CheckExiste{"¿Existe el archivo 'data/perfiles/<usuario>.json'?"}
    
    %% Flujo Usuario Nuevo
    CheckExiste -- "NO (Usuario Nuevo)" --> CrearArchivo["Crear automáticamente archivo '<usuario>.json'"]
    CrearArchivo --> HashPass["Hashear contraseña"]
    HashPass --> InitPerfil["Inicializar perfil con club asignado y estadísticas en 0"]
    InitPerfil --> EscribirDisco["Escribir JSON en disco: data/perfiles/<usuario>.json"]
    EscribirDisco --> LoginOkNuevo["Autenticación exitosa (Perfil Nuevo Creado)"]
    
    %% Flujo Usuario Existente
    CheckExiste -- "SÍ (Usuario Registrado)" --> LeerArchivo["Leer datos desde '<usuario>.json'"]
    LeerArchivo --> ValidarPass{"¿La contraseña coincide con el hash guardado?"}
    ValidarPass -- "No" --> ErrorPass["Error: Contraseña incorrecta"]
    ErrorPass --> FormLogin
    ValidarPass -- "Sí" --> CargarDatos["Cargar historial, club asignado y estadísticas"]
    CargarDatos --> LoginOkExistente["Autenticación exitosa (Perfil Cargado)"]
    
    LoginOkNuevo --> SelModo
    LoginOkExistente --> SelModo
    
    SelModo["Avanzar a Pantalla de Selección de Modo"] --> FinLogin([Fin de Flujo de Inicio de Sesión])
```

---

## 2. Diagrama de Flujo: Selección y Coordinación de Modos de Juego (Solo vs 2 Jugadores)

Muestra la lógica de bifurcación entre el modo en solitario y el modo multijugador coordinado:

```mermaid
flowchart TD
    InicioModo([Seleccionar Modo de Simulación]) --> CheckModo{"¿Qué modo eligió el usuario?"}
    
    %% Rama Modo Solitario
    CheckModo -- "Modo 1: Simular Solo (vs IA)" --> Modo1_Setup["Asignar IA a los demás N-1 clubes de la liga"]
    Modo1_Setup --> Modo1_Tactica["DT Humano diseña y valida su alineación (Builder)"]
    Modo1_Tactica --> Modo1_Send["Enviar PlanDeJuegoDTO al Servidor"]
    Modo1_Send --> Modo1_Simular["Disparar Simulación de Jornada inmediatamente"]
    Modo1_Simular --> SimularJornadaEngine
    
    %% Rama Modo 2 Jugadores
    CheckModo -- "Modo 2: Simular con Otra Persona" --> Modo2_Lobby["Crear / Unirse a Sala de Competición"]
    Modo2_Lobby --> Modo2_Check2Auth{"¿Ambos DTs iniciaron sesión con sus perfiles?"}
    
    Modo2_Check2Auth -- "Falta DT 2" --> Modo2_WaitAuth["Esperar a que el segundo usuario inicie sesión"]
    Modo2_WaitAuth --> Modo2_Check2Auth
    
    Modo2_Check2Auth -- "Ambos Conectados" --> Modo2_FaseTactica["Habilitar Planificación Táctica en Paralelo"]
    Modo2_FaseTactica --> Modo2_Edicion["DT 1 y DT 2 configuran sus estrategias simultáneamente"]
    
    Modo2_Edicion --> Modo2_PrimerEnvio{"¿Un DT termina y presiona 'Simular'?"}
    
    Modo2_PrimerEnvio -- "DT confirma primero" --> Modo2_StartTimer["Iniciar Contador Regresivo de 60 Segundos en Servidor"]
    Modo2_StartTimer --> Modo2_Notify["Notificar en UI del segundo DT: 'Tiempo restante: 60s'"]
    
    Modo2_Notify --> Modo2_CheckFinal{"¿El segundo DT envía su plan antes de 60s?"}
    
    Modo2_CheckFinal -- "SÍ (Confirma a tiempo)" --> Modo2_CancelTimer["Detener temporizador inmediatamente"]
    Modo2_CancelTimer --> SimularJornadaEngine
    
    Modo2_CheckFinal -- "NO (Expiran 60s / Timeout)" --> Modo2_Force["Forzar simulación con la táctica actual (Auto-completar vacíos con IA)"]
    Modo2_Force --> SimularJornadaEngine
    
    SimularJornadaEngine([Motor Concurrente: Simulación de N/2 Partidos])
```

---

## 3. Diagrama de Flujo Principal: Ciclo de Juego por Jornada (Game Loop Completo)

Integra la autenticación, los dos modos de juego y el guardado automático de archivos de perfil al concluir los partidos:

```mermaid
flowchart TD
    Inicio([Inicio del Simulador]) --> Login["1. Login / Creación automática de archivo <usuario>.json"]
    Login --> SelModo["2. Selección de Modo: Solo vs 2 Jugadores"]
    
    SelModo --> PreJornada["3. Fase de Evaluación: Plantilla y Scouting (ScoutingFacade)"]
    PreJornada --> PlanTactica["4. Fase de Planificación: Formación, 11 Titulares y Estrategia"]
    
    PlanTactica --> ValidaBuilder{"5. ¿Plan de juego válido? (Builder)"}
    ValidaBuilder -- "No (Faltan jugadores/lesionados)" --> CorregirTactica["Corregir alineación en la pizarra"]
    CorregirTactica --> PlanTactica
    
    ValidaBuilder -- "Sí" --> EnviarPlan["6. Enviar PlanDeJuegoDTO validado al Servidor"]
    
    EnviarPlan --> CheckBarreraModo{"7. ¿Modo 2P activo?"}
    CheckBarreraModo -- "Sí (Modo 2P)" --> Start60s["Iniciar contador de 60s al DT rival"]
    Start60s --> CheckTimer{"¿El rival confirma o expiran 60s?"}
    CheckTimer --> DispararSim
    CheckBarreraModo -- "No (Modo Solo)" --> DispararSim["8. Disparar Simulación Concurrente (ThreadPool)"]
    
    subgraph SimServidor ["Simulación y Consolidación Concurrente"]
        DispararSim --> SimPartidos["9. Ejecutar N/2 partidos en paralelo (Pipes & Filters)"]
        SimPartidos --> BarreraHilos{"10. ¿Finalizaron todos los partidos?"}
        BarreraHilos -- "No" --> SimPartidos
        BarreraHilos -- "Sí" --> LockConsolidar["11. Adquirir ReentrantLock: Actualizar Tabla"]
    end
    
    LockConsolidar --> UpdatePerfiles["12. Actualizar archivos individuales 'data/perfiles/<usuario>.json'"]
    UpdatePerfiles --> PublicarResultados["13. Transmitir marcadores, incidencias y posiciones"]
    
    PublicarResultados --> FinFecha{"¿Fin de la Liga?"}
    FinFecha -- "No" --> PreJornada
    FinFecha -- "Sí" --> FinTorneo([Fin del Torneo: Premiación y Campeón])
```

---

## 4. Diagrama de Flujo del Motor de Simulación: Tuberías y Filtros (*Pipes and Filters*)

Muestra las 5 etapas secuenciales que atraviesa cada partido individual y la actualización final de los archivos de perfil:

```mermaid
flowchart TD
    InContext([Contexto Inicial del Encuentro]) --> F1_Val{"1. Filtro de Validación"}
    
    F1_Val -- "¿Algún club sin 11 aptos?" --> AutoFix["Asignar suplentes o táctica por defecto de IA"]
    AutoFix --> F2_Tactico
    F1_Val -- "11 vs 11 aptos" --> F2_Tactico["2. Filtro de Cálculo Táctico"]
    
    subgraph CalculoTactico ["Cálculo Táctico"]
        F2_Tactico --> CompLines["Calcular poder por líneas: Ataque, Medio, Defensa (Composite)"]
        CompLines --> MatchStrat["Cruce de Estrategias y contrarrestos (Strategy)"]
        MatchStrat --> CalcProb["Generar coeficientes de probabilidad de gol"]
    end
    
    CalcProb --> F3_Eventos["3. Filtro de Simulación de Eventos (Min 1 a 90)"]
    
    subgraph Eventos90Min ["Simulación Temporal"]
        F3_Eventos --> LoopMin["Iterar minutos 1 a 90"]
        LoopMin --> CheckGol{"¿Gol?"}
        CheckGol -- "Sí" --> AddGol["Registrar anotador y sumatoria al marcador"]
        CheckGol -- "No" --> CheckFalta{"¿Falta / Tarjeta?"}
        CheckFalta -- "Sí" --> AddTarjeta["Registrar falta y evaluar amarilla/roja"]
        CheckFalta -- "No" --> EvalCalifs["Actualizar calificación de jugadores"]
        AddGol --> LoopMin
        AddTarjeta --> LoopMin
        EvalCalifs --> Check90{"¿Minuto 90?"}
        Check90 -- "No" --> LoopMin
        Check90 -- "Sí" --> FinEventos["Marcador final y lista de incidencias"]
    end
    
    FinEventos --> F4_Desgaste["4. Filtro de Desgaste y Estado"]
    
    subgraph DesgasteSalud ["Salud y Fatiga"]
        F4_Desgaste --> ReducirEner["Disminuir energía según exigencia física"]
        ReducirEner --> CheckLesion{"¿Lesión en partido?"}
        CheckLesion -- "Sí" --> SetLesionado["Transición a EstadoLesionado"]
        CheckLesion -- "No" --> CheckFatiga["Transición a EstadoCansado si energía < 50%"]
    end
    
    SetLesionado --> F5_Consolidar["5. Filtro de Consolidación y Persistencia"]
    CheckFatiga --> F5_Consolidar
    
    subgraph ConsolidacionSegura ["Consolidación Segura en Disco y Memoria"]
        F5_Consolidar --> LockTabla["Adquirir ReentrantLock de Tabla de Posiciones"]
        LockTabla --> WriteTabla["Actualizar puntos, goles y posiciones"]
        WriteTabla --> UnlockTabla["Liberar ReentrantLock de Tabla"]
        UnlockTabla --> CheckHumano{"¿El partido involucra mánagers humanos?"}
        CheckHumano -- "Sí" --> PersistirPerfil["Actualizar archivo 'data/perfiles/<usuario>.json'"]
        CheckHumano -- "No (Solo IA)" --> OutResultado
        PersistirPerfil --> OutResultado
    end
    
    OutResultado([ResultadoPartidoDTO preparado])
```

---

## 5. Diagrama de Secuencia: Modo 2 Jugadores (Autenticación, Espera y Simulación)

Muestra la interacción completa entre dos clientes humanos, el servidor, los archivos de perfil y el motor de simulación:

```mermaid
sequenceDiagram
    autonumber
    actor DT1 as DT 1 (Cliente 1)
    actor DT2 as DT 2 (Cliente 2)
    participant Net as Capa de Red (REST/JSON)
    participant Srv as Servidor Central
    participant Disco as Sistema de Archivos (/data/perfiles/)
    participant Sala as GestorModoYSala
    participant Pool as ThreadPool (N/2 Partidos)

    %% Autenticación DT 1
    DT1->>Net: POST /api/sesion/login-o-registro (dt1, clave)
    Net->>Srv: Validar o crear perfil
    Srv->>Disco: ¿Existe 'data/perfiles/dt1.json'?
    Disco-->>Srv: No existe -> Crear automáticamente dt1.json
    Srv-->>DT1: 200 OK (SesionDTO: Club A asignado)

    %% Autenticación DT 2
    DT2->>Net: POST /api/sesion/login-o-registro (dt2, clave)
    Net->>Srv: Validar o crear perfil
    Srv->>Disco: ¿Existe 'data/perfiles/dt2.json'?
    Disco-->>Srv: No existe -> Crear automáticamente dt2.json
    Srv-->>DT2: 200 OK (SesionDTO: Club B asignado)

    %% Configuración de Modo
    DT1->>Net: POST /api/partida/modo (DOS_JUGADORES)
    Net->>Sala: Configurar sala para 2 jugadores
    Sala-->>DT1: Sala activa: esperando que ambos confirmen táctica

    %% Planificación simultánea y DT1 envía su plan primero
    Note over DT1,DT2: Ambos editan sus estrategias en paralelo
    DT1->>Net: POST /api/jornada/simular (Plan DT 1 listo)
    Net->>Sala: registrarPlan(DT1)
    Sala->>Sala: Iniciar ScheduledExecutorService (Cuenta regresiva 60s)
    Sala-->>DT1: 200 OK {"estado": "CUENTA_REGRESIVA", "mensaje": "Esperando al DT 2 (60s restante)"}
    Sala-->>DT2: Notificación / Polling: {"estado": "CUENTA_REGRESIVA", "segundosRestantes": 60}

    %% Resolución: Opción DT2 confirma o expira
    alt Caso 1: DT 2 confirma antes de 60 segundos
        DT2->>Net: POST /api/jornada/simular (Plan DT 2 listo)
        Net->>Sala: registrarPlan(DT2)
        Sala->>Sala: Cancelar temporizador
        Sala-->>DT2: 200 OK {"estado": "SIMULANDO"}
    else Caso 2: Expiran los 60 segundos (Timeout)
        Note over Sala: Temporizador llega a 0s. Servidor toma la táctica actual de DT 2 y auto-completa vacíos.
    end

    %% Simulación Concurrente
    Sala->>Pool: Disparar simulación de la jornada en paralelo
    par Hilos Simulando Partidos
        Pool->>Pool: Partido 1 (Club A vs Club B u otro)
        Pool->>Pool: Partidos restantes (Equipos IA)
    end
    Note over Pool: Barrera superada (invokeAll / CompletableFuture)

    %% Persistencia en Archivos de Perfil
    Pool->>Disco: Actualizar estadísticas en 'dt1.json'
    Pool->>Disco: Actualizar estadísticas en 'dt2.json'

    %% Notificación de Resultados
    DT1->>Net: GET /api/torneo/tabla-y-resultados
    Net-->>DT1: Marcadores, resumen y tabla actualizada
    DT2->>Net: GET /api/torneo/tabla-y-resultados
    Net-->>DT2: Marcadores, resumen y tabla actualizada
```

---

## 6. Diagrama de Transición de Estados: Ciclo de Vida del Futbolista (Patrón State)

```mermaid
stateDiagram-v2
    [*] --> Optimo: Fichaje / Inicio de Torneo

    Optimo --> Cansado: Participa en encuentro de alta exigencia (Energía < 50%)
    Optimo --> Lesionado: Evento de lesión en Filtro 4
    Optimo --> Sancionado: Expulsión (tarjeta roja) o acumulación de amarillas

    Cansado --> Optimo: Fecha de descanso / Descanso táctico
    Cansado --> Lesionado: Sobrecarga muscular extrema
    Cansado --> Sancionado: Tarjeta roja por juego brusco

    Lesionado --> Optimo: Fin del periodo de incapacidad médica
    Sancionado --> Optimo: Cumplimiento de fechas de suspensión arbitral

    note right of Optimo
        - Habilitado al 100% de atributos
    end note

    note right of Cansado
        - Habilitado para jugar
        - Penalización física (-15% a -30%)
    end note

    note right of Lesionado
        - Inhabilitado para ser alineado
    end note

    note right of Sancionado
        - Inhabilitado reglamentariamente
    end note
```

---

## 7. Diagrama de Flujo del Patrón Builder: Validación de Alineación en Cliente

```mermaid
flowchart TD
    InicioBuilder([Iniciar Plan de Juego]) --> SetForm["1. Asignar Formación (ej. 4-4-2)"]
    SetForm --> SetStrat["2. Asignar Estrategia (ej. Contraataque)"]
    SetStrat --> PickPlayer["3. Seleccionar Jugador para la Alineación"]
    
    PickPlayer --> CheckDup{"¿El jugador ya fue seleccionado?"}
    CheckDup -- "Sí" --> ErrDup["Error: Jugador repetido en la alineación"]
    ErrDup --> PickPlayer
    
    CheckDup -- "No" --> CheckDisp{"¿Estado es Lesionado o Sancionado?"}
    CheckDisp -- "Sí" --> ErrDisp["Error: Futbolista no disponible"]
    ErrDisp --> PickPlayer
    
    CheckDisp -- "No" --> AddPlayer["Añadir jugador a la posición táctica"]
    AddPlayer --> CheckTotal{"¿Total de titulares = 11?"}
    CheckTotal -- "No (< 11)" --> PickPlayer
    CheckTotal -- "Sí (= 11)" --> CheckRoles{"¿Posiciones clave cubiertas (Arquero, etc.)?"}
    
    CheckRoles -- "No" --> ErrRoles["Error: Posiciones indispensables vacías"]
    ErrRoles --> PickPlayer
    
    CheckRoles -- "Sí" --> GenerarDTO["Ejecutar build(): Generar PlanDeJuegoDTO"]
    GenerarDTO --> Serializar["Serializar a JSON"]
    Serializar --> Enviar([Enviar al Servidor])
```
