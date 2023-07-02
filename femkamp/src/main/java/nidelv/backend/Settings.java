package nidelv.backend;

import java.util.Arrays;
import java.util.List;

public class Settings {
    // Dårlig navn, fordi plotting vil si plotting fra stevneperspektiv, og readonly er fra stevneperspektiv
    // TODO ordne opp i dette
    public final static String googleDockURL_plotting = "https://docs.google.com/spreadsheets/d/1-lEYgwkYeuHEXyxK6Fga0L6H5fW-LN4bE003jCCcjHc/edit?usp=sharing";
    public final static String googleDockURL_readonly = "https://docs.google.com/spreadsheets/d/1sEkojzoIyxrt8ZI3536483bmjP4rwtxyUFWSxdTU7is/edit?usp=sharing";

    // OBS: rekkefølgen kan trygt endres på her, MEN navnene må fortsatt være slik de er. 
    // Rekkefølgen her må være lik som i google sheet.
    public final static List<String> rekkefolgeKolonnerInput = Arrays.asList("vektklasse", "kroppsvekt", 
        "kategori", "femkampkategori", "fodselsdato", "navn", "lag", 
        "rykk1", "rykk2", "rykk3", "stot1", "stot2", "stot3", "treHopp", "kuleKast", "40Sprint");

    public final static int antallRaderSomLeses = 16;


    public final static List<String> rekkefolgeKolonnerOutput = Arrays.asList("navn", 
        //"kroppsvekt", "femkampkategori", "fodselsdato", "lag", 
        "rykk1", "rykk2", "rykk3", "stot1", "stot2", "stot3", "treHopp", "kuleKast", "40Sprint", "poeng");
        // leggtil rank og hva som trengs for å finne også!

}
