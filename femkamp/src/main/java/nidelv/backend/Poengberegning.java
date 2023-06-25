package nidelv.backend;

import java.util.HashMap;
import java.util.Map;

public class Poengberegning {

    // OBS! usikker på om poeng for rykk og støt beregnes riktig, var uklart i regelverket.



    public static double calculateTotalPoints(Map<String, Object> results, char gender, double kroppsvekt) {
        double points = 0.0;

        Map<String, Object> resultMap = getResultMap(results);

        for (Map.Entry<String, Object> entry : resultMap.entrySet()) {

            char ovelse = entry.getKey().charAt(0);
            double resultat = (double) entry.getValue();

            points+= calculatePoints(ovelse, resultat, gender, kroppsvekt);
        }

        return round(points, 2);
    }

    private static Map<String, Object> getResultMap(Map<String, Object> results) {
        Map<String, Object> resultMap = new HashMap<>();

        int RykkHighest = findHighest(results, "Rykk");
        int StotHighest = findHighest(results, "Stot");

        resultMap.put("RykkHighest", RykkHighest);
        resultMap.put("StotHighest", StotHighest);
        resultMap.put("Kulekast", results.get("Kulekast"));
        resultMap.put("TreHopp", results.get("TreHopp"));
        resultMap.put("40Sprint", results.get("40Sprint"));

        return resultMap;
    }

    private static int findHighest(Map<String, Object> results, String keyPrefix) {
        return results.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(keyPrefix) && entry.getValue() instanceof Integer)
            .mapToInt(entry -> (Integer) entry.getValue())
            .max()
            .orElse(0);
    }


    private static double calculatePoints(char ovelse, double resultat, char gender, double kroppsvekt) {
        // trehopp
        if (ovelse == 'H') {
            return round(resultat*20, 2);
        }

        // kulekast
        if (ovelse == 'K') {
            return round(resultat*13, 2);
        }

        // 40 meter
        if (ovelse == '4') {
            return round(calculateSprintScore(resultat), 2);
        }

        // rykk
        if (ovelse == 'R' || ovelse == 'S') {
            return round(calculateSinclaire(gender, kroppsvekt, (int) resultat)*1.2, 2);
        }
        
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
        
        // Poengsum kan ikke være negativ
        if (finalScore < 0) {
            finalScore = 0;
        }
        
        return finalScore;
    }


    private static double calculateSinclaire(char gender, double kroppsvekt, int weight) {
        double points = 0.0;
        double coefficient;
        double divisor;

        if (gender == 'M') {
            coefficient = 0.751945030;
            divisor = 175.508;
        }
        else if (gender == 'K') {
            coefficient = 0.783497476;
            divisor = 153.655;
        }
        else {
            throw new IllegalArgumentException("Not a valid gender");
        }

        points = weight * Math.pow(10, coefficient * Math.pow(Math.log10(kroppsvekt/divisor), 2));
        return points;

    }

    
}
