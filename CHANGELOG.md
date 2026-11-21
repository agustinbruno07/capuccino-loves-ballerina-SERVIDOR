# Changelog — Servidor

Todos los cambios importantes del repositorio **SERVIDOR** de Cappuccino Loves Ballerina serán registrados en este archivo.

Este historial conserva los cambios del videojuego que afectan a la simulación y al funcionamiento del servidor. Desde la separación de los repositorios, este archivo registra únicamente la aplicación SERVIDOR.

## [2.0.0] - 2026-11-21

### Modificado
- Separación del proyecto en dos repositorios independientes: CLIENTE y SERVIDOR. Este repositorio corresponde exclusivamente al SERVIDOR.
- Se consolidó la responsabilidad del servidor como autoridad de la partida: recibe las acciones de los jugadores, ejecuta la simulación y distribuye el estado resultante. No funciona como un tercer jugador ni renderiza una partida local.
- Se simplificó `Servidor` separando los datos de cada conexión en la clase `game.red.Cliente`, que representa a cada jugador conectado y conserva su dirección, sesión, controles y datos de secuencia.
- Se simplificó `ControladorEntradaJugador` para que el servidor aplique los controles recibidos por red, sin depender de un teclado local para manejar a los personajes.
- Se ordenó `ProtocolosRed` manteniendo el mismo formato de mensajes que utiliza el repositorio CLIENTE. Ambos proyectos deben conservar esta compatibilidad para comunicarse.

### Documentación
- Se incorporó este changelog específico del repositorio SERVIDOR.
- Se preparó un README propio que explica que este programa inicia y supervisa la partida online.

## [1.7.0] - 2026-11-16

### Agregado
- Aplicación gráfica exclusiva del servidor: `CapuccinoServidorGame`, `PantallaMenuServidor`, `ServicioServidor` y `Lwjgl3ServidorLauncher`.
- Pantalla de supervisión con los jugadores conectados, el estado de la partida y el botón para detener el servidor.
- Distribución independiente para Windows: `CappuccinoServidor.exe`, con Java incluido y sin consola, generada mediante `jpackage`.
- Pruebas reproducibles de las reglas de la partida, comunicación UDP, ciclo de vida del servidor y ejecución de varios procesos gráficos.

### Modificado
- Integración de la comunicación UDP con la simulación autoritativa. El servidor recibe los controles y sincroniza movimientos, habilidades, espuma, cortes, palancas, puerta, derrota, victoria y reinicios.
- Descubrimiento por broadcast general, direcciones de las interfaces de red y loopback. El descubrimiento no ocupa un cupo: el registro de cada jugador se realiza por separado.
- Asignación automática de los dos personajes y confirmación de ambos jugadores antes de comenzar la cuenta regresiva.
- Validación de controles mediante sesión, ronda, secuencia y contadores de acciones.
- Simulación de la partida a 60 actualizaciones por segundo y envío periódico del estado a los clientes.
- Menú propio para iniciar y salir del servidor. El punto de entrada determina que esta aplicación funciona como servidor.

### Corregido
- Registro duplicado de un mismo jugador cuando el cliente descubre el servidor a través de distintas interfaces de red.
- Recepción de asignaciones aunque el mensaje de confirmación inicial `OK` se pierda o llegue después.
- Rechazo de paquetes inválidos, antiguos o duplicados.
- Detección de desconexiones por timeout y liberación de los lugares de los jugadores.
- Prevención de movimientos que quedan activos cuando dejan de llegar controles.
- Carga de mapas y tilesets del servidor desde el classpath al ejecutar la aplicación fuera del proyecto.
- Inicio del servicio fuera del hilo gráfico, cierre de sockets y liberación de recursos.
- Mensaje entendible cuando el puerto UDP ya está ocupado.

### Eliminado
- `InicioLan` y la conversión automática entre cliente y servidor.
- La posibilidad de que la aplicación SERVIDOR se comporte como un jugador.

## [1.6.0] - 2026-11-07

### Agregado
- Infraestructura de red UDP:
  - `Servidor`: administra hasta dos jugadores, asigna los personajes y conserva el estado autoritativo de la partida.
  - `HiloServidor`: escucha el puerto UDP, recibe los datagramas y los entrega al servidor para su procesamiento.
  - Envío de estados de la partida a los jugadores conectados.
- Asignación automática de roles para Cappuccino Assassino y Ballerina Capuccina.

