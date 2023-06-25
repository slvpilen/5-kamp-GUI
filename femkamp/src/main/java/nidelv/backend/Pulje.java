package nidelv.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Get;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.cache.Cache;

import nidelv.backend.Resultat.Lifter;
import nidelv.backend.Resultat.Lifter.IllegalLifterDataException;

public class Pulje {

    private final String puljeName;
    private Comparator<Lifter> comparator;
    private com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get response;
    //private ArrayList<Lifter> lifters = new ArrayList<>();
    private ArrayList<FemkampKategori> femkampKategoris = new ArrayList<>();


    public Pulje(final String puljeName, com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Get get) {
        this.puljeName = puljeName;
        this.response = get;        
        
    }

    // public void createLifters() throws IOException{
    //     if (lifters.size()>0) throw new IllegalStateException("already created some lifters!");
    //     List<List<Object>> values = response.execute().getValues();
    //     values = values.stream().filter(line -> line.size()>0).collect(Collectors.toList());
    //     for (int i = 0; i < values.size(); i++) {
    //         lifters.add(new Lifter(puljeName, i, values.get(i)));                  
    //     }
    //     sortLifters();
    // }

    public void createLifters() throws IOException{
        if (femkampKategoris.size()>0) throw new IllegalStateException("already created some lifters!");

        femkampKategoris.add(new FemkampKategori());

        int lifterid = 0;
        List<List<Object>> values = response.execute().getValues();
        for (int i = 0; i < values.size(); i++) {

            boolean tomRad = values.get(i).size()==0; 
            if (tomRad) {
                femkampKategoris.add(new FemkampKategori());
            }
            else {
                try {
                femkampKategoris.get(femkampKategoris.size()-1).addLifter(new Lifter(puljeName, lifterid, values.get(i)));
                lifterid++;
                }
                catch (IllegalLifterDataException e) {
                    handleUserInputExceptions(e);
                }
            }
        }
        this.femkampKategoris = new ArrayList<>(femkampKategoris.stream().filter(femkampkat -> femkampkat.numberOfLifters()>0).collect(Collectors.toList()));
        sortLifters();
    }

    private void sortLifters() {
        if (comparator != null)
            femkampKategoris.forEach(femkapmkat -> femkapmkat.sortLifters(this.comparator));
    }

    public String getName() {
        return puljeName;
    }


    public void setComparator(Comparator<Lifter> comparator) {
        this.comparator = comparator;
        sortLifters();
    }

    public void updateResults() throws IOException {
        List<List<Object>> values = response.execute().getValues();;
        values = values.stream().filter(line -> line.size()>0).collect(Collectors.toList());
        int numberOfLiftersInPulje = femkampKategoris.stream().mapToInt(femkampKat -> femkampKat.numberOfLifters()).sum();
        if (values.size() != numberOfLiftersInPulje)
            throw new IllegalNumberOfLiftersException("feil antall løftere i dock og i programmet"); 

        Collection<Lifter> lifters = getAllLiftersInPulje();

        for (int i = 0; i < values.size(); i++) {
            int i2 = i;
            Optional<Lifter> lifter = lifters.stream().filter(l -> l.getId()==i2).findFirst();  
            if (lifter.isPresent())   

                try {    

                    lifter.get().updateLifter(values.get(i));
                } catch (IllegalLifterDataException e) {

                    handleUserInputExceptions(e);
                }
                // denne må håndteres med at løftere sletts og create kjøres på nytt
            else
                throw new IllegalNumberOfLiftersException("can't find the missing lifter!"); 
        }
        sortLifters();                
    }

    // denne skal skrive meldingen til google sheet
    public void handleUserInputExceptions(Exception e) {
        System.out.println(e.getMessage());

    }

    public Collection<Lifter> getAllLiftersInPulje() {
        Collection<Lifter> lifters = new ArrayList<>();
        femkampKategoris.forEach(femkampkat-> lifters.addAll(femkampkat.getLifters()));
        return lifters;

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
