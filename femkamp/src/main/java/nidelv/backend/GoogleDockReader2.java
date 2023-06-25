package nidelv.backend;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class GoogleDockReader2 {

    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Google Sheets Example";
    private static String SPREADSHEET_ID;// = "1sEkojzoIyxrt8ZI3536483bmjP4rwtxyUFWSxdTU7is";

    public static void setSpreadsheetID () throws IOException, GeneralSecurityException {
        SPREADSHEET_ID = extractSpreadsheetId(Settings.googleDockURL_plotting);
        sheetsService = getSheetsService();
        //SaveAndReadSettings.saveURL(url);
    }

    private static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = GoogleDockReader2.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            GsonFactory.getDefaultInstance(), new InputStreamReader(in)
        );
        
        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
            clientSecrets, scopes)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
            .setAccessType("offline")
            .build();
        Credential credential = new AuthorizationCodeInstalledApp(
            flow, new LocalServerReceiver())
            .authorize("user");

        return credential;
    }

    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory.getDefaultInstance(), credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
    }



    public static List<String> getSpreadsheetNames() throws IOException, GeneralSecurityException {


        Spreadsheet sp = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute();
        List<Sheet> sheets = sp.getSheets();
        List<String> sheetNames = sheets.stream().map(sheet -> sheet.getProperties().getTitle()).collect(Collectors.toList());
    
        return sheetNames;
    }

    private static String extractSpreadsheetId(String url) {
        String pattern = "https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)/";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid Google Sheets URL");
        }
    }

    public static Get getRespons(String sheetName) throws IOException {
        String range = sheetName+"!A2:P25";

        // execute will update from web
        return sheetsService.spreadsheets().values()
            .get(SPREADSHEET_ID, range);
            //.execute();

    //     while (true){
    //     List<List<Object>> values = response.getValues();

    //     if (values == null || values.isEmpty()) {
    //         System.out.println("no data found"); // throw a exception instead??
    //     } else {
    //         for (List<Object> row : values) {
    //             System.out.printf("%s %s from %s\n", row.get(5), row.get(4), row.get(1));
    //         }
    //     }
    // }
        //return values;
        
    }
    
    
    

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        GoogleDockReader2.setSpreadsheetID();
  
        List<String> spreadsheetNames = getSpreadsheetNames();
        spreadsheetNames.forEach(name -> System.out.println(name));

        System.out.println("----------");
        //ValueRange response = getRespons(spreadsheetNames.get(0));


        
    }
    
}
