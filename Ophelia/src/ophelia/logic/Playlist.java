package ophelia.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class Playlist {

    private ArrayList<TrackWithID3> trackPlaylist;
    private ArrayList<TrackWithID3> resultsPlaylist;
    private boolean indexing;

    public Playlist() {
        loadPlaylistFile();
        this.resultsPlaylist = new ArrayList<TrackWithID3>(trackPlaylist.size());
        this.indexing = false;
    }

    public void addTracks(File[] files) {
        /* we start a new thread... in case of very big array */
        indexing = true;
        new Thread(new TrackIndexing(files)).start();
    }

    public TrackWithID3[] getTracks() {
        return trackPlaylist.toArray(new TrackWithID3[0]);
    }

    public int getTrackCount() {
        return trackPlaylist.size();
    }

    public String getPlaylistStats() {
        int flacfiles = 0;
        int mp3files = 0;
        for (TrackWithID3 file : trackPlaylist) {
            if (file.getAbsoluteFile().getName().endsWith(".flac")) {
                flacfiles++;
            } else if (file.getAbsoluteFile().getName().endsWith(".mp3")) {
                mp3files++;
            }
        }
        return trackPlaylist.size() + " Tracks (" + mp3files + " MP3, " + flacfiles + " FLAC) ";
    }

    public boolean isIndexing() {
        return indexing;
    }

    public void clearPlaylist() {
        trackPlaylist.clear();
    }

    public TrackWithID3[] searchTracks(String keyword) {
        resultsPlaylist.clear();
        String title;
        for (TrackWithID3 track : trackPlaylist) {
            try {
                title = track.getTitle() + track.getArtist();
            } catch (Exception ex) {
                title = track.getAbsoluteFile().getName();
            }
            if (title.toLowerCase().indexOf(keyword.toLowerCase()) != -1) {
                resultsPlaylist.add(track);
            }
        }
        return resultsPlaylist.toArray(new TrackWithID3[0]);
    }

    public void loadPlaylistFile(String playlist) {
        if (trackPlaylist == null) {
            trackPlaylist = new ArrayList();
        } else {
            trackPlaylist.clear();
        }
        try {
            BufferedReader in_test = new BufferedReader(new InputStreamReader(new FileInputStream(playlist)));
            while (in_test.ready()) {
                trackPlaylist.add(new TrackWithID3(in_test.readLine()));
            }
        } catch (Exception ex) {
            //TODO
        }
    }

    private void loadPlaylistFile() {
        loadPlaylistFile(Settings.getInstance().getDefaultPlaylistName());
    }

    public void savePlaylistFile(String playlistName) {
        try {
            PrintWriter out = new PrintWriter(new File(playlistName));
            for (TrackWithID3 file : trackPlaylist) {
                out.println(file.getAbsoluteFile().getAbsolutePath());
            }
            out.close();
        } catch (FileNotFoundException ex) {
            //TODO
        }
    }

    private void savePlaylistFile() {
        savePlaylistFile(Settings.getInstance().getDefaultPlaylistName());
    }

    private class TrackIndexing implements Runnable {

        private File[] files;

        public TrackIndexing(File[] files) {
            this.files = files;
        }

        public void addTracks(File[] files) {
            try {
                for (File file : files) {
                    if (file.isDirectory()) {
                        addTracks(file.listFiles(new SupportedFormatsFilter()));
                    } else {
                        TrackWithID3 track = new TrackWithID3(file.getAbsolutePath());
                        if (!trackPlaylist.contains(track)) {
                            trackPlaylist.add(track);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        public void run() {
            addTracks(files);
            savePlaylistFile();
            indexing = false;
        }
    }
}
