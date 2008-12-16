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
            stop();
            player = new Player(new File(filename));
            playThread.submit(new PlayingTask(player));
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