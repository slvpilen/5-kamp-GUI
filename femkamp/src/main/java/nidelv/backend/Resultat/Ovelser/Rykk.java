package nidelv.backend.Resultat.Ovelser;

import java.util.Arrays;
import java.util.List;

import nidelv.backend.Resultat.Lifter;

public class Rykk implements Ovelse {
    protected Lifter lifter;

    // TODO: håndter double i trekamp og int i rykk/støs....
    private int forsok1, forsok2, forsok3;
    private double poeng;
    // TODO: håndter double i trekamp og int i rykk/støs....
    protected int besteResultat;


    public Rykk(List<Object> forsok, Lifter lifter) {
        this.lifter = lifter;
        validateInput(forsok);

        updateBesteResultatOgAlleForsok(forsok, lifter);

    }


    protected void validateInput(List<Object> forsok) {

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


    protected boolean anyForsok(List<Object> forsoks) {
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


    public void updateBesteResultatOgAlleForsok(List<Object> forsok, Lifter lifter) {
        this.lifter = lifter;
        validateInput(forsok);


        forsok1 = convertObjToInt(forsok.get(0), lifter);
        forsok2 = convertObjToInt(forsok.get(1), lifter);
        forsok3 = convertObjToInt(forsok.get(2), lifter);
        updateBesteResultatForLift();
    }


    private void updateBesteResultatForLift() {
        List<Integer> alleForsok = getForsok();
        alleForsok.sort((a,b) -> b-a);
        
        this.besteResultat =  alleForsok.get(0);
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
                    return 0;
                }
            }

        } else {
            throw new IllegalArgumentException("Objectet kan ikke konverteres");
        }
    }


    public void updatePoeng() {        
        double sinclaire = calculateSinclaire(lifter.getKjonn(), lifter.getKroppsvekt(), getBesteResultat());
        double femKampLofteScore = sinclaire*1.2;

        this.poeng = round(femKampLofteScore, 2);
    }

    // TODO: leggtil veteranpoeng
    private static double calculateSinclaire(char kjonn, double kroppsvekt, int weight) {
        boolean missingInfo = !(kjonn=='M' || kjonn=='K') || kroppsvekt<=0 || weight<1;
        if (missingInfo)
            return 0;

        boolean negativeWeight = weight<0;
        if (negativeWeight)
            return 0;

        double points = 0.0;
        
        List<Double> coefficientAndDevisor = coefficientAndDivisor(kjonn);
        double coefficient = coefficientAndDevisor.get(0);
        double divisor = coefficientAndDevisor.get(1);

        points = weight * Math.pow(10, coefficient * Math.pow(Math.log10(kroppsvekt/divisor), 2));
        return round(points, 2);
    }


    private static List<Double> coefficientAndDivisor(char kjonn) {
        switch(kjonn) {

            case 'M':
                double coefficient = 0.751945030;
                double divisor = 175.508;
                return Arrays.asList(coefficient, divisor);
            
            case 'K':
                coefficient = 0.783497476;
                divisor = 153.655;
                return Arrays.asList(coefficient, divisor);
            
            default:
                throw new IllegalArgumentException("Not a valid kjonn");            

        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);

        return (double) tmp / factor;
    }

    private static int convertToNegative(String objStreng) {
        if (objStreng.length()<1)
            throw new NumberFormatException("ikke et negativt tall!");

        String utenMinusTegn = objStreng.substring(1, objStreng.length());
        return -Integer.valueOf(utenMinusTegn);
    }


    public double calculateHvaSomTrengsForLedelse(double lederScore) {
        double scoreForOvelse = getPoeng();
        double lofterSinScoreUtenSisteOvelse = lifter.getPoeng() - scoreForOvelse;
        double scoreAaOppnaaIOvelse = round(lederScore,2)-lofterSinScoreUtenSisteOvelse; 

        double somTrengs = calculateDetSomTrengsLoft(scoreAaOppnaaIOvelse);

        return somTrengs;
    }


    private double calculateDetSomTrengsLoft(double scoreAaOppnaa) {
        List<Double> coefficientAndDivisor = coefficientAndDivisor(lifter.getKjonn());
        double coefficient = coefficientAndDivisor.get(0);
        double divisor = coefficientAndDivisor.get(1);

        // deler på 1.2, fordi 5-kamp poeng er sinclaire ganget med 1.2
        double nodvendigVekt= scoreAaOppnaa/1.2 / (Math.pow(10, coefficient * Math.pow(Math.log10(lifter.getKroppsvekt()/divisor), 2)));

        return Math.ceil(nodvendigVekt);
    }


    public List<Integer> getForsok() {
        return Arrays.asList(forsok1, forsok2, forsok3);
    }

    public int getBesteResultat() {
        return besteResultat;
    }


    public String getNavn() {
        return "Rykk";
    }


    public double getPoeng() {
        return poeng;
    }


    public String toString() {
        return "{ovelse: " + getNavn() +  " resultat: " + besteResultat + "}";
    }


}
