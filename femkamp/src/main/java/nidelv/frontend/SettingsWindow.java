package nidelv.frontend;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import nidelv.backend.Settings;
import nidelv.backend.CredentialsPathStore;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.datatransfer.DataFlavor;
import java.net.URI;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.concurrent.CountDownLatch;

public class SettingsWindow extends JFrame {
    private static final Preferences PREFS =
            Preferences.userRoot().node("nidelv.backend.settings");
    private static final String KEY_INPUT  = "sheets_input_url";
    private static final String KEY_OUTPUT = "sheets_output_url";

    private final JTextField fieldInput   = new JTextField(34);
    private final JTextField fieldOutput  = new JTextField(34);
    private final JTextField fieldCreds   = new JTextField(34);

    private final JButton btnOpenInput    = new JButton("Åpne INPUT");
    private final JButton btnOpenOutput   = new JButton("Åpne OUTPUT");
    private final JButton btnBrowseCreds  = new JButton("Bla gjennom…");
    private final JButton saveStartBtn    = new JButton("Lagre og start");
    private final JButton exitBtn         = new JButton("Avslutt");
    private final CountDownLatch readyLatch;

    public SettingsWindow(CountDownLatch readyLatch) {
        super("Oppsett – Google Sheets (INPUT/OUTPUT) + Credentials");
        this.readyLatch = readyLatch;

        // Forhåndsfyll: bruk sist lagret om finnes, ellers Settings.*
        String inputDefault  = Settings.googleDockURL_input;
        String outputDefault = Settings.googleDockURL_output;
        fieldInput.setText(PREFS.get(KEY_INPUT,  inputDefault));
        fieldOutput.setText(PREFS.get(KEY_OUTPUT, outputDefault));

        // Credentials sti fra egen store
        fieldCreds.setText(CredentialsPathStore.get().getPath());

        // Tooltips
        fieldInput.setToolTipText("INPUT – arket programmet LESER fra (redigerbart for funksjonærer).");
        fieldOutput.setToolTipText("OUTPUT – arket programmet SKRIVER til (kun lesing for alle).");
        fieldCreds.setToolTipText("Sti til credentials.json (OAuth-klient) – dra filen hit, eller klikk 'Bla gjennom…'.");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var root = new JPanel(new GridBagLayout());
        var c = new GridBagConstraints();
        c.insets = new Insets(8, 10, 8, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        // INPUT-seksjon
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(sectionBorder("Google Sheets – INPUT (redigerbart)"));
        var ci = new GridBagConstraints();
        ci.insets = new Insets(6, 8, 6, 8);
        ci.fill = GridBagConstraints.HORIZONTAL;
        ci.weightx = 1.0;

        ci.gridx = 0; ci.gridy = 0;
        inputPanel.add(makeHint(
                "Dette er <b>kildearket</b> som programmet <b>LESER</b> fra. " +
                "Her fylles parametre/data inn av funksjonærer. " +
                "Format: <code>https://docs.google.com/spreadsheets/d/…</code>"
        ), ci);

        ci.gridx = 0; ci.gridy = 1;
        inputPanel.add(labeledFieldWithButton("URL til INPUT-ark:", fieldInput, btnOpenInput), ci);

        // OUTPUT-seksjon
        JPanel outputPanel = new JPanel(new GridBagLayout());
        outputPanel.setBorder(sectionBorder("Google Sheets – OUTPUT (kun lesing)"));
        var co = new GridBagConstraints();
        co.insets = new Insets(6, 8, 6, 8);
        co.fill = GridBagConstraints.HORIZONTAL;
        co.weightx = 1.0;

        co.gridx = 0; co.gridy = 0;
        outputPanel.add(makeHint(
                "Dette er <b>resultat-/rapportarket</b> som programmet <b>SKRIVER</b> til. " +
                "Åpnes for å se live score board / resultater. " +
                "Ikke rediger her. Format: <code>https://docs.google.com/spreadsheets/d/…</code>"
        ), co);

        co.gridx = 0; co.gridy = 1;
        outputPanel.add(labeledFieldWithButton("URL til OUTPUT-ark:", fieldOutput, btnOpenOutput), co);

        // Credentials-seksjon
        JPanel credsPanel = new JPanel(new GridBagLayout());
        credsPanel.setBorder(sectionBorder("Google API – credentials.json"));
        var cc = new GridBagConstraints();
        cc.insets = new Insets(6, 8, 6, 8);
        cc.fill = GridBagConstraints.HORIZONTAL;
        cc.weightx = 1.0;

        cc.gridx = 0; cc.gridy = 0;
        credsPanel.add(makeHint(
                "Velg eller dra inn <b>credentials.json</b> (OAuth 2.0-klient) fra Google Cloud Console. " +
                "Brukes for å autorisere Sheets-tilgang."
        ), cc);

        cc.gridx = 0; cc.gridy = 1;
        credsPanel.add(labeledFieldWithButton("Sti til credentials.json:", fieldCreds, btnBrowseCreds), cc);

        // Drag & drop for .json
        new DropTarget(fieldCreds, new DropTargetAdapter() {
            @Override public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                    Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (data instanceof List<?> files && !files.isEmpty()) {
                        java.io.File f = (java.io.File) files.get(0);
                        if (f.getName().toLowerCase().endsWith(".json")) {
                            fieldCreds.setText(f.getAbsolutePath());
                        } else {
                            JOptionPane.showMessageDialog(SettingsWindow.this,
                                "Velg en .json-fil (credentials.json).", "Feil filtype",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } catch (Exception ignore) {}
            }
        });

        // Bla gjennom…
        btnBrowseCreds.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Velg credentials.json");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON", "json"));
            if (chooser.showOpenDialog(SettingsWindow.this) == JFileChooser.APPROVE_OPTION) {
                fieldCreds.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Knapper
        saveStartBtn.addActionListener(e -> onSaveStart());
        exitBtn.addActionListener(e -> { dispose(); System.exit(0); });
        btnOpenInput.addActionListener(e -> openInBrowser(fieldInput.getText().trim()));
        btnOpenOutput.addActionListener(e -> openInBrowser(fieldOutput.getText().trim()));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(exitBtn);
        btns.add(saveStartBtn);

        // Plasser alt i root
        c.gridx = 0; c.gridy = 0; root.add(inputPanel, c);
        c.gridx = 0; c.gridy = 1; root.add(outputPanel, c);
        c.gridx = 0; c.gridy = 2; root.add(credsPanel, c);
        c.gridx = 0; c.gridy = 3; root.add(btns, c);

        setContentPane(root);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void onSaveStart() {
        String inputUrl   = fieldInput.getText().trim();
        String outputUrl  = fieldOutput.getText().trim();
        String credsPath  = fieldCreds.getText().trim();

        // Valider Sheets-URLer
        if (!looksLikeSheetsUrl(inputUrl)) {
            JOptionPane.showMessageDialog(
                    this,
                    "INPUT ser ikke ut som en gyldig Google Sheets-URL.\nForventet: https://docs.google.com/spreadsheets/d/…",
                    "Ugyldig INPUT-URL",
                    JOptionPane.WARNING_MESSAGE
            );
            fieldInput.requestFocus();
            return;
        }
        if (!looksLikeSheetsUrl(outputUrl)) {
            JOptionPane.showMessageDialog(
                    this,
                    "OUTPUT ser ikke ut som en gyldig Google Sheets-URL.\nForventet: https://docs.google.com/spreadsheets/d/…",
                    "Ugyldig OUTPUT-URL",
                    JOptionPane.WARNING_MESSAGE
            );
            fieldOutput.requestFocus();
            return;
        }

        // Valider credentials.json
        if (credsPath.isEmpty() || !credsPath.toLowerCase().endsWith(".json") ||
            !new java.io.File(credsPath).isFile()) {
            JOptionPane.showMessageDialog(this,
                "Velg gyldig credentials.json (eksisterende .json-fil).",
                "Manglende/ugyldig credentials.json",
                JOptionPane.WARNING_MESSAGE);
            fieldCreds.requestFocus();
            return;
        }

        // (valgfritt) enkel sanity check på innhold
        try (var r = new java.io.FileReader(credsPath)) {
            char[] buf = new char[2048];
            int n = r.read(buf);
            String head = new String(buf, 0, Math.max(0, n));
            if (!(head.contains("\"installed\"") || head.contains("\"web\""))) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Filen ser ikke ut som en standard OAuth credentials.json.\nFortsette likevel?",
                    "Uventet innhold", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) return;
            }
        } catch (Exception ignore) {
            // authorize() håndterer eventuelle formatfeil senere
        }

        // Lagre til Settings (runtime)
        Settings.googleDockURL_input  = inputUrl;
        Settings.googleDockURL_output = outputUrl;

        // Husk sist brukte til neste oppstart
        PREFS.put(KEY_INPUT,  inputUrl);
        PREFS.put(KEY_OUTPUT, outputUrl);

        // Lagre credentials-sti via egen store
        CredentialsPathStore.get().setPath(credsPath);

        // Lås feltene så det er tydelig at vi starter
        setInputsEnabled(false);

        // Lukk og gi main grønt lys
        dispose();
        if (readyLatch.getCount() > 0) readyLatch.countDown();
    }

