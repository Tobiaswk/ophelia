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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class Playlist {

    private Vector<TrackWithID3> trackPlaylist;
    private Vector<TrackWithID3> resultsPlaylist;
    private int lastPlayedPosition;
    private boolean indexing;

    public Playlist() {
        this.resultsPlaylist = new Vector<TrackWithID3>();
        this.trackPlaylist = new Vector<TrackWithID3>();
        if (Settings.getInstance().isLoadPlaylistStartup()) {
            loadPlaylistFile();
        }
    }

    public void addTracks(File[] files) {
        /* we start a new thread... in case of very big array */
        indexing = true;
        new Thread(new TrackIndexing(files)).start();
    }

    public Vector getTracks() {
        return trackPlaylist;
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

    public void loadPlaylistFile(String playlistFilename) {
        indexing = true;
        new Thread(new TrackIndexing(playlistFilename)).start();
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
        private String playlistFilename;

        /**
         * 
         * @param this constructor is used when indexing newly added tracks
         */
        public TrackIndexing(File[] files) {
            this.files = files;
        }

        /**
         * 
         * @param this constructor is used when indexing from playlistfile
         */
        public TrackIndexing(String playlistFilename) {
            this.playlistFilename = playlistFilename;
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

        public void addTracks(String playlistFilename) {
            ArrayList<File> result = new ArrayList<File>();
            try {
                BufferedReader in_test = new BufferedReader(new InputStreamReader(new FileInputStream(playlistFilename)));
                while (in_test.ready()) {
                    result.add(new File(in_test.readLine()));
                }
                in_test.close();
                addTracks(result.toArray(new File[0]));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            if (files != null) {
                addTracks(files);
                savePlaylistFile();
            } else {
                addTracks(playlistFilename);
            }
            indexing = false;
        }
    }
}
