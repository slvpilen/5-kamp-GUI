package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import nidelv.backend.Pulje.IllegalNumberOfLiftersException;
import nidelv.backend.Resultat.Lifter;

public class ManageData {
    
    //private static final String SPREAD_SHEET_ID = Settings.googleDockURL_plotting;
    private Collection<Pulje> puljer = new ArrayList<>();

    public ManageData() throws IOException, GeneralSecurityException {
        GoogleDockReaderAndWriter.setSpreadsheetIDAndSheetService();
        createPulje();
    }

    private void createPulje() throws IOException, GeneralSecurityException {
        List<String> puleSpreadsheetNames = GoogleDockReaderAndWriter.getSpreadsheetNames().stream().
        filter(n -> n.contains("pulje")).collect(Collectors.toList());
        puleSpreadsheetNames.forEach(ssName -> {
        try {
            puljer.add(new Pulje(ssName, GoogleDockReaderAndWriter.getRespons(ssName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        });
        createLiftersInPulje();
    }

    public Pulje getPulje(String puljeName) {
        Optional<Pulje> pulje = puljer.stream().filter(p -> p.getName().equals(puljeName)).findFirst();
        if (pulje.isPresent())
            return pulje.get();
        throw new IllegalArgumentException("cant find pulje: " + puljeName);

    }

    public void setComparator(Pulje pulje, Comparator<Lifter> comparator) {
        pulje.setComparator(comparator);
    }

    private void createLiftersInPulje() {
        puljer.forEach(p -> {
            try {
                p.createLifters();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });        
    }

    public void updatePuljer() {
        puljer.forEach(pulje -> {
            try {
                pulje.updateResults();
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
            

    

