
package com.capuccinolovesballerina.game.red;

import com.capuccinolovesballerina.game.red.ProtocolosRed.Controles;
import java.net.InetSocketAddress;
import java.util.UUID;

public class Cliente {

    private final InetSocketAddress destino;
    private final String sesion;

    private long ultimoMensaje = System.nanoTime();
    private long ultimoControl;
    private long ultimaSecuencia = -1;
    private long reinicio = -1;

    private Controles controles = Controles.VACIOS;
    private boolean listo;

    public Cliente(InetSocketAddress destino) {
        this.destino = destino;
        this.sesion = UUID.randomUUID().toString();
    }

    public InetSocketAddress getDestino() {
        return destino;
    }

    public String getSesion() {
        return sesion;
    }

    public long getUltimoMensaje() {
        return ultimoMensaje;
    }

    public long getUltimoControl() {
        return ultimoControl;
    }

    public Controles getControles() {
        return controles;
    }

    public boolean estaListo() {
        return listo;
    }

    public boolean esDireccion(InetSocketAddress direccion) {
        return destino.equals(direccion);
    }

    public boolean tieneSesion(String sesion) {
        return this.sesion.equals(sesion);
    }

    public boolean estaDesconectado(long ahora) {
        return ahora - ultimoMensaje > ProtocolosRed.TIMEOUT_MS * 1_000_000L;
    }

    public void registrarMensaje(long ahora) {
        ultimoMensaje = ahora;
    }

    public void marcarListo() {
        listo = true;
    }

    public boolean actualizarControles(
        Controles nuevos,
        long rondaRecibida,
        long secuenciaRecibida,
        long rondaActual,
        long ahora
    ) {
        if (secuenciaRecibida <= ultimaSecuencia || rondaRecibida != rondaActual) {
            return false;
        }

        if (nuevos.salto() < controles.salto()
            || nuevos.habilidad() < controles.habilidad()
            || nuevos.interactuar() < controles.interactuar()) {
            return false;
        }

        controles = nuevos;
        ultimaSecuencia = secuenciaRecibida;
        ultimoControl = ahora;

        return true;
    }

    public boolean aceptarReinicio(long rondaRecibida, long numero, long rondaActual) {
        if (rondaRecibida != rondaActual || numero <= reinicio) {
            return false;
        }

        reinicio = numero;
        return true;
    }
}
