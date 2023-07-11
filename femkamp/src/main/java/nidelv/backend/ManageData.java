package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import nidelv.backend.Pulje.IllegalNumberOfLiftersException;
import nidelv.backend.Resultat.Lifter;

public class ManageData {
    

    private Collection<Pulje> puljer = new ArrayList<>();
    private List<String> puljeSpreadsheetNames = new ArrayList<>();


    public ManageData() throws IOException, GeneralSecurityException {
        GoogleDockReaderAndWriter.setSpreadsheetIDAndSheetService();
        createPuljer();
    }


    private void createPuljer() throws IOException, GeneralSecurityException {
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



    public void createMissingOutputSheets() throws IOException, GeneralSecurityException {
        List<String> puljeOutputSheetnames = GoogleDockReaderAndWriter.getOutputSpreadsheetNames();

        List<String> manglendeOutputSheets = findMissingElements(puljeSpreadsheetNames, puljeOutputSheetnames);

        boolean finnesNoenSomMangler = manglendeOutputSheets.size()>0;

        if (finnesNoenSomMangler)
            GoogleDockReaderAndWriter.createNewSheetsOutput(manglendeOutputSheets);
    }

    
    private static List<String> findMissingElements(List<String> list1, List<String> list2) {
        List<String> missing = new ArrayList<>(list1);
        missing.removeAll(list2);
        return missing;
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

    public List<Pulje> getPuljer() {
        return new ArrayList<>(puljer);
    }




    public void updatePuljer() throws IOException {

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
            

    

