package com.capuccinolovesballerina.game.red;

import java.net.BindException;

public final class ServicioServidor implements AutoCloseable {
    private volatile Servidor servidor;
    private volatile boolean cerrado, iniciado, terminado;
    private volatile String mensaje = "Iniciando servidor...";
    private final Thread hilo;

    public ServicioServidor() {
        hilo = new Thread(this::ejecutar, "Servicio-servidor");
        hilo.setDaemon(true);
        hilo.start();
    }
    private void ejecutar() {
        try {
            if (cerrado) return;
            Servidor nuevo = new Servidor();
            synchronized (this) {
                servidor = nuevo;
                if (cerrado) nuevo.close();
                else iniciado = true;
            }
            nuevo.ejecutar();
        } catch (BindException error) {
            mensaje = "Ya hay un servidor u otra aplicacion abierta. Cerra la otra instancia e intenta de nuevo.";
        } catch (Exception error) {
            mensaje = "No se pudo iniciar el servidor. Intenta de nuevo.";
            error.printStackTrace();
        } finally {
            terminado = true;
        }
    }
    public Servidor getServidor() { return servidor; }
    public boolean estaIniciado() { return iniciado; }
    public boolean termino() { return terminado; }
    public String getMensaje() { return mensaje; }
    @Override public synchronized void close() {
        cerrado = true;
        if (servidor != null) servidor.close();
        hilo.interrupt();
    }
}
