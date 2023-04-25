package nidelv.backend;

import java.io.IOException;

public class Validation {

    public static void googleDockURL(String googleDockURL) throws IOException {
        int NUMBERS_OF_ROWS = 16;


        if (googleDockURL.length()<26)
            throw new IllegalArgumentException("url is too short!");

        
        if (GoogleDocReader.getDateFromDoc(googleDockURL).size() == 0)
            throw new IllegalArgumentException("the doc is empty");

        if (GoogleDocReader.getDateFromDoc(googleDockURL).get(0).size() != NUMBERS_OF_ROWS){
            System.out.println(GoogleDocReader.getDateFromDoc(googleDockURL).get(0).size());
            throw new IllegalArgumentException("Wrong amount of rows in google dock");
        }
    }


    
}
