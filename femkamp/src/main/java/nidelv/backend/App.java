package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class App {

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
     */
    public static void main(String[] args) throws IOException, GeneralSecurityException {

        while (true) 
            run();
    }
    
}
