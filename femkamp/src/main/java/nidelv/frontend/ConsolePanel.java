package nidelv.frontend;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import nidelv.backend.Settings;

public class ConsolePanel extends JPanel {
    private final JTextArea area = new JTextArea(28, 110);
    private final JButton backBtn = UiUtil.makeRedButton("← Tilbake til oppsett");
    private final JButton btnOpenInput = new JButton("Åpne INPUT");
    private final JButton btnOpenOutput = new JButton("Åpne OUTPUT");

    private final List<Runnable> backListeners = new ArrayList<>();

    public ConsolePanel() {
        super(new BorderLayout());

        // Toppelinje
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(backBtn);
        top.add(btnOpenInput);
        top.add(btnOpenOutput);

        btnOpenInput.addActionListener(e -> UiUtil.openInBrowser(Settings.googleDockURL_input));
        btnOpenOutput.addActionListener(e -> UiUtil.openInBrowser(Settings.googleDockURL_output));

        backBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Er du sikker? Scoreboard blir ikke oppdatert når du går ut.",
                    "Gå tilbake til oppsett?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                backListeners.forEach(Runnable::run);
            }
        });

        // Console-område
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBackground(Color.BLACK);
        area.setForeground(new Color(170, 255, 170));
        area.setCaretColor(new Color(170, 255, 170));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        DefaultCaret caret = (DefaultCaret) area.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        var scroll = new JScrollPane(
                area,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /** Registrer callback for "Tilbake". */
    public void onBack(Runnable r) {
        if (r != null)
            backListeners.add(r);
    }

    /** Tøm konsollen. */
    public void clear() {
        area.setText("");
    }

    /** Append tekst direkte (EDT-sikkert). */
    public void append(String s) {
        if (SwingUtilities.isEventDispatchThread()) {
            area.append(s);
        } else {
            SwingUtilities.invokeLater(() -> area.append(s));
        }
    }

    /** OutputStream til bruk for System.out/err redirect. */
    public OutputStream getOutputStream() {
        return new TextAreaOutputStream(area);
    }

    /** Koble System.out/err til dette panelet (kall én gang). */
    public void redirectSystemStreamsHere() {
        OutputStream outStream = getOutputStream();
        PrintStream psOut = new PrintStream(outStream, true, StandardCharsets.UTF_8);
        PrintStream psErr = new PrintStream(outStream, true, StandardCharsets.UTF_8);
        System.setOut(psOut);
        System.setErr(psErr);
    }

    /** Thread-sikker OutputStream som appender til JTextArea på EDT. */
    private static class TextAreaOutputStream extends OutputStream {
        private final JTextArea target;
        private final StringBuilder buffer = new StringBuilder();

        TextAreaOutputStream(JTextArea target) {
            this.target = target;
        }

        @Override
        public void write(int b) {
            buffer.append((char) b);
            if (b == '\n')
                flush();
        }

        @Override
        public void write(byte[] b, int off, int len) {
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            appendString(s);
        }

        @Override
        public void flush() {
            if (buffer.length() > 0) {
                String s = buffer.toString();
                buffer.setLength(0);
                appendOnEdt(s);
            }
        }

        private void appendString(String s) {
            int start = 0, idx;
            while ((idx = s.indexOf('\n', start)) >= 0) {
                String chunk = s.substring(start, idx + 1);
                appendOnEdt(chunk);
                start = idx + 1;
            }
            if (start < s.length()) {
                buffer.append(s.substring(start));
            }
        }

        private void appendOnEdt(String s) {
            if (SwingUtilities.isEventDispatchThread()) {
                target.append(s);
            } else {
                SwingUtilities.invokeLater(() -> target.append(s));
            }
        }
    }
}
