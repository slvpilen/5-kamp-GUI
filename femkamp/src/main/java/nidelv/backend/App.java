package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class App {
    /*
     * Start with: mvn exec:java -Dexec.mainClass="nidelv.backend.App"
     */

    private static void run() {
        try {
            ProgrammRunner programmRunner = new ProgrammRunner();
            programmRunner.runProgram();
        } catch (Exception e) {
            e.printStackTrace();
            ProgrammRunner.takeBreak(10);
        }
    }

    /*
     * Ved "api request error" fungere det å slette /tokens/StoredCredential
     * 
     * VED 400 Bad Request... kan src/main/resources/credential.sjon være expired.
     * => generer ny credential.json
     * (https://console.cloud.google.com/apis/credentials)
     * og slett tokens mappa (den ytterste)
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {

        while (true)
            run();
    }

}
