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
package ophelia.main;

import java.util.Observable;

/**
 *
 * @author Tobias W. Kjeldsen
 */

/**
 * this class works as a type of reporter that holds information about the
 * latest scrobbled track submitted to last.fm
 * the class is also observable - the gui, Main.java, is observer and updates- 
 * when change of values occur */
public class ScrobbleStatus extends Observable {
    
    private final String NOTHING_SUBMITTED = java.util.ResourceBundle.getBundle("ophelia/gui/localization/MainResources").getString("NOTHING_SUBMITTED");

    private static ScrobbleStatus singleton;
    private String artist;
    private String trackTitle;

    public static ScrobbleStatus getInstance() {
        if (singleton == null) {
            singleton = new ScrobbleStatus();
        }
        return singleton;
    }

    private ScrobbleStatus() {
    }

    public String getArtist() {
        return artist;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public void setLastPlayed(String artist, String trackTitle) {
        this.artist = artist;
        this.trackTitle = trackTitle;
        setChanged();
        notifyObservers();
    }
    
    public String getLastPlayed() {
        if (artist == null && trackTitle == null) {
            return NOTHING_SUBMITTED;
        }
        return trackTitle + " (" + artist + ")";
    }

    @Override
    public String toString() {
        if (artist == null && trackTitle == null) {
            return NOTHING_SUBMITTED;
        }
        return "<html><u>" + trackTitle + "</u></html>";
    }
}
