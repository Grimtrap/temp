import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class PhoneticTranslator {

    private final String URL = "https://api.datamuse.com/words?sp=";
    private final String URL2 = "&md=r&ipa=1&max=1";


    public String phoneticPronounciation(String search) throws IOException {
        java.net.URL wordURL = new URL(URL + search + URL2); //wynnapi url
        URLConnection yc = wordURL.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine;
        inputLine = in.readLine();

        Gson gson = new Gson();

    }

}