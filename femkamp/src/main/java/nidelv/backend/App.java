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
        AtomicBoolean cancelFlag = new AtomicBoolean(true);
        final Thread[] workerRef = new Thread[1];

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.redirectSystemStreams();

            frame.getSettingsPanel().onStart(() -> {
                frame.getConsolePanel().clear();
                frame.showConsole();

                if (workerRef[0] != null && workerRef[0].isAlive()) return;

                cancelFlag.set(false);
                Thread worker = new Thread(() -> {
                    while (!cancelFlag.get()) {
                        run(cancelFlag);
                    }
                }, "Femkamp-Worker");

                worker.setDaemon(true);
                worker.start();
                workerRef[0] = worker;
            });

            frame.getConsolePanel().onBack(() -> {  
                // 1) Gi bruker umiddelbar feedback/visuell respons
                System.out.println("[Console] Tilbake trykket, stopper kjøring …");
                frame.showSettings();                       // ← vis Settings med en gang
                frame.getSettingsPanel().setInputsEnabled(true);

                // 2) Stopp bakgrunnsløkken
                cancelFlag.set(true);
                Thread w = workerRef[0];
                if (w != null) w.interrupt();              // ← prøv å vekke blokkert jobbing
            });

            frame.setVisible(true);
            frame.showSettings();
        });
    }
}
