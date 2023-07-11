package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class App {
  

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        while (true) {
            try {
                ProgrammRunner programmRunner = new ProgrammRunner();
                programmRunner.runProgram();
            } catch (Exception e) {
                e.printStackTrace();
                ProgrammRunner.takeBreak(10);
            }
        }
    }
}
