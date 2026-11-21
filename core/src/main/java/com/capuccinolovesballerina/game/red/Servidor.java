
package com.capuccinolovesballerina.game.red;

import com.capuccinolovesballerina.game.Utilidades.*;
import com.capuccinolovesballerina.game.entidades.*;
import com.capuccinolovesballerina.game.mapa.Nivel;
import com.capuccinolovesballerina.game.objetos.*;
import com.capuccinolovesballerina.game.red.hilos.HiloServidor;

import java.net.*;
import java.util.concurrent.locks.LockSupport;

import static com.capuccinolovesballerina.game.red.ProtocolosRed.*;

public class Servidor implements AutoCloseable {

    private final HiloServidor red;
    private final Cliente[] clientes = new Cliente[2];

    private final Nivel nivel;
    private final ElementosNivel elementos;

    private final ControladorEntradaJugador entrada = new ControladorEntradaJugador();

    private final CapuccinoAssassino cappuccino;
    private final BallerinaCapuccina ballerina;

    private final ControladorPartida partida;

    public record Resumen(boolean cappuccino, boolean ballerina, String fase) {}

    private volatile Resumen resumen = new Resumen(false, false, ESPERA);

    private volatile boolean cerrado;

    private String fase = ESPERA;

    private long finCuenta;
    private long ronda;
    private long secuencia;
    private long espumaId;

    private BloqueEspuma ultimaEspuma;

    private float tiempo;

    public Servidor() throws SocketException {

        nivel = new Nivel(MAPA, true);
        elementos = new ElementosNivel(nivel);

        cappuccino = new CapuccinoAssassino(
            elementos.getSpawnX(),
            elementos.getSpawnY()
        );

        ballerina = new BallerinaCapuccina(
            elementos.getSpawnBallerinaX(),
            elementos.getSpawnBallerinaY()
        );

        partida = new ControladorPartida(
            nivel,
            elementos,
            entrada,
            cappuccino,
            ballerina
        );

        try {
            red = new HiloServidor();
        } catch (SocketException error) {
            nivel.dispose();
            throw error;
        }
    }

    public static void main(String[] args) throws Exception {

        try {
            Servidor servidor = new Servidor();

            Runtime.getRuntime().addShutdownHook(
                new Thread(servidor::close)
            );

            try (servidor) {
                servidor.ejecutar();
            }

        } catch (BindException error) {
            System.err.println(
                "[RED] No se pudo abrir UDP " + PUERTO +
                    ": ya hay otro servidor o prueba usando el puerto."
            );

            System.exit(1);
        }
    }

    public void ejecutar() {

        try {
            if (cerrado) return;

            red.start();

            System.out.println(
                "[RED] Servidor UDP en 0.0.0.0:" + PUERTO +
                    ". Esperando dos jugadores."
            );

            mostrarDirecciones();

            long siguienteTick = System.nanoTime();
            long ultimoEstado = 0;

            int ultimaCuenta = -1;

            while (!cerrado) {

                HiloServidor.Recibido recibido;

                int leidos = 0;

                while (leidos++ < 512 &&
                    (recibido = red.siguiente()) != null) {

                    procesar(recibido);
                }

                long ahora = System.nanoTime();

                for (Cliente cliente : clientes) {

                    if (cliente != null && cliente.estaDesconectado(ahora)) {
                        desconectar("Un jugador perdio la conexion");
                        break;
                    }
                }

                if (fase.equals(CUENTA)) {

                    int cuenta = cuenta(ahora);

                    if (cuenta != ultimaCuenta) {
                        System.out.println("[RED] Inicio en " + cuenta);
                        ultimaCuenta = cuenta;
                    }

                    if (ahora >= finCuenta) {
                        fase = JUGANDO;
                        System.out.println("[RED] INICIO de ronda " + ronda);
                    }
                }

                if (ahora >= siguienteTick) {

                    if (fase.equals(JUGANDO)) {

                        for (int rol = 0; rol < 2; rol++) {

                            Cliente cliente = clientes[rol];

                            if (cliente != null &&
                                ahora - cliente.getUltimoControl() > 350_000_000L) {

                                entrada.aplicarControlesRed(
                                    rol, false, false, false, false, false
                                );
                            }
                        }

                        partida.actualizar(1f / 60f);
                        tiempo += 1f / 60f;

                        if (partida.estaDerrotada()) {
                            fase = DERROTA;
                            System.out.println("[PARTIDA] Derrota");
                        }

                        if (partida.hayVictoria()) {
                            fase = VICTORIA;
                            System.out.println("[PARTIDA] Victoria cooperativa");
                        }
                    }

                    siguienteTick += 1_000_000_000L / 60;

                    if (ahora - siguienteTick > 250_000_000L) {
                        siguienteTick = ahora;
                    }
                }

                if (ahora - ultimoEstado >= 50_000_000L) {
                    enviarEstado(ahora);
                    ultimoEstado = ahora;
                }

                resumen = new Resumen(
                    clientes[0] != null,
                    clientes[1] != null,
                    fase
                );

                LockSupport.parkNanos(1_000_000L);
            }

        } finally {
            red.detener();
            nivel.dispose();
        }
    }

