package nidelv.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nidelv.backend.Resultat.Lifter;
//import nidelv.backend.Resultat.Lifter.IllegalLifterDataException;
import nidelv.backend.Resultat.Ovelse;

public class Pulje {

    private final String puljeName;
    private Comparator<Lifter> comparator;
    //private com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get response;
    private List<List<Object>> values;
    private ArrayList<FemkampKategori> femkampKategoris = new ArrayList<>();
    //private String currentOvelse; // Legg til for å regne ut hva må ta for å gå forbi
    private List<Object> errorMeldinger;


    public Pulje(final String puljeName, List<List<Object>> values) throws IOException {
        this.puljeName = puljeName;
        this.values = values; 
        createLifters();       
    }


    public void createLifters() throws IOException{
        this.errorMeldinger = new ArrayList<>();

        femkampKategoris.clear();

        femkampKategoris.add(new FemkampKategori());

        String currentOvelse = extractCurrentOvelse(values);

        int lifterid;
        for (int i = 1; i < values.size(); i++) {
            lifterid = i;

            boolean tomRad = values.get(i).size()==0; 
            if (tomRad) {
                femkampKategoris.add(new FemkampKategori());
            }
            else {
                createLifter(lifterid, currentOvelse);
            }
        }
        this.femkampKategoris = new ArrayList<>(femkampKategoris.stream().filter(femkampkat -> femkampkat.numberOfLifters()>0).collect(Collectors.toList()));
        sortLifters();
        appendErrorMeldingerTilGoogleSheet();
    }

    private void createLifter(int lifterid, String currentOvelse) {
                        Lifter newLifter = new Lifter(puljeName, lifterid, values.get(lifterid), currentOvelse);
                femkampKategoris.get(femkampKategoris.size()-1).addLifter(newLifter);

                String errorMessage = newLifter.getErrorMessage();
                this.errorMeldinger.add(errorMessage);
    }


    // TODO: bytt navn, denne gjør mer enn sortere; se selv
    private void sortLifters() {
        if (comparator != null)
            femkampKategoris.forEach(femkapmkat -> femkapmkat.sortLiftersAndUpdateRankAndPoengForLedelse(this.comparator));
    }

    public String getName() {
        return puljeName;
    }


    public void setComparatorAndSort(Comparator<Lifter> comparator) {
        this.comparator = comparator;
        sortLifters();
    }

    public void updateResults(List<List<Object>> values) throws IOException {
        this.errorMeldinger.clear();

        this.values = values;
        values = values.stream().filter(line -> line.size()>0).collect(Collectors.toList());
        int numberOfLiftersInPulje = femkampKategoris.stream().mapToInt(femkampKat -> femkampKat.numberOfLifters()).sum();
        // -1 fordi første linje ikke skal telles, det er currentOvelse info
        if (values.size()-1 != numberOfLiftersInPulje) 
            throw new IllegalNumberOfLiftersException("feil antall løftere i dock og i programmet"); 
        
        String currentOvelse = extractCurrentOvelse(values);

        updateAndvaluateNumbersOfLifters(currentOvelse);

        sortLifters();  
        appendErrorMeldingerTilGoogleSheet();         
    }

    //private List<List<Object>> 


    private void updateAndvaluateNumbersOfLifters(String currentOvelse) {

        int currentFemkampKategoriIndex = 0;
        FemkampKategori currentFemkampKategori = femkampKategoris.get(0);
        Collection<Lifter> lifters = currentFemkampKategori.getLifters();

        for (int i =1 ; i<values.size() ; i++) {
            List<Object> line = values.get(i);

            boolean harInnehold = line.size()>0;

            if (harInnehold) {
                int lifterID = i;
                Optional<Lifter> lifter = lifters.stream().filter(
                    lofter -> lofter.getId()==lifterID).findFirst();
                if (lifter.isPresent()) {
                    Lifter lifterToUpdate = lifter.get();
                    lifterToUpdate.updateLifter(line, currentOvelse);

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



    private String extractCurrentOvelse(List<List<Object>> values) {
        List<Object> currentOvelseLine = values.get(0);
        Object tegn = (Object) "*";
        currentOvelseLine.contains(tegn);
        int antallForekomster = Collections.frequency(currentOvelseLine, tegn);


        // validering
        if (antallForekomster!= 1) {
            errorMeldinger.add("feil antall current ovelse!");
            return null;
        }
        
        int indexTilCurrentOvelse = currentOvelseLine.indexOf(tegn);


        String currentOvelseNavn = Settings.rekkefolgeKolonnerInput.get(indexTilCurrentOvelse);

        currentOvelseNavn = convertToValidOvelse(currentOvelseNavn);

        return currentOvelseNavn;
    }

    // TODO bedre navn; denne gjør om eks rykk1 til rykk, dersom ikke fines gyldig ovelse returnerernull
    // og skriver feilmelding
    private String convertToValidOvelse(String currentOvelseNavn) {
        for (String ovelse : Ovelse.validOvelser) {
            if (currentOvelseNavn.contains(ovelse)){
                return ovelse;
            }
        }
        errorMeldinger.add(currentOvelseNavn + " er ikke en gyldig ovelse");
        return null;
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
