package nidelv.backend;

import java.util.prefs.Preferences;

public final class CredentialsPathStore {
    private static final String NODE = "nidelv.backend.settings";
    private static final String KEY = "credentials_json_path";

    private static final CredentialsPathStore I = new CredentialsPathStore();
    private final Preferences prefs = Preferences.userRoot().node(NODE);

    private CredentialsPathStore() {}

    public static CredentialsPathStore get() { return I; }

    public String getPath() { return prefs.get(KEY, ""); }

    public void setPath(String path) { prefs.put(KEY, path != null ? path : ""); }
}
