package ophelia.logic;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class UpdateComponent {

    private final String versionCheckURL = "http://wkjeldsen.dk/ophelia/Ophelia.version";

    public boolean isNewVersion() {
        try {
            URL url = new URL(versionCheckURL);
            URLConnection urlc = url.openConnection();
            DataInputStream in = new DataInputStream(urlc.getInputStream()); // To download
            BufferedReader read = new BufferedReader(new InputStreamReader(in));
            if (!read.readLine().equals(Settings.getInstance().getOpheliaVersion())) {
                return true;
            }
            read.close();
        } catch (Exception ex) {
            return false;
        }
        return false;
    }
}
