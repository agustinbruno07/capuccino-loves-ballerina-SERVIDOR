# Cappuccino Loves Ballerina — Servidor

## Integrantes del grupo

- Guillermo Bordoli
- Agustín Bruno
- Santino Fierro

## Descripción del videojuego

Cappuccino Loves Ballerina es un videojuego cooperativo de plataformas y puzles en 2D para PC, inspirado en las mecánicas principales de Fireboy and Watergirl.

Cada jugador controla desde una computadora diferente a Cappuccino Assassino o Ballerina Capuccina. Los dos personajes deben colaborar para superar obstáculos, activar mecanismos, resolver puzles y escapar de una estructura misteriosa mediante el uso coordinado de sus habilidades especiales.

## ¿Qué hace este repositorio? — SERVIDOR

Este repositorio contiene el programa que crea, coordina y administra la partida. Se ejecuta en una PC que funciona como anfitriona.

- Abre el servicio de red y espera que se conecten los dos jugadores.
- Registra a cada jugador y le asigna un personaje y una sesión de conexión.
- Recibe los controles que envían los clientes y los aplica a la partida.
- Ejecuta las reglas del juego y mantiene el estado real de la partida: posiciones, colisiones, mecanismos, tiempo, habilidades y resultado.
- Envía periódicamente a los dos clientes la información actualizada para que ambos vean la misma partida.
- Controla el inicio de la ronda, la cuenta regresiva, los reinicios y las desconexiones.

**El servidor no es una tercera computadora de jugador:** no controla a Cappuccino ni a Ballerina. Su función es actuar como anfitrión y autoridad de la partida, asegurándose de que los dos clientes compartan el mismo juego.

## Tecnologías principales

- Java JDK 17
- LibGDX 1.14.1
- Gradle
- IntelliJ IDEA
- Git y GitHub
- Plataforma objetivo: escritorio mediante LWJGL3
- Comunicación entre computadoras: UDP

## Wiki del proyecto

La propuesta formal y detallada del videojuego se encuentra en la Wiki:

[Ver la propuesta del proyecto](https://github.com/agustinbruno07/capuccino-loves-ballerina/wiki)

## Cómo iniciar una partida online — versión 2.0

El servidor debe iniciarse antes que los clientes.

1. En la PC anfitriona, abrir **CappuccinoServidor.exe**.
2. Pulsar **JUGAR** para iniciar el servidor.
3. El servidor abre el puerto UDP **9992** y queda esperando a los dos jugadores.
4. En otras dos PC conectadas a la misma red local, los jugadores abren **CappuccinoLovesBallerina.exe** y pulsan **JUGAR**.
5. Cuando ambos clientes se conectan y confirman que están listos, el servidor inicia una cuenta regresiva de cinco segundos y comienza la partida.
6. Durante el juego, el servidor recibe los controles, procesa la simulación y envía el estado actualizado a ambos jugadores.

La PC servidor y las dos PC cliente deben estar conectadas a la misma red local. El firewall de la PC servidor debe permitir la comunicación UDP por el puerto **9992**. Esta configuración está pensada para jugar en una red local; jugar desde redes distintas puede requerir una configuración adicional de red.

La pantalla del servidor permite supervisar el funcionamiento y ver si los dos jugadores están conectados. Si un jugador se desconecta o deja de responder, el servidor informa la situación y actualiza el estado de la partida.

## Distribución para Windows

En la PC anfitriona se utiliza **CappuccinoServidor.exe**. Los jugadores utilizan la aplicación CLIENTE en sus propias computadoras.

Para generar el paquete del servidor desde la raíz del proyecto, ejecutar:

```bash
gradlew.bat :lwjgl3:zipServidorWindows
```

El archivo ZIP se genera en `lwjgl3/build/distributions`. Extraer el contenido completo del ZIP: incluye Java y los archivos necesarios para ejecutar el servidor.

El nombre y número de versión del ZIP se obtienen de la propiedad `projectVersion` configurada en `gradle.properties`.

## Compilación y ejecución (desarrollo)

### Requisitos

- Java JDK 17.
- Git.
- IntelliJ IDEA o cualquier entorno compatible con proyectos Gradle.

### Ejecutar en Windows

Desde la carpeta raíz del proyecto:

```bash
gradlew.bat :lwjgl3:runServidor
```

### Ejecutar en Linux o macOS

Desde la carpeta raíz del proyecto:

```bash
./gradlew :lwjgl3:runServidor
```

### Ejecutar desde IntelliJ IDEA

1. Abrir IntelliJ IDEA y seleccionar **Open**.
2. Elegir la carpeta raíz del proyecto SERVIDOR.
3. Esperar a que Gradle importe el proyecto y descargue las dependencias.
4. Abrir el módulo `lwjgl3`.
5. Buscar `Lwjgl3ServidorLauncher.java` y ejecutar su método `main`.

## Video de demostración (segunda pre-entrega)

[▶ Ver video de demostración](https://drive.google.com/file/d/1RYrjh4mWFUnnSkVwSDYIL4kscPV1n7NP/view?usp=drive_link)

## Estado actual del proyecto — versión 2.0

Servidor multijugador para la versión cooperativa del juego con:

- **Administración de dos jugadores:** acepta hasta dos clientes y asigna Cappuccino Assassino y Ballerina Capuccina.
- **Comunicación UDP:** recibe y envía mensajes de red utilizando el puerto 9992.
- **Registro de jugadores:** identifica cada conexión mediante una sesión para distinguir a los participantes.
- **Inicio coordinado:** espera a que los dos jugadores estén conectados y listos antes de iniciar la cuenta regresiva de cinco segundos.
- **Simulación centralizada:** procesa los controles y ejecuta las reglas del juego en un único lugar, manteniendo un estado común para los dos jugadores.
- **Actualización compartida:** envía periódicamente las posiciones, animaciones, tiempo, palancas, puerta, objetos y otros datos necesarios para representar la partida en cada cliente.
- **Control de conexión:** utiliza mensajes de comprobación para saber si los clientes siguen conectados y detecta desconexiones.
- **Control de datos recibidos:** verifica la sesión, la ronda y el orden de los controles antes de aplicarlos, evitando aceptar mensajes atrasados o repetidos.
- **Reinicio coordinado:** procesa las solicitudes de reinicio y vuelve a preparar la ronda para ambos jugadores.
- **Pantalla de supervisión:** permite observar el estado del servidor y la conexión de los jugadores.

En pocas palabras: **el servidor es el árbitro de la partida. Recibe lo que hacen los jugadores, aplica las reglas y les comunica a ambos el resultado actualizado.**
