package ophelia.logic;

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

