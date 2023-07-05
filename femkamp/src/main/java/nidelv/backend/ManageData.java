package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import nidelv.backend.Pulje.IllegalNumberOfLiftersException;
import nidelv.backend.Resultat.Lifter;

public class ManageData {
    
    //private static final String SPREAD_SHEET_ID = Settings.googleDockURL_plotting;
    private Collection<Pulje> puljer = new ArrayList<>();
    private List<String> puljeSpreadsheetNames = new ArrayList<>();

    public ManageData() throws IOException, GeneralSecurityException {
        GoogleDockReaderAndWriter.setSpreadsheetIDAndSheetService();
        createPulje();
    }

    private void createPulje() throws IOException, GeneralSecurityException {
        this.puljeSpreadsheetNames = GoogleDockReaderAndWriter.getInputSpreadSheetNamesContaining("pulje");

        BatchGetValuesResponse inputDataRespons = GoogleDockReaderAndWriter.getMultipleSheetInputData(puljeSpreadsheetNames);

        puljeSpreadsheetNames.forEach(spreadSheetName -> {
            List<List<Object>> values = extractValues(spreadSheetName, inputDataRespons, puljeSpreadsheetNames);
            try {
                puljer.add(new Pulje(spreadSheetName, values));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    private List<List<Object>> extractValues(String spreadSheetName, BatchGetValuesResponse inputDataRespons, List<String> puljeSpreadsheetNames) {
            int sheetIndex = puljeSpreadsheetNames.indexOf(spreadSheetName);
            ValueRange specificSheetData = inputDataRespons.getValueRanges().get(sheetIndex);
            List<List<Object>> values = specificSheetData.getValues();
            return values;

    }

    public Pulje getPulje(String puljeName) {
        Optional<Pulje> pulje = puljer.stream().filter(p -> p.getName().equals(puljeName)).findFirst();
        if (pulje.isPresent())
            return pulje.get();
        throw new IllegalArgumentException("cant find pulje: " + puljeName);

    }

    public void setComparatorAndSort(Pulje pulje, Comparator<Lifter> comparator) {
        pulje.setComparatorAndSort(comparator);
    }



    public void updatePuljer() throws IOException {
        // Merk: dersom sletting, agt til nye eller endring av ark ,å programmet starte på nytt. 
        // Håndtering av dette ville gitt extra request hvert 5 sek
        BatchGetValuesResponse inputDataRespons = GoogleDockReaderAndWriter.getMultipleSheetInputData(puljeSpreadsheetNames);
        puljer.forEach(pulje -> {
            String spreadSheetName = pulje.getName();
            List<List<Object>> values = extractValues(spreadSheetName, inputDataRespons, puljeSpreadsheetNames);
            try {
                pulje.updateResults(values);
            } catch (IllegalNumberOfLiftersException e) {
                createLifters(pulje);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void createLifters(Pulje pulje) {
        try{
            pulje.createLifters();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
            

    

