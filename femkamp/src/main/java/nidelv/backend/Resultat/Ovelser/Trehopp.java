package nidelv.backend.Resultat.Ovelser;

import java.util.List;

import nidelv.backend.Resultat.Lifter;

public class Trehopp extends Rykk {


    public Trehopp(List<Object> forsok, Lifter lifter) {
        super(forsok, lifter);
    }

    @Override
    protected void validateInput(List<Object> forsok) {

        // TODO: leggtil 3 forsøk her: 3 her
        boolean feilAntallForsok = forsok.size() != 3;
        if (feilAntallForsok)
            throw new IllegalArgumentException("Feil antall forsok");

        if (anyForsok(forsok)) 
            return;

        try {
            forsok.forEach(f -> convertObjToDouble(f, lifter));

        } catch (NumberFormatException e) {
            lifter.addErrorMessage("trekampresultat: " + forsok + " er ikke riktig format");
        }

    }    


    
}
