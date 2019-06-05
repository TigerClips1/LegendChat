package br.com.devpaulo.legendchat.channels.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.api.events.BungeecordChatMessageEvent;
import br.com.devpaulo.legendchat.channels.utils.ChannelUtils;

public class BungeecordChannel implements Channel {

	private String name = "";
	private String nick = "";
	private String format = "";
	private String formatMe = "";
	private String color = "";
	private String color2 = "";
	private boolean shortcut = false;
	private boolean focus = false;
	private double distance = 0;
	private boolean crossworlds = false;
	private double cost = 0;
	private boolean show_cost_msg = false;
	private int delay = 0;

	public BungeecordChannel(String name, String nick, String format, String meFormat, String color, boolean shortcut, boolean focus, double distance, boolean crossworlds, int delay, double cost, boolean show_cost_msg) {
		this.name = name;
		this.nick = nick;
		this.format = format;
		formatMe = meFormat;
		this.color = ChannelUtils.translateStringColor(color);
		color2 = color.toLowerCase();
		this.shortcut = shortcut;
		this.focus = focus;
		this.distance = distance;
		this.crossworlds = crossworlds;
		this.cost = cost;
		this.show_cost_msg = show_cost_msg;
		this.delay = delay;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getNickname() {
		return nick;
	}

	@Override
	public String getFormat() {
		return format;
	}

	@Override
	public String getMeFormat() {
		return formatMe;
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public String getStringColor() {
		return color2;
	}

	@Override
	public boolean isShortcutAllowed() {
		return shortcut;
	}

	@Override
	public boolean isFocusNeeded() {
		return focus;
	}

	@Override
	public boolean isCrossworlds() {
		return crossworlds;
	}

	@Override
	public double getMaxDistance() {
		return distance;
	}

	@Override
	public double getMessageCost() {
		return cost;
	}

	@Override
	public double getCostPerMessage() {
		return cost;
	}

	@Override
	public boolean showCostMessage() {
		return show_cost_msg;
	}

	@Override
	public int getDelayPerMessage() {
		return delay;
	}

	@Override
	public List<Player> getPlayersFocusedInChannel() {
		return Legendchat.getPlayerManager().getPlayersFocusedInChannel(this);
	}

	@Override
	public List<Player> getPlayersWhoCanSeeChannel() {
		return Legendchat.getPlayerManager().getPlayersWhoCanSeeChannel(this);
	}

	@Override
	public void sendMessage(final String message) {
		ChannelUtils.otherMessage(this, message);
	}

	@Override
	public void sendMessage(final Player sender, final String message) {
		ChannelUtils.fakeMessage(this, sender, message);
	}

	@Override
	public void sendMessage(Player sender, String message, String bukkit_format, boolean cancelled) {
		ChannelUtils.realMessage(this, sender, message, bukkit_format, cancelled);
	}

	@Override
	public void sendMe(Player sender, String message) {
		ChannelUtils.fakeMe(this, sender, message);
	}

	@Override
	public void sendMe(Player sender, String message, String bukkit_format, boolean cancelled) {
		ChannelUtils.realMe(this, sender, message, bukkit_format, cancelled);
	}

	public void sendBungeecordMessage(HashMap<String, String> tags, String message) {
		Set<Player> recipients = new HashSet<Player>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.hasPermission("legendchat.channel." + getName().toLowerCase() + ".chat") || p.hasPermission("legendchat.admin")) {
				recipients.add(p);
			}
		}
		BungeecordChatMessageEvent e = new BungeecordChatMessageEvent(this, message, Legendchat.format(getFormat()), getFormat(), recipients, tags, false);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return;
		}
		String completa = e.getFormat();
		if (Legendchat.blockRepeatedTags()) {
			if (e.getTags().contains("prefix") && e.getTags().contains("groupprefix")) {
				if (e.getTagValue("prefix").equals(e.getTagValue("groupprefix"))) {
					e.setTagValue("prefix", "");
				}
			}
			if (e.getTags().contains("suffix") && e.getTags().contains("groupsuffix")) {
				if (e.getTagValue("suffix").equals(e.getTagValue("groupsuffix"))) {
					e.setTagValue("suffix", "");
				}
			}
		}
		for (String n : e.getTags()) {
			completa = completa.replace("{" + n + "}", ChatColor.translateAlternateColorCodes('&', e.getTagValue(n)));
		}
		completa = completa.replace("{msg}", message);

		for (Player p : e.getRecipients()) {
			p.sendMessage(completa);
		}

		if (Legendchat.logToBukkit()) {
			Bukkit.getConsoleSender().sendMessage(completa);
		}
	}

	@Override
	public void setNickname(String n) {
		nick = n;
	}

	@Override
	public void setFormat(String n) {
		format = n;
	}

	@Override
	public void setMeFormat(String n) {
		formatMe = n;
	}

	@Override
	public void setColor(ChatColor c) {
		color2 = ChannelUtils.translateChatColorToStringColor(c);
		color = ChannelUtils.translateStringColor(color2);
	}

	@Override
	public void setShortcutAllowed(boolean n) {
		shortcut = n;
	}

	@Override
	public void setFocusNeeded(boolean n) {
		focus = n;
	}

	@Override
	public void setCrossworlds(boolean n) {
		crossworlds = n;
	}

	@Override
	public void setMaxDistance(double n) {
		distance = n;
	}

	@Override
	public void setMessageCost(double n) {
		cost = n;
	}

	@Override
	public void setCostPerMessage(double n) {
		cost = n;
	}

	@Override
	public void setShowCostMessage(boolean n) {
		show_cost_msg = n;
	}

	@Override
	public void setDelayPerMessage(int n) {
		delay = n;
	}
}
