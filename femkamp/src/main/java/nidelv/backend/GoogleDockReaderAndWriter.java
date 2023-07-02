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
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class GoogleDockReaderAndWriter {

    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Google Sheets Example";
    private static String SPREADSHEET_ID_PLOTTING;// = "1sEkojzoIyxrt8ZI3536483bmjP4rwtxyUFWSxdTU7is";
    private static String SPREADSHEET_ID_READING;

    private static List<ValueRange> inPutSheetsData  = new ArrayList<>();
    private static List<ValueRange> outPutSheetsData  = new ArrayList<>();

    public static void setSpreadsheetIDAndSheetService() throws IOException, GeneralSecurityException {
        SPREADSHEET_ID_PLOTTING = extractSpreadsheetId(Settings.googleDockURL_plotting);
        SPREADSHEET_ID_READING = extractSpreadsheetId(Settings.googleDockURL_readonly);
        sheetsService = getSheetsService();
        //SaveAndReadSettings.saveURL(url);
    }

    private static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = GoogleDockReaderAndWriter.class.getResourceAsStream("/credentials.json");
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


        Spreadsheet sp = sheetsService.spreadsheets().get(SPREADSHEET_ID_PLOTTING).execute();
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
        String range = sheetName+"!A2:P" + String.valueOf(Settings.antallRaderSomLeses);

        return sheetsService.spreadsheets().values()
            .get(SPREADSHEET_ID_PLOTTING, range);
       
    }

    public static void deletInputSheetData() {
        inPutSheetsData.clear();
    }

    public static void deletOutoutSheetData() {
        outPutSheetsData.clear();
    }

    public static void addInputSheetData(String sheetName, String cellStartPlotting, List<Object> errorMeldinger) throws IOException {
        StandarizeAndAddValueRangeToSheetData(inPutSheetsData, sheetName ,cellStartPlotting, errorMeldinger);
    }

    public static void addOutputSheetData(String sheetName, List<List<Object>> dataToWrite) throws IOException {
        String cellStartPlotting = "A1";
        addValueRangeToSheetData(outPutSheetsData, sheetName ,cellStartPlotting, dataToWrite);
    }

    private static void StandarizeAndAddValueRangeToSheetData(List<ValueRange> sheetData, String sheetName, String cellStartPlotting, List<Object> dataToWrite) throws IOException {
        List<List<Object>> rows = dataToWrite.stream()
                                    .map(data -> Collections.singletonList(data)) // brug et lambda-udtryk her
                                    .collect(Collectors.toList());

        addValueRangeToSheetData(sheetData, sheetName, cellStartPlotting, rows);
    }

    private static void addValueRangeToSheetData(List<ValueRange> sheetData, String sheetName, String cellStartPlotting, List<List<Object>> dataToWrite) throws IOException {
        String range = sheetName + "!" + cellStartPlotting;
        sheetData.add(new ValueRange()
            .setRange(range)
            .setValues(dataToWrite));
    }

    public static void writeAllToFiles() {
        Map<String, List<ValueRange>> filesToWrite = new HashMap<>();

        // kunne lagra dem som map fra start av
        filesToWrite.put(SPREADSHEET_ID_PLOTTING, inPutSheetsData);
        filesToWrite.put(SPREADSHEET_ID_READING, outPutSheetsData);

        filesToWrite.forEach((key, value) -> {
            try {
                writeToFile(value, key);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }

    private static void writeToFile(List<ValueRange> data, String spreadsheetId) throws IOException {
        BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
            .setValueInputOption("RAW")
            .setData(data);

        sheetsService.spreadsheets().values()
            .batchUpdate(spreadsheetId, batchBody)
            .execute();
    }
     
    
    

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        GoogleDockReaderAndWriter.setSpreadsheetIDAndSheetService();
  
        List<String> spreadsheetNames = getSpreadsheetNames();
        spreadsheetNames.forEach(name -> System.out.println(name));

        System.out.println("----------");
        //ValueRange response = getRespons(spreadsheetNames.get(0));


        
    }
    
}
