package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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

        // enkel poen Comparator for soertering av loftere
        Comparator<Lifter> poengComparator = new Comparator<Lifter>() {
            @Override
            public int compare(Lifter o1, Lifter o2) {
                return Double.compare(o2.getPoeng(), o1.getPoeng());
            }
        };

        // setter comparatoren til pulje 1
        Pulje pulje = manageData.getPulje("pulje1");
        pulje.setComparator(poengComparator);

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
            pulje.getAllLiftersInPulje().forEach(lofter -> lofterLines.add(lofter.getOutput()));
            // skriver til sheet
            GoogleDockReaderAndWriter.writeListToOutputSheet("pulje1", lofterLines);

            takeBreak(5);

            // oppdaterer manageData for en ny iterasjon i løkken 
            manageData.updatePuljer();
        }
    }
    
}
