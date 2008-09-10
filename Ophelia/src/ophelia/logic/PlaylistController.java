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
