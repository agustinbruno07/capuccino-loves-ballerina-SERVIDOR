package com.capuccinolovesballerina.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.capuccinolovesballerina.game.CapuccinoServidorGame;

public final class Lwjgl3ServidorLauncher {

    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;

        Lwjgl3ApplicationConfiguration configuracion =
            new Lwjgl3ApplicationConfiguration();

        configuracion.setTitle("Cappuccino Loves Ballerina - Servidor");
        configuracion.useVsync(true);
        configuracion.setWindowedMode(1366, 768);

        new Lwjgl3Application(
            new CapuccinoServidorGame(),
            configuracion
        );
    }
}
