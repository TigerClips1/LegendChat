package br.com.devpaulo.legendchat.api.events;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.channels.types.Channel;

public class ChatMessageEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private String message = "";
	private String format = "";
	private Player sender = null;
	private Channel ch = null;
	private String base_format = "";
	private String bukkit_format = "";
	private final Set<Player> recipients = new HashSet<>();
	private boolean cancelled = false;
	private final HashMap<String, String> tags = new HashMap<>();

	public ChatMessageEvent(Channel ch, Player sender, String message, String format, String base_format, String bukkit_format, Set<Player> recipients, HashMap<String, String> tags, boolean cancelled) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.sender = sender;
		this.ch = ch;
		this.message = applyMarkdown(message, ch.getColor());
		this.recipients.addAll(recipients);

		if (tags.containsValue(null)) {
			List<String> ns = new ArrayList<>(tags.keySet());
			for (String n : ns) {
				if (tags.get(n) == null) {
					tags.remove(n);
					tags.put(n, "");
				}
			}
		}
		this.tags.putAll(tags);
		this.cancelled = cancelled;
		this.base_format = base_format;
		this.bukkit_format = bukkit_format;
		this.format = ChatColor.translateAlternateColorCodes('&', format);
		for (int i = 0; i < format.length(); i++) {
			if (format.charAt(i) == '{') {
				String tag = format.substring(i + 1).split("}")[0].toLowerCase();
				if (!tag.equals("msg")) {
					if (!this.tags.containsKey(tag)) {
						this.tags.put(tag, "");
					}
				}
			}
		}
	}

	private String applyMarkdown(String message, String baseColor) {
		if (message == null) {
			return "";
		}

		// Translate initial color codes using '&' symbols
		message = ChatColor.translateAlternateColorCodes('&', message);

		// Apply bold, italic, underline, and strikethrough formatting
		message = message.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", ChatColor.BOLD.toString() + ChatColor.ITALIC + "$1" + ChatColor.RESET + baseColor);
		message = message.replaceAll("\\*\\*(.*?)\\*\\*", ChatColor.BOLD + "$1" + ChatColor.RESET + baseColor);
		message = message.replaceAll("\\*(.*?)\\*", ChatColor.ITALIC + "$1" + ChatColor.RESET + baseColor);
		message = message.replaceAll("__(.*?)__", ChatColor.UNDERLINE + "$1" + ChatColor.RESET + baseColor);
		message = message.replaceAll("~~(.*?)~~", ChatColor.STRIKETHROUGH + "$1" + ChatColor.RESET + baseColor);

		return message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message == null ? "" : applyMarkdown(message, ch.getColor());
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		if (format != null) {
			this.format = ChatColor.translateAlternateColorCodes('&', format);
		}
	}

	public Player getSender() {
		return sender;
	}

	public void setSender(Player sender) {
		if (sender != null) {
			this.sender = sender;
		}
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public Set<Player> getRecipients() {
		return recipients;
	}

	public Channel getChannel() {
		return ch;
	}

	public String getBukkitFormat() {
		return bukkit_format;
	}

	public String getBaseFormat() {
		return base_format;
	}

	public String baseFormatToFormat(String base_format) {
		return Legendchat.format(base_format);
	}

	public List<String> getTags() {
		return new ArrayList<>(tags.keySet());
	}

	public boolean setTagValue(String tag, String value) {
		if (tag == null) {
			return false;
		}
		tag = tag.toLowerCase();
		if (!tags.containsKey(tag)) {
			return false;
		}
		tags.put(tag, value == null ? "" : value);
		return true;
	}

	public String getTagValue(String tag) {
		if (tag == null) {
			return null;
		}
		return tags.get(tag.toLowerCase());
	}

	public void addTag(String tag, String value) {
		if (tag == null) {
			return;
		}
		tag = tag.toLowerCase();
		if (!tags.containsKey(tag)) {
			tags.put(tag, value == null ? "" : value);
		}
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}