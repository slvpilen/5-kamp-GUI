package nidelv.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import nidelv.backend.Resultat.Lifter;

public class FemkampKategori {

    private String name;
    private ArrayList<Lifter> lifters = new ArrayList<>();

    public FemkampKategori() {}

    public FemkampKategori(String name) {
        this.name = name;
    }

    public void addLifter(Lifter lifter) {

        String loftersFemkampkategori = lifter.getfemkampkategori();

        if (name == null)
            this.name = loftersFemkampkategori;
            
        else if (!name.equals(loftersFemkampkategori))
            throw new IllegalFemkampNavnException(lifter.getNavn() + " har en annen kategori enn tidligere loftere, som har: " + this.name);

        lifters.add(lifter);
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

    public void sortLifters(Comparator<Lifter> comparator) {
        lifters.sort(comparator);
    }

    public Collection<Lifter> getLifters() {
        return new ArrayList<>(this.lifters);
    }

    public static class IllegalFemkampNavnException extends IllegalArgumentException {
        public IllegalFemkampNavnException(String message) {
            super(message);
        }
    }




    
}
