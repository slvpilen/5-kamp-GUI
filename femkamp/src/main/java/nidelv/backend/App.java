package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import nidelv.backend.Resultat.Lifter;

public class App {

    private static void takeBreak(int seconds) {
        try {
            int millies = seconds*1000;
            Thread.sleep(millies);
        } catch (InterruptedException e) {
            System.err.println("Sleep was interrupted");
        }
    }
    

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // laster inn data og lager alt under panseret
        ManageData manageData = new ManageData();

        // enkel poeng Comparator for soertering av loftere
        Comparator<Lifter> poengComparator = new Comparator<Lifter>() {
            @Override
            public int compare(Lifter o1, Lifter o2) {
                return Double.compare(o2.getPoeng(), o1.getPoeng());
            }
        };

        // setter comparatoren til pulje 1
        Pulje pulje1 = manageData.getPulje("pulje1");
        pulje1.setComparatorAndSort(poengComparator);

        // denne kjøres gjennom hele stevnet
        int iterationNumber = 0;
        while (true) {
            // kun for å ha en se  hvor mange iterasjoner som er kjørt 
            iterationNumber++;
            System.out.println(iterationNumber);
            // det som skal skrivet til readonly havner her
            List<List<Object>> lofterLines = new ArrayList<>();
            lofterLines.add(new ArrayList<Object>(Settings.rekkefolgeKolonnerOutput));

            // legger til det som skal skrives
            // TODO lag en get linesToWrite i pulje (som tar hensin til forskjellige katogorier...)
           for (FemkampKategori femkampKategori : pulje1.getFemkampKategoris()) {

                ArrayList<Lifter> lifters = femkampKategori.getLifters();  
                lifters.forEach(lofter -> lofterLines.add(lofter.getOutput())); 
                lofterLines.add(Arrays.asList(""));         
           }

            // legger til output data til readonly filen før skriving
            GoogleDockReaderAndWriter.addOutputSheetData("pulje1", lofterLines);


            // skriver til sheet
            GoogleDockReaderAndWriter.writeErrorAndOutputToFiles();
            
            // deleting outputData
            GoogleDockReaderAndWriter.deletInputSheetData();
            GoogleDockReaderAndWriter.deletOutoutSheetData();
            

            takeBreak(5);

            // laster inn data fra inputSheet og inn i programmet
            manageData.updatePuljer();
        }
    }
    
}
