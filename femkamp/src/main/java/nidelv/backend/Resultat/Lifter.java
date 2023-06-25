package nidelv.backend.Resultat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nidelv.backend.Settings;


public class Lifter {

    private final String pulje;
    private final int linjeId;
    private String vektklasse, nvn, kategori, femkampkategori, lag, fodselsdato;
    private double kroppsvekt;
    private char kjonn;
    private Collection<Ovelse> ovelser = new ArrayList<>();

    private double poeng;


    public Lifter(final String pulje, int linjeId, List<Object> sheetLine) {
        this.pulje = pulje;
        this.linjeId = linjeId;

        setLifterData(sheetLine);
        createOvelser(sheetLine);

    }

    public int getId() {
        return linjeId;
    }

    // finn bedre navn for denne metoden
    private void setLifterData(List<Object> sheetLine) {
        standarizeSheetLine(sheetLine);
        validateLine(sheetLine);

        this.vektklasse = (String) hentLofterInfo("vektklasse", sheetLine);
        this.kroppsvekt = Ovelse.convertObjToDouble(hentLofterInfo("kroppsvekt", sheetLine));
        this.kategori = (String) hentLofterInfo("kategori", sheetLine);
        this.kjonn = this.kategori.charAt(1);
        this.femkampkategori = (String) hentLofterInfo("femkampkategori", sheetLine);
        this.fodselsdato = (String) hentLofterInfo("fodselsdato", sheetLine);
        this.nvn = (String) hentLofterInfo("navn", sheetLine);
        this.lag = (String) hentLofterInfo("lag", sheetLine);

    }



    public void updateLifter(List<Object> sheetLine) {
        setLifterData(sheetLine); 
        updateOvelser(sheetLine);
    }

    private void updateOvelser(List<Object> sheetLine) {
        for (String ovelseNavn : Ovelse.validOvelser) {

            int kolonneIndexTilOvelse = Settings.rekkefolgeKolonner.indexOf(ovelseNavn);
            updateOvelse(ovelseNavn, sheetLine.get(kolonneIndexTilOvelse));
            
        }

        this.poeng = Poengberegning.calculateTotalPoeng(ovelser, kjonn, kroppsvekt);
    }


    private void updateOvelse(String navn, Object ovelseOppdatert){
        ovelser.stream().filter(ovelse-> ovelse.getNavn().equals(navn)).findFirst().get().setResultat(ovelseOppdatert);
    }

    private void standarizeSheetLine(List<Object> sheetLine) {
        if (sheetLine.size()<16){
            while(sheetLine.size()<16)
                sheetLine.add(null);
        }
    }

    private void createOvelser(List<Object> sheetLine) {
        for (String ovelseNavn : Ovelse.validOvelser) {

            int kolonneIndexTilOvelse = Settings.rekkefolgeKolonner.indexOf(ovelseNavn);
            ovelser.add(new Ovelse(ovelseNavn, sheetLine.get(kolonneIndexTilOvelse)));

        }

        this.poeng = Poengberegning.calculateTotalPoeng(ovelser, kjonn, kroppsvekt);
    }

    private Object hentLofterInfo(String type, List<Object> sheetLine) {
        boolean validType = Settings.rekkefolgeKolonner.contains(type);
        if (!validType)
            throw new IllegalArgumentException("type : " + type + "er ikke en gyldig type.");

        int index = Settings.rekkefolgeKolonner.indexOf(type);
        return sheetLine.get(index);
    }


    private void validateLine(List<Object> sheetLine) {

        String navn = (String) hentLofterInfo("navn", sheetLine);
        boolean tomtNavn = navn.length() == 0;
        navn = "'" + navn + "'";
        if (tomtNavn)
            navn = "'@NO_NAME'";

        Object kroppsvekt = hentLofterInfo("kroppsvekt", sheetLine);
        if (kroppsvekt == null) 
            throw new IllegalLifterDataException("Mangler kroppsvekt for lofter med navn: "+ navn);
        
        try {
            Double.parseDouble( (String) kroppsvekt);
        } catch (NumberFormatException e) {
            throw new IllegalLifterDataException("kroppsvekt: " + kroppsvekt +  " for lofter med navn " + navn + " er ikke riktig format");
        }
        

        Object kategori = hentLofterInfo("kategori", sheetLine);
        if (kategori == null)
            throw new IllegalLifterDataException("Mangler kategori for lofter med navn: "+ navn);  

        String kategoriStreng = (String) kategori;   

        if (kategoriStreng.length() != 2)
            throw new IllegalLifterDataException("Feil lengde på kategori for lofter med navn: " + navn);  
        
        boolean gyldigKjonn = Arrays.asList('M', 'K').contains(kategoriStreng.charAt(1));
        if (!gyldigKjonn)
            throw new IllegalLifterDataException("kategori til lofter: "+ navn + " er ugyldig, slutter ikke på M eller K");  

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
        return this.nvn;
    }

    public String getLag() {
        return this.lag;
    }

    public double getPoeng() {
        return poeng;
    }


    @Override
    public String toString() {
        return "{" +
            "name='" + getNavn() + "'" +
            ", poeng='" + poeng +
            "} ";
   
    }


    public static class IllegalLifterDataException extends IllegalArgumentException {
        public IllegalLifterDataException(String message) {
            super(message);
        }
    }

    
}







