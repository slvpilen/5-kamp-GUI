package nidelv.backend;

import java.util.ArrayList;
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

        String loftersFemkampkategori = lifter.getfemkampkategoriNavn();

        if (name == null)
            this.name = loftersFemkampkategori;
            
        else if (!name.equals(loftersFemkampkategori))
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

    // TODO!! denne blinket ved annen rank setting; dvs den kjører to ganger i starten
    // TODO : dårlig og langt navn, rewrite!
    // TODO: flytt comparatoren his, så slipper å ta den inn som parameter (static)!
    public void sortLiftersAndUpdateRankAndPoengForLedelse(Comparator<Lifter> comparator) {
        lifters.sort(comparator);

        double lederScore = lifters.get(0).getPoeng();

        int rank = 1;
        Lifter lifter = lifters.get(0);

        lifter.setRank(rank);
        lifter.setNodvendigForLedelsen(null);
        for (int i=1 ; i<lifters.size() ; i++) {
            lifter = lifters.get(i);
            Lifter previusLifter = lifters.get(i-1);

            boolean equalScore = previusLifter.getPoeng() == lifter.getPoeng(); 
            if (equalScore)
                lifter.setRank(rank);

            else {
                rank++;
                lifter.setRank(rank);
            }
            
            lifter.oppdaterNodvendigForLedelsen(lederScore);
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
