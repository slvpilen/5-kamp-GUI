package nidelv.backend;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import nidelv.frontend.MainFrame;

public class App {

    /** Kjør én iterasjon av programmet. Logger feil i Console og tar kort pause ved krasj. */
    private static void run(AtomicBoolean cancelFlag) {
        try {
            ProgrammRunner pr = new ProgrammRunner();
            pr.runProgram(cancelFlag);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            ProgrammRunner.takeBreak(10);
        }
    }

    public static void main(String[] args) {
        AtomicBoolean cancelFlag = new AtomicBoolean(true); // true = stoppet; false = kjører
        final Thread[] workerRef = new Thread[1];

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.redirectSystemStreams();

            Runnable startWorker = () -> {
                // Start ikke hvis allerede kjører
                if (workerRef[0] != null && workerRef[0].isAlive()) return;

                cancelFlag.set(false); // aktiver løkken
                Thread worker = new Thread(() -> {
                    while (!cancelFlag.get()) {
                        run(cancelFlag);
                    }
                }, "Femkamp-Worker");
                worker.setDaemon(true);
                worker.start();
                workerRef[0] = worker;
            };

            // Start fra Settings
            frame.getSettingsPanel().onStart(() -> {
                frame.getConsolePanel().clear();
                frame.showConsole();
                startWorker.run();
            });

            // Tilbake fra Console -> stopp kjøring og vis Settings
            frame.getConsolePanel().onBack(() -> {
                System.out.println("[Console] Tilbake trykket, stopper kjøring …");
                frame.showSettings();
                frame.getSettingsPanel().setInputsEnabled(true);

                cancelFlag.set(true);
                Thread w = workerRef[0];
                if (w != null) w.interrupt();
            });

            // NY: Restart-knapp fra Console
            frame.getConsolePanel().onRestart(() -> {
                System.out.println("[Console] Restart trykket, stopper og starter på nytt …");

                // 1) Stopp nåværende kjøring
                cancelFlag.set(true);
                Thread w = workerRef[0];
                if (w != null) {
                    w.interrupt();
                    try { w.join(2000); } catch (InterruptedException ignored) {}
                }

                // 2) Tøm console og vis den (hvis ikke allerede)
                frame.getConsolePanel().clear();
                frame.showConsole();

                // 3) Start ny kjøring (lager ny ProgrammRunner via run())
                startWorker.run();
            });

            frame.setVisible(true);
            frame.showSettings();
        });
    }
}
