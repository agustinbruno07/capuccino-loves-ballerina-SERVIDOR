
package com.capuccinolovesballerina.game.Utilidades;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.capuccinolovesballerina.game.entidades.BallerinaCapuccina;
import com.capuccinolovesballerina.game.entidades.CapuccinoAssassino;
import com.capuccinolovesballerina.game.entidades.Player;
import com.capuccinolovesballerina.game.graficos.Estado;
import com.capuccinolovesballerina.game.mapa.Nivel;
import com.capuccinolovesballerina.game.objetos.BloqueEspuma;
import com.capuccinolovesballerina.game.objetos.ElementosNivel;
import com.capuccinolovesballerina.game.objetos.ObjetoCortable;
import com.capuccinolovesballerina.game.objetos.Palanca;
import com.capuccinolovesballerina.game.objetos.Puerta;

public class ControladorPartida {

    private static final float ALCANCE_PALANCA = 12f;
    private static final float ALCANCE_CORTE = 40f;

    private final Nivel nivel;
    private final ElementosNivel elementos;
    private final ControladorEntradaJugador entrada;

    private final CapuccinoAssassino cappuccino;
    private final BallerinaCapuccina ballerina;

    private boolean derrota;
    private boolean victoria;

    private final Estado[] estados = {
        Estado.IDLE,
        Estado.IDLE
    };

    public ControladorPartida(
        Nivel nivel,
        ElementosNivel elementos,
        ControladorEntradaJugador entrada,
        CapuccinoAssassino cappuccino,
        BallerinaCapuccina ballerina
    ) {
        this.nivel = nivel;
        this.elementos = elementos;
        this.entrada = entrada;
        this.cappuccino = cappuccino;
        this.ballerina = ballerina;
    }

    public boolean estaDerrotada() {
        return derrota;
    }

    public boolean hayVictoria() {
        return victoria;
    }

    public Estado getEstado(int rol) {
        return estados[rol];
    }

    public void actualizar(float delta) {

        if (derrota || victoria) return;

        ballerina.actualizarHabilidad(delta);

        if (entrada.isHabilidadPresionadaBallerina()) {
            ballerina.crearEspuma();
        }

        Array<Rectangle> solidos =
            new Array<>(nivel.getColisiones().getSolidos());

        for (ObjetoCortable cortable : elementos.getCortables()) {
            if (cortable.bloquea()) {
                solidos.add(cortable.getZona());
            }
        }

        BloqueEspuma espuma = ballerina.getEspuma();

        if (espuma != null) {
            solidos.add(espuma.getZona());
        }

        cappuccino.actualizar(
            delta,
            entrada.isIzquierda(),
            entrada.isDerecha(),
            entrada.isSaltoPresionado(),
            solidos
        );

        ballerina.actualizar(
            delta,
            entrada.isIzquierdaBallerina(),
            entrada.isDerechaBallerina(),
            entrada.isSaltoPresionadoBallerina(),
            solidos
        );

        actualizarEstados();

        if (entrada.isHabilidadPresionada()) {
            cortar();
        }

        if (estaMuerto(cappuccino) || estaMuerto(ballerina)) {
            derrota = true;
            entrada.soltarTodo();
            entrada.limpiarEventos();
            return;
        }

        comprobarVictoria();

        if (victoria) {
            return;
        }

        actualizarPalancas();
        entrada.limpiarEventos();
    }

    private void actualizarEstados() {

        if (entrada.isHabilidadPresionada()) {
            estados[0] = Estado.ABILITY;
        } else if (!cappuccino.isEnSuelo()) {
            estados[0] = Estado.JUMP;
        } else if (entrada.isIzquierda() || entrada.isDerecha()) {
            estados[0] = Estado.WALK;
        } else {
            estados[0] = Estado.IDLE;
        }

        if (entrada.isHabilidadPresionadaBallerina()) {
            estados[1] = Estado.ABILITY;
        } else if (!ballerina.isEnSuelo()) {
            estados[1] = Estado.JUMP;
        } else if (entrada.isIzquierdaBallerina()
            || entrada.isDerechaBallerina()) {
            estados[1] = Estado.WALK;
        } else {
            estados[1] = Estado.IDLE;
        }
    }

