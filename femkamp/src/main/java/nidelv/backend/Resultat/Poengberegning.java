package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Poengberegning {

    // OBS! Usikker på om poeng for rykk og støt beregnes riktig, var uklart i regelverket.


    public static double calculateTotalPoeng(Collection<Ovelse> ovelser, Lifter lifter) {
         double points = 0.0;

        char kjonn = lifter.getKjonn();
        double kroppsvekt = lifter.getKroppsvekt();

        for (Ovelse ovelse : ovelser) {
            points+= calculatePoints(ovelse, kjonn, kroppsvekt);            
        }

        return round(points, 2);
    }



    public static double calculatePoints(Ovelse ovelse, char kjonn, double kroppsvekt) {
        String ovelseNavn = ovelse.getNavn();
        double resultat = ovelse.getBesteResultat();

        switch (ovelseNavn) {

            case "treHopp":
                return calculateTreHoppScore(resultat);

            case "kuleKast":
                return calculateKulekastScore(resultat);

            case "40Sprint":
                return calculateSprintScore(resultat);
            
            case "rykk":
            case "stot":
                return calculateLofteScore(kjonn, kroppsvekt, (int) resultat);

            default:
                throw new IllegalArgumentException("Invalid ovelse: " + ovelse);
        }
    }


    private static double calculateTreHoppScore(double lengde) {
        double score = lengde*20;
        return round(score, 2);
    }

    private static double calculateKulekastScore(double lengde) {
        double score = lengde*13;
        return round(score, 2);
    }

    private static int calculateSprintScore(double time) {
        double BASE_TIME = 8.0;
        int BASE_SCORE = 80;
        double TIME_DELTA = 0.1;
        int SCORE_DELTA = 4;

        double difference = time - BASE_TIME;
        int scoreChange = (int) (difference / TIME_DELTA) * SCORE_DELTA;
        int finalScore = BASE_SCORE - scoreChange;
        
        boolean negativeScore = finalScore < 0;
        if (negativeScore) {
            finalScore = 0;
        }

        boolean nullTidEllerNegativ = time <= TIME_DELTA;
        if (nullTidEllerNegativ) {
            finalScore = 0;
        }
        
        return finalScore;
    }
    

    private static double calculateLofteScore(char kjonn, double kroppsvekt, int weight) {
        double sinclaire = calculateSinclaire(kjonn, kroppsvekt, weight);
        double femKampLofteScore = sinclaire*1.2;

        return round(femKampLofteScore, 2);
    }


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
    // TODO lag dette ferdi:
    public static double calculateHvaSomTrengsForLedelse(Ovelse ovelse, Lifter lifter, double lederScore) {

        double scoreForOvelse = calculatePoints(ovelse, lifter.getKjonn(), lifter.getKroppsvekt());
        double lofterSinScoreUtenSisteOvelse = lifter.getPoeng() - scoreForOvelse;
        double scoreAaOppnaaIOvelse = round(lederScore,2)-lofterSinScoreUtenSisteOvelse; // TODO bør rundes av?

        double somTrengs = calculateDetSomTrengs(scoreAaOppnaaIOvelse, ovelse.getNavn(), lifter);

        return somTrengs;

    }

    private static double calculateDetSomTrengs(double scoreAaOppnaa, String ovelseNavn, Lifter lifter) {
        switch (ovelseNavn) {
            case "treHopp":
                return calculateDetSomTrengsTreHopp(scoreAaOppnaa);

            case "kuleKast":
                return calculateDetSomTrengsKulekast(scoreAaOppnaa);

            case "40Sprint":
                return calculateDetSomTrengsSprint(scoreAaOppnaa);
            
            case "rykk":
            case "stot":
                return calculateDetSomTrengsLoft(scoreAaOppnaa, lifter);

            default:
                throw new IllegalArgumentException("Invalid ovelse: " + ovelseNavn);

        }
    }


    private static double calculateDetSomTrengsTreHopp(double scoreAaOppnaa) {
        double lengde = scoreAaOppnaa/20;
        return Math.ceil(lengde * 100) / 100; // runder av til øvre centimeter
    }



    private static double calculateDetSomTrengsKulekast(double scoreAaOppnaa) {
        double lengde = scoreAaOppnaa/13;
        return Math.ceil(lengde * 100) / 100; // runder av til øvre centimeter
    }



    // TODO sjekk om dette gi riktig tider!
    private static double calculateDetSomTrengsSprint(double scoreAaOppnaa) {
        double BASE_TIME = 8.0;
        int BASE_SCORE = 80;
        double TIME_DELTA = 0.1;
        int SCORE_DELTA = 4;

        double scoreDifference = BASE_SCORE - scoreAaOppnaa-0.01;
        double timeChange = (scoreDifference / SCORE_DELTA) * TIME_DELTA;
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

        double nodvendigVekt= scoreAaOppnaa / Math.pow(10, coefficient * Math.pow(Math.log10(lifter.getKroppsvekt()/divisor), 2));

        return Math.ceil(nodvendigVekt);
    }



    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);

        return (double) tmp / factor;
    }


    
}
