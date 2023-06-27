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
    private List<Object> sheetLine;

    private double poeng;


    public Lifter(final String pulje, int linjeId, List<Object> sheetLine) {
        this.pulje = pulje;
        this.linjeId = linjeId;

        standarizeSheetLine(sheetLine);
        // obs burde egt validert først, men bruker metode som henter fra this.shetLine
        setSheetLine(sheetLine);
        validateLofterInfo();

        updateLifterData();
        createOvelser();
        updatePoeng();

    }


    private void standarizeSheetLine(List<Object> sheetLine) {
        if (sheetLine.size()<16){
            while(sheetLine.size()<16)
                sheetLine.add(null);
        }
    }

    public void setSheetLine(List<Object> sheetLine) {
        this.sheetLine = sheetLine;
    }


    // finn bedre navn for denne metoden
    private void updateLifterData() {
        validateLofterInfo();

        this.vektklasse = (String) hentLofterInfo("vektklasse");
        this.kroppsvekt = Ovelse.convertObjToDouble(hentLofterInfo("kroppsvekt"));
        this.kategori = (String) hentLofterInfo("kategori");
        this.kjonn = this.kategori.charAt(1);
        this.femkampkategori = (String) hentLofterInfo("femkampkategori");
        this.fodselsdato = (String) hentLofterInfo("fodselsdato");
        this.nvn = (String) hentLofterInfo("navn");
        this.lag = (String) hentLofterInfo("lag");

    }

    private void createOvelser() {
        for (String ovelseNavn : Ovelse.validOvelser) {

            int kolonneIndexTilOvelse = Settings.rekkefolgeKolonner.indexOf(ovelseNavn);
            ovelser.add(new Ovelse(ovelseNavn, sheetLine.get(kolonneIndexTilOvelse)));

        }
    }


    public int getId() {
        return linjeId;
    }




    public void updateLifter() {
        updateLifterData(); 
        updateOvelser();
        updatePoeng();
    }

    private void updateOvelser() {
        for (String ovelseNavn : Ovelse.validOvelser) {

            int kolonneIndexTilOvelse = Settings.rekkefolgeKolonner.indexOf(ovelseNavn);
            updateOvelse(ovelseNavn, sheetLine.get(kolonneIndexTilOvelse));
        }
    }


    private void updateOvelse(String navn, Object ovelseOppdatert){
        ovelser.stream().filter(ovelse-> ovelse.getNavn().equals(navn)).findFirst().get().setResultat(ovelseOppdatert);
    }



    private void updatePoeng() {
        this.poeng = Poengberegning.calculateTotalPoeng(ovelser, kjonn, kroppsvekt);
    }

    private Object hentLofterInfo(String type) {
        boolean validType = Settings.rekkefolgeKolonner.contains(type);
        if (!validType)
            throw new IllegalArgumentException("type : " + type + "er ikke en gyldig type.");

        int index = Settings.rekkefolgeKolonner.indexOf(type);
        return sheetLine.get(index);
    }


    private void validateLofterInfo() {

        String lofterNavn = (String) hentLofterInfo("navn");
        boolean tomtNavn = lofterNavn.length() == 0;

        lofterNavn = "'" + lofterNavn + "'";
        if (tomtNavn)
            lofterNavn = "'@NO_NAME'";
        
        validateKroppsvekt(lofterNavn);

        validateKategori(lofterNavn);
  
    }
       
                    
    private void validateKroppsvekt(String lofterNavn) {
        Object kroppsvekt = hentLofterInfo("kroppsvekt");
        if (kroppsvekt == null) 
            throw new IllegalLifterDataException("Mangler kroppsvekt for lofter med navn: "+ lofterNavn);
        
        try {
            Double.parseDouble( (String) kroppsvekt);
        } catch (NumberFormatException e) {
            throw new IllegalLifterDataException("kroppsvekt: " + kroppsvekt +  " for lofter med navn " + lofterNavn + " er ikke riktig format");
        }
    }

    private void validateKategori(String lofterNavn) {
        Object kategori = hentLofterInfo("kategori");
        if (kategori == null)
            throw new IllegalLifterDataException("Mangler kategori for lofter med navn: "+ lofterNavn);  

        String kategoriStreng = (String) kategori;   

        if (kategoriStreng.length() != 2)
            throw new IllegalLifterDataException("Feil lengde på kategori for lofter med navn: " + lofterNavn);  
            
        validateKjonn(lofterNavn, kategoriStreng);
    }

    private void validateKjonn(String lofterNavn, String kategoriStreng) {
        boolean gyldigKjonn = Arrays.asList('M', 'K').contains(kategoriStreng.charAt(1));
        if (!gyldigKjonn)
            throw new IllegalLifterDataException("kategori til lofter: "+ lofterNavn + " er ugyldig, slutter ikke på M eller K");  

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







