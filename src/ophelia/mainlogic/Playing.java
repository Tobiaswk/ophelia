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
package ophelia.mainlogic;

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
