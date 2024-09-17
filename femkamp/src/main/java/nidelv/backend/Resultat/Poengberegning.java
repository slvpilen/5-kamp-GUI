package nidelv.backend.Resultat;

import java.util.Arrays;
import java.util.List;

// TODO: sinclare poeng for veteran gang med alderskoefficient
// OBS: hvorfor skal ikke kule bruke altersk?
public class Poengberegning {


    public static double calculatePoints(Ovelse ovelse, Lifter lifter) {
        String ovelseNavn = ovelse.getNavn();
        double resultat = ovelse.getBesteResultat();

        switch (ovelseNavn) {

            case "3-hopp":
                return calculateTreHoppScore(lifter, resultat);

            case "kule":
                return calculateKulekastScore(lifter,  resultat);

            case "40-meter":
                return calculateSprintScore(lifter, resultat);
            
            case "rykk":
            case "stot":
                return calculateLofteScore(lifter, (int) round(resultat,0));

            default:
                throw new IllegalArgumentException("Invalid ovelse: " + ovelse);
        }
    }

    

    private static double calculateTreHoppScore(Lifter lifter, double lengde) {
        double score = lengde*20;
        if (lifter.isVeteran())
            score*= MeltzerFaber.getMeltzerCoefficient(lifter.getKjonn(), lifter.getAlder());
        return round(score, 2);
    }

    private static double calculateKulekastScore(Lifter lifter, double lengde) {
        // sinclariek. for menn benyttes for begge kjønn
        
        double score = 10 * lengde * sinclaireCoefficient('M', lifter.getKroppsvekt());
        if (lifter.isVeteran()) 
            score*= MeltzerFaber.getMeltzerCoefficient(lifter.getKjonn(), lifter.getAlder());
        return round(score, 2);
    }


    private static int calculateSprintScore(Lifter lifter, double time) {
        double BASE_TIME = 8.0;
        int BASE_SCORE = 80;
        double TIME_DELTA = 0.1;
        int SCORE_DELTA = 40;
    
        double roundedTime = Math.ceil(time * 10) / 10.0;
        double difference = BASE_TIME - roundedTime;
        int scoreChange = (int) round((difference * SCORE_DELTA),0);
        int finalScore = BASE_SCORE + scoreChange;
    
        boolean negativeScore = finalScore < 0;
        boolean negativeTime = time <= TIME_DELTA; 
        if (negativeScore || negativeTime) {
            finalScore = 0;
        }
        if (lifter.isVeteran())
            finalScore*= MeltzerFaber.getMeltzerCoefficient(lifter.getKjonn(), lifter.getAlder());
            
        return finalScore;
    }
    

    protected static double calculateLofteScore(Lifter lifter, int weight) {
        double sinclaire = calculateSinclaire(lifter, weight);
        double femKampLofteScore = sinclaire*1.2;


        return round(femKampLofteScore, 2);
    }


    private static double calculateSinclaire(Lifter lifter, int weight) {
        char kjonn = lifter.getKjonn();
        double kroppsvekt = lifter.getKroppsvekt();
        int alder = lifter.getAlder();

        boolean missingInfo = !(kjonn=='M' || kjonn=='K') || kroppsvekt<=0 || weight<1;
        if (missingInfo)
            return 0;

        boolean negativeWeight = weight<0;
        if (negativeWeight)
            return 0;

        double points = weight * sinclaireCoefficient(kjonn, kroppsvekt);

        if (lifter.isVeteran()) 
            points*= MeltzerFaber.getMeltzerCoefficient(kjonn, alder);
        
    
        return round(points, 2);
    }


    private static double sinclaireCoefficient(char kjonn, double kroppsvekt) {
        List<Double> coefficientAndDevisor = coefficientAndDivisor(kjonn);
        double coefficient = coefficientAndDevisor.get(0);
        double divisor = coefficientAndDevisor.get(1);

        kroppsvekt = adjustMaxMin(kroppsvekt, kjonn);

        return Math.pow(10, coefficient * Math.pow(Math.log10(kroppsvekt/divisor), 2));

    }


    private static List<Double> coefficientAndDivisor(char kjonn) {
        switch(kjonn) {

            case 'M':
                double coefficient = 0.722762521;
                double divisor = 193.609;
                return Arrays.asList(coefficient, divisor);
            
            case 'K':
                coefficient = 0.787004341;
                divisor = 153.757;
                return Arrays.asList(coefficient, divisor);
            
            default:
                throw new IllegalArgumentException("Not a valid kjonn");            

        }
    }

    private static double adjustMaxMin(double kroppsvekt, char kjonn) {
        switch(kjonn){

            case 'M':
                return Math.max(Math.min(193.6, kroppsvekt), 32);

            case 'K':
                return Math.max(Math.min(153.8, kroppsvekt), 28);

            default:
                throw new IllegalArgumentException("Not a valid kjonn");   

        }
    }
    
