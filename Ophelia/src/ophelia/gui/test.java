package ophelia.gui;

import ophelia.logic.TrackWithID3;

/**
 *
 * @author Tobias W. Kjeldsen
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        /* some math to calculate audio length
         * ignoring the ID3 tags size... its so small (1 kB), so the calculation
         * is fairly precise -
         * size_in_bytes / bitrate = length_in_seconds
         * bits_per_sample * total_samples = kbit_per_second */
//        String filename = "c:/contact.flac";
//        StreamInfo info = new StreamInfo(new BitInputStream(new FileInputStream(filename)), (int) new File(filename).length(), false);
//        System.out.println(new File(filename).getName());
//        long size_bits = ((new File(filename).length()));
//        System.out.println("size: " + size_bits + " bytes");
//        int bitrate =  (int)(((info.getBitsPerSample()) * info.getTotalSamples()));
//        System.out.println("bitrate: " + bitrate + " kbit/s");
//        System.out.println("" + (size_bits / bitrate));
//        VorbisComment vorbiscomment = new VorbisComment(new BitInputStream(new FileInputStream(filename)), (int)new File(filename).length(), true);
//        System.out.println(vorbiscomment.getCommentByName("title"));
        TrackWithID3 test = new TrackWithID3("C:/Users/Tobias W. Kjeldsen/Music/Foo Fighters/Foo Fighters - Foo Fighters/Foo_Fighters_-_Foo_Fighters_-_09_For_All_The_Cows.mp3");
        System.out.println(test.getArtist());
        System.out.println(test.getAlbum());
        System.out.println(test.getTitle());
        System.out.println(test.getTrack());
        System.out.println(test.getLength());
    }
}
