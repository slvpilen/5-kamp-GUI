package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import nidelv.backend.Resultat.Lifter;

public class ManageData {
    
    private static final String SPREAD_SHEET_ID = Settings.googleDockURL_plotting;
    private Collection<Pulje> puljer = new ArrayList<>();

    public ManageData() throws IOException, GeneralSecurityException {
        GoogleDockReader.setSpreadsheetID();
        createPulje();
    }

    private void createPulje() throws IOException, GeneralSecurityException {
        List<String> puleSpreadsheetNames = GoogleDockReader.getSpreadsheetNames().stream().
        filter(n -> n.contains("pulje")).collect(Collectors.toList());
        puleSpreadsheetNames.forEach(ssName -> {
        try {
            puljer.add(new Pulje(ssName, GoogleDockReader.getRespons(ssName)));
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
        puljer.forEach(p -> {
            try {
                p.updateResults();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) throws IOException, GeneralSecurityException {
        ManageData manageData = new ManageData();

        Comparator<Lifter> poengComparator = new Comparator<Lifter>() {
            @Override
            public int compare(Lifter o1, Lifter o2) {
                return Double.compare(o2.getPoeng(), o1.getPoeng());
            }
        };

        Pulje pulje = manageData.getPulje("pulje1");
        pulje.setComparator(poengComparator);

        pulje.getAllLiftersInPulje().forEach(lofter -> System.out.println(lofter));
    }
}
            

    

