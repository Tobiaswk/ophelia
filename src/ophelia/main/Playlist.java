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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Vector;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class Playlist extends Observable {

    private Vector<TrackWithID3> trackPlaylist;
    private Vector<TrackWithID3> resultsPlaylist;
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

    public void searchTracks(String keyword) {
        indexing = true;
        new Thread(new TrackIndexing(keyword, "search")).start();
    }

    private void loadPlaylistFile() {
        loadPlaylistFile(Settings.getInstance().getDefaultPlaylistName());
    }

    public void loadPlaylistFile(String playlistFilename) {
        indexing = true;
        new Thread(new TrackIndexing(playlistFilename, "indexing")).start();
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

    /**
     * we use observer-pattern, this class is observable, we call this-
     * method when something in playlist has changed
     * @param object which was updated
     */
    public void signalChange(Object object) {
        setChanged();
        notifyObservers(object);
    }

    private class TrackIndexing implements Runnable {

        private File[] files;
        private String filename;
        private String job;

        /**
         * this constructor is used when indexing newly added tracks
         * @param array of File
         */
        public TrackIndexing(File[] files) {
            this.files = files;
        }

        /**
         * this constructor is used when indexing or searching
         * @param arg either playlistfilename or a seach keyword
         * @param jobType "indexing" or "search"
         */
        public TrackIndexing(String arg, String job) {
            this.filename = arg;
            this.job = job;
        }

        public Vector<TrackWithID3> searchTracks(String keyword) {
            if (keyword.equals("")) {
                return trackPlaylist;
            }
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
            return resultsPlaylist;
        }

        public Vector<TrackWithID3> addTracks(File[] files) {
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
            return trackPlaylist;
        }

        public Vector<TrackWithID3> addTracks(String playlistFilename) {
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
            return trackPlaylist;
        }

        public void run() {
            if (files != null) {
                signalChange(addTracks(files));
            } else if (job.equals("indexing")) {
                signalChange(addTracks(filename));
            } else if (job.equals("search")) {
                signalChange(searchTracks(filename));
            }
            indexing = false;
        }
    }
}
