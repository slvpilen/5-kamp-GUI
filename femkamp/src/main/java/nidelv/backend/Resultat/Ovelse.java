package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nidelv.backend.Resultat.Lifter.IllegalLifterDataException;

public class Ovelse {
    public static final Collection<String> validOvelser = Arrays.asList("rykk", "stot", "treHopp", "kuleKast", "40Sprint");

    protected final String navn;

    // kun løft bruker alle forsøk (ikke 3-kamp øvelsene)
    private int forsok1;
    private int forsok2;
    private int forsok3;

    protected double besteResultat;

    public Ovelse(String navn, List<Object> forsok) {
        validateInput(navn, forsok);

        this.navn = navn;
        updateBesteResultatOgAlleForsok(forsok);
    }
    
    protected void validateInput(String navn, List<Object> forsok) {
        if (!validOvelser.contains(navn))        
            throw new IllegalArgumentException(navn + " er ikke en valid ovelsenavn");
        
        boolean isLoft = navn.contains("rykk") || navn.contains("stot");
        boolean isTreHopp = navn.equals("treHopp");
        boolean isKuleKast = navn.equals("kuleKast");
        boolean isSprint40 = navn.equals("40Sprint");

        if (isLoft) 
            validateLoft(forsok);

        if (isTreHopp || isKuleKast) 
            valdateHoppOgKast(forsok);

        if (isSprint40)
            validateSprint40(forsok);
    }

    private void validateLoft(List<Object> forsok) {
        boolean feilAntallForsok = forsok.size() != 3;
        if (feilAntallForsok)
            throw new IllegalArgumentException("Feil antall forsok");

        if (!anyForsok(forsok)) 
            return;

        try{
            forsok.forEach(f -> convertObjToInt(f));

        } catch (NumberFormatException e) {
            throw new IllegalLifterDataException("loft: " + forsok +  " er ikke riktig format");
        }        
    } 

    private boolean anyForsok(List<Object> forsoks) {
        for (Object forsok : forsoks) {
            if (forsok !=  null)
                return true;
        }
        return false;
    }


    private void valdateHoppOgKast(List<Object> trekampForsok) {

        boolean feilAntallForsok = trekampForsok.size() != 1;
        if (feilAntallForsok)
            throw new IllegalArgumentException("Feil antall forsok");

        if (anyForsok(trekampForsok)) 
            return;

        try {
            trekampForsok.forEach(f -> convertObjToDouble(f));

        } catch (NumberFormatException e) {
            throw new IllegalLifterDataException("trekampresultat: " + trekampForsok + " er ikke riktig format");
        }
        
    }

    private void validateSprint40(List<Object> sprint40Forsok) {

        boolean feilAntallForsok = sprint40Forsok.size() != 1;
        if (feilAntallForsok)
            throw new IllegalArgumentException("Feil antall forsok");

        if (!anyForsok(sprint40Forsok))
            return;

        sprint40Forsok.forEach(sprint40 -> checkNumberOfDesimal(sprint40));

    }

    private void checkNumberOfDesimal(Object sprint40) {
        int numberOfDecimals = Ovelse.countDecimalPlaces((String) sprint40);
        boolean forMangeDesimaler = numberOfDecimals > 1; 

        if (forMangeDesimaler)
            throw new IllegalLifterDataException("40 meter skal rundes opp til 1/10! " + sprint40 + " opfylller ikke dette!");       
    }

    public void updateBesteResultatOgAlleForsok(List<Object> forsok) {
        validateInput(navn, forsok);

        boolean isLoft = navn.equals("rykk") || navn.equals("stot");
        boolean istreHopp = navn.equals("treHopp");
        boolean iskuleKast = navn.equals("kuleKast");
        boolean isSprint40 = navn.equals("40Sprint");

        if (isLoft) {
            forsok1 = convertObjToInt(forsok.get(0));
            forsok2 = convertObjToInt(forsok.get(1));
            forsok3 = convertObjToInt(forsok.get(2));
            updateBesteResultatForLift();
        }
            
        else if (istreHopp || 
            iskuleKast ||
            isSprint40)
            this.besteResultat = Ovelse.convertObjToDouble(forsok.get(0));
    }

    private void updateBesteResultatForLift() {
        // lag metode som gir alle fosrok som liste
        List<Integer> alleForsok = getForsok();
        alleForsok.sort((a,b) -> b-a);
        
        this.besteResultat =  alleForsok.get(0);
    }

    public double getBesteResultat() {
        return besteResultat;
    }

    public List<Integer> getForsok() {
        return Arrays.asList(forsok1, forsok2, forsok3);
    }

    public String getNavn() {
        return this.navn;
    }


    public static int countDecimalPlaces(String numStr) {
        int decimalPlaces = 0;
        int index = numStr.indexOf(".");
        if (index != -1) {
            decimalPlaces = numStr.length() - index - 1;
        }
        return decimalPlaces;
    }

    public static double convertObjToDouble(Object obj) {
        if (obj == null) 
            return 0.0;
            
        if (obj instanceof String) {
            try {
                return Double.valueOf((String) obj);
            } catch (NumberFormatException e) {
                throw new NumberFormatException(obj + " kan ikke konverteres til et flyttall");
            }

        } else {
            throw new IllegalArgumentException("Objectet kan ikke konverteres");
        }
    }

    public static int convertObjToInt(Object obj) {
        if (obj == null)
            return 0;

        if (obj instanceof String) {
            try {
                return Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                throw new NumberFormatException(obj + " kan ikke konverteres til et heltall");
            }

        } else {
            throw new IllegalArgumentException("Objectet kan ikke konverteres");
        }
    }


    public String toString() {
        return "{ovelse: " + this.navn +  " resultat: " + besteResultat + "}";
    }

}
