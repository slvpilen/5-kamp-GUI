package nidelv.backend.Resultat;

import java.util.Arrays;
import java.util.Collection;

import nidelv.backend.Resultat.Lifter.IllegalLifterDataException;

public class Ovelse {
    public static final Collection<String> validOvelser = Arrays.asList("rykk1", "rykk2", "rykk3", "stot1","stot2", "stot3", "treHopp", "kuleKast", "40Sprint");

    private final String navn;
    private double resultat;

    public Ovelse(String navn, Object resultat) {
        validateInput(navn, resultat);

        this.navn = navn;
        setResultat(resultat);
    }
    
    private void validateInput(String navn, Object resultat) {
        if (!validOvelser.contains(navn))        
            throw new IllegalArgumentException(navn + " er ikke en valid ovelsenavn");
        
        boolean isLoft = navn.contains("rykk") || navn.contains("stot");

        if (isLoft) 
            validateLoft(resultat);


        boolean isTreHopp = navn.equals("treHopp");
        boolean isKuleKast = navn.equals("kuleKast");

        if (isTreHopp || isKuleKast) 
            valdateHoppOgKast(resultat);
        

        boolean isSprint40 = navn.equals("40Sprint");

        if (isSprint40)
            validateSprint40(resultat);
    }


    private void validateLoft(Object lift) {
            if (lift != null) {
                try{
                    int loft = Integer.parseInt((String) lift);
                } catch (NumberFormatException e) {
                    throw new IllegalLifterDataException("loft: " + lift +  " er ikke riktig format");
                }
            }  

    } 

    private void valdateHoppOgKast(Object treKamp) {
        if (treKamp == null) 
            return;
        try {
            Double.parseDouble( (String) treKamp);
        } catch (NumberFormatException e) {
            throw new IllegalLifterDataException("trekampresultat: " + treKamp + " er ikke riktig format");
        }
        
    }

    private void validateSprint40(Object sprint40) {
        if (sprint40 == null)
            return;

        int numberOfDecimals = countDecimalPlaces((String) sprint40);
        if (numberOfDecimals > 1)
            throw new IllegalLifterDataException("40 meter skal rundes opp til 1/10! " + sprint40 + " opfylller ikke dette!");       
        
    }

    public void setResultat(Object resultat) {
        validateInput(navn, resultat);

        boolean isLoft = navn.contains("rykk") || navn.contains("stot");
        boolean istreHopp = navn.equals("treHopp");
        boolean iskuleKast = navn.equals("kuleKast");
        boolean isSprint40 = navn.equals("40Sprint");

        if (isLoft)
            this.resultat = convertObjToInt(resultat);
            
        else if (istreHopp || 
            iskuleKast ||
            isSprint40)
            this.resultat = convertObjToDouble(resultat);
    }

    public double getResultat() {
        return resultat;
    }

    public String getNavn() {
        return this.navn;
    }

    public String getNavnUtenForsok() {
        return getNavn().replace("1", "").replace("2", "").replace("3", "").replace(", ", "");
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
                return 0.0;
            }

        } else {
            return 0.0;
        }
    }

    private int convertObjToInt(Object obj) {
        if (obj instanceof String) {
            try {
                return Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public String toString() {
        return "{ovelse: " + this.navn +  "resultat: " + resultat + "}";
    }



}
