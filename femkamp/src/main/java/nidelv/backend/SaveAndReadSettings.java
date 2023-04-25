package nidelv.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SaveAndReadSettings {

    private static final String FILE_NAME = "settings.txt";
    private static final String FOLDER_PATH = System.getProperty("user.dir") + "/src/main/resources/settingsFile";


    public static void saveURL(String googleDockURL) throws IOException {
        
        List<String> lines = new ArrayList<>();
        lines.add(googleDockURL);

        Files.write(Paths.get(FOLDER_PATH, FILE_NAME), lines);
    }

    

    public static String getGoogleDocURL() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(FOLDER_PATH, FILE_NAME));

        if (lines.isEmpty()) {
            throw new IOException("The file is empty.");
        }

        return lines.get(0);
    }
    
}
