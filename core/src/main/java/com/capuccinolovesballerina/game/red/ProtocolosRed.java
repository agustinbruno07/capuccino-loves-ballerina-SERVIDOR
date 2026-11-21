
package com.capuccinolovesballerina.game.red;

import com.capuccinolovesballerina.game.graficos.Estado;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public final class ProtocolosRed {

    private ProtocolosRed() {}

    public static final int PUERTO = 9992;
    public static final int MAX_PAQUETE = 1400;

    public static final long TIMEOUT_MS = 5000;
    public static final long BUSQUEDA_MS = 10000;

    public static final String CONEXION = "CONEXION";
    public static final String OK = "OK";
    public static final String ASIGNADO = "ASIGNADO";
    public static final String REGISTRO = "REGISTRO";
    public static final String LISTO = "LISTO";
    public static final String PONG = "PONG";
    public static final String LLENO = "LLENO";
    public static final String CONTROLES = "CONTROLES";
    public static final String ESTADO = "ESTADO";
    public static final String PING = "PING";
    public static final String SALIR = "SALIR";
    public static final String DESCONECTADO = "DESCONECTADO";
    public static final String REINICIO = "REINICIO";

    public static final String ESPERA = "ESPERA";
    public static final String CUENTA = "CUENTA";
    public static final String JUGANDO = "JUGANDO";
    public static final String DERROTA = "DERROTA";
    public static final String VICTORIA = "VICTORIA";

    public static final String MAPA = "mapas/NIVEL 0.tmx";

    public static byte[] bytes(String mensaje) {
        return mensaje.getBytes(StandardCharsets.UTF_8);
    }

    public static String[] partes(String mensaje) {
        return mensaje.split("\\|", -1);
    }

    public static int bit(boolean valor) {
        return valor ? 1 : 0;
    }

    public static boolean booleano(String valor) {
        if (!valor.equals("0") && !valor.equals("1")) {
            throw new IllegalArgumentException("Booleano invalido");
        }

        return valor.equals("1");
    }

    public static long natural(String valor) {
        long numero = Long.parseLong(valor);

        if (numero < 0) {
            throw new IllegalArgumentException("Numero negativo");
        }

        return numero;
    }

    public static float numero(String valor) {
        float numero = Float.parseFloat(valor);

        if (!Float.isFinite(numero) || Math.abs(numero) > 1_000_000f) {
            throw new IllegalArgumentException("Numero invalido");
        }

        return numero;
    }

    public record Controles(
        boolean izquierda,
        boolean derecha,
        long salto,
        long habilidad,
        long interactuar
    ) {

        public static final Controles VACIOS =
            new Controles(false, false, 0, 0, 0);

        public String mensaje(String sesion, long ronda, long secuencia) {

            return String.join("|",
                CONTROLES,
                sesion,
                String.valueOf(ronda),
                String.valueOf(secuencia),
                String.valueOf(bit(izquierda)),
                String.valueOf(bit(derecha)),
                String.valueOf(salto),
                String.valueOf(habilidad),
                String.valueOf(interactuar)
            );
        }

        public static Controles leer(String[] partes) {

            if (partes.length != 9) {
                throw new IllegalArgumentException("Controles incompletos");
            }

            return new Controles(
                booleano(partes[4]),
                booleano(partes[5]),
                natural(partes[6]),
                natural(partes[7]),
                natural(partes[8])
            );
        }
    }

    public record Jugador(
        float x,
        float y,
        boolean mirando,
        boolean suelo,
        Estado animacion,
        long habilidad
    ) {

        public String texto() {

            return String.join(",",
                String.valueOf(x),
                String.valueOf(y),
                String.valueOf(bit(mirando)),
                String.valueOf(bit(suelo)),
                String.valueOf(animacion),
                String.valueOf(habilidad)
            );
        }

        public static Jugador leer(String texto) {

            String[] partes = texto.split(",", -1);

            if (partes.length != 6) {
                throw new IllegalArgumentException("Jugador incompleto");
            }

            return new Jugador(
                numero(partes[0]),
                numero(partes[1]),
                booleano(partes[2]),
                booleano(partes[3]),
                Estado.valueOf(partes[4]),
                natural(partes[5])
            );
        }
    }

    public record EstadoPartida(
        long secuencia,
        long ronda,
        String fase,
        int cuenta,
        float tiempo,
        Jugador cappuccino,
        Jugador ballerina,
        boolean palancaCap,
        boolean palancaBal,
        boolean puerta,
        String cortados,
        long espumaId,
        float espumaX,
        float espumaY,
        float espumaVida
    ) {

        public String mensaje(String sesion) {

            return String.join("|",
                ESTADO,
                sesion,
                String.valueOf(secuencia),
                String.valueOf(ronda),
                fase,
                String.valueOf(cuenta),
                String.valueOf(tiempo),
                cappuccino.texto(),
                ballerina.texto(),
                String.valueOf(bit(palancaCap)),
                String.valueOf(bit(palancaBal)),
                String.valueOf(bit(puerta)),
                cortados,
                String.valueOf(espumaId),
                String.valueOf(espumaX),
                String.valueOf(espumaY),
                String.valueOf(espumaVida)
            );
        }

        public static EstadoPartida leer(String[] partes) {

            if (partes.length != 17) {
                throw new IllegalArgumentException("Estado incompleto");
            }

            if (!faseValida(partes[4])) {
                throw new IllegalArgumentException("Fase invalida");
            }

            int cuenta = Integer.parseInt(partes[5]);

            float tiempo = numero(partes[6]);
            float vida = numero(partes[16]);

            if (cuenta < 0 || cuenta > 5 ||
                tiempo < 0 ||
                vida < 0 || vida > 4f ||
                !partes[12].matches("[01]*")) {

                throw new IllegalArgumentException("Estado invalido");
            }

            return new EstadoPartida(
                natural(partes[2]),
                natural(partes[3]),
                partes[4],
                cuenta,
                tiempo,
                Jugador.leer(partes[7]),
                Jugador.leer(partes[8]),
                booleano(partes[9]),
                booleano(partes[10]),
                booleano(partes[11]),
                partes[12],
                natural(partes[13]),
                numero(partes[14]),
                numero(partes[15]),
                vida
            );
        }
    }

    private static boolean faseValida(String fase) {

        return Set.of(
            ESPERA,
            CUENTA,
            JUGANDO,
            DERROTA,
            VICTORIA
        ).contains(fase);
    }
}
