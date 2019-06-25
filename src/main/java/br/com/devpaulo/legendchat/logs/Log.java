package br.com.devpaulo.legendchat.logs;

import java.util.Date;
import org.bukkit.Location;

public class Log {

	private Date date = null;
	private String msg = null, loc = null, channel = null;

	public Log(Date d, String m, Location l) {
		date = d;
		msg = m;
		loc = l == null ? "" : String.format("%.1f %.1f %.1f %.1f %.1f %s", l.getX(), l.getY() + .1, l.getZ(), l.getYaw(), l.getPitch(), l.getWorld().getName());
	}

	public Log(Date d, String m) {
		date = d;
		msg = m;
	}

	public Log(String c, Date d, String m) {
		channel = c;
		date = d;
		msg = m;
	}

	public Date getDate() {
		return date;
	}

	public String getMessage() {
		return msg;
	}

	public String getLocation() {
		return loc;
	}

	public String getChannelName() {
		return channel;
	}

	public void setDate(Date d) {
		date = d;
	}

	public void setMessage(String m) {
		msg = m;
	}

}
