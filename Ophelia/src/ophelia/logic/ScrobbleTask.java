package ophelia.logic;

import net.roarsoftware.lastfm.scrobble.Scrobbler;
import net.roarsoftware.lastfm.scrobble.Source;

/**
 *
 * @author Tobias W. Kjeldsen
 * TODO:
 * missing flac support for scrobble lastfm due to no flac tag support yet
 */
public class ScrobbleTask implements Runnable {

    private static Scrobbler scrobbler; //nasty fix - avoid multiple handshakes
    private javazoom.jl.player.Player mp3Playing;
    private int taskMode;
    private ScrobbleStatus scrobbleStatus;

    /**
     * @param taskMode 1, for submission to "Recently Listened Tracks"
     * @param taskMode 2, for submission to "Playing Now"
     */
    public ScrobbleTask(javazoom.jl.player.Player mp3Playing, int taskMode) {
        this.mp3Playing = mp3Playing;
        this.taskMode = taskMode;
        this.scrobbleStatus = ScrobbleStatus.getInstance();
        /* perform a handshake with 3 retries if bad connection */
        performHandshake(3);
    }

    private void performHandshake(int retries) {
        if (scrobbler == null) {
            scrobbler = Scrobbler.newScrobbler(Settings.getInstance().getClientID(), Settings.getInstance().getOpheliaVersion(), Settings.getInstance().getLasfmUsername());
            try {
                if (!scrobbler.handshake(Settings.getInstance().getLasfmPassword()).ok() && retries != 0) {
                    performHandshake(retries - 1);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /* this will submit to "Recently Listened Tracks" on lastfm */
    private void mp3PlayingSubmit() {
        try {
            boolean success = scrobbler.submit(mp3Playing.getPlayingFile().getArtist(),
                    mp3Playing.getPlayingFile().getTitle(),
                    mp3Playing.getPlayingFile().getAlbum(),
                    1000,
                    mp3Playing.getPlayingFile().getTrack(),
                    Source.USER,
                    System.currentTimeMillis() / 1000).ok();
            if (success) {
                scrobbleStatus.setArtist(mp3Playing.getPlayingFile().getArtist());
                scrobbleStatus.setTrackTitle(mp3Playing.getPlayingFile().getTitle());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /* this will only submit to "Playing Now" status on lastfm */
    private void mp3PlayingNowSubmit() {
        try {
            while (mp3Playing.isPlaying()) {
                scrobbler.nowPlaying(mp3Playing.getPlayingFile().getArtist(), mp3Playing.getPlayingFile().getTitle());
                Thread.sleep(15000);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        if (mp3Playing != null) {
            if (taskMode == 1) {
                mp3PlayingSubmit();
            } else if (taskMode == 2) {
                mp3PlayingNowSubmit();
            }
        }
    }
}