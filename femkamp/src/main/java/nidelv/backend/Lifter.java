package nidelv.backend;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.google.common.cache.Cache;

public class Lifter {

    private final String pulje;
    private final int id;
    private String vektklasse, name, kat, katFemkamp, lag, fodselsdato;
    private double kroppsvekt, treHopp, kulekast, m40Sprint;
    private int rykk1, rykk2, rykk3, stot1, stot2, stot3;

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
        this.vektklasse = (String) data.get(0);
        this.kroppsvekt = convertObjToDouble(data.get(1));
        this.kat = (String) data.get(2);
        this.katFemkamp = (String) data.get(3);
        this.fodselsdato = (String) data.get(4);
        this.name = (String) data.get(5);
        this.lag = (String) data.get(6);
        this.rykk1 = Math.max(0,convertObjToInt(data.get(7)));
        this.rykk2 = Math.max(0, convertObjToInt(data.get(8)));
        this.rykk3 = Math.max(0, convertObjToInt(data.get(9)));
        this.stot1 = Math.max(0, convertObjToInt(data.get(10)));
        this.stot2 = Math.max(0, convertObjToInt(data.get(11)));
        this.stot3 = Math.max(0, convertObjToInt(data.get(12)));
        this.treHopp = convertObjToDouble(data.get(13));
        this.kulekast = convertObjToDouble(data.get(14));
        this.m40Sprint = convertObjToDouble(data.get(15));
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

    public String getLag() {
        return this.lag;
    }

    public void setLag(String lag) {
        this.lag = lag;
    }

    public int getRykk1() {
        return this.rykk1;
    }


    public int getRykk2() {
        return this.rykk2;
    }


    public int getRykk3() {
        return this.rykk3;
    }


    public int getStot1() {
        return this.stot1;
    }


    public int getStot2() {
        return this.stot2;
    }


    public int getStot3() {
        return this.stot3;
    }


    public double getTreHopp() {
        return this.treHopp;
    }

    public void setTreHopp(double treHopp) {
        this.treHopp = treHopp;
    }

    public double getKulekast() {
        return this.kulekast;
    }


    public double getM40Sprint() {
        return this.m40Sprint;
    }


    public String getName() {
        return this.name;
    }

    public int getTotal() {
        int bestRykk = Math.max(rykk1, Math.max(rykk2, rykk3));
        int bestStot = Math.max(stot1, Math.max(stot2, stot3));

        if (bestRykk==0 || bestStot==0) return 0;
        return bestRykk+bestStot;
    }
    



    @Override
    public String toString() {
        return "{" +
            ", name='" + getName() + "'" +
            ", total='" + getTotal();
   
    }

    
}
