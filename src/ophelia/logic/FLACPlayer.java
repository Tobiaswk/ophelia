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
