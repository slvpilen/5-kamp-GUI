package nidelv.frontend;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    public static final String CARD_SETTINGS = "settings";
    public static final String CARD_CONSOLE  = "console";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final ConsolePanel consolePanel = new ConsolePanel();

    public MainFrame() {
        super("Femkamp");
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationByPlatform(true);

        root.add(settingsPanel, CARD_SETTINGS);
        root.add(consolePanel,  CARD_CONSOLE);
        setContentPane(root);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int result = javax.swing.JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "Er du sikker på at du vil lukke?",
                    "Bekreft avslutning",
                    javax.swing.JOptionPane.YES_OPTION,
                    javax.swing.JOptionPane.QUESTION_MESSAGE
                );

                if (result == javax.swing.JOptionPane.YES_OPTION) {
                    // Hvis bruker velger "Ja", avslutt programmet
                    dispose();
                    System.exit(0);
                }

            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        UiUtil.applyAppIcon(this);
    }

    public void showSettings() { cards.show(root, CARD_SETTINGS); }
    public void showConsole()  { cards.show(root, CARD_CONSOLE);  }

    public SettingsPanel getSettingsPanel() { return settingsPanel; }
    public ConsolePanel  getConsolePanel()  { return consolePanel;  }

    /** Koble System.out/err til console-tekstvinduet. Kall dette én gang ved oppstart. */
    public void redirectSystemStreams() {
        PrintStream ps = new PrintStream(consolePanel.getOutputStream(), true);
        System.setOut(ps);
        System.setErr(ps);
    }
}
