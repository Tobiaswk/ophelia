package net.roarsoftware.lastfm;

import java.util.Collection;
import java.util.Date;

/**
 * Bean for Chart information. Contains a start date, an end date and a list of entries.
 *
 * @author Janni Kovacs
 */
public class Chart<T extends MusicEntry> {

	private Date from, to;
	private Collection<T> entries;

	public Chart(Date from, Date to, Collection<T> entries) {
		this.from = from;
		this.to = to;
		this.entries = entries;
	}

	public Collection<T> getEntries() {
		return entries;
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}
}
