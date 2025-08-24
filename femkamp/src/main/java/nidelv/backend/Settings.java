package nidelv.backend;

import java.util.Arrays;
import java.util.List;

public class Settings {

    public static String googleDockURL_input = ""; // INPUT, kun stevnemannskap skal ha tilgang til
    public static String googleDockURL_output = ""; // Må settes til read only,og tilgjengelig for alle
    /*
     * OBS: rekkefølgen kan trygt endres på her, MEN navnene må fortsatt være slik
     * de er.
     * Rekkefølgen her må være lik som i google sheet.
     */
    public final static List<String> rekkefolgeKolonnerInput = Arrays.asList("vektklasse", "kroppsvekt",
            "kategori", "kat 5-kamp", "fodselsdato", "navn", "lag", "rykk1", "rykk2", "rykk3", "stot1",
            "stot2", "stot3", "3-hopp1", "3-hopp2", "3-hopp3", "kule1", "kule2", "kule3", "40-meter1", "40-meter2");
    // kat 5-kamp
    public final static int ANTALL_RADER_SOM_LESES = 21;

    public final static List<String> rekkefolgeKolonnerOutput = Arrays.asList("Navn", "Klubb", "Kat 5-kamp",
            "Rykk", "Støt", "40-meter", "3-hopp", "Kule", "Poeng", "Rank", "1. plass", "2. plass", "3. plass");

    public static int getAntallRaderSomLeses() {
        return ANTALL_RADER_SOM_LESES;
    }

}
