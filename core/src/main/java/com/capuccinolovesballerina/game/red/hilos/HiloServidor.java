package com.capuccinolovesballerina.game.red.hilos;

import com.capuccinolovesballerina.game.red.ProtocolosRed;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;

public class HiloServidor extends Thread {

    public record Recibido(InetSocketAddress origen, String mensaje) {}
    private final DatagramSocket socket;
    private final ArrayBlockingQueue<Recibido> bandeja = new ArrayBlockingQueue<>(512);
    private volatile boolean fin;

    public HiloServidor() throws SocketException {
        super("UDP-servidor");
        socket = new DatagramSocket(ProtocolosRed.PUERTO);
    }
    @Override public void run() {
        while (!fin) {
            byte[] buffer = new byte[ProtocolosRed.MAX_PAQUETE + 1];

            DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(paquete);

                if (paquete.getLength() > ProtocolosRed.MAX_PAQUETE) {
                    continue;
                }

                bandeja.offer(new Recibido((InetSocketAddress) paquete.getSocketAddress(),
                    new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8)));

            } catch (IOException error) {

                if (!fin) System.err.println(" Error de recepcion: " + error.getMessage());

            }
        }
    }
    public Recibido siguiente() {
        return bandeja.poll();
    }
    public void enviar(InetSocketAddress destino, String mensaje) {

        byte[] datos = ProtocolosRed.bytes(mensaje);
        if (datos.length > ProtocolosRed.MAX_PAQUETE) throw new IllegalStateException("Estado demasiado grande para un datagrama");
        try {
            socket.send(new DatagramPacket(datos, datos.length, destino));
        }
        catch (IOException error) {
            if (!fin) System.err.println(" Error de envio: " + error.getMessage());
        }

    }
    public void detener() { fin = true; socket.close(); interrupt(); }
}
