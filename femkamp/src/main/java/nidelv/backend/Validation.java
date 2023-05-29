package nidelv.backend;

import java.io.IOException;

public class Validation {

    public static void googleDockURL() throws IOException {

        // do some validating of the data. get is static....
        if (GoogleDocReader.getDateFromDoc(googleDockURL).get(0).size() != NUMBERS_OF_ROWS){
            System.out.println(GoogleDocReader.getDateFromDoc(googleDockURL).get(0).size());
            throw new IllegalArgumentException("Wrong amount of rows in google dock");
        }
    }


    
}
