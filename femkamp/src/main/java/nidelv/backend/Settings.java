package nidelv.backend;

import java.util.Arrays;
import java.util.List;

public class Settings {

    // TODO: dårlig navn, fordi plotting vil si plotting fra stevneperspektiv, og readonly er fra stevneperspektiv

    /* 
    * Linker som skal brukes legge inn her. OBS linkene må være på brukeren som har lagt inn API-key (credentials.json)
    */
    public final static String googleDockURL_plotting = "https://docs.google.com/spreadsheets/d/1fsUyJ2K62KM11X39ZH2F-ECdppVZVlq5c8yFrKGS1Kg/edit?usp=sharing"; //"https://docs.google.com/spreadsheets/d/1-lEYgwkYeuHEXyxK6Fga0L6H5fW-LN4bE003jCCcjHc/edit?usp=sharing";
    public final static String googleDockURL_readonly = "https://docs.google.com/spreadsheets/d/1TavANVCMX0dWd8J8wz-jx_CthTm5CTBdxrxuDRm6rWU/edit"; //"https://docs.google.com/spreadsheets/d/1sEkojzoIyxrt8ZI3536483bmjP4rwtxyUFWSxdTU7is/edit?usp=sharing";

    /* 
    * OBS: rekkefølgen kan trygt endres på her, MEN navnene må fortsatt være slik de er. 
    * Rekkefølgen her må være lik som i google sheet.
    */
    public final static List<String> rekkefolgeKolonnerInput = Arrays.asList("vektklasse", "kroppsvekt", 
        "kategori", "femkampkategori", "fodselsdato", "navn", "lag", "rykk1", "rykk2", "rykk3", "stot1", 
        "stot2", "stot3", "3-hopp1", "3-hopp2", "3-hopp3", "kule1", "kule2", "kule3", "40-meter1", "40-meter2");

    public final static int antallRaderSomLeses = 25;


    public final static List<String> rekkefolgeKolonnerOutput = Arrays.asList("navn", "klubb", "femkampkategori",
        "rykk", "stot", "3-hopp", "kule", "40-meter", "poeng", "rank", "nodevendig for ledelse");

}
