package com.capuccinolovesballerina.game.Utilidades;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

public class ControladorEntradaJugador extends InputAdapter {

    private boolean izquierda;
    private boolean derecha;
    private boolean saltoW;
    private boolean saltoEspacio;
    private boolean saltoPresionado;
    private boolean escapePresionado;
    private boolean interactuarPresionado;
    private boolean habilidadPresionada;

    private boolean izquierdaBallerina;
    private boolean derechaBallerina;
    private boolean saltoPresionadoBallerina;
    private boolean habilidadPresionadaBallerina;
    private boolean interactuarPresionadoBallerina;

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            escapePresionado = true;
        }

        if (keycode == Input.Keys.A) {
            izquierda = true;
        }
        if (keycode == Input.Keys.D) {
            derecha = true;
        }
        if (keycode == Input.Keys.W) {
            if (!saltoW && !saltoEspacio) saltoPresionado = true;
            saltoW = true;
        }
        if (keycode == Input.Keys.SPACE) {
            if (!saltoW && !saltoEspacio) saltoPresionado = true;
            saltoEspacio = true;
        }

        if (keycode == Input.Keys.E) {
            interactuarPresionado = true;
        }
        if (keycode == Input.Keys.F) {
            habilidadPresionada = true;
        }

        if (keycode == Input.Keys.LEFT) {
            izquierdaBallerina = true;
        }
        if (keycode == Input.Keys.RIGHT) {
            derechaBallerina = true;
        }

        if (keycode == Input.Keys.UP) {
            saltoPresionadoBallerina = true;
        }

        if (keycode == Input.Keys.SHIFT_RIGHT) {
            habilidadPresionadaBallerina = true;
        }

        if (keycode == Input.Keys.ENTER) {
            interactuarPresionadoBallerina = true;
        }

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.A) {
            izquierda = false;
        }
        if (keycode == Input.Keys.D) {
            derecha = false;
        }

        if (keycode == Input.Keys.W) {
            saltoW = false;
        }
        if (keycode == Input.Keys.SPACE) {
            saltoEspacio = false;
        }

        if (keycode == Input.Keys.LEFT) {
            izquierdaBallerina = false;
        }
        if (keycode == Input.Keys.RIGHT) {
            derechaBallerina = false;
        }

        return true;
    }

    public boolean isIzquierda() { return izquierda; }
    public boolean isDerecha() { return derecha; }
    public boolean isSaltoPresionado() { return saltoPresionado; }
    public boolean isEscapePresionado() { return escapePresionado; }
    public boolean isInteractuarPresionado() { return interactuarPresionado; }
    public boolean isHabilidadPresionada() { return habilidadPresionada; }

    public boolean isIzquierdaBallerina() { return izquierdaBallerina; }
    public boolean isDerechaBallerina() { return derechaBallerina; }
    public boolean isSaltoPresionadoBallerina() { return saltoPresionadoBallerina; }
    public boolean isHabilidadPresionadaBallerina() { return habilidadPresionadaBallerina; }
    public boolean isInteractuarPresionadoBallerina() { return interactuarPresionadoBallerina; }

    public void limpiarEventos() {
        saltoPresionado = false;
        interactuarPresionado = false;
        habilidadPresionada = false;
        escapePresionado = false;

        saltoPresionadoBallerina = false;
        habilidadPresionadaBallerina = false;
        interactuarPresionadoBallerina = false;
    }

    public void aplicarControlesRed(int rol, boolean izq, boolean der, boolean salto, boolean habilidad, boolean interactuar) {
        if (rol == 0) {
            izquierda = izq;
            derecha = der;
            saltoPresionado |= salto;
            habilidadPresionada |= habilidad;
            interactuarPresionado |= interactuar;
        } else {
            izquierdaBallerina = izq;
            derechaBallerina = der;
            saltoPresionadoBallerina |= salto;
            habilidadPresionadaBallerina |= habilidad;
            interactuarPresionadoBallerina |= interactuar;
        }
    }

    public void soltarTodo() {
        izquierda = false;
        derecha = false;
        saltoPresionado = false;
        interactuarPresionado = false;
        habilidadPresionada = false;
        escapePresionado = false;
        saltoW = false;
        saltoEspacio = false;

        izquierdaBallerina = false;
        derechaBallerina = false;
        saltoPresionadoBallerina = false;
        habilidadPresionadaBallerina = false;
        interactuarPresionadoBallerina = false;
    }
}
