package ophelia.logic;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javazoom.jl.player.Player;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class MP3Player {

    private Player player;
    private ExecutorService playThread = Executors.newSingleThreadExecutor();

    public MP3Player() {
        /* the easteregg suprise ;) */
        if (Settings.getInstance().isEasterEgg()) {
            try {
                new Player(this.getClass().getResourceAsStream("cat_1.mp3")).play();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean isPlaying() {
        if (player != null) {
            return player.isPlaying();
        }
        return false;
    }

    public boolean isComplete() {
        if (player != null) {
            return player.isComplete();
        }
        return true;
    }

    public int getPosition() {
        return player.getPosition();
    }

    public void play(String filename) {
        try {
            if (player != null) {
                player.stop();
            }
            player = new Player(new File(filename));
            playThread.submit(new Playing(player));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }

    public void stop() {
        if (player != null) {
            player.stop();
        }
    }
}