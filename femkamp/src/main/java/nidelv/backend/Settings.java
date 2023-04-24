package nidelv.backend;

public class Settings {
    private String googleDockURL;

    public Settings(String googleDockURL) {
        this.googleDockURL = googleDockURL;
        saveToFile();
    }
    
    public void setGoogleDockURL(String googleDockURL) {
        this.googleDockURL = googleDockURL;
        saveToFile();
    }

    private void saveToFile(){

    }


    public String googleDocURL() {
        return this.googleDockURL;
    }
    
}
