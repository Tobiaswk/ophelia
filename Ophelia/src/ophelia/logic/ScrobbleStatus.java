package ophelia.logic;

/**
 *
 * @author Tobias W. Kjeldsen
 */

/*
 * this class works as a type of reporter that holds information about the
 * latest scrobbled track submitted to last.fm */
public class ScrobbleStatus {
    
    private final String NOTHING_SUBMITTED = "Nothing submitted";

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

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }
    
    public String getLastPlayed() {
        if (artist == null && trackTitle == null) {
            return NOTHING_SUBMITTED;
        }
        return trackTitle + " by " + artist + "";
    }

    @Override
    public String toString() {
        if (artist == null && trackTitle == null) {
            return NOTHING_SUBMITTED;
        }
        return "<html><u>" + trackTitle + "</u></html>";
    }
}
