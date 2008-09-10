package net.roarsoftware.lastfm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.roarsoftware.xml.DomElement;

/**
 * Provides nothing more than a namespace for the API methods starting with tag.
 *
 * @author Janni Kovacs
 */
public class Tag {

	private Tag() {
	}

	public static Collection<String> getSimilar(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getSimilar", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<String> tags = new ArrayList<String>();
		for (DomElement domElement : result.getContentElement().getChildren("tag")) {
			tags.add(domElement.getChildText("name"));
		}
		return tags;
	}

	public static SortedMap<Integer, String> getTopTags(String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopTags", apiKey);
		if (!result.isSuccessful())
			return new TreeMap<Integer, String>();
		SortedMap<Integer, String> tags = new TreeMap<Integer, String>(Collections.reverseOrder());
		for (DomElement domElement : result.getContentElement().getChildren("tag")) {
			tags.put(Integer.valueOf(domElement.getChildText("count")), domElement.getChildText("name"));
		}
		return tags;
	}

	public static Collection<Album> getTopAlbums(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopAlbums", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<Album> albums = new ArrayList<Album>();
		for (DomElement domElement : result.getContentElement().getChildren("album")) {
			albums.add(Album.albumFromElement(domElement));
		}
		return albums;
	}

	public static Collection<Track> getTopTracks(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopTracks", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<Track> tracks = new ArrayList<Track>();
		for (DomElement domElement : result.getContentElement().getChildren("track")) {
			tracks.add(Track.trackFromElement(domElement));
		}
		return tracks;
	}

	public static Collection<Artist> getTopArtists(String tag, String apiKey) {
		Result result = Caller.getInstance().call("tag.getTopArtists", apiKey, "tag", tag);
		if (!result.isSuccessful())
			return Collections.emptyList();
		List<Artist> artists = new ArrayList<Artist>();
		for (DomElement domElement : result.getContentElement().getChildren("artist")) {
			artists.add(Artist.artistFromElement(domElement));
		}
		return artists;
	}

	public static Collection<String> search(String tag, String apiKey) {
		return search(tag, 30, apiKey);
	}

	public static Collection<String> search(String tag, int limit, String apiKey) {
		Result result = Caller.getInstance().call("tag.search", apiKey, "tag", tag, "limit", String.valueOf(limit));
		List<String> tags = new ArrayList<String>();
		for (DomElement s : result.getContentElement().getChild("tagmatches").getChildren("tag")) {
			tags.add(s.getChildText("name"));
		}
		return tags;
	}
}
