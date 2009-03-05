package net.roarsoftware.lastfm;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import net.roarsoftware.xml.DomElement;

/**
 * Bean for Events.
 *
 * @author Janni Kovacs
 */
public class Event extends ImageHolder {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH);
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

	private int id;
	private String title;
	private Collection<String> artists;
	private String headliner;

	private Date startDate;

	private String description;
	private String url;
	private int attendance;
	private int reviews;

	private Venue venue;

	private Event() {
	}

	public Collection<String> getArtists() {
		return artists;
	}

	public int getAttendance() {
		return attendance;
	}

	public String getDescription() {
		return description;
	}

	public String getHeadliner() {
		return headliner;
	}

	public int getId() {
		return id;
	}

	public int getReviews() {
		return reviews;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public Venue getVenue() {
		return venue;
	}

	/**
	 * Get the metadata for an event on Last.fm. Includes attendance and lineup information.
	 *
	 * @param eventId The numeric last.fm event id
	 * @param apiKey A Last.fm API key.
	 * @return Event metadata
	 */
	public static Event getInfo(String eventId, String apiKey) {
		Result result = Caller.getInstance().call("event.getInfo", apiKey, "event", eventId);
		return eventFromElement(result.getContentElement());
	}

	/**
	 * Set a user's attendance status for an event.
	 *
	 * @param eventId The numeric last.fm event id
	 * @param status The attendance status
	 * @param session A Session instance
	 * @return the Result of the operation.
	 * @see net.roarsoftware.lastfm.Event.AttendanceStatus
	 * @see net.roarsoftware.lastfm.Authenticator
	 */
	public static Result attend(String eventId, AttendanceStatus status, Session session) {
		return Caller.getInstance()
				.call("event.attend", session, "event", eventId, "status", String.valueOf(status.getId()));
	}

	/**
	 * Share an event with one or more Last.fm users or other friends.
	 *
	 * @param eventId An event ID
	 * @param recipients A comma delimited list of email addresses or Last.fm usernames. Maximum is 10.
	 * @param message An optional message to send with the recommendation.
	 * @param session A Session instance
	 * @return the Result of the operation
	 */
	public static Result share(String eventId, String recipients, String message, Session session) {
		return Caller.getInstance()
				.call("event.share", session, "event", eventId, "recipient", recipients, "message", message);
	}

	static Event eventFromElement(DomElement e) {
		if (e == null)
			return null;
		Event event = new Event();
		ImageHolder.loadImages(event, e);
		event.id = Integer.parseInt(e.getChildText("id"));
		event.title = e.getChildText("title");
		event.description = e.getChildText("description");
		event.url = e.getChildText("url");
		if (e.hasChild("attendance"))
			event.attendance = Integer.parseInt(e.getChildText("attendance"));
		if (e.hasChild("reviews"))
			event.reviews = Integer.parseInt(e.getChildText("reviews"));
		try {
			event.startDate = DATE_FORMAT.parse(e.getChildText("startDate"));
			if (e.hasChild("startTime")) {
				Date startTime = TIME_FORMAT.parse(e.getChildText("startTime"));
				Calendar c = GregorianCalendar.getInstance();
				c.setTime(event.startDate);
				Calendar timeCalendar = GregorianCalendar.getInstance();
				timeCalendar.setTime(startTime);
				c.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
				c.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
				event.startDate = c.getTime();
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		event.headliner = e.getChild("artists").getChildText("headliner");
		event.artists = new ArrayList<String>();
		for (DomElement element : e.getChild("artists").getChildren("artist")) {
			event.artists.add(element.getText());
		}
		event.venue = Venue.venueFromElement(e.getChild("venue"));
		return event;
	}


	/**
	 * Enumeration for the attendance status parameter of the <code>attend</code> operation.
	 */
	public static enum AttendanceStatus {

		ATTENDING(0),
		MAYBE_ATTENDING(1),
		NOT_ATTENDING(2);

		private int id;

		private AttendanceStatus(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}
}
