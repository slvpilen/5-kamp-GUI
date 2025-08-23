package nidelv.backend.Resultat;

public class MeltzerFaber {

    // alder, coefficient menn, coefficient kvinner
    private final static double[][] meltzerTabell = {
        {30, 1.000, 1}, {31, 1.016, 1.016}, {32, 1.031, 1.017}, {33, 1.046, 1.046},
        {34, 1.059, 1.059}, {35, 1.072, 1.072}, {36, 1.083, 1.084}, {37, 1.096, 1.097},
        {38, 1.109, 1.110}, {39, 1.122, 1.124}, {40, 1.135, 1.138}, {41, 1.149, 1.153},
        {42, 1.162, 1.170}, {43, 1.176, 1.187}, {44, 1.189, 1.205}, {45, 1.203, 1.223},
        {46, 1.218, 1.244}, {47, 1.233, 1.265}, {48, 1.248, 1.288}, {49, 1.263, 1.313},
        {50, 1.279, 1.340}, {51, 1.297, 1.369}, {52, 1.316, 1.401}, {53, 1.338, 1.435},
        {54, 1.361, 1.470}, {55, 1.385, 1.507}, {56, 1.411, 1.545}, {57, 1.437, 1.585},
        {58, 1.462, 1.625}, {59, 1.488, 1.665}, {60, 1.514, 1.705}, {61, 1.541, 1.744},
        {62, 1.568, 1.778}, {63, 1.598, 1.808}, {64, 1.629, 1.839}, {65, 1.663, 1.873},
        {66, 1.699, 1.909}, {67, 1.738, 1.948}, {68, 1.779, 1.989}, {69, 1.823, 2.033},
        {70, 1.867, 2.077}, {71, 1.910, 2.120}, {72, 1.953, 2.163}, {73, 2.004, 2.214},
        {74, 2.060, 2.270}, {75, 2.117, 2.327}, {76, 2.181, 2.391}, {77, 2.255, 2.465},
        {78, 2.336, 2.546}, {79, 2.419, 2.629}, {80, 2.504, 2.714}, {81, 2.597, 0},
        {82, 2.702, 0}, {83, 2.831, 0}, {84, 2.981, 0}, {85, 3.153, 0},
        {86, 3.352, 0}, {87, 3.580, 0}, {88, 3.842, 0}, {89, 4.145, 0},
        {90, 4.493, 0}
    };


    public static double getMeltzerCoefficient(char kjonn, int age) {
        int maxAge = 90;
        if (kjonn == 'K' || kjonn == 'k')
            maxAge = 80;

        boolean ikkeKvallifisert = age < 30;
        if (ikkeKvallifisert)
            return 1;
        
        boolean eldreEnnSkalaen = age > maxAge;
        if (eldreEnnSkalaen)
            return getMeltzerCoefficient(kjonn, maxAge);
    
        // Get the corresponding row for the ages
        double[] row = meltzerTabell[age - 30];
    
        if (kjonn == 'M' || kjonn == 'm') 
            return row[1];

        else if (kjonn == 'K' || kjonn == 'k')
            return row[2];

        else 
            throw new IllegalArgumentException("Invalid gender. Must be 'M' or 'K'");  
    }
    
    


    public static void main(String[] args) {
        // Testing
        double meltzerCoefficient = getMeltzerCoefficient('M', 31);
        System.out.println("shold be: " + 1.016 + " is: " + meltzerCoefficient);

        meltzerCoefficient = getMeltzerCoefficient('K', 31);
        System.out.println("shold be: " + 1.017 + " is: " + meltzerCoefficient);
    }
}
