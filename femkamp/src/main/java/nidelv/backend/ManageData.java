package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.api.services.sheets.v4.model.ValueRange;

public class ManageData {
    
    private static final String SPREAD_SHEET_ID = Settings.googleDockURL_plotting;
    private Collection<Pulje> puljer = new ArrayList<>();

    public ManageData() throws IOException, GeneralSecurityException {
        GoogleDockReader2.setSpreadsheetID();
        createPulje();
        // lag alle løftere i pulje, etter laget, kan være formateringsproblem, men ønsker alle blir laget
    }

    private void createPulje() throws IOException, GeneralSecurityException {
        List<String> puleSpreadsheetNames = GoogleDockReader2.getSpreadsheetNames().stream().
        filter(n -> n.contains("pulje")).collect(Collectors.toList());
        puleSpreadsheetNames.forEach(ssName -> {
        try {
            puljer.add(new Pulje(ssName, GoogleDockReader2.getRespons(ssName)));
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
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });        
    }

    public void updatePuljer() {
        //puljer.stream().parallel().forEach(p -> p.updateResults());
        puljer.forEach(p -> {
            try {
                p.updateResults();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
    }


    public static void main(String[] args) throws IOException, GeneralSecurityException {
        ManageData manageData = new ManageData();
        //while (true)
        //    manageData.updatePuljer();

        Comparator<Lifter> totalKGComparator = new Comparator<Lifter>() {
            @Override
            public int compare(Lifter o1, Lifter o2) {
                return (o2.getTotal()-o1.getTotal());
            }
        };

        Pulje pulje = manageData.getPulje("pulje1");
        pulje.setComparator(totalKGComparator);

        pulje.getAllLiftersInPulje().forEach(l -> System.out.println(l));
    }
}
            

    

