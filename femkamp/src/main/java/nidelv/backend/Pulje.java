package nidelv.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nidelv.backend.Resultat.Lifter;
//import nidelv.backend.Resultat.Lifter.IllegalLifterDataException;

public class Pulje {

    private final String puljeName;
    private Comparator<Lifter> comparator;
    private com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get response;
    private ArrayList<FemkampKategori> femkampKategoris = new ArrayList<>();
    private String currentOvelse; // Legg til for å regne ut hva må ta for å gå forbi
    private List<Object> errorMeldinger;


    public Pulje(final String puljeName, com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get get) {
        this.puljeName = puljeName;
        this.response = get;        
    }


    public void createLifters() throws IOException{
        this.errorMeldinger = new ArrayList<>();

        femkampKategoris.clear();

        femkampKategoris.add(new FemkampKategori());

        int lifterid = 0;
        List<List<Object>> values = response.execute().getValues();
        for (int i = 0; i < values.size(); i++) {

            boolean tomRad = values.get(i).size()==0; 
            if (tomRad) {
                femkampKategoris.add(new FemkampKategori());
            }
            else {
                Lifter newLifter = new Lifter(puljeName, lifterid, values.get(i));
                femkampKategoris.get(femkampKategoris.size()-1).addLifter(newLifter);

                String errorMessage = newLifter.getErrorMessage();
                this.errorMeldinger.add(errorMessage);

                lifterid++;
            }
        }
        this.femkampKategoris = new ArrayList<>(femkampKategoris.stream().filter(femkampkat -> femkampkat.numberOfLifters()>0).collect(Collectors.toList()));
        sortLifters();
        appendErrorMeldingerTilGoogleSheet();
    }

    private void sortLifters() {
        if (comparator != null)
            femkampKategoris.forEach(femkapmkat -> femkapmkat.sortLiftersAndUpdateRank(this.comparator));
    }

    public String getName() {
        return puljeName;
    }


    public void setComparator(Comparator<Lifter> comparator) {
        this.comparator = comparator;
        sortLifters();
    }

    public void updateResults() throws IOException {
        this.errorMeldinger.clear();

        List<List<Object>> values = response.execute().getValues();;
        values = values.stream().filter(line -> line.size()>0).collect(Collectors.toList());
        int numberOfLiftersInPulje = femkampKategoris.stream().mapToInt(femkampKat -> femkampKat.numberOfLifters()).sum();
        if (values.size() != numberOfLiftersInPulje)
            throw new IllegalNumberOfLiftersException("feil antall løftere i dock og i programmet"); 

        updateAndvaluateNumbersOfLifters(values);

        sortLifters();  
        appendErrorMeldingerTilGoogleSheet();         
    }


    private void updateAndvaluateNumbersOfLifters(List<List<Object>> values) {

        int currentFemkampKategoriIndex = 0;
        FemkampKategori currentFemkampKategori = femkampKategoris.get(0);
        Collection<Lifter> lifters = currentFemkampKategori.getLifters();

        int index = 0;

        for (List<Object> line : values) {
            index++;

            boolean harInnehold = line.size()>0;

            if (harInnehold) {
                int index2 = index;
                Optional<Lifter> lifter = lifters.stream().filter(l -> l.getId()==index2).findFirst();
                if (lifter.isPresent())   {
                    Lifter lifterToUpdate =  lifter.get();
                    lifterToUpdate.updateLifter(line);
                    lifter.get().updateLifter(line);

                    String errorMessage = lifterToUpdate.getErrorMessage();
                    errorMeldinger.add(errorMessage);
                }    
                else
                    throw new IllegalNumberOfLiftersException("can't find the missing lifter!"); 
            }

            else {
                currentFemkampKategoriIndex++;
                currentFemkampKategori = femkampKategoris.get(currentFemkampKategoriIndex);
                lifters = currentFemkampKategori.getLifters();
            }
            

        }

    }



    // denne skal skrive meldingen til google sheet
    private void appendErrorMeldingerTilGoogleSheet() throws IOException {
        String celleAaStarteAaSkrive = "A"+String.valueOf(Settings.antallRaderSomLeses+1);

        errorMeldinger.removeIf(melding -> melding == null || melding.equals(""));

        for (int i=0 ; i<10 ; i++) {
            errorMeldinger.add("");
        }

        GoogleDockReaderAndWriter.addInputSheetData(puljeName, celleAaStarteAaSkrive, errorMeldinger);
    }


    public Collection<Lifter> getAllLiftersInPulje() {
        Collection<Lifter> lifters = new ArrayList<>();
        femkampKategoris.forEach(femkampkat-> lifters.addAll(femkampkat.getLifters()));
        return lifters;

    }

    public List<FemkampKategori> getFemkampKategoris() {
        // burde returnere ny liste for å hndre yttre påvirkning
        return this.femkampKategoris;
    }

    

    @Override
    public String toString() {
        return this.puljeName;
    }

    public static class IllegalNumberOfLiftersException extends IllegalArgumentException {
        public IllegalNumberOfLiftersException(String message) {
            super(message);
        }
    }
}