    private void actualizarPalancas() {

        Palanca palCap = elementos.getPalancaCappuccino();
        Palanca palBal = elementos.getPalancaBallerina();

        if (entrada.isInteractuarPresionado()
            && estaCerca(cappuccino, palCap)) {
            intentarAlternar(palCap);
        }

        if (entrada.isInteractuarPresionadoBallerina()
            && estaCerca(ballerina, palBal)) {
            intentarAlternar(palBal);
        }
    }

    private boolean estaCerca(Player personaje, Palanca palanca) {

        if (palanca == null) return false;

        Rectangle caja = personaje.getCaja();

        Rectangle alcance = new Rectangle(
            caja.x - ALCANCE_PALANCA,
            caja.y - ALCANCE_PALANCA,
            caja.width + 2 * ALCANCE_PALANCA,
            caja.height + 2 * ALCANCE_PALANCA
        );

        return alcance.overlaps(palanca.getZona());
    }

    private void intentarAlternar(Palanca palanca) {

        Puerta puerta = elementos.getPuerta();

        boolean seCerraria =
            palanca.estaActivada()
                && puerta != null
                && puerta.estaAbierta();

        if (seCerraria && alguienEnLaPuerta()) return;

        palanca.alternar();
        actualizarPuerta();
    }

    private void actualizarPuerta() {

        Puerta puerta = elementos.getPuerta();

        Palanca palCap = elementos.getPalancaCappuccino();
        Palanca palBal = elementos.getPalancaBallerina();

        if (puerta == null || (palCap == null && palBal == null)) {
            return;
        }

        boolean capOk = palCap == null || palCap.estaActivada();
        boolean balOk = palBal == null || palBal.estaActivada();

        puerta.setAbierta(capOk && balOk);
    }

    private boolean alguienEnLaPuerta() {

        Puerta puerta = elementos.getPuerta();

        return puerta != null
            && (
            cappuccino.getCaja().overlaps(puerta.getZona())
                || ballerina.getCaja().overlaps(puerta.getZona())
        );
    }

    private void comprobarVictoria() {

        Puerta puerta = elementos.getPuerta();

        if (puerta == null || !puerta.estaAbierta()) return;

        Rectangle zona = puerta.getZona();

        if (cappuccino.getCaja().overlaps(zona)
            && ballerina.getCaja().overlaps(zona)) {
            entrada.soltarTodo();
            victoria = true;
        }
    }

    private boolean estaMuerto(Player personaje) {

        if (personaje.getY() < -personaje.getAlto()) {
            return true;
        }

        for (Rectangle peligro : elementos.getHitboxPeligros()) {
            if (personaje.getCaja().overlaps(peligro)) {
                return true;
            }
        }

        return false;
    }

    private void cortar() {

        Rectangle caja = cappuccino.getCaja();
        Rectangle zonaCorte;

        if (cappuccino.isMirandoDerecha()) {
            zonaCorte = new Rectangle(
                caja.x + caja.width,
                caja.y,
                ALCANCE_CORTE,
                caja.height
            );
        } else {
            zonaCorte = new Rectangle(
                caja.x - ALCANCE_CORTE,
                caja.y,
                ALCANCE_CORTE,
                caja.height
            );
        }

        for (ObjetoCortable cortable : elementos.getCortables()) {

            if (!cortable.estaCortado()
                && zonaCorte.overlaps(cortable.getZona())) {
                cortable.setCortado(true);
            }
        }
    }

    public void reiniciar() {

        derrota = false;
        victoria = false;

        estados[0] = Estado.IDLE;
        estados[1] = Estado.IDLE;

        cappuccino.reaparecer(
            elementos.getSpawnX(),
            elementos.getSpawnY()
        );

        ballerina.reaparecer(
            elementos.getSpawnBallerinaX(),
            elementos.getSpawnBallerinaY()
        );

        ballerina.reiniciarHabilidad();

        Puerta puerta = elementos.getPuerta();

        if (puerta != null) {
            puerta.setAbierta(false);
        }

        Palanca palCap = elementos.getPalancaCappuccino();
        Palanca palBal = elementos.getPalancaBallerina();

        if (palCap != null && palCap.estaActivada()) {
            palCap.alternar();
        }

        if (palBal != null && palBal.estaActivada()) {
            palBal.alternar();
        }

        for (ObjetoCortable cortable : elementos.getCortables()) {
            cortable.setCortado(false);
        }

        entrada.soltarTodo();
        entrada.limpiarEventos();
    }
}
