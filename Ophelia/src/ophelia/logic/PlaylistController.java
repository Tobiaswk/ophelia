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

import java.io.File;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class PlaylistController {

    private Playlist playlist;

    public PlaylistController() {
        playlist = new Playlist();
    }

    public void addPlaylistTracks(File[] files) {
        playlist.addTracks(files);
    }
    
    public int getTrackCount() {
        return playlist.getTrackCount();
    }

    public TrackWithID3[] getPlaylistTracks() {
        return playlist.getTracks();
    }

    public String getPlaylistStats() {
        return playlist.getPlaylistStats();
    }
    
    public boolean isIndexing() {
        return playlist.isIndexing();
    }

    public void clearPlaylist() {
        playlist.clearPlaylist();
    }

    public TrackWithID3[] searchTracks(String keyword) {
        return playlist.searchTracks(keyword);
    }

    public void savePlaylistFile(String playlistFilename) {
        playlist.savePlaylistFile(playlistFilename);
    }

    public void loadPlaylistFile(String playlistFilename) {
        playlist.loadPlaylistFile(playlistFilename);
    }
}
