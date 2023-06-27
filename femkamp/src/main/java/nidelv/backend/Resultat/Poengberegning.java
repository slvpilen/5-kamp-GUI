package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

public class Poengberegning {

    // OBS! usikker på om poeng for rykk og støt beregnes riktig, var uklart i regelverket.


    public static double calculateTotalPoeng(Collection<Ovelse> ovelser, char kjonn, double kroppsvekt) {
         double points = 0.0;

        Collection<Ovelse> besteOvelser = onlyBestOvelse(ovelser);

        for (Ovelse ovelse : besteOvelser) {
            points+= calculatePoints(ovelse, kjonn, kroppsvekt);            
        }

        return round(points, 2);
    }

    private static Collection<Ovelse> onlyBestOvelse(Collection<Ovelse> resultater) {
        
        Ovelse besteRykk = findHighest(resultater, "rykk"); 
        Ovelse besteStot = findHighest(resultater, "stot"); 

        Collection<Ovelse> onlyTrekamp = filtrerKunTrekamp(resultater);

        Collection<Ovelse> onlyBeste = new ArrayList<>();

        onlyBeste.add(besteRykk);
        onlyBeste.add(besteStot);
        onlyBeste.addAll(onlyTrekamp);

        return onlyBeste;
    }

    private static Collection<Ovelse> filterNavnContains(Collection<Ovelse> resultater, String navn) {
        return resultater.stream()
            .filter(ovelse -> ovelse.getNavn().contains("rykk"))
            .collect(Collectors.toList());

    }

    private static Collection<Ovelse> filtrerKunTrekamp(Collection<Ovelse> resultater) {
        return resultater.stream()
        .filter(ovelse -> !ovelse.getNavn().contains("rykk") || !ovelse.getNavn().contains("stot"))
        .collect(Collectors.toList());

    }

    private static Ovelse findHighest(Collection<Ovelse> resultater, String navn) {
        Collection<Ovelse> alleSomInneholderNavn = filterNavnContains(resultater, navn);
        Optional<Ovelse> besteForsok = alleSomInneholderNavn.stream()
            .sorted(Comparator.comparingDouble(Ovelse::getResultat).reversed())
            .findFirst();

        if (besteForsok.isPresent())
            return besteForsok.get();

        throw new IllegalStateException("resultater innholder ingen: " + navn);
    }


    public static double calculatePoints(Ovelse ovelse, char kjonn, double kroppsvekt) {
        String ovelseNavn = ovelse.getNavnUtenForsok();
        double resultat = ovelse.getResultat();

        switch (ovelseNavn) {

            case "trehopp":
                return calculateTreHoppScore(resultat);

            case "kuleKast":
                return calculateKulekastScore(resultat);

            case "40Sprint":
                return calculateSprintScore(resultat);
            
            case "rykk":
            case "stot":
                return round(calculateSinclaire(kjonn, kroppsvekt, (int) resultat)*1.2, 2);

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

        boolean impossibleTime = time <= TIME_DELTA;
        if (impossibleTime) {
            finalScore = 0;
        }
        
        return finalScore;
    }
    
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);

        return (double) tmp / factor;
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

    
}
