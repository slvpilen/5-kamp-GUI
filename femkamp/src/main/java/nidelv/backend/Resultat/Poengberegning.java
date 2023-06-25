package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
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
        String ovelseNavn = ovelse.getNavn();
        double resultat = ovelse.getResultat();

        if ("treHopp".equals(ovelseNavn)) 
            return round(resultat*20, 2);

        if ("kuleKast".equals(ovelseNavn)) 
            return round(resultat*13, 2);

        if ("40Sprint".equals(ovelseNavn)) 
            return round(calculateSprintScore(resultat), 2);

        if (ovelseNavn.contains("rykk") || 
            ovelseNavn.contains("stot")) 
            return round(calculateSinclaire(kjonn, kroppsvekt, (int) resultat)*1.2, 2);
        
        throw new IllegalArgumentException("Invalid ovelse: " + ovelse);
    }


    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    private static int calculateSprintScore(double time) {
        double BASE_TIME = 8.0;
        int BASE_SCORE = 80;
        double TIME_DELTA = 0.1;
        int SCORE_DELTA = 4;

        double difference = time - BASE_TIME;
        int scoreChange = (int) (difference / TIME_DELTA) * SCORE_DELTA;
        int finalScore = BASE_SCORE - scoreChange;
        
        // Poengsum kan ikke være 0 eller negativ
        if (finalScore < 0) {
            finalScore = 0;
        }

        // tid 0 er ikke mulig og tolkes ikke godkjent løp gjennomført, gir 0 poeng
        if (time <= TIME_DELTA) {
            finalScore = 0;
        }
        
        return finalScore;
    }


    private static double calculateSinclaire(char kjonn, double kroppsvekt, int weight) {
        double points = 0.0;
        double coefficient;
        double divisor;

        if (kjonn == 'M') {
            coefficient = 0.751945030;
            divisor = 175.508;
        }
        else if (kjonn == 'K') {
            coefficient = 0.783497476;
            divisor = 153.655;
        }
        else {
            throw new IllegalArgumentException("Not a valid kjonn");
        }

        points = weight * Math.pow(10, coefficient * Math.pow(Math.log10(kroppsvekt/divisor), 2));
        return points;

    }

    
}
