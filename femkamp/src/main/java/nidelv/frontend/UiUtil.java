package nidelv.frontend;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

// import com.google.api.services.sheets.v4.model.Color;

// import com.google.api.services.sheets.v4.model.Color;

public class UiUtil {
    private static final Image APP_ICON =
        Toolkit.getDefaultToolkit().getImage(UiUtil.class.getResource("/icon.png"));

    public static void applyAppIcon(JFrame frame) {
        frame.setIconImage(APP_ICON);
    }

    public static JButton makeGreenButton(String text) {
        return makeButton(text, new java.awt.Color(0, 153, 0));  // grønn
    }

    // Hvis du vil, kan du legge til flere "temafarger"
    public static JButton makeRedButton(String text) {
        return makeButton(text, new java.awt.Color(220, 80, 80));
    }

    public static JButton makeButton(String text, java.awt.Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(java.awt.Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    public static void openInBrowser(String url) {
        try {
            if (url == null || url.isBlank()) return;
            if (!Desktop.isDesktopSupported()) return;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ignored) {
            JOptionPane.showMessageDialog(null,
                    "Kunne ikke åpne nettleser for den angitte URLen.",
                    "Åpning feilet",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

}
