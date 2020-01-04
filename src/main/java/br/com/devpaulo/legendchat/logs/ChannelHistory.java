package br.com.devpaulo.legendchat.logs;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.channels.types.Channel;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import org.bukkit.entity.Player;

public class ChannelHistory {

	// todo? don't send channels that player isn't in? (needs persistent player data)
	private final LinkedList<Log> log = new LinkedList();
	final static long MILLISECONDS_PER_MINUTE = 60000;
	long nextTrim = 0;

	public void addLogToCache(Channel c, String m) {
		if (c != null && c.getMaxDistance() == 0 && c.isCrossworlds()) {
			synchronized (log) {
				log.add(new Log(c.getName(), new Date(), m));
			}
		}
		trimTask();
	}

	public void trimTask() {
		final long now = System.currentTimeMillis();
		if (now > nextTrim) {
			nextTrim = now + MILLISECONDS_PER_MINUTE * Legendchat.getJoinChatHistoryTime() / 2;
			long minAge = now - MILLISECONDS_PER_MINUTE * Legendchat.getJoinChatHistoryTime();
			if (!log.isEmpty()) {
				synchronized (log) {
					Log l;
					while ((l = log.peek()) != null) {
						if (l.getDate().getTime() < minAge) {
							log.pop();
						} else {
							break;
						}
					}
				}
			}
		}
	}

	public void sendHistory(Player p) {
		final long now = System.currentTimeMillis();
		long minAge = now - MILLISECONDS_PER_MINUTE * Legendchat.getJoinChatHistoryTime();
		int max = Legendchat.getJoinChatHistoryMax();
		LinkedList<String> toSend = new LinkedList();
		synchronized (log) {
			Iterator<Log> iter = log.descendingIterator();
			while (iter.hasNext()) {
				Log l = iter.next();
				if (l.getDate().getTime() > minAge) {
					if (p.hasPermission("legendchat.channel." + l.getChannelName().toLowerCase() + ".chat") || p.hasPermission("legendchat.admin")) {
						toSend.push(l.getMessage());
						if (--max == 0) {
							break;
						}
					}
				} else {
					break;
				}
			}
		}
		for (String s : toSend) {
			p.sendMessage(s);
		}
	}
}
