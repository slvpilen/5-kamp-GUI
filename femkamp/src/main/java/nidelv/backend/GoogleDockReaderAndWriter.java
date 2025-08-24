package nidelv.backend;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GoogleDockReaderAndWriter {

    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Femkamp";
    private static String SPREADSHEET_ID_PLOTTING;
    private static String SPREADSHEET_ID_READING;

    private static List<ValueRange> inPutSheetsData  = new ArrayList<>();
    private static List<ValueRange> outPutSheetsData  = new ArrayList<>();

    private static List<ValueRange> previusInPutSheetDate = new ArrayList<>();
    private static List<ValueRange> previusOutPutSheetData = new ArrayList<>();

    // Token-lagring (bruker-mappe, ikke Program Files)
    private static final String TOKENS_SUBDIR = "Femkamp/tokens";
    private static final String USER_ID = "user";

    private static boolean isInvalidGrant(TokenResponseException e) {
        return e.getStatusCode() == 400
            && e.getDetails() != null
            && "invalid_grant".equals(e.getDetails().getError());
    }

    private static void clearStoredCredential(FileDataStoreFactory factory, String userId) throws IOException {
        DataStore<StoredCredential> store = StoredCredential.getDefaultDataStore(factory);
        store.delete(userId);
    }

    /** Kryssplattform: finn en brukerskrivbar token-mappe */
    private static Path getTokenDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA"); // Roaming: C:\Users\<user>\AppData\Roaming
            if (appData == null || appData.isBlank()) {
                appData = System.getProperty("user.home");
            }
            return Paths.get(appData, TOKENS_SUBDIR);
        } else if (os.contains("mac")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", TOKENS_SUBDIR);
        } else { // Linux/Unix
            return Paths.get(System.getProperty("user.home"), ".config", TOKENS_SUBDIR);
        }
    }

    public static void setSpreadsheetIDAndSheetService() throws IOException, GeneralSecurityException {
        SPREADSHEET_ID_PLOTTING = extractSpreadsheetId(Settings.googleDockURL_input);
        SPREADSHEET_ID_READING = extractSpreadsheetId(Settings.googleDockURL_output);
        sheetsService = getSheetsService();
    }

    private static Credential authorize() throws IOException, GeneralSecurityException {
        // 1) Finn credentials.json (som før)
        String userChosenPath = CredentialsPathStore.get().getPath();
        InputStream in = null;
        if (userChosenPath != null && !userChosenPath.isBlank()) {
            try { in = new java.io.FileInputStream(userChosenPath); } catch (IOException ignored) {}
        }
        if (in == null) {
            in = GoogleDockReaderAndWriter.class.getResourceAsStream("/credentials.json");
            if (in == null) {
                throw new IOException("Fant ikke credentials.json. Velg fil i UI, eller legg den i resources som /credentials.json");
            }
        }

        var jsonFactory = GsonFactory.getDefaultInstance();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        // 2) Tokens-dir (må være mappe)
        Path tokenDir = getTokenDir();
        Files.createDirectories(tokenDir);
        if (!tokenDir.toFile().isDirectory()) {
            throw new IOException("Tokens-sti er ikke en mappe: " + tokenDir);
        }

        // 3) Forsøk bygg + autorisasjon. Ved korrupt store: slett mappa og prøv én gang til.
        for (int attempt = 0; attempt < 2; attempt++) {
            FileDataStoreFactory storeFactory;
            GoogleAuthorizationCodeFlow flow;

            try {
                storeFactory = new FileDataStoreFactory(tokenDir.toFile());
                flow = new GoogleAuthorizationCodeFlow.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        jsonFactory,
                        clientSecrets,
                        scopes)
                    .setDataStoreFactory(storeFactory)
                    .setAccessType("offline")
                    .build();
            } catch (java.io.StreamCorruptedException | java.io.OptionalDataException e) {
                // Korrupt serialisering allerede ved bygging – self heal
                if (attempt == 0) { nukeTokenDir(tokenDir); continue; }
                throw e;
            }

            try {
                Credential credential = new AuthorizationCodeInstalledApp(
                        flow, new LocalServerReceiver.Builder().setPort(0).build()
                ).authorize(USER_ID);

                // Tving en tidlig refresh for å avdekke ugyldig refresh-token nå
                try {
                    credential.refreshToken();
                } catch (TokenResponseException tre) {
                    if (isInvalidGrant(tre)) {
                        if (attempt == 0) {
                            // enten slett kun posten…
                            try {
                                DataStore<StoredCredential> store = StoredCredential.getDefaultDataStore(storeFactory);
                                store.delete(USER_ID);
                            } catch (Exception ignore) {}
                            // …eller slett alt om store også er suspekt:
                            nukeTokenDir(tokenDir);
                            continue; // ny runde → ny login
                        }
                    }
                    throw tre; // annet tokenproblem
                }

                return credential; // suksess

            } catch (java.io.StreamCorruptedException | java.io.OptionalDataException e) {
                // Korrupt lesing inne i authorize()-flyten
                if (attempt == 0) { nukeTokenDir(tokenDir); continue; }
                throw e;
            } catch (TokenResponseException e) {
                // Andre tokenfeil ved første forsøk → self heal og prøv på nytt
                if (attempt == 0 && isInvalidGrant(e)) {
                    nukeTokenDir(tokenDir);
                    continue;
                }
                throw e;
            }
        }

        // Skal aldri nå hit
        throw new IllegalStateException("Autorisasjon mislyktes etter self-heal.");
    }


    private static void nukeTokenDir(Path tokenDir) {
        try {
            java.io.File dir = tokenDir.toFile();
            java.io.File[] files = dir.listFiles();
            if (files != null) {
                for (java.io.File f : files) {
                    if (!f.delete()) f.deleteOnExit();
                }
            }
        } catch (Exception ignore) {}
    }


    public static Sheets getSheetsService() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(), credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    public static List<String> getInputSpreadSheetNamesContaining(String containing) throws IOException, GeneralSecurityException {
        List<String> sheetNames = getInputSpreadsheetNames();
        return sheetNames.stream().filter(n -> n.contains(containing)).collect(Collectors.toList());
    }

    public static List<String> getInputSpreadsheetNames() throws IOException, GeneralSecurityException {
        Spreadsheet sp = sheetsService.spreadsheets().get(SPREADSHEET_ID_PLOTTING).execute();
        List<Sheet> sheets = sp.getSheets();
        return sheets.stream().map(s -> s.getProperties().getTitle()).collect(Collectors.toList());
    }

    public static List<String> getOutputSpreadsheetNames() throws IOException, GeneralSecurityException {
        Spreadsheet sp = sheetsService.spreadsheets().get(SPREADSHEET_ID_READING).execute();
        List<Sheet> sheets = sp.getSheets();
        return sheets.stream().map(s -> s.getProperties().getTitle()).collect(Collectors.toList());
    }

    private static String extractSpreadsheetId(String url) {
        String pattern = "https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)/";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        if (matcher.find()) return matcher.group(1);
        throw new IllegalArgumentException("Invalid Google Sheets URL");
    }

    public static BatchGetValuesResponse getMultipleSheetInputData(List<String> sheetNames) throws IOException {
        List<String> ranges = new ArrayList<>();
        for (String sheetName : sheetNames) {
            String range = sheetName + "!A3:U" + Settings.antallRaderSomLeses;
            ranges.add(range);
        }
        return sheetsService.spreadsheets().values()
            .batchGet(SPREADSHEET_ID_PLOTTING)
            .setRanges(ranges)
            .execute();
    }

    public static void deletInputSheetData() { inPutSheetsData.clear(); }

    public static void deletOutoutSheetData() { outPutSheetsData.clear(); }

    public static void addInputSheetData(String sheetName, String cellStartPlotting, List<Object> errorMeldinger) throws IOException {
        StandarizeAndAddValueRangeToSheetData(inPutSheetsData, sheetName, cellStartPlotting, errorMeldinger);
    }

    private static void StandarizeAndAddValueRangeToSheetData(List<ValueRange> sheetData, String sheetName, String cellStartPlotting, List<Object> dataToWrite) throws IOException {
        List<List<Object>> rows = dataToWrite.stream()
            .map(Collections::singletonList)
            .collect(Collectors.toList());
        addValueRangeToSheetData(sheetData, sheetName, cellStartPlotting, rows);
    }

    private static void addValueRangeToSheetData(List<ValueRange> sheetData, String sheetName, String cellStartPlotting, List<List<Object>> dataToWrite) throws IOException {
        String range = sheetName + "!" + cellStartPlotting;
        sheetData.add(new ValueRange().setRange(range).setValues(dataToWrite));
    }

    public static void addOutputSheetData(String sheetName, List<List<Object>> dataToWrite) throws IOException {
        String cellStartPlotting = "A2";
        addValueRangeToSheetData(outPutSheetsData, sheetName, cellStartPlotting, dataToWrite);
    }

    public static void writeErrorAndOutputToFiles() throws IOException {
        if (writeToFileIfNewData(inPutSheetsData, previusInPutSheetDate, SPREADSHEET_ID_PLOTTING))
            previusInPutSheetDate = new ArrayList<>(inPutSheetsData);
        if (writeToFileIfNewData(outPutSheetsData, previusOutPutSheetData, SPREADSHEET_ID_READING))
            previusOutPutSheetData = new ArrayList<>(outPutSheetsData);
    }

    private static boolean writeToFileIfNewData(List<ValueRange> newData, List<ValueRange> previusData, String spreadsheet_id) throws IOException {
        boolean ulikData = !compareValueRangeLists(newData, previusData);
        if (ulikData) {
            writeToFile(newData, spreadsheet_id);
            System.out.println("skrev til fil!");
            return true;
        }
        return false;
    }

    private static boolean compareValueRangeLists(List<ValueRange> list1, List<ValueRange> list2) {
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (!compareValueRanges(list1.get(i), list2.get(i))) return false;
        }
        return true;
    }

    public static boolean compareValueRanges(ValueRange vr1, ValueRange vr2) {
        if (vr1 == vr2) return true;
        if (vr1 == null || vr2 == null) return false;
        if (!Objects.equals(vr1.getRange(), vr2.getRange())) return false;
        if (!Objects.equals(vr1.getMajorDimension(), vr2.getMajorDimension())) return false;
        return Objects.equals(vr1.getValues(), vr2.getValues());
    }

    private static void writeToFile(List<ValueRange> data, String spreadsheetId) throws IOException {
        BatchUpdateValuesRequest batchBody = new BatchUpdateValuesRequest()
            .setValueInputOption("RAW")
            .setData(data);
        sheetsService.spreadsheets().values()
            .batchUpdate(spreadsheetId, batchBody)
            .execute();
    }

    public static void createNewSheetsOutput(List<String> sheetNames) throws IOException {
        List<Request> requests = new ArrayList<>();
        for (String newSheetName : sheetNames) {
            AddSheetRequest addSheetRequest = new AddSheetRequest();
            SheetProperties sheetProperties = new SheetProperties();
            sheetProperties.setTitle(newSheetName);
            addSheetRequest.setProperties(sheetProperties);
            Request request = new Request();
            request.setAddSheet(addSheetRequest);
            requests.add(request);
        }
        BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest();
        batchUpdateRequest.setRequests(requests);
        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID_READING, batchUpdateRequest).execute();
    }

    // Used for debugging/testing
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        GoogleDockReaderAndWriter.setSpreadsheetIDAndSheetService();
        List<String> spreadsheetNames = getInputSpreadsheetNames();
        spreadsheetNames.forEach(System.out::println);
        System.out.println("----------");
    }
}
