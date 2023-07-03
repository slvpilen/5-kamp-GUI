package nidelv.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import nidelv.backend.Resultat.Lifter;
//import nidelv.backend.Resultat.Lifter.IllegalLifterDataException;

public class FemkampKategori {

    private String name;
    private ArrayList<Lifter> lifters = new ArrayList<>();

    public FemkampKategori() {}

    public FemkampKategori(String name) {
        this.name = name;
    }

    public void addLifter(Lifter lifter) {

        String loftersFemkampkategori = lifter.getfemkampkategoriNavn();

        if (name == null)
            this.name = loftersFemkampkategori;
            
        else if (!name.equals(loftersFemkampkategori))
            //throw new IllegalFemkampNavnException(lifter.getNavn() + " har en annen kategori enn tidligere loftere, som har: " + this.name);
            lifter.addErrorMessage(lifter.getNavn() + " har en annen kategori enn tidligere loftere, som har: " + this.name);

        lifters.add(lifter);
        lifter.setFemkampkategori(this);
    }



    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int numberOfLifters() {
        return lifters.size();
    }

    public void sortLiftersAndUpdateRank(Comparator<Lifter> comparator) {
        // TODO ved lik poeng, skal rangers etter startnummer?
        lifters.sort(comparator);
        
        for (int i=0 ; i<lifters.size() ; i++) {
            lifters.get(i).setRank(i+1);
        }
    }

    public ArrayList<Lifter> getLifters() {
        return new ArrayList<>(this.lifters);
    }

    public static class IllegalFemkampNavnException extends IllegalArgumentException {
        public IllegalFemkampNavnException(String message) {
            super(message);
        }
    }




    
}
