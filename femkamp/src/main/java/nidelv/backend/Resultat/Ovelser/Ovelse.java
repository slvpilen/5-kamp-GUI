package nidelv.backend.Resultat.Ovelser;

import java.util.List;
import nidelv.backend.Resultat.Lifter;

public interface Ovelse {

    public void updateBesteResultatOgAlleForsok(List<Object> forsok, Lifter lifter);

    public void updatePoeng();

    public int getBesteResultat();

    public double getPoeng();

    public double calculateHvaSomTrengsForLedelse(double lederScore);

    
}
