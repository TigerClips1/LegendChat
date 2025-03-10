package br.com.devpaulo.legendchat.api.events;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.devpaulo.legendchat.channels.utils.ChannelUtils;
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

	@Deprecated
	public ChatMessageEvent(Channel ch, Player sender, String message, String format, String base_format, String bukkit_format, Set<Player> recipients, HashMap<String, String> tags, boolean cancelled) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.sender = sender;
		this.ch = ch;
		this.message = applyMarkdown(sender, message, ch.getColor());
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

	@Deprecated
	private String applyMarkdown(Player player, String message, String baseColor) {
		if (message == null) {
			return "";
		}

		// Apply color codes with permissions
		message = ChannelUtils.translateAlternateChatColorsWithPermission(player, message);

		// Regular expressions for different markdown and colors
		String boldItalicPattern = "\\*\\*\\*(.*?)\\*\\*\\*";
		String boldPattern = "\\*\\*(.*?)\\*\\*";
		String italicPattern = "(?<!_)_(?!_)(.*?)_|(?<!\\*)\\*(?!\\*)(.*?)\\*";
		String underlinePattern = "__(.*?)__";
		String italicUnderlinePattern = "___(.*?)___";
		String strikethroughPattern = "~~(.*?)~~";

		// Apply markdown formatting and preserve user-added color codes
		message = applyPattern(message, boldItalicPattern, ChatColor.BOLD.toString() + ChatColor.ITALIC, baseColor);
		message = applyPattern(message, boldPattern, ChatColor.BOLD.toString(), baseColor);
		message = applyPattern(message, italicUnderlinePattern, ChatColor.ITALIC.toString() + ChatColor.UNDERLINE, baseColor);
		message = applyPattern(message, italicPattern, ChatColor.ITALIC.toString(), baseColor);
		message = applyPattern(message, underlinePattern, ChatColor.UNDERLINE.toString(), baseColor);
		message = applyPattern(message, strikethroughPattern, ChatColor.STRIKETHROUGH.toString(), baseColor);

		// Apply base color after reset code
		message = message.replaceAll("[§&]r", ChatColor.RESET + baseColor);

		return message;
	}
	@Deprecated

	private String applyPattern(String message, String pattern, String colorCode, String baseColor) {
		Matcher matcher = Pattern.compile(pattern).matcher(message);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String match = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
			String beforeMatch = message.substring(0, matcher.start());
			String lastColor = extractLastColor(beforeMatch);
			String replacement = colorCode + match + ChatColor.RESET + baseColor + lastColor;
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	// Helper method to extract the last color code from a string
	private String extractLastColor(String message) {
		String lastColorCode = "";
		Matcher matcher = Pattern.compile("([§&][0-9a-fk-or])").matcher(message);
		while (matcher.find()) {
			lastColorCode = matcher.group(1);
		}
		return lastColorCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message == null ? "" : applyMarkdown(sender, message, ch.getColor());
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		if (format != null) {
			this.format = format;
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