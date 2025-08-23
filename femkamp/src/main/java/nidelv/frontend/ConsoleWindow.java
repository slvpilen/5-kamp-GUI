package nidelv.frontend;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class ConsoleWindow extends JFrame {
    private final JTextArea area = new JTextArea(28, 110);

    private ConsoleWindow() {
        super("Program-output");

        // Vi håndterer lukking selv for å kunne spørre om bekreftelse
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(
                        ConsoleWindow.this,
                        "Vil du avslutte programmet?",
                        "Avslutt?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );
                if (choice == JOptionPane.YES_OPTION) {
                    // Evt. opprydding kan gjøres her før exit
                    System.exit(0);
                }
                // Ved "Nei" gjør vi ingenting (vinduet forblir åpent)
            }
        });

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
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        setContentPane(scroll);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /** Åpner konsollvindu og redirecter System.out/err til det. */
    public static void openAndRedirectSystemStreams() {
        SwingUtilities.invokeLater(() -> {
            ConsoleWindow win = new ConsoleWindow();

            OutputStream outStream = new TextAreaOutputStream(win.area);
            PrintStream psOut = new PrintStream(outStream, true, StandardCharsets.UTF_8);
            PrintStream psErr = new PrintStream(outStream, true, StandardCharsets.UTF_8);

            System.setOut(psOut);
            System.setErr(psErr);

            System.out.println("[Console] Output redirigert til dette vinduet.");
        });
    }

    /** Thread-sikker OutputStream som appender til JTextArea på EDT. */
    private static class TextAreaOutputStream extends OutputStream {
        private final JTextArea target;
        private final StringBuilder buffer = new StringBuilder();

        TextAreaOutputStream(JTextArea target) { this.target = target; }

        @Override public void write(int b) {
            buffer.append((char) b);
            if (b == '\n') flush();
        }

        @Override public void write(byte[] b, int off, int len) {
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            appendString(s);
        }

        @Override public void flush() {
            if (buffer.length() > 0) {
                String s = buffer.toString();
                buffer.setLength(0);
                appendOnEdt(s);
            }
        }

        private void appendString(String s) {
            // Del opp på linjeskift for jevn auto-scroll
            int start = 0, idx;
            while ((idx = s.indexOf('\n', start)) >= 0) {
                appendOnEdt(s.substring(start, idx + 1));
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
