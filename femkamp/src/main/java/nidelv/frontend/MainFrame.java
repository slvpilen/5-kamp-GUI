package nidelv.frontend;

import java.awt.*;
import java.io.PrintStream;
import javax.swing.*;

public class MainFrame extends JFrame {
    public static final String CARD_SETTINGS = "settings";
    public static final String CARD_CONSOLE  = "console";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final SettingsPanel settingsPanel = new SettingsPanel();
    private final ConsolePanel consolePanel = new ConsolePanel();

    public MainFrame() {
        super("Femkamp");
        UiUtil.applyAppIcon(this);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationByPlatform(true);

        root.add(settingsPanel, CARD_SETTINGS);
        root.add(consolePanel,  CARD_CONSOLE);
        setContentPane(root);
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
