package nidelv.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Get;
import com.google.api.services.sheets.v4.model.ValueRange;

public class Pulje {

    private final String puljeName;
    private Comparator<Lifter> comparator;
    private com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get response;
    private ArrayList<Lifter> lifters = new ArrayList<>();


    public Pulje(final String puljeName, com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get get) {
        this.puljeName = puljeName;
        this.response = get;             
    }

    public void createLifters() throws IOException{
        if (lifters.size()>0) throw new IllegalStateException("already created some lifters!");
        List<List<Object>> values = response.execute().getValues();
        values = values.stream().filter(line -> line.size()>0).collect(Collectors.toList());
        for (int i = 0; i < values.size(); i++) {
            lifters.add(new Lifter(puljeName, i, values.get(i)));                  
        }
        sortLifters();
    }

    public String getName() {
        return puljeName;
    }

    public void sortLifters() {
        if (this.comparator == null) {
            this.comparator = new Comparator<Lifter>(){
                @Override
                public int compare(Lifter o1, Lifter o2) {
                    return o1.getName().compareTo(o2.getName());
                }
                
            };
            lifters.sort(comparator);
            return;
        }
        lifters.sort(this.comparator);
    }

    public void setComparator(Comparator<Lifter> comparator) {
        this.comparator = comparator;
        sortLifters();
    }

    public void updateResults() throws IOException {
        List<List<Object>> values = response.execute().getValues();;
        values = values.stream().filter(line -> line.size()>0).collect(Collectors.toList());
        if (values.size()> lifters.size())
            this.lifters = new ArrayList<>(lifters.subList(0, lifters.size()-1)); 
        for (int i = 0; i < values.size(); i++) {
            int i2 = i;
            Optional<Lifter> lifter = lifters.stream().filter(l -> l.getId()==i2).findFirst();  
            if (lifter.isPresent())        
                lifter.get().updateData(values.get(i));
            else
                lifters.add(new Lifter(puljeName, i, values.get(i)));
        }
        sortLifters();                
    }

    public ArrayList<Lifter> getLifters() {
        return this.lifters;
    }
    

    @Override
    public String toString() {
        return this.puljeName;
    }
}
