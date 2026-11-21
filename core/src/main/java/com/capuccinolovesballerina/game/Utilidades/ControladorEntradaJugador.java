
package com.capuccinolovesballerina.game.Utilidades;

public class ControladorEntradaJugador {

    private boolean izquierda;
    private boolean derecha;
    private boolean saltoPresionado;
    private boolean habilidadPresionada;
    private boolean interactuarPresionado;

    private boolean izquierdaBallerina;
    private boolean derechaBallerina;
    private boolean saltoPresionadoBallerina;
    private boolean habilidadPresionadaBallerina;
    private boolean interactuarPresionadoBallerina;

    public boolean isIzquierda() {
        return izquierda;
    }

    public boolean isDerecha() {
        return derecha;
    }

    public boolean isSaltoPresionado() {
        return saltoPresionado;
    }

    public boolean isInteractuarPresionado() {
        return interactuarPresionado;
    }

    public boolean isHabilidadPresionada() {
        return habilidadPresionada;
    }

    public boolean isIzquierdaBallerina() {
        return izquierdaBallerina;
    }

    public boolean isDerechaBallerina() {
        return derechaBallerina;
    }

    public boolean isSaltoPresionadoBallerina() {
        return saltoPresionadoBallerina;
    }

    public boolean isHabilidadPresionadaBallerina() {
        return habilidadPresionadaBallerina;
    }

    public boolean isInteractuarPresionadoBallerina() {
        return interactuarPresionadoBallerina;
    }

    public void aplicarControlesRed(
        int rol,
        boolean izquierda,
        boolean derecha,
        boolean salto,
        boolean habilidad,
        boolean interactuar
    ) {
        if (rol == 0) {
            this.izquierda = izquierda;
            this.derecha = derecha;
            saltoPresionado |= salto;
            habilidadPresionada |= habilidad;
            interactuarPresionado |= interactuar;

        } else if (rol == 1) {
            izquierdaBallerina = izquierda;
            derechaBallerina = derecha;
            saltoPresionadoBallerina |= salto;
            habilidadPresionadaBallerina |= habilidad;
            interactuarPresionadoBallerina |= interactuar;
        }
    }

    public void limpiarEventos() {
        saltoPresionado = false;
        habilidadPresionada = false;
        interactuarPresionado = false;

        saltoPresionadoBallerina = false;
        habilidadPresionadaBallerina = false;
        interactuarPresionadoBallerina = false;
    }

    public void soltarTodo() {
        izquierda = false;
        derecha = false;
        saltoPresionado = false;
        habilidadPresionada = false;
        interactuarPresionado = false;

        izquierdaBallerina = false;
        derechaBallerina = false;
        saltoPresionadoBallerina = false;
        habilidadPresionadaBallerina = false;
        interactuarPresionadoBallerina = false;
    }
}
