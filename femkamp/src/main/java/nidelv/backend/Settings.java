package nidelv.backend;

import java.util.Arrays;
import java.util.List;

public class Settings {

    // TODO: dårlig navn, fordi plotting vil si plotting fra stevneperspektiv, og
    // readonly er fra stevneperspektiv

    /*
     * Linker som skal brukes legge inn her. OBS linkene må være på brukeren som har
     * lagt inn API-key (credentials.json)
     */
    public final static String googleDockURL_plotting = "https://docs.google.com/spreadsheets/d/1tIPWzdBv_9z_YrkT0kpSv3tL7d-OeI399kquKH3yo7o/edit?gid=0#gid=0"; // INPUT
    public final static String googleDockURL_readonly = "https://docs.google.com/spreadsheets/d/1edrKPgaKlE1V20O5-juHpfYXuzKDbnR6lwnIMj4umDc/edit?gid=1551205779#gid=1551205779"; // OUTPUT

    /*
     * OBS: rekkefølgen kan trygt endres på her, MEN navnene må fortsatt være slik
     * de er.
     * Rekkefølgen her må være lik som i google sheet.
     */
    public final static List<String> rekkefolgeKolonnerInput = Arrays.asList("vektklasse", "kroppsvekt",
            "kategori", "kat 5-kamp", "fodselsdato", "navn", "lag", "rykk1", "rykk2", "rykk3", "stot1",
            "stot2", "stot3", "3-hopp1", "3-hopp2", "3-hopp3", "kule1", "kule2", "kule3", "40-meter1", "40-meter2");
    // kat 5-kamp
    public final static int antallRaderSomLeses = 21;

    public final static List<String> rekkefolgeKolonnerOutput = Arrays.asList("Navn", "Klubb", "Kat 5-kamp",
            "Rykk", "Støt", "40-meter", "3-hopp", "Kule", "Poeng", "Rank", "1. plass", "2. plass", "3. plass");

}
