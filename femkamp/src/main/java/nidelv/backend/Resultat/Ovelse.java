package nidelv.backend.Resultat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class Ovelse {
    public static final Collection<String> validOvelser = Arrays.asList("rykk", "stot", "3-hopp", "kule", "40-meter");

    protected final String navn;
    private Lifter lifter;

    // kun løft bruker alle forsøk (ikke 3-kamp øvelsene)
    // TODO kunne vørt bedre å lage en klasse pr øvelse. Evt også flytte Poengberegning til ovelse i den klassen også
    private double forsok1;
    private double forsok2;
    private double forsok3;

    private boolean fullfort;

    private double poeng;
    protected double besteResultat;

    public Ovelse(String navn, List<Object> forsok, Lifter lifter) {
        this.lifter = lifter;
        validateInput(navn, forsok);

        this.navn = navn;
        this.fullfort = false;
        updateBesteResultatOgAlleForsok(forsok, lifter);
    }
    
    protected void validateInput(String navn, List<Object> forsok) {
        if (!validOvelser.contains(navn))        
            throw new IllegalArgumentException(navn + " er ikke en valid ovelsenavn");
        
        boolean isLoft = navn.contains("rykk") || navn.contains("stot");
        boolean isTreHopp = navn.equals("3-hopp");
        boolean isKuleKast = navn.equals("kule");
        boolean isSprint40 = navn.equals("40-meter");

        if (isLoft) 
            validateLoft(forsok);

        else if (isTreHopp || isKuleKast) 
            valdateHoppOgKast(forsok);

        else if (isSprint40)
            validateSprint40(forsok);
    }

    private void validateLoft(List<Object> forsok) {

        boolean feilAntallForsok = forsok.size() != 3;
        if (feilAntallForsok)
            throw new IllegalArgumentException("Feil antall forsok");

        if (!anyForsok(forsok)) 
            return;
        
        boolean riktigForsokRekkefolge = riktigForsokRekkefolge(forsok);
        if (!riktigForsokRekkefolge) {
            lifter.addErrorMessage("1. loft før 2. løft og 2. før 3. løft");
        }

        try{
            forsok.forEach(f -> convertObjToInt(f, lifter));
        } catch (NumberFormatException e) {
            lifter.addErrorMessage("loft: " + forsok +  " er ikke riktig format");
        }        
    } 


    private boolean anyForsok(List<Object> forsoks) {
        for (Object forsok : forsoks) {
            if (forsok !=  null)
                return true;
        }
        return false;
    }

    private boolean riktigForsokRekkefolge(List<Object> forsokene) {
        boolean forrigeVarEtForsok = true;
        for (Object forsok : forsokene) {
            String forsokStreng = (String) forsok;
            boolean etResultat = forsokStreng == null || !forsokStreng.equals("");

            if (!forrigeVarEtForsok && etResultat)
                return false;

            else if (etResultat)
                forrigeVarEtForsok = true;
            else
                forrigeVarEtForsok = false;
        }
        return true;
    }


    private void valdateHoppOgKast(List<Object> trekampForsok) {

        boolean feilAntallForsok = trekampForsok.size() != 3;
        if (feilAntallForsok)
            throw new IllegalArgumentException("Feil antall forsok");

        if (anyForsok(trekampForsok)) 
            return;

        try {
            trekampForsok.forEach(f -> convertObjToDouble(f, lifter));

        } catch (NumberFormatException e) {
            lifter.addErrorMessage("trekampresultat: " + trekampForsok + " er ikke riktig format");
        }
        
    }

    private void validateSprint40(List<Object> sprint40Forsok) {

        boolean feilAntallForsok = sprint40Forsok.size() != 2;
        if (feilAntallForsok)
            throw new IllegalArgumentException("Feil antall forsok");

        if (!anyForsok(sprint40Forsok))
            return;

        
        sprint40Forsok.forEach(sprint40 -> {
            if (sprint40!= null) 
                checkNumberOfDesimal(sprint40);});

    }

    private void checkNumberOfDesimal(Object sprint40) {
        int numberOfDecimals = Ovelse.countDecimalPlaces((String) sprint40);
        boolean forMangeDesimaler = numberOfDecimals > 1; 

        if (forMangeDesimaler)
            lifter.addErrorMessage("40 meter skal rundes opp til 1/10! " + sprint40 + " opfylller ikke dette!");       
    }

    public void updateBesteResultatOgAlleForsok(List<Object> forsok, Lifter lifter) {
        this.lifter = lifter;
        validateInput(navn, forsok);


        boolean isLoft = navn.equals("rykk") || navn.equals("stot");
        boolean istreHopp = navn.equals("3-hopp");
        boolean iskuleKast = navn.equals("kule");
        boolean isSprint40 = navn.equals("40-meter");

        if (isLoft) {
            forsok1 = convertObjToInt(forsok.get(0), lifter);
            forsok2 = convertObjToInt(forsok.get(1), lifter);
            forsok3 = convertObjToInt(forsok.get(2), lifter);
        }

        if (iskuleKast || istreHopp) {
            forsok1 = convertObjToDouble(forsok.get(0), lifter);
            forsok2 = convertObjToDouble(forsok.get(1), lifter);
            forsok3 = convertObjToDouble(forsok.get(2), lifter);
        }
            
        else if (isSprint40){
            forsok1 = convertObjToDouble(forsok.get(0), lifter);
            forsok2 = convertObjToDouble(forsok.get(1), lifter);
        }

        updateFullfort();
        updateBesteResultatForLift();
    }



    private void updateFullfort() {
        if (forsok1==0 || forsok2==0) {
            this.fullfort = false;
            return;
        }

        boolean isSprint40 = navn.equals("40-meter");
        if (isSprint40) {
            this.fullfort = true;
            return;
        }

        if (forsok3==0) 
            this.fullfort= false;
        else
            this.fullfort = true;

    }

    private void updateBesteResultatForLift() {
        List<Double> alleForsok = getForsok();
        double besteResultat;
        if (navn.equals("40-meter")) {

            if (alleForsok.get(0)<alleForsok.get(1) || alleForsok.get(1)<1){
                besteResultat = alleForsok.get(0);
            }
            else
                besteResultat = alleForsok.get(1);
        } else {
            alleForsok.sort((a,b) -> b.compareTo(a));
            besteResultat =  alleForsok.get(0);
        }

        this.besteResultat = Math.max(besteResultat, 0);
        
    }

    public double getBesteResultat() {
        return besteResultat;
    }

    public void updatePoeng() {
        this.poeng = Poengberegning.calculatePoints(this, lifter);
    }

    public double getPoeng() {
        return poeng;
    }

    public List<Double> getForsok() {
        if (navn.equals("40-meter"))
            return Arrays.asList(forsok1, forsok2);
            
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

    public static double convertObjToDouble(Object obj, Lifter lifter) {
        if (obj == null) 
            return 0.0;
            
        if (obj instanceof String) {
            if (obj.equals(""))
                return 0;
            try {
                return Double.valueOf((String) obj);
            } catch (NumberFormatException e) {
                String objStreng = (String) obj;
                if (objStreng.length()==1) // anntar at det er "-", men contains fungerer ikke...
                    return -1;

                lifter.addErrorMessage(obj + " kan ikke konverteres til et flyttall");
                return 0;
            }
            

        } else {
            throw new IllegalArgumentException("Objectet kan ikke konverteres");
        }
    }

    
    public static int convertObjToInt(Object obj, Lifter lifter) {
        if (obj == null)
            return 0;

        if (obj instanceof String) {
            if (obj.equals(""))
                return 0;
            try {
                return Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                try {
                    return convertToNegative((String) obj);
                } catch (NumberFormatException exception){
                    lifter.addErrorMessage(obj + " kan ikke konverteres til et heltall");
                    return -1;
                }
            }

        } else {
            throw new IllegalArgumentException("Objectet kan ikke konverteres");
        }
    }


    private static int convertToNegative(String objStreng) {
        if (objStreng.length()<1)
            throw new NumberFormatException("ikke et negativt tall!");

        String utenMinusTegn = objStreng.substring(1, objStreng.length());
        return -Integer.valueOf(utenMinusTegn);
    }

    public String toString() {
        return "{ovelse: " + this.navn +  " resultat: " + besteResultat + "}";
    }

    public boolean isFullfort() {
        return fullfort;
    }

    public boolean isUnderkjennt() {
        return fullfort && besteResultat==0;
    }

}
