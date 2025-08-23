package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;

import nidelv.frontend.ConsoleWindow;
import nidelv.frontend.SettingsWindow;

public class App {

    private static void run() {
        try {
            ProgrammRunner programmRunner = new ProgrammRunner();
            programmRunner.runProgram();
        } catch (Exception e) {
            e.printStackTrace(); // vises i ConsoleWindow (stderr er redirectet)
            ProgrammRunner.takeBreak(10);
        }
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        CountDownLatch readyLatch = new CountDownLatch(1);

        // 1) Vis GUI for innstillinger (bruker trykker "Lagre/Start")
        SwingUtilities.invokeLater(() -> new SettingsWindow(readyLatch));

        try {
            // 2) Vent til bruker bekrefter
            readyLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        // 3) Åpne "terminal"-vindu og redirect System.out/err dit
        ConsoleWindow.openAndRedirectSystemStreams();

        // 4) Start programløkka
        while (true) run();
    }
}
