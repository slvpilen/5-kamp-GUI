package nidelv.backend;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

public class GoogleDocReader {

    //private final static String GOOGLEDOC_URL = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTzr19rA3wS8pD5iBbvbwzDYBypgyMUeN8t5tihOf5QevRdmQsC2bS41kCvg7SaxnnysIEpu1WAJl33/pub?output=csv";


    public static List<List<String>> getDateFromDoc(final String googleDocURL) throws IOException {

        List<List<String>> result = new ArrayList<>();

        try {
            URL url = new URL(googleDocURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Reader reader = new InputStreamReader(connection.getInputStream());
                CSVReader csvReader = new CSVReader(reader);

                String[] nextLine;
                while ((nextLine = csvReader.readNext()) != null) {
                    List<String> line = new ArrayList<>();
                    for (String element : nextLine) {
                        line.add(element);
                    }
                    result.add(line);
                }

                csvReader.close();
                connection.disconnect();
            } else {
                System.out.println("GET request failed. Response Code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("error reading from google dock. Link: " + googleDocURL);
        }
        return result;
    }


    
    // tester
    public static void main(String[] args) throws IOException {

        List<List<String>> data = GoogleDocReader.getDateFromDoc("https://docs.google.com/spreadsheets/d/e/2PACX-1vTzr19rA3wS8pD5iBbvbwzDYBypgyMUeN8t5tihOf5QevRdmQsC2bS41kCvg7SaxnnysIEpu1WAJl33/pub?output=csv");
        for (List<String> liste : data) 
            for (String elem : liste)
                System.out.println(elem);
    }
        
    
}
