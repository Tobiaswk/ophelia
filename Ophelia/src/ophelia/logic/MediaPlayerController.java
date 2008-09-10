package ophelia.logic;

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