    // --- UI hjelpere ---

    private static TitledBorder sectionBorder(String title) {
        var border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font(border.getTitleFont().getName(), Font.BOLD, 13));
        return border;
    }

    private static JPanel labeledFieldWithButton(String label, JTextField field, JButton openBtn) {
        JPanel p = new JPanel(new GridBagLayout());
        var c = new GridBagConstraints();
        c.insets = new Insets(4, 0, 4, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel l = new JLabel(label);
        l.setLabelFor(field);

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(l, c);

        c.gridy = 1; c.gridwidth = 1; c.weightx = 1.0; p.add(field, c);
        c.gridx = 1; c.weightx = 0.0; c.fill = GridBagConstraints.NONE;
        p.add(openBtn, c);

        return p;
    }

    private static JComponent makeHint(String html) {
        JLabel l = new JLabel("<html><div style='width:560px; color:#555;'>" + html + "</div></html>");
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        return l;
    }

    private static boolean looksLikeSheetsUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String u = url.toLowerCase();
        return (u.startsWith("http://") || u.startsWith("https://"))
                && u.contains("docs.google.com/spreadsheets/")
                && u.contains("/d/");
    }

    private static void openInBrowser(String url) {
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

    private void setInputsEnabled(boolean enabled) {
        fieldInput.setEnabled(enabled);
        fieldOutput.setEnabled(enabled);
        fieldCreds.setEnabled(enabled);
        btnOpenInput.setEnabled(enabled);
        btnOpenOutput.setEnabled(enabled);
        btnBrowseCreds.setEnabled(enabled);
        saveStartBtn.setEnabled(enabled);
    }
}