    public static double calculateHvaSomTrengsForLedelse(Ovelse ovelse, Lifter lifter, double lederScore) {

        double scoreForOvelse = ovelse.getPoeng();
        // if (ovelse.getNavn().equals("rykk") || ovelse.getNavn().equals("stot")) {
        //     scoreForOvelse = ca;
        // }
        double lofterSinScoreUtenSisteOvelse = lifter.getPoeng() - scoreForOvelse;
        double scoreAaOppnaaIOvelse = round(lederScore,2)-lofterSinScoreUtenSisteOvelse; 

        double somTrengs = calculateDetSomTrengs(scoreAaOppnaaIOvelse, ovelse.getNavn(), lifter);

        return somTrengs;

    }

    private static double calculateDetSomTrengs(double scoreAaOppnaa, String ovelseNavn, Lifter lifter) {
        switch (ovelseNavn) {
            case "3-hopp":
                return calculateDetSomTrengsTreHopp(lifter, scoreAaOppnaa);

            case "kule":
                return calculateDetSomTrengsKulekast(scoreAaOppnaa, lifter);

            case "40-meter":
                return calculateDetSomTrengsSprint(lifter, scoreAaOppnaa);
            
            case "rykk":
            case "stot":
                return calculateDetSomTrengsLoft(scoreAaOppnaa, lifter);

            default:
                throw new IllegalArgumentException("Invalid ovelse: " + ovelseNavn);

        }
    }


    private static double calculateDetSomTrengsTreHopp(Lifter lifter, double scoreAaOppnaa) {
        double alderfaktor = MeltzerFaber.getMeltzerCoefficient(lifter.getKjonn(), lifter.getAlder());
        double lengde = scoreAaOppnaa/(20*alderfaktor);
        return Math.ceil(lengde * 100) / 100; // runder av til øvre centimeter
    }



    private static double calculateDetSomTrengsKulekast(double scoreAaOppnaa, Lifter lifter) {
        // double lengde = scoreAaOppnaa/13;
        // return Math.ceil(lengde * 100) / 100; // runder av til øvre centimeter
        // sinclairek. for menn brukes for begge kjonn
        List<Double> coefficientAndDivisor = coefficientAndDivisor('M');
        double coefficient = coefficientAndDivisor.get(0);
        double divisor = coefficientAndDivisor.get(1);

        double kroppsvekt = adjustMaxMin(lifter.getKroppsvekt(), 'M');

        // deler på 1.2, fordi 5-kamp poeng er sinclaire ganget med 1.2
        double alderfaktor = 1;
        if (lifter.isVeteran())
            alderfaktor = MeltzerFaber.getMeltzerCoefficient(lifter.getKjonn(), lifter.getAlder());

        double nodvendigLengde= scoreAaOppnaa/ (alderfaktor*Math.pow(10, coefficient * Math.pow(Math.log10(kroppsvekt/divisor), 2)))/10; // TODO: riktig for vet?

        return Math.ceil(nodvendigLengde*100)/100;
    }


    private static double calculateDetSomTrengsSprint(Lifter lifter, double scoreAaOppnaa) {
        double BASE_TIME = 8.0;
        int BASE_SCORE = 80;
        double TIME_DELTA = 0.1;
        int SCORE_DELTA = 4;

        double aldersfaktor = MeltzerFaber.getMeltzerCoefficient(lifter.getKjonn(), lifter.getAlder());

        double scoreDifference = BASE_SCORE - scoreAaOppnaa-0.01;  //TODO: trenger å trekkefra 0.01? lik poeng-> delt ledelse?
        double timeChange = (scoreDifference / (SCORE_DELTA*aldersfaktor)) * TIME_DELTA; // TODO: blir dette rett for veteran??
        double finalTime = BASE_TIME + timeChange;

        double roundedFinalTime = Math.floor(finalTime * 10) / 10;

        boolean negativTid = roundedFinalTime<0;
        if (negativTid)
            return 0;

        return roundedFinalTime;
    }



    private static double calculateDetSomTrengsLoft(double scoreAaOppnaa, Lifter lifter) {
        List<Double> coefficientAndDivisor = coefficientAndDivisor(lifter.getKjonn());
        double coefficient = coefficientAndDivisor.get(0);
        double divisor = coefficientAndDivisor.get(1);

        double kroppsvekt = adjustMaxMin(lifter.getKroppsvekt(), lifter.getKjonn());

        // deler på 1.2, fordi 5-kamp poeng er sinclaire ganget med 1.2
        double aldersfaktor = 1;
        if (lifter.isVeteran()) {
            aldersfaktor = MeltzerFaber.getMeltzerCoefficient(lifter.getKjonn(), lifter.getAlder());
        }

        // TODO: riktig for veteran?
        double nodvendigVekt= scoreAaOppnaa/ (1.2 * aldersfaktor) / (Math.pow(10, coefficient * Math.pow(Math.log10(kroppsvekt/divisor), 2)));

        return Math.ceil(nodvendigVekt);
    }



    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);

        return (double) tmp / factor;
    }

    // public static void main(String[] args) {

    //     double poeng = Poengberegning.calculateLofteScore('M', 66.2, 100);

    //     System.out.println(poeng);
    // }

    
}
