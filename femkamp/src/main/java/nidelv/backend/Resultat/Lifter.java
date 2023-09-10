package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import nidelv.backend.FemkampKategori;
import nidelv.backend.Settings;


public class Lifter {

    private final String pulje;
    private final int linjeId;
    private String navn, kategori, femkampkategoriNavn, lag;
    private double kroppsvekt;
    private char kjonn;
    private Collection<Ovelse> ovelser = new ArrayList<>();
    private List<Object> sheetLine;
    private double poeng;
    private int rank;
    private FemkampKategori femkampKategori;

    private String errorMessage;

    private boolean underkjenntOvelse;
    private String currentOvelse; // oppdateres fra pulje (ikke laget ennå)
    private Double nodvendigForLedelsen, nodvendigForAndre, nodvendigForTredje;


    public Lifter(final String pulje, int linjeId, List<Object> sheetLine, String currentOvelse) {
        this.pulje = pulje;
        this.linjeId = linjeId;
        setCurrentOvelse(currentOvelse);

        standarizeSheetLine(sheetLine);
        
       
        updateLifterAttributesFromSheetLine(sheetLine);

        createOvelser();
        updateUnderkjent();
        updatePoeng();
    }

    private void setCurrentOvelse(String currentOvelse) {
        if (currentOvelse == null) {
            this.currentOvelse = "rykk";
        }
        else {
            this.currentOvelse = currentOvelse;
        }
    }

    private void standarizeSheetLine(List<Object> sheetLine) {
        int antallKolonner = Settings.rekkefolgeKolonnerInput.size();

        while(sheetLine.size()<antallKolonner)
            sheetLine.add(null);
    }

    // finn bedre navn for denne metoden
    private void updateLifterAttributesFromSheetLine(List<Object> sheetLine) {
        //nullstillErrormelding();

        String lofterNavn = ValidateAndExtractNavn(sheetLine);
        this.navn = lofterNavn;


        // validating femkampkategori
        if (femkampKategori!= null && !this.femkampkategoriNavn.equals(femkampKategori.getName()))
            addErrorMessage("Feil femkampkategori for: " + lofterNavn);

        double kroppsvekt = validateAndExctactKroppsvekt(lofterNavn, sheetLine);
        String kategori = validateAndExtractKategori(lofterNavn, sheetLine);

        setSheetLine(sheetLine);

        this.kroppsvekt = kroppsvekt;
        this.kategori = kategori;

        if (this.kategori.length() == 2) {
            if (Arrays.asList('M', 'K').contains(kategori.charAt(1)))
                this.kjonn = this.kategori.charAt(1);
            else
                this.kjonn = this.kategori.charAt(0);
        }

        this.femkampkategoriNavn = convertToString(hentLofterInfo("kat 5-kamp"));
        this.lag = convertToString(hentLofterInfo("lag"));

    }

    public void updateLifter(List<Object> sheetLine, String currentOvelse) {
        nullstillErrormelding();
        setCurrentOvelse(currentOvelse);
        standarizeSheetLine(sheetLine);
        updateLifterAttributesFromSheetLine(sheetLine); 
        updateOvelserOgPoeng();
        updateUnderkjent();
    }

    private void updateUnderkjent() {
        if (isAnyUnderkjennt())
            this.underkjenntOvelse = true;
        else
            this.underkjenntOvelse = false;
    }

    public boolean isAnyUnderkjennt() {
        return ovelser.stream().anyMatch(Ovelse::isUnderkjennt);
    }


    private void nullstillErrormelding() {
        this.errorMessage = null;
    }



    private String ValidateAndExtractNavn(List<Object> sheetLine) {
        String lofterNavn = convertToString(hentLofterInfo("navn", sheetLine));
        validateteNavn(lofterNavn);
    
        return lofterNavn;
    }


    private void validateteNavn(String navn) {
        boolean tomtNavn = navn.equals("");

        if (tomtNavn)
            addErrorMessage("En lofter mangler navn!");
    }