### Corregido
- Serialización de los datos booleanos de los personajes, como la dirección en la que miran y si están en el suelo, para que los estados recibidos puedan interpretarse correctamente.

### Notas técnicas
- Se realizaron pruebas locales de conexión secuencial y de límite de dos jugadores.
- En esta etapa inicial, la integración completa entre la red y las reglas de la partida todavía estaba pendiente y se completó en iteraciones posteriores.

## [1.5.2] - 2026-10-30

### Agregado
- Integración de Ballerina Capuccina como personaje de la partida.
- Habilidad de creación de `BloqueEspuma`, una plataforma temporal que puede utilizarse durante el nivel.
- Duración y tiempo de espera (*cooldown*) configurables para la habilidad.
- Posición inicial de Ballerina configurable desde Tiled mediante `spawn_ballerina`.
- Reglas cooperativas:
  - La victoria requiere que ambos personajes lleguen a la puerta abierta.
  - Si cualquiera de los personajes cae al vacío o toca una zona peligrosa, la partida termina en derrota.
  - Ambos personajes pueden activar las palancas al encontrarse cerca.

### Modificado
- `Constantes.java` incorpora los parámetros de tamaño, duración y cooldown de la espuma.

## [1.5.1] - 2026-10-29

### Refactorizado
- Se reemplazó la dependencia directa de `CapuccinoLovesBallerinaGame` por la clase base `Game` en las pantallas y los menús, reduciendo el acoplamiento entre clases.

## [1.5] - 2026-10-27

### Agregado
- Comunicación UDP inicial entre clientes y servidor mediante las clases `Servidor` y `HiloServidor`.
- Lectura de mensajes por consola para realizar pruebas de comunicación entre los procesos.

### Cambiado
- Se configuró la resolución inicial de la aplicación de escritorio en 1280x720.

## [1.4.6] - 2026-10-10

### Agregado
- Controles de volumen y silencio para el sistema de audio.

### Refactorizado
- Se separaron responsabilidades de la partida en `ControladorPartida`, `RenderizadorJuego` y `ElementosNivel`, permitiendo reutilizar la lógica de la simulación sin depender del renderizado del cliente.

## [1.4.5] - 2026-09-22

### Corregido
- Liberación de recursos compartidos desde los menús.
- `Recursos` queda liberado desde la aplicación principal para evitar liberar texturas o `Skin` mientras todavía están en uso.

## [1.4.0] - 2026-09-04

### Agregado
- Sistema de audio mediante `Utilidades/Audio`, con música de fondo y volumen configurable.

## [1.2.0] - 2026-08-24

### Agregado
- Zonas de peligro identificadas mediante objetos `peligro` en el mapa.
- Mecánica de corte de obstáculos con la katana de Cappuccino Assassino.
- Mecánica cooperativa de palancas y puerta.
- Condición de victoria al interactuar con la puerta abierta.
- Movimiento de Cappuccino Assassino con desplazamiento horizontal, salto, gravedad y colisiones.
- Posiciones iniciales de los personajes definidas mediante objetos del mapa de Tiled.
- Derrota por caída al vacío o contacto con zonas peligrosas.
- Nivel 0 diseñado en Tiled Map Editor, con capas de objetos y colisiones.
- Paquete `mapa` con `Nivel` y `GestorColisiones`, cuyas reglas de colisión también se utilizan en la simulación del servidor.

### Cambiado
- El reinicio de la partida restablece también los elementos interactivos, como la puerta y las palancas.
- Se centralizaron las dimensiones del mundo y otros parámetros en `Constantes`.

### Corregido
- Se restablecen los estados de entrada al cambiar de pantalla o reiniciar, evitando que queden acciones activas.

## [1.1.0] - 2026-08-23

### Agregado
- Clase `BallerinaCapuccina` como base del segundo personaje.
- Paquete `Utilidades` con constantes, fábrica de viewports y gestor de recursos.

### Cambiado
- Se generalizó el uso de `Game` en las pantallas para facilitar la separación entre la aplicación de juego y la aplicación del servidor.

## [1.0.1] - 2026-08-04

### Agregado
- Estructura inicial de pantallas y navegación del videojuego.

## [1.0.0] - 2026-07-16

### Agregado
- Creación inicial del proyecto con LibGDX.
- Configuración del módulo Core y de la plataforma de escritorio LWJGL3.
- Incorporación de Gradle, `.gitignore`, repositorio GitHub y Wiki del proyecto.
