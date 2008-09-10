package ophelia.logic;

import javazoom.jl.player.Player;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class Playing implements Runnable {

    private javazoom.jl.player.Player mp3Playing;
    private org.kc7bfi.jflac.apps.Player flacPlaying;

    public Playing(javazoom.jl.player.Player mp3Playing) {
        this.mp3Playing = mp3Playing;
    }

    public Playing(org.kc7bfi.jflac.apps.Player flacPlaying) {
        this.flacPlaying = flacPlaying;
    }

    public Player getMp3Playing() {
        return mp3Playing;
    }

    public void run() {
        try {
            if (flacPlaying == null) {
                /* for submission to "Playing Now" */
                if (!Settings.getInstance().getLasfmUsername().equals("") && !Settings.getInstance().getLasfmUsername().equals("")) {
                    new Thread(new ScrobbleTask(mp3Playing, 2), "Last.fm Playing Now").start();
                }
                mp3Playing.play();
                /* for submission to "Recently Listened Tracks" */
                if (Settings.getInstance().isLastfmScrobble() && mp3Playing.isComplete()) {
                    new Thread(new ScrobbleTask(mp3Playing, 1), "Last.fm Recently Listened Tracks").start();
                }
            } else {
                flacPlaying.play();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
