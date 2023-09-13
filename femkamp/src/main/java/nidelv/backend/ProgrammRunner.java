package nidelv.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nidelv.backend.Resultat.Lifter;

public class ProgrammRunner {
    ManageData manageData;
    List<Pulje> puljer;

    public void runProgram() throws IOException, GeneralSecurityException {

        manageData = new ManageData();
        manageData.createMissingOutputSheets();
        puljer = manageData.getPuljer();


        int iterationNumber = 0;
        while (true) {
            iteration();

            iterationNumber++;
            System.out.println(iterationNumber);
        }
    }

    private void iteration() throws IOException {
  
        puljer.forEach(p -> {
            try {
                addLinesToOutPut(p);
            } catch (IOException e) {
                throw new writingToOutpurIssue("feil ved skriving til fil");
            }
        });


        try {
            GoogleDockReaderAndWriter.writeErrorAndOutputToFiles();
        } catch (IOException e) {
            throw new writingToOutpurIssue("feil ved skriving til fil");
        }
            
        // deleting outputData
        GoogleDockReaderAndWriter.deletInputSheetData();
        GoogleDockReaderAndWriter.deletOutoutSheetData();
            
        takeBreak(5);

        // laster inn data fra inputSheet og inn i programmet
        manageData.updatePuljer();
    }

    private void addLinesToOutPut(Pulje pulje) throws IOException {
        List<List<Object>> lofterLines = new ArrayList<>();
        // TODO: rewrite: ############################################
        String currentOvelse = pulje.getCurrentOvelse();
        if (currentOvelse.equals("stot"))
            currentOvelse = "støt";
        int indexTilCurrentOvelse = 3;  // default to -1 (not found)
        for (int i = 0; i < Settings.rekkefolgeKolonnerOutput.size(); i++) {
            if (Settings.rekkefolgeKolonnerOutput.get(i).equalsIgnoreCase(currentOvelse)) {
                indexTilCurrentOvelse = i;
                break;
            }
        }                       
        ArrayList<Object> outputLineOne = new ArrayList<Object>(Settings.rekkefolgeKolonnerOutput);
        String obj = (String) outputLineOne.get(indexTilCurrentOvelse);
        obj = "*" + obj;
        // #####################################
        outputLineOne.set(indexTilCurrentOvelse, obj);

        //lofterLines.add(new ArrayList<Object>(Settings.rekkefolgeKolonnerOutput));
        lofterLines.add(outputLineOne);

        for (FemkampKategori femkampKategori : pulje.getFemkampKategoris()) {
            lofterLines.add(Arrays.asList(femkampKategori.toString()));  

            ArrayList<Lifter> lifters = femkampKategori.getLifters();  
            lifters.forEach(lofter -> lofterLines.add(lofter.getOutput())); 
            lofterLines.add(Arrays.asList(""));      
       }
       lofterLines.add(Arrays.asList("* = Gjeldende øvelse"));      
       lofterLines.add(Arrays.asList("Grønn = ingen flere forsøk"));      
       lofterLines.add(Arrays.asList("Rødt = ingen gyldige forsøk"));      

        // legger til output data til readonly filen før skriving
        GoogleDockReaderAndWriter.addOutputSheetData(pulje.getName(), lofterLines);
    }

    public static void takeBreak(int seconds) {
        try {
            int millies = seconds*1000;
            Thread.sleep(millies);
        } catch (InterruptedException e) {
            System.err.println("Sleep was interrupted");
        }
    }


    public static class writingToOutpurIssue extends IllegalArgumentException {
        public writingToOutpurIssue(String message) {
            super(message);
        }
    }
    
}
