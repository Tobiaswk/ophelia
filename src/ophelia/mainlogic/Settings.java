/* 
Ophelia, the cat! is a lightweight mediaplayer written in Java. The main goal 
is(and was) to create a very light and fast mediaplayer 
with the most wanted features.

Copyright (C) 2008 Tobias W. Kjeldsen; tobias@wkjeldsen.dk

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ophelia.mainlogic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class Settings {

    /* singleton-pattern used to ensure 1 instance ALWAYS */
    private static Settings singleton;
    /* data from configuration-file stored in this string */
    private String configurationData;

    /* last.fm configuration data */
    private final String opheliaVersion = "0.1";
    private final String clientID = "oph"; //this clientid provided by last.fm
    private boolean lastfmScrobble = false;
    private String lasfmUsername = "";
    private String lasfmPassword = "";

    /* ophelia configuration data */
    private String defaultPlaylistName = "playlist.txt";
    private boolean trayIcon = false;
    private boolean trayMinimize = false;
    private boolean trayClose = false;
    private String windowTitleText = "Ophelia, the cat!";
    private boolean trackInWindowTitle = false;
    private String configurationFileName = "settings.ini";
    /* a little easteregg ;) */
    private boolean easterEgg = false;

    public static Settings getInstance() {
        if (singleton == null) {
            singleton = new Settings();
        }
        return singleton;
    }

    private Settings() {
        if (importLocalSettings()) {
            setupConfiguration();
        } else {
            setupNewConfiguration();
        }
    }

    public boolean isLastfmScrobble() {
        return lastfmScrobble;
    }

    public void setLastfmScrobble(boolean LastfmScrobble) {
        this.lastfmScrobble = LastfmScrobble;
        eksportLocalSettings();
    }

    public String getDefaultPlaylistName() {
        return defaultPlaylistName;
    }

    public void setDefaultPlaylistName(String defaultPlaylistName) {
        this.defaultPlaylistName = defaultPlaylistName;
        eksportLocalSettings();
    }

    public String getLasfmPassword() {
        return lasfmPassword;
    }

    public void setLasfmPassword(String lasfmPassword) {
        this.lasfmPassword = lasfmPassword;
        eksportLocalSettings();
    }

    public String getLasfmUsername() {
        return lasfmUsername;
    }

    public void setLasfmUsername(String lasfmUsername) {
        this.lasfmUsername = lasfmUsername;
        eksportLocalSettings();
    }

    public boolean isTrayClose() {
        return trayClose;
    }

    public void setTrayClose(boolean trayClose) {
        this.trayClose = trayClose;
        eksportLocalSettings();
    }

    public boolean isTrayIcon() {
        return trayIcon;
    }

    public void setTrayIcon(boolean trayIcon) {
        this.trayIcon = trayIcon;
        eksportLocalSettings();
    }

    public boolean isTrayMinimize() {
        return trayMinimize;
    }

    public void setTrayMinimize(boolean trayMinimize) {
        this.trayMinimize = trayMinimize;
        eksportLocalSettings();
    }

    public boolean isTrackInWindowTitle() {
        return trackInWindowTitle;
    }

    public void setTrackInWindowTitle(boolean trackInWindowTitle) {
        this.trackInWindowTitle = trackInWindowTitle;
        eksportLocalSettings();
    }

    public String getWindowTitleText() {
        return windowTitleText;
    }

    public void setWindowTitleText(String windowTitleText) {
        this.windowTitleText = windowTitleText;
        eksportLocalSettings();
    }

    public boolean isEasterEgg() {
        return easterEgg;
    }

    public String getClientID() {
        return clientID;
    }

    public String getOpheliaVersion() {
        return opheliaVersion;
    }

    public String getSetting(String setting_name) throws Exception {
        String xmlString = new String(configurationData);
        String v = null;
        String beginTagToSearch = "<" + setting_name + ">";
        String endTagToSearch = "</" + setting_name + ">";

        // Look for the first occurrence of begin tag
        int index = xmlString.indexOf(beginTagToSearch);
        while (index != -1) {
            // Look for end tag
            // DOES NOT HANDLE <section Blah />
            int lastIndex = xmlString.indexOf(endTagToSearch);

            // Make sure there is no error
            if ((lastIndex == -1) || (lastIndex < index)) {
                throw new Exception("Parse Error");
            }
            // extract the substring
            String subs = xmlString.substring((index + beginTagToSearch.length()), lastIndex);

            // Add it to our list of tag values
            v = subs;

            // Try it again. Narrow down to the part of string which is not
            // processed yet.
            try {
                xmlString = xmlString.substring(lastIndex + endTagToSearch.length());
            } catch (Exception e) {
                xmlString = "";
            }

            // Start over again by searching the first occurrence of the begin tag
            // to continue the loop.
            index = xmlString.indexOf(beginTagToSearch);
        }
        return v;
    }

    private void setupNewConfiguration() {
        configurationData =
                "[Ophelia, the cat! configuration file]\n" +
                "<opheliaVersion>" + opheliaVersion + "</opheliaVersion>\n" +
                "<clientID>" + clientID + "</clientID>\n" +
                "<lastfmScrobble>" + lastfmScrobble + "</lastfmScrobble>\n" +
                "<lastfmUsername>" + lasfmUsername + "</lastfmUsername>\n" +
                "<lastfmPassword>" + lasfmPassword + "</lastfmPassword>\n" +
                "<defaultPlaylistName>" + defaultPlaylistName + "</defaultPlaylistName>\n" +
                "<trayIcon>" + trayIcon + "</trayIcon>\n" +
                "<trayMinimize>" + trayMinimize + "</trayMinimize>\n" +
                "<trayClose>" + trayClose + "</trayClose>\n" +
                "<windowTitleText>" + windowTitleText + "</windowTitleText>\n" +
                "<trackInWindowTitle>" + trackInWindowTitle + "</trackInWindowTitle>";
    }

    public void setupConfiguration() {
        try {
            this.lastfmScrobble = Boolean.parseBoolean(getSetting("lastfmScrobble"));
            this.lasfmUsername = getSetting("lastfmUsername");
            this.lasfmPassword = getSetting("lastfmPassword");
            this.defaultPlaylistName = getSetting("defaultPlaylistName");
            this.trayIcon = Boolean.parseBoolean(getSetting("trayIcon"));
            this.trayMinimize = Boolean.parseBoolean(getSetting("trayMinimize"));
            this.trayClose = Boolean.parseBoolean(getSetting("trayClose"));
            this.windowTitleText = getSetting("windowTitleText");
            this.trackInWindowTitle = Boolean.parseBoolean(getSetting("trackInWindowTitle"));
            this.easterEgg = Boolean.parseBoolean(getSetting("easterEgg"));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }

    private boolean importLocalSettings() {
        configurationData = "";
        try {
            BufferedReader read = new BufferedReader(new FileReader(new File(configurationFileName)));
            while (read.ready()) {
                configurationData += read.readLine() + "\n";
            }
            read.close();
        } catch (Exception ex) {
            //TODO
        }
        return !configurationData.isEmpty();
    }

    private boolean eksportLocalSettings() {
        setupNewConfiguration();
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new File(configurationFileName))));
            writer.write(configurationData);
            writer.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getStackTrace());
        }
        /* quick check to see if data was saved */
        return new File(configurationFileName).exists();
    }
}
