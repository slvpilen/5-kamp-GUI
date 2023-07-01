package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nidelv.backend.Settings;


public class Lifter {

    private final String pulje;
    private final int linjeId;
    private String vektklasse, navn, kategori, femkampkategori, lag, fodselsdato, klubb;
    private double kroppsvekt;
    private char kjonn;
    private Collection<Ovelse> ovelser = new ArrayList<>();
    private List<Object> sheetLine;
    private double poeng;

    private String currentOvelse; // oppdateres fra pulje (ikke laget ennå)
    private double nodvendigForAaTaLedelsen;


    public Lifter(final String pulje, int linjeId, List<Object> sheetLine) {
        this.pulje = pulje;
        this.linjeId = linjeId;

        standarizeSheetLine(sheetLine);
        
       
        updateLifterAttributesFromSheetLine(sheetLine);

        createOvelser();
        updatePoeng();

    }

    public void updateLifter(List<Object> sheetLine) {
        standarizeSheetLine(sheetLine);
        updateLifterAttributesFromSheetLine(sheetLine); 
        updateOvelserOgPoeng();
    }


    private void standarizeSheetLine(List<Object> sheetLine) {
        int antallKolonner = Settings.rekkefolgeKolonnerInput.size();

        while(sheetLine.size()<antallKolonner)
            sheetLine.add(null);

    }


    // finn bedre navn for denne metoden
    private void updateLifterAttributesFromSheetLine(List<Object> sheetLine) {

        //validateLofterInfo(sheetLine);

        String lofterNavn = ValidateAndExtractNavn(sheetLine);
        double kroppsvekt = validateAndExctactKroppsvekt(lofterNavn, sheetLine);
        String kategori = validateAndExtractKategori(lofterNavn, sheetLine);

        setSheetLine(sheetLine);

        this.vektklasse = convertToString(hentLofterInfo("vektklasse"));
        this.kroppsvekt = kroppsvekt;
        this.kategori = kategori;
        this.kjonn = this.kategori.charAt(1);
        this.femkampkategori = convertToString(hentLofterInfo("femkampkategori"));
        this.fodselsdato = convertToString(hentLofterInfo("fodselsdato"));
        this.navn = lofterNavn;
        this.lag = convertToString(hentLofterInfo("lag"));
        //this.klubb = convertToString(hentLofterInfo("klubb"));

    }





    private String ValidateAndExtractNavn(List<Object> sheetLine) {
        String lofterNavn = convertToString(hentLofterInfo("navn", sheetLine));
        validateteNavn(lofterNavn);
    
        return lofterNavn;
    }


    private void validateteNavn(String navn) {
        boolean tomtNavn = navn.equals("");

        if (tomtNavn)
            throw new IllegalLifterDataException("En lofter mangler navn!");
    }


    private double validateAndExctactKroppsvekt(String lofterNavn, List<Object> sheetLine) {

        Object kroppsvekt = hentLofterInfo("kroppsvekt", sheetLine);

        if (kroppsvekt == null) 
            throw new IllegalLifterDataException("Mangler kroppsvekt for lofter med navn: "+ lofterNavn);
        
        try {
            return Ovelse.convertObjToDouble(kroppsvekt);
        } catch (NumberFormatException e) {
            throw new IllegalLifterDataException("kroppsvekt: " + kroppsvekt +  " for lofter med navn " + lofterNavn + " er ikke riktig format");
        }
    }


    private String validateAndExtractKategori(String lofterNavn, List<Object> sheetLine) {
        Object kategori = hentLofterInfo("kategori", sheetLine);

        if (kategori == null)
            throw new IllegalLifterDataException("Mangler kategori for lofter med navn: "+ lofterNavn);  

        String kategoriStreng = convertToString(kategori);   

        if (kategoriStreng.length() != 2)
            throw new IllegalLifterDataException("Feil lengde på kategori for lofter med navn: " + lofterNavn);  
            
        validateKjonn(lofterNavn, kategoriStreng);

        return kategoriStreng;
    }

    private void validateKjonn(String lofterNavn, String kategoriStreng) {
        boolean gyldigKjonn = Arrays.asList('M', 'K').contains(kategoriStreng.charAt(1));

        if (!gyldigKjonn)
            throw new IllegalLifterDataException("kategori til lofter: "+ lofterNavn + " er ugyldig, slutter ikke på M eller K");  

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
            ovelser.add(new Ovelse(ovelseNavn, alleForsok));
        }

        updatePoeng();
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
        this.poeng = Poengberegning.calculateTotalPoeng(ovelser, this);
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
        ovelser.stream().filter(ovelse-> ovelse.getNavn().equals(navn)).findFirst().get().updateBesteResultatOgAlleForsok(alleForsok);
    }



    private Object hentLofterInfo(String type) {
        if (type.equals("poeng"))
             return poeng;

        // samme for rank
        // samme for maa ha for å ta ledelsen     
        return hentLofterInfo(type, this.sheetLine);
    }


    private Object hentLofterInfo(String type, List<Object> sheetLine) {

        boolean validType = Settings.rekkefolgeKolonnerInput.contains(type);
        if (!validType)
            throw new IllegalArgumentException("type : " + type + "er ikke en gyldig type.");

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
    

    public String getPulje() {
        return this.pulje;
    }

    public String getKat() {
        return this.kategori;
    }

    public void setKat(String kategori) {
        this.kategori = kategori;
    }

    public String getfemkampkategori() {
        return this.femkampkategori;
    }

    public void setfemkampkategori(String femkampkategori) {
        this.femkampkategori = femkampkategori;
    }
    public String getNavn() {
        return this.navn;
    }

    public String getLag() {
        return this.lag;
    }

    public double getPoeng() {
        return poeng;
    }

    public char getKjonn() {
        return this.kjonn;
    }

    public double getKroppsvekt() {
        return this.kroppsvekt;
    }


    @Override
    public String toString() {
        return "{" +
            getOutput() + 
            "} ";
    }


    public static class IllegalLifterDataException extends IllegalArgumentException {
        public IllegalLifterDataException(String message) {
            super(message);
        }
    }

    
}







