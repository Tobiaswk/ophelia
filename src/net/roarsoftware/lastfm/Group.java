package net.roarsoftware.lastfm;

import java.util.*;

import net.roarsoftware.xml.DomElement;

/**
 * Provides nothing more than a namespace for the API methods starting with group.
 *
 * @author Janni Kovacs
 */
public class Group {

	private Group() {
	}

	public static Chart<Album> getWeeklyAlbumChart(String group, String apiKey) {
		return getWeeklyAlbumChart(group, null, null, -1, apiKey);
	}

	public static Chart<Album> getWeeklyAlbumChart(String group, int limit, String apiKey) {
		return getWeeklyAlbumChart(group, null, null, limit, apiKey);
	}

	public static Chart<Album> getWeeklyAlbumChart(String group, String from, String to, int limit, String apiKey) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("group", group);
		if (from != null && to != null) {
			params.put("from", from);
			params.put("to", to);
		}
		if (limit != -1) {
			params.put("limit", String.valueOf(limit));
		}
		Result result = Caller.getInstance().call("group.getWeeklyAlbumChart", apiKey, params);
		if (!result.isSuccessful())
			return null;
		DomElement element = result.getContentElement();
		Collection<Album> albums = new ArrayList<Album>();
		for (DomElement domElement : element.getChildren("album")) {
			albums.add(Album.albumFromElement(domElement));
		}
		long fromTime = 1000 * Long.parseLong(element.getAttribute("from"));
		long toTime = 1000 * Long.parseLong(element.getAttribute("to"));
		return new Chart<Album>(new Date(fromTime), new Date(toTime), albums);
	}

	public static Chart<Artist> getWeeklyArtistChart(String group, String apiKey) {
		return getWeeklyArtistChart(group, null, null, -1, apiKey);
	}

	public static Chart<Artist> getWeeklyArtistChart(String group, int limit, String apiKey) {
		return getWeeklyArtistChart(group, null, null, limit, apiKey);
	}

	public static Chart<Artist> getWeeklyArtistChart(String group, String from, String to, int limit, String apiKey) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("group", group);
		if (from != null && to != null) {
			params.put("from", from);
			params.put("to", to);
		}
		if (limit != -1) {
			params.put("limit", String.valueOf(limit));
		}
		Result result = Caller.getInstance().call("group.getWeeklyArtistChart", apiKey, params);
		if (!result.isSuccessful())
			return null;
		DomElement element = result.getContentElement();
		Collection<Artist> artists = new ArrayList<Artist>();
		for (DomElement domElement : element.getChildren("artist")) {
			artists.add(Artist.artistFromElement(domElement));
		}
		long fromTime = 1000 * Long.parseLong(element.getAttribute("from"));
		long toTime = 1000 * Long.parseLong(element.getAttribute("to"));
		return new Chart<Artist>(new Date(fromTime), new Date(toTime), artists);
	}

	public static Chart<Track> getWeeklyTrackChart(String group, String apiKey) {
		return getWeeklyTrackChart(group, null, null, -1, apiKey);
	}

	public static Chart<Track> getWeeklyTrackChart(String group, int limit, String apiKey) {
		return getWeeklyTrackChart(group, null, null, limit, apiKey);
	}

	public static Chart<Track> getWeeklyTrackChart(String group, String from, String to, int limit, String apiKey) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("group", group);
		if (from != null && to != null) {
			params.put("from", from);
			params.put("to", to);
		}
		if (limit != -1) {
			params.put("limit", String.valueOf(limit));
		}
		Result result = Caller.getInstance().call("group.getWeeklyTrackChart", apiKey, params);
		if (!result.isSuccessful())
			return null;
		DomElement element = result.getContentElement();
		Collection<Track> tracks = new ArrayList<Track>();
		for (DomElement domElement : element.getChildren("track")) {
			tracks.add(Track.trackFromElement(domElement));
		}
		long fromTime = 1000 * Long.parseLong(element.getAttribute("from"));
		long toTime = 1000 * Long.parseLong(element.getAttribute("to"));
		return new Chart<Track>(new Date(fromTime), new Date(toTime), tracks);
	}

	public static LinkedHashMap<String, String> getWeeklyChartList(String group, String apiKey) {
		Result result = Caller.getInstance().call("group.getWeeklyChartList", apiKey, "group", group);
		if (!result.isSuccessful())
			return new LinkedHashMap<String, String>(0);
		DomElement element = result.getContentElement();
		LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
		for (DomElement domElement : element.getChildren("chart")) {
			list.put(domElement.getAttribute("from"), domElement.getAttribute("to"));
		}
		return list;
	}

	public static Collection<Chart> getWeeklyChartListAsCharts(String group, String apiKey) {
		Result result = Caller.getInstance().call("group.getWeeklyChartList", apiKey, "group", group);
		if (!result.isSuccessful())
			return Collections.emptyList();
		DomElement element = result.getContentElement();
		List<Chart> list = new ArrayList<Chart>();
		for (DomElement domElement : element.getChildren("chart")) {
			long fromTime = 1000 * Long.parseLong(domElement.getAttribute("from"));
			long toTime = 1000 * Long.parseLong(domElement.getAttribute("to"));
			list.add(new Chart<Track>(new Date(fromTime), new Date(toTime), null));
		}
		return list;
	}
}