    private void procesar(HiloServidor.Recibido recibido) {

        String[] p = partes(recibido.mensaje());
        InetSocketAddress origen = recibido.origen();

        long ahora = System.nanoTime();

        try {

            if (p.length == 1 && p[0].equals(CONEXION)) {

                red.enviar(origen, OK);
                return;
            }

            if (p.length == 1 && p[0].equals(REGISTRO)) {
                conectar(origen);
                return;
            }

            int rol = buscar(origen);

            if (rol < 0 || p.length < 2 ||
                !clientes[rol].tieneSesion(p[1])) {
                return;
            }

            Cliente cliente = clientes[rol];

            if (p[0].equals(LISTO) && p.length == 2) {

                cliente.marcarListo();

                if (fase.equals(ESPERA) &&
                    clientes[0] != null &&
                    clientes[1] != null &&
                    clientes[0].estaListo() &&
                    clientes[1].estaListo()) {

                    comenzarCuenta();
                }

            } else if (p[0].equals(CONTROLES)) {

                Controles nuevos = Controles.leer(p);

                long rondaRecibida = natural(p[2]);
                long numero = natural(p[3]);

                Controles anteriores = cliente.getControles();

                if (!cliente.actualizarControles(
                    nuevos,
                    rondaRecibida,
                    numero,
                    ronda,
                    ahora
                )) {
                    return;
                }

                if (fase.equals(JUGANDO)) {

                    entrada.aplicarControlesRed(
                        rol,
                        nuevos.izquierda(),
                        nuevos.derecha(),
                        nuevos.salto() > anteriores.salto(),
                        nuevos.habilidad() > anteriores.habilidad(),
                        nuevos.interactuar() > anteriores.interactuar()
                    );
                }

            } else if (p[0].equals(REINICIO) && p.length == 4) {

                long rondaRecibida = natural(p[2]);
                long numero = natural(p[3]);

                if (cliente.aceptarReinicio(rondaRecibida, numero, ronda)) {

                    if (fase.equals(DERROTA) || fase.equals(VICTORIA)) {
                        comenzarCuenta();
                    }
                }

            } else if (p[0].equals(SALIR) && p.length == 2) {

                desconectar("El otro jugador salio de la partida");
                return;

            } else if (p[0].equals(PING) && p.length == 2) {

                red.enviar(origen, PONG + "|" + cliente.getSesion());

            } else {
                return;
            }

            cliente.registrarMensaje(ahora);

        } catch (IllegalArgumentException error) {
        }
    }

    private int buscar(InetSocketAddress origen) {

        for (int i = 0; i < 2; i++) {

            if (clientes[i] != null &&
                clientes[i].esDireccion(origen)) {
                return i;
            }
        }

        return -1;
    }

