package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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

    // TODO lag dette ferdi:
    // public static double calculateHvaSomTrengsForLedelse(Ovelse ovelse, Lifter lifter, double lederScore) {

    //     double scoreForOvelse = calculatePoints(ovelse, lifter.getKjonn(), lifter.getKroppsvekt());
    //     double lofterSinScoreUtenSisteOvelse = lifter.getPoeng() - scoreForOvelse;
    //     double scoreAaOppnaa = lederScore-lofterSinScoreUtenSisteOvelse; // bør rundes av?

    //     double 

    // }

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
        double points = 0.0;
        double coefficient;
        double divisor;

        switch(kjonn) {

            case 'M':
                coefficient = 0.751945030;
                divisor = 175.508;
                break;
            
            case 'K':
                coefficient = 0.783497476;
                divisor = 153.655;
                break;
            
            default:
                throw new IllegalArgumentException("Not a valid kjonn");            

        }


        points = weight * Math.pow(10, coefficient * Math.pow(Math.log10(kroppsvekt/divisor), 2));
        return round(points, 2);

    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);

        return (double) tmp / factor;
    }
    
}
