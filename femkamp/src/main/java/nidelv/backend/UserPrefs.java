package nidelv.backend;

import java.util.prefs.Preferences;

public final class UserPrefs {
    private static final String NODE = "nidelv.backend";
    private static final String KEY_INPUT  = "sheets_input_url";
    private static final String KEY_OUTPUT = "sheets_output_url";

    private static final UserPrefs INSTANCE = new UserPrefs();
    private final Preferences prefs = Preferences.userRoot().node(NODE);

    private UserPrefs() {}

    public static UserPrefs get() { return INSTANCE; }

    public String getInputUrl(String fallback)  { return prefs.get(KEY_INPUT,  fallback); }
    public String getOutputUrl(String fallback) { return prefs.get(KEY_OUTPUT, fallback); }

    public void setInputUrl(String url)  { prefs.put(KEY_INPUT,  url != null ? url : ""); }
    public void setOutputUrl(String url) { prefs.put(KEY_OUTPUT, url != null ? url : ""); }
}
