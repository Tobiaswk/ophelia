package net.roarsoftware.lastfm.scrobble;

/**
 * Bean that contains track information.
 *
 * @author Janni Kovacs
 */
public class SubmissionData {

	private String artist;
	private String track;
	private String album;
	private long startTime;
	private Source source;
	private int length;
	private int tracknumber;

	public SubmissionData(String artist, String track, String album, int length, int tracknumber, Source source,
						  long startTime) {
		this.artist = artist;
		this.track = track;
		this.album = album;
		this.length = length;
		this.tracknumber = tracknumber;
		this.source = source;
		this.startTime = startTime;
	}

	String toString(String sessionId, int index) {
		String b = album != null ? album : "";
		String l = length == -1 ? "" : String.valueOf(length);
		String n = tracknumber == -1 ? "" : String.valueOf(tracknumber);
		return String
				.format("s=%s&a[%9$d]=%s&t[%9$d]=%s&i[%9$d]=%s&o[%9$d]=%s&r[%9$d]=&l[%9$d]=%s&b[%9$d]=%s&n[%9$d]=%s&m[%9$d]=",
						sessionId, artist, track, startTime, source.getCode(), l, b, n, index);
	}

}
