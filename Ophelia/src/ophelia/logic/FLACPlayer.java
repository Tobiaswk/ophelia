package ophelia.logic;

import java.io.FileInputStream;
import org.kc7bfi.jflac.apps.Player;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class FLACPlayer {

    private Player player;

    public FLACPlayer() {
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

    public void play(String filename) throws Exception {
        try {
            if (player != null) {
                player.close();
                player = null;
            }
            player = new Player(new FileInputStream(filename));
            new Thread(new Playing(player)).start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        if (player != null) {
            player.close();
        }
    }

    public void pause() {
        if (player != null) {
            player.pause();
        }
    }
}
