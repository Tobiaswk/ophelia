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

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class MediaPlayerController {

    private MP3Player mp3player;
    private FLACPlayer flacplayer;
    private UpdateComponent updateComponent;

    public MediaPlayerController() {
        mp3player = new MP3Player();
        flacplayer = new FLACPlayer();
        updateComponent = new UpdateComponent();
    }

    public void playTrack(String filename) {
        try {
            if (filename.endsWith(".flac")) {
                mp3player.stop();
                flacplayer.play(filename);
            } else if (filename.endsWith(".mp3")) {
                flacplayer.stop();
                mp3player.play(filename);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void pauseTrack() {
        flacplayer.pause();
        mp3player.pause();
    }

    public void stopTrack() {
        flacplayer.stop();
        mp3player.stop();
    }

    public int getTrackPosition() {
        return mp3player.getPosition();
    }

    public boolean isComplete() {
        return flacplayer.isComplete() && mp3player.isComplete();
    }
    
    public boolean isNewVersion() {
        return updateComponent.isNewVersion();
    }
}
