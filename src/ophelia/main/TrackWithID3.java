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

import de.vdheide.mp3.ID3;
import de.vdheide.mp3.MP3Properties;
import de.vdheide.mp3.NoID3TagException;
import java.io.File;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class TrackWithID3 extends ID3 {

    private MP3Properties mp3Properties;

    public TrackWithID3(String filename) {
        super(new File(filename));
    }

    public int getLength() {
        try {
            if (mp3Properties == null) {
                mp3Properties = new MP3Properties(mediaFile);
            }
            return (int) mp3Properties.getLength();
        } catch (Exception ex) {
            return 0;
        }
    }

    public File getAbsoluteFile() {
        return mediaFile;
    }

    /**
     * @return Formatted String with length and trackname
     */
    public String getOSDStatus() {
        try {
            String calculation = (getLength() / (60 * 60)) + ":" + (getLength() / 60 % 60) + ":" + (getLength() % 60);
            String trackTitle = getTitle();
            if (trackTitle == null) {
                trackTitle = mediaFile.getName();
            }
            if (calculation.substring(0, 1).equals("0")) {
                return calculation.substring(2, calculation.length()) + " ¤ " + trackTitle;
            }
            return calculation + " ¤ " + trackTitle;
        } catch (Exception ex) {
            return "Track information not available";
        }
    }

    @Override
    public String toString() {
        try {
            return "<html>" + getArtist() + " - " + getTitle() + " <i>(" + getAlbum() + ")</i> </html>";
        } catch (NoID3TagException ex) {
            return mediaFile.getName();
        }
    }
}

