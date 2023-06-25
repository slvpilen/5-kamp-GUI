package nidelv.backend;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Lifter {

    private final String pulje;
    private final int id;
    private String vektklasse, name, kat, katFemkamp, lag, fodselsdato;
    private double kroppsvekt;
    private char gender;
    private Map<String, Object> resultater = new HashMap<>();

    private double points;


    public Lifter(final String pulje, int id, List<Object> data) {
        this.pulje = pulje;
        this.id = id;
        updateData(data);
    }

    public int getId() {
        return id;
    }

    public void updateData(List<Object> data) {
        if (data.size()<16){
            while(data.size()<16)
                data.add(null);
        }
        validateDate(data);
        this.vektklasse = (String) data.get(0);
        this.kroppsvekt = convertObjToDouble(data.get(1));
        this.kat = (String) data.get(2);
        this.gender = this.kat.charAt(1);
        this.katFemkamp = (String) data.get(3);
        this.fodselsdato = (String) data.get(4);
        this.name = (String) data.get(5);
        this.lag = (String) data.get(6);

        resultater.put("TreHopp", convertObjToDouble(data.get(13)));  
        resultater.put("Kulekast", convertObjToDouble(data.get(14)));  
        resultater.put("40Sprint", convertObjToDouble(data.get(15))); 

        resultater.put("Rykk1", Math.max(0,convertObjToInt(data.get(7)))); 
        resultater.put("Rykk2", Math.max(0, convertObjToInt(data.get(8))));  
        resultater.put("Rykk3", Math.max(0, convertObjToInt(data.get(9))));  

        resultater.put("Stot1", Math.max(0, convertObjToInt(data.get(10))));  
        resultater.put("Stot2", Math.max(0, convertObjToInt(data.get(11)))); 
        resultater.put("Stot3", Math.max(0, convertObjToInt(data.get(12)))); 

        this.points = Poengberegning.calculateTotalPoints(resultater, gender, kroppsvekt);
    }

    private void validateDate(List<Object> data) {

        Object kroppsvekt = data.get(1);
        if (kroppsvekt == null) 
            throw new IllegalLifterDataException("Mangler kroppsvekt for lofter med navn: "+ data.get(5));
        
        try {
            Double.parseDouble( (String) kroppsvekt);
        } catch (NumberFormatException e) {
            throw new IllegalLifterDataException("kroppsvekt: " + kroppsvekt +  " for lofter med navn " + data.get(5) + " er ikke riktig format");
        }
        

        Object kategori = data.get(2);
        if (kategori == null)
            throw new IllegalLifterDataException("Mangler kategori for lofter med navn: "+ data.get(5));  

        String kategoriStreng = (String) kategori;   

        if (kategoriStreng.length() != 2)
            throw new IllegalLifterDataException("Feil lengdde på kategori for lofter med navn: "+ data.get(5));  
        
        boolean gyldigKjonn = Arrays.asList('M', 'K').contains(kategoriStreng.charAt(1));
        if (!gyldigKjonn)
            throw new IllegalLifterDataException("kat til lofter: "+ data.get(5) + " er ugyldig, slutter ikke på M eller K");  



        for (int i = 7; i <= 12; i++) {
            Object lift = data.get(i);
            if (lift != null) {
                try{
                    int lifte = Integer.parseInt((String) lift);
                } catch (NumberFormatException e) {
                    throw new IllegalLifterDataException("loft: " + lift +  " for lofter med navn " + data.get(5) +  " er ikke riktig format");
                }
            }  
        }

        for (int i = 13; i <= 15; i++) {
            Object trekampResultat = data.get(i);
            if (trekampResultat != null) {
                try {
                    Double.parseDouble( (String) trekampResultat);
                } catch (NumberFormatException e) {
                    throw new IllegalLifterDataException("trekampresulktat: " + trekampResultat +  " for lofter med navn " + data.get(5) +  " er ikke riktig format");
                }
            }

            // 15 er 40-meter
            if (i == 15 && trekampResultat != null) {
                int numberOfDecimals = countDecimalPlaces((String) trekampResultat);
                if (numberOfDecimals > 1)
                    throw new IllegalLifterDataException("40 meter skal rundes opp til 1/10! " + trekampResultat +  " for lofter med navn " + data.get(5) +  " opfylller ikke dette!");                
            }
         }
    }
    public static int countDecimalPlaces(String numStr) {
        int decimalPlaces = 0;
        int index = numStr.indexOf(".");
        if (index != -1) {
            decimalPlaces = numStr.length() - index - 1;
        }
        return decimalPlaces;
}


    private double convertObjToDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof String) {
            try {
                return Double.valueOf((String) obj);
            } catch (NumberFormatException e) {
                //System.out.println("The object cannot be converted to Double");
                return 0.0;
            }
        } else {
            //System.out.println("The object is not a String and cannot be converted to Double");
            return 0.0;
        }
    }

    private int convertObjToInt(Object obj) {
        if (obj instanceof String) {
            try {
                return Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                //System.out.println("The object cannot be converted to int");
                return 0;
            }
        } else {
            //System.out.println("The object is not a String and cannot be converted to int");
            return 0;
        }
    }


       
                      
    
    public static String getNameFrom(List<Object> data) {
        if (data.size()<6) throw new IllegalArgumentException("dataLine doest contain a name");
        return (String) data.get(5);
    }

    public String getPulje() {
        return this.pulje;
    }

    public String getKat() {
        return this.kat;
    }

    public void setKat(String kat) {
        this.kat = kat;
    }

    public String getKatFemkamp() {
        return this.katFemkamp;
    }

    public void setKatFemkamp(String katFemkamp) {
        this.katFemkamp = katFemkamp;
    }
    public String getName() {
        return this.name;
    }

    public String getLag() {
        return this.lag;
    }



    // public int getTotal() {
    //     int bestRykk = Math.max((int) resultater.get("rykk1"), Math.max((int) resultater.get("rykk2"), (int) resultater.get("rykk3")));
    //     int bestStot = Math.max((int) resultater.get("stot1"), Math.max((int) resultater.get("stot2"), (int) resultater.get("stot3")));

    //     if (bestRykk==0 || bestStot==0) return 0;
    //     return bestRykk+bestStot;
    // }
    



    @Override
    public String toString() {
        return "{" +
            "name='" + getName() + "'" +
            ", total='" + //getTotal() +
            "}";
   
    }


    public static class IllegalLifterDataException extends IllegalArgumentException {
        public IllegalLifterDataException(String message) {
            super(message);
        }
    }

    
}