    private void conectar(InetSocketAddress origen) {

        int rol = buscar(origen);

        if (rol < 0) {

            rol = clientes[0] == null ? 0 :
                clientes[1] == null ? 1 : -1;

            if (rol < 0) {
                red.enviar(origen, LLENO);
                return;
            }

            clientes[rol] = new Cliente(origen);

            System.out.println(
                "[RED] " + origen + " registrado como " +
                    (rol == 0 ? "Cappuccino" : "Ballerina")
            );
        }

        Cliente cliente = clientes[rol];

        cliente.registrarMensaje(System.nanoTime());

        red.enviar(
            origen,
            ASIGNADO + "|" + cliente.getSesion() + "|" + rol
        );
    }

    private void comenzarCuenta() {

        partida.reiniciar();

        tiempo = 0;
        ronda++;

        fase = CUENTA;

        finCuenta = System.nanoTime() + 5_000_000_000L;

        System.out.println(
            "[RED] Dos jugadores listos. Cuenta regresiva de cinco segundos, ronda " + ronda
        );
    }

    private int cuenta(long ahora) {

        if (!fase.equals(CUENTA)) return 0;

        return Math.max(
            0,
            (int) Math.ceil((finCuenta - ahora) / 1e9)
        );
    }

    private void enviarEstado(long ahora) {

        BloqueEspuma espuma = ballerina.getEspuma();

        if (espuma != null && espuma != ultimaEspuma) {
            espumaId++;
        }

        ultimaEspuma = espuma;

        StringBuilder cortados = new StringBuilder();

        for (ObjetoCortable objeto : elementos.getCortables()) {
            cortados.append(bit(objeto.estaCortado()));
        }

        EstadoPartida estado = new EstadoPartida(
            ++secuencia,
            ronda,
            fase,
            cuenta(ahora),
            tiempo,
            jugador(cappuccino, 0),
            jugador(ballerina, 1),
            activada(elementos.getPalancaCappuccino()),
            activada(elementos.getPalancaBallerina()),
            elementos.getPuerta() != null &&
                elementos.getPuerta().estaAbierta(),
            cortados.toString(),
            espuma == null ? 0 : espumaId,
            espuma == null ? 0 : espuma.getZona().x,
            espuma == null ? 0 : espuma.getZona().y,
            espuma == null ? 0 : espuma.getVidaRestante()
        );

        for (Cliente cliente : clientes) {

            if (cliente != null) {
                red.enviar(
                    cliente.getDestino(),
                    estado.mensaje(cliente.getSesion())
                );
            }
        }
    }

    private Jugador jugador(Player jugador, int rol) {

        Cliente cliente = clientes[rol];

        return new Jugador(
            jugador.getX(),
            jugador.getY(),
            jugador.isMirandoDerecha(),
            jugador.isEnSuelo(),
            partida.getEstado(rol),
            cliente == null ? 0 : cliente.getControles().habilidad()
        );
    }

    private boolean activada(Palanca palanca) {
        return palanca != null && palanca.estaActivada();
    }

    private void mostrarDirecciones() {

        try {
            var interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {

                NetworkInterface interfaz = interfaces.nextElement();

                if (!interfaz.isUp() || interfaz.isLoopback()) {
                    continue;
                }

                for (InterfaceAddress direccion : interfaz.getInterfaceAddresses()) {

                    if (direccion.getAddress() instanceof Inet4Address) {

                        System.out.println(
                            "[RED] IP manual en " +
                                interfaz.getDisplayName() + ": " +
                                direccion.getAddress().getHostAddress()
                        );
                    }
                }
            }

        } catch (SocketException error) {
            System.err.println(
                "[RED] No se pudieron listar interfaces: " + error.getMessage()
            );
        }
    }

    private void desconectar(String motivo) {

        System.out.println("[RED] " + motivo + ". Se libera la sala.");

        for (Cliente cliente : clientes) {

            if (cliente != null) {

                for (int i = 0; i < 3; i++) {

                    red.enviar(
                        cliente.getDestino(),
                        DESCONECTADO + "|" + cliente.getSesion() + "|" + motivo
                    );
                }
            }
        }

        clientes[0] = null;
        clientes[1] = null;

        fase = ESPERA;

        partida.reiniciar();

        tiempo = 0;
    }

    public Resumen getResumen() {
        return resumen;
    }

    @Override
    public void close() {
        cerrado = true;
        red.detener();
    }
}
