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
import java.util.Collections;
import java.util.HashMap;
import java.util.Observable;
import java.util.List;
import java.util.Vector;
import ophelia.main.interfaces.StandardTrack;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class Playlist extends Observable {

    private final String PLAYLISTDIR = "playlists";
    private int trackNumbersMP3,  trackNumbersFLAC;

    /* save/load playlist format identifier */
    public enum PlaylistType {

        M3U, Cleartext;
    }

    /* we store playlist in a hashmap, each playlist is stored in the-
     * hasmap with key as the playlist name and a List with-
     *  the playlist tracks */
    private HashMap<String, List<StandardTrack>> trackPlaylists;
    private List<StandardTrack> resultsPlaylist;
    private int indexing;
    private int trackNumber;

    public Playlist() {
        this.trackPlaylists = new HashMap<String, List<StandardTrack>>();
        this.resultsPlaylist = Collections.synchronizedList(new Vector<StandardTrack>());
        if (Settings.getInstance().isLoadPlaylistsStartup()) {
            loadPlaylists();
        }
    }

    public void addTracks(String playlistName, File[] files) {
        /* we start a new thread... in case of very big array */
        indexing++;
        new Thread(new TrackIndexing(playlistName, files)).start();
    }

    public List getTracks(String playlistName) {
        return trackPlaylists.get(playlistName);
    }

    public int getTrackCount() {
        return trackNumber;
    }

    public int getMP3TrackCount() {
        return trackNumbersMP3;
    }

    public int getFLACTrackCount() {
        return trackNumbersFLAC;
    }

    private synchronized int getTrackTypeCount(String endsWith) {
        int typeCount = 0;
        trackNumber = 0;
        for (List<StandardTrack> playlist : trackPlaylists.values()) {
            for (StandardTrack file : playlist) {
                if (file.getAbsoluteFile().getName().endsWith(endsWith)) {
                    typeCount++;
                }
                trackNumber++;
            }
        }
        return typeCount;
    }

    public String[] getPlaylistNames() {
        File[] playlists = new File(PLAYLISTDIR).listFiles();
        String[] result = new String[playlists.length];
        if (playlists != null && playlists.length != 0) {
            result = new String[playlists.length];
            for (int i = 0; i < playlists.length; i++) {
                result[i] = playlists[i].getAbsolutePath();
            }
        }
        for (String str : result) {
            System.out.println(str + " blev indekseret.");
        }
        return result;
    }

    public boolean isIndexing() {
        return indexing != 0;
    }

    public void clearPlaylists() {
        trackPlaylists.clear();
    }

    public void clearPlaylist(String playlistName) {
        trackPlaylists.remove(playlistName);
    }

    public void searchTracks(String playlistName, String keyword) {
        indexing++;
        new Thread(new TrackIndexing(new String[]{playlistName, keyword}, Job.SEARCHING)).start();
    }

    public void loadPlaylists(String... playlistNames) {
        if (playlistNames.length == 0) {
            playlistNames = getPlaylistNames();
        }
        for (String playlistName : playlistNames) {
            if (trackPlaylists.containsKey(parsePlaylistName(playlistName))) {
                trackPlaylists.get(parsePlaylistName(playlistName)).clear();
            }
            indexing++;
            new Thread(new TrackIndexing(new String[]{playlistName}, Job.INDEXING)).start();
        }
    }

    public void savePlaylists() {
        savePlaylists(trackPlaylists.keySet().toArray(new String[0]));
    }

    public void savePlaylists(String... playlistNames) {
        for (String playlist : playlistNames) {
            savePlaylistFile(playlist);
        }
    }

    public void savePlaylistFile(String playlistName) {
        try {
            new File(PLAYLISTDIR).mkdir();
            PrintWriter out = new PrintWriter(new File(PLAYLISTDIR + "/" + playlistName));
            for (StandardTrack file : trackPlaylists.get(playlistName)) {
                out.println(file.getAbsoluteFile().getAbsolutePath());
            }
            out.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /* parse the playlist name from a string containing the full-
     * playlist location path
     * this is a temporary fix - needs a cleaner solution */
    private String parsePlaylistName(String playlistLocation) {
        String[] playlistFileName = playlistLocation.split("\\\\");
        return playlistFileName[playlistFileName.length - 1];
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
        /* this String-array contains the-
         * following arguments (depends on indexing situation):
         * index 0 is the playlists name
         * index 1 is search keyword */
        private String[] args = new String[2];
        private Job job;

        /**
         * this constructor is used when indexing newly added tracks
         * @param Array of Files
         */
        public TrackIndexing(String playlistName, File[] files) {
            this.args[0] = playlistName;
            this.files = files;
        }

        /**
         * this constructor is used when indexing or searching
         * @param Array of arguments, etc. [playlistName, searchKeyword]
         * @param Job INDEXING, SEARCHING, NEWFILES
         */
        public TrackIndexing(String[] args, Job job) {
            this.args = args;
            this.job = job;
        }

        public synchronized SinglePlaylist searchTracks(String playlistName, String keyword) {
            if (keyword.equals("")) {
                /* return the playlist if no search keyword */
                return new SinglePlaylist(playlistName, trackPlaylists.get(playlistName));
            }
            resultsPlaylist.clear();
            String title;
            for (StandardTrack track : trackPlaylists.get(playlistName)) {
                try {
                    title = track.toString();
                } catch (Exception ex) {
                    title = track.getAbsoluteFile().getName();
                }
                if (title.toLowerCase().indexOf(keyword.toLowerCase()) != -1) {
                    resultsPlaylist.add(track);
                }
            }
            return new SinglePlaylist(playlistName, resultsPlaylist);
        }

        public SinglePlaylist addTracks(String playlistName, File[] files) {
            try {
                for (File file : files) {
                    if (file.isDirectory()) {
                        addTracks(playlistName, file.listFiles(new SupportedFormatsFilter()));
                    } else {
                        trackPlaylists.get(playlistName).add(new TrackMP3(file.getAbsolutePath()));
                    }
                }
                // done here to save some resources; only count tracks added
                trackNumbersMP3 = getTrackTypeCount(".mp3");
                trackNumbersFLAC = getTrackTypeCount(".flac");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return new SinglePlaylist(playlistName, trackPlaylists.get(playlistName));
        }

        public SinglePlaylist addTracks(String playlistLocation) {
            ArrayList<File> result = new ArrayList<File>();
            try {
                BufferedReader in_test;
                try {
                    in_test = new BufferedReader(new InputStreamReader(new FileInputStream(playlistLocation)));
                    while (in_test.ready()) {
                        result.add(new File(in_test.readLine()));
                    }
                    in_test.close();
                    addTracks(parsePlaylistName(playlistLocation), result.toArray(new File[0]));
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return new SinglePlaylist(parsePlaylistName(playlistLocation), trackPlaylists.get(parsePlaylistName(playlistLocation)));
        }

        public void run() {
            /* we ensure that the playlist List exists, if not, we create it */
            if (trackPlaylists.get(parsePlaylistName(args[0])) == null) {
                trackPlaylists.put(parsePlaylistName(args[0]), new Vector<StandardTrack>());
            }
            /* We signal change to the observer(s) and send an HashMap-
             * back. This HashMap contains have playlist name as key-
             * and the actual playlist as the data in the HashMap-
             * this way the observer will know what playlist changed and-
             * have the new playlist data as well */
            if (files != null) {
                /* add files to existing playlist */
                signalChange(addTracks(args[0], files));
            } else if (job.ordinal() == Job.INDEXING.ordinal()) {
                /* load playlists */
                signalChange(addTracks(args[0]));
            } else if (job.ordinal() == Job.SEARCHING.ordinal()) {
                /* search a playlist */
                signalChange(searchTracks(args[0], args[1]));
            }
            indexing--;
            System.out.println(indexing);
        }
    }
}