    private double validateAndExctactKroppsvekt(String lofterNavn, List<Object> sheetLine) {

        Object kroppsvekt = hentLofterInfo("kroppsvekt", sheetLine);

        if (kroppsvekt == null || kroppsvekt.equals(("")))
            addErrorMessage("Mangler kroppsvekt");
        
        try {
            double kroppsvektDouble = Ovelse.convertObjToDouble(kroppsvekt, this);
            if (kroppsvektDouble < 10)
                addErrorMessage("kroppsvekt: " + kroppsvekt +  "kg er usansynelig lav!");
            return kroppsvektDouble;
        } catch (NumberFormatException e) {
            addErrorMessage("kroppsvekt: " + kroppsvekt +  "er ikke riktig format");
            return 0;
        }
    }


    private String validateAndExtractKategori(String lofterNavn, List<Object> sheetLine) {
        Object kategori = hentLofterInfo("kategori", sheetLine);

        if (kategori == null)
            addErrorMessage(" Mangler kategori for lofter ");  

        String kategoriStreng = convertToString(kategori);   

        if (kategoriStreng.length() != 2)
            addErrorMessage("Feil lengde på kategori");  
            
        validateKjonn(lofterNavn, kategoriStreng);

        return kategoriStreng;
    }

    private void validateKjonn(String lofterNavn, String kategoriStreng) {
        boolean gyldigKjonn = kategoriStreng.length() == 2 && (Arrays.asList('M', 'K').contains(kategoriStreng.charAt(1)) || Arrays.asList('M', 'K').contains(kategoriStreng.charAt(0)));

        if (!gyldigKjonn)
            addErrorMessage(" er ugyldig, slutter ikke på M eller K");  

    }

    public void addErrorMessage(String errorMessage) {
        if (this.errorMessage == null)
            this.errorMessage = "navn: " + getNavn() + "; " + errorMessage;
    }

    private String convertToString(Object obj) {
        if (obj instanceof String) 
            return (String) obj;

        if (obj == null) 
            return "";
        
        throw new IllegalArgumentException("Objektet er ikke en streng: " + obj);
    }

    private void setSheetLine(List<Object> sheetLine) {
        this.sheetLine = sheetLine;
    }

    private void createOvelser() {

        for (String ovelseNavn : Ovelse.validOvelser) {
            List<Object> alleForsok = finnAlleForsok(ovelseNavn);
            Ovelse ovelse = new Ovelse(ovelseNavn, alleForsok, this);
            ovelser.add(ovelse);
        }
    }

    private List<Object> finnAlleForsok(String ovelseNavn) {
        ArrayList<Object> forsok = new ArrayList<>();
        List<Integer> indekser = finnIndekser(ovelseNavn);

        for (int index : indekser) {
            forsok.add(sheetLine.get(index));
        }

        return forsok;
    }

    private List<Integer> finnIndekser(String ovelseNavn) {
        ArrayList<Integer> indekserTilOvelse = new ArrayList<>();

        for(int i = 0; i < Settings.rekkefolgeKolonnerInput.size(); i++) {
            if(Settings.rekkefolgeKolonnerInput.get(i).contains(ovelseNavn)) 
            indekserTilOvelse.add(i);
        }

        return indekserTilOvelse;
    }


    private void updatePoeng() {
        updatePoengForAlleOvelser();
        this.poeng = ovelser.stream().mapToDouble(ovelse -> ovelse.getPoeng()).sum();
    }

    private void updatePoengForAlleOvelser() {
        ovelser.forEach(ovelse -> ovelse.updatePoeng());
    }


    public int getId() {
        return linjeId;
    }


    private void updateOvelserOgPoeng() {
        for (String ovelseNavn : Ovelse.validOvelser) {
            List<Object> alleForsok = finnAlleForsok(ovelseNavn);

            updateOvelse(ovelseNavn, alleForsok);
        }
        updatePoeng();
    }


    private void updateOvelse(String navn, List<Object> alleForsok){
        getOvelse(navn).updateBesteResultatOgAlleForsok(alleForsok, this);
    }



