package nidelv.backend;

import java.util.Arrays;
import java.util.List;

public class Settings {
    public final static String googleDockURL_plotting = "https://docs.google.com/spreadsheets/d/1-lEYgwkYeuHEXyxK6Fga0L6H5fW-LN4bE003jCCcjHc/edit#gid=0";
    public final static String googleDockURL_readonly = "https://docs.google.com/spreadsheets/d/e/2PACX-1vRm7A8VVgMaKdAlVw0ow7ebS6vYlyHEl65scwPQJTu3aC-CakM3JqloZPD8kMqIcC9b0cfgPUFgS6t8/pubhtml";

    // OBS: rekkefølgen kan trygt endres på her, MEN navnene må fortsatt være slik de er. 
    // Rekkefølgen her må være lik som i google sheet.
    public final static List<String> rekkefolgeKolonner = Arrays.asList("vektklasse", "kroppsvekt", "kategori", "femkampkategori", "fodselsdato", "navn", "lag", 
        "rykk1", "rykk2", "rykk3", "stot1", "stot2", "stot3", "treHopp", "kuleKast", "40Sprint");
}