    private Object hentLofterInfo(String type) {
        type = type.toLowerCase();
        if (type.equals("poeng")) {
            if (isUnderkjennt())
                return "--";
             return poeng;
        }

        if (type.equals("rank")) {
            if (isUnderkjennt())
                return "--";
            if (getPoeng() == 0)
                return "";
            return rank;
        }
        
        if (type.equals("1. plass")) {
            if (isUnderkjennt())
                return "--";
            if (nodvendigForLedelsen == null || nodvendigForLedelsen==0)
                return "";
            return nodvendigForLedelsen;
        }

        if (type.equals("2. plass")) {
            if (isUnderkjennt())
                return "--";
            if (nodvendigForAndre == null || nodvendigForAndre==0 || getRank()<=2)
                return "";
            return nodvendigForAndre;
        }

        if (type.equals("3. plass")) {
            if (isUnderkjennt())
                return "--";
            if (nodvendigForTredje == null || nodvendigForTredje==0 || getRank()<=3)
                return "";
            return nodvendigForTredje;
        }
        if (type.equals("støt"))
            type = "stot";
        boolean isOvelse = Ovelse.validOvelser.contains(type);
        if (isOvelse) {
            Ovelse ovelse = getOvelse(type);
            double besteResultet = ovelse.getBesteResultat();

            if (ovelse.isUnderkjennt())
                return "--";

            if (besteResultet==0)
                return "";

            return besteResultet;
        }

        if(type.equals("klubb")) {
            return lag;  // TODO: endre denne attributten til klubb
        }

        return hentLofterInfo(type, this.sheetLine);
    }


    private Object hentLofterInfo(String type, List<Object> sheetLine) {

        boolean validType = Settings.rekkefolgeKolonnerInput.contains(type);
        if (!validType)
            throw new IllegalArgumentException("type : " + type + " er ikke en gyldig type.");

        int index = Settings.rekkefolgeKolonnerInput.indexOf(type);


        return sheetLine.get(index);
    }

    
    public List<Object> getOutput() {
        List<Object> outputLine = new ArrayList<>();

        for (String type : Settings.rekkefolgeKolonnerOutput) {
            Object lineToAdd = hentLofterInfo(type);
            // for å ungå at ting lagres på ulik linje
            if (lineToAdd == null)
                outputLine.add("");
            else
                outputLine.add(hentLofterInfo(type));
        }

        return outputLine;
    }

    public void oppdaterNodvendigForLedelsen(double lederScore) {
        Ovelse ovelse = getOvelse(currentOvelse);
        this.nodvendigForLedelsen = Poengberegning.calculateHvaSomTrengsForLedelse(ovelse, this, lederScore);
    }

    public void oppdaterNodvendigForAndre(double andreScore) {
        Ovelse ovelse = getOvelse(currentOvelse);
        this.nodvendigForAndre = Poengberegning.calculateHvaSomTrengsForLedelse(ovelse, this, andreScore);
    }

    public void oppdaterNodvendigForTredje(double tredjeScore) {
        Ovelse ovelse = getOvelse(currentOvelse);
        this.nodvendigForTredje = Poengberegning.calculateHvaSomTrengsForLedelse(ovelse, this, tredjeScore);
    }
    

    public String getPulje() {
        return this.pulje;
    }

    public String getKat() {
        return this.kategori;
    }

    public void setKat(String kategori) {
        this.kategori = kategori;
    }

    public String getfemkampkategoriNavn() {
        return this.femkampkategoriNavn;
    }

    public void setfemkampkategoriNavn(String femkampkategoriNavn) {
        this.femkampkategoriNavn = femkampkategoriNavn;
    }

    public void setFemkampkategori(FemkampKategori femkampkategori) {
        this.femkampKategori = femkampkategori;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getNavn() {
        return this.navn;
    }

    public String getLag() {
        return this.lag;
    }

    public double getPoeng() {
        if (isUnderkjennt())
            return -1; // TODO: isteden for denne sjekken fix Comparatoren til å sortere alle isUnderkjennt sist
        return poeng;
    }

    public char getKjonn() {
        return this.kjonn;
    }

    public double getKroppsvekt() {
        return this.kroppsvekt;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getRank() {
        return rank;
    }

    public Ovelse getOvelse(String ovelseNavn) {
        Optional<Ovelse> ovelse = ovelser.stream().filter(o -> o.getNavn().equals(ovelseNavn)).findFirst();
        if (ovelse.isPresent())
            return ovelse.get();

        throw new IllegalArgumentException("kan ikke finne ovelse: " + ovelseNavn);
    }

    public void setNodvendigForLedelsen(Double resultat) {
        this.nodvendigForLedelsen = resultat;
    }

    public boolean isUnderkjennt() {
        return underkjenntOvelse;
    }

    @Override
    public String toString() {
        return "{" +
            getOutput() + 
            "} ";
    }


    
}







