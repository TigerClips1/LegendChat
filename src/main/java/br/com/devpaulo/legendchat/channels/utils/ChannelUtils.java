package br.com.devpaulo.legendchat.channels.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;

import br.com.devpaulo.legendchat.Main;
import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import br.com.devpaulo.legendchat.channels.types.BungeecordChannel;
import br.com.devpaulo.legendchat.channels.types.Channel;
import br.com.devpaulo.legendchat.channels.types.TemporaryChannel;
import br.com.devpaulo.legendchat.listeners.Listeners;
import br.com.devpaulo.legendchat.listeners.Listeners_old;

@SuppressWarnings("deprecation")
public class ChannelUtils {

	public static void fakeMe(final Channel c, final Player sender, final String message) {
		if (!Legendchat.sendFakeMessageToChat()) {
			c.sendMe(sender, message, "", false);
			return;
		}
		if (!Legendchat.useAsyncChat()) {
			PlayerChatEvent event = new PlayerChatEvent(sender, "* " + message + " *");
			Listeners_old.addFakeChat(event, false);
			Bukkit.getPluginManager().callEvent(event);
			c.sendMe(sender, event.getMessage().replaceAll("^\\* | \\*$", ""), event.getFormat(), Listeners_old.getFakeChat(event));
			Listeners_old.removeFakeChat(event);
		} else {
			HashSet<Player> p = new HashSet<>();
			p.add(sender);
			final AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender, "* " + message + " *", p);
			Listeners.addFakeChat(event, false);
			Bukkit.getScheduler().runTaskAsynchronously(Legendchat.getPlugin(), () -> {
				Bukkit.getPluginManager().callEvent(event);
				c.sendMe(sender, event.getMessage().replaceAll("^\\* | \\*$", ""), event.getFormat(), Listeners.getFakeChat(event));
				Listeners.removeFakeChat(event);
			});
		}
	}

	public static void fakeMessage(final Channel c, final Player sender, final String message) {
		if (!Legendchat.sendFakeMessageToChat()) {
			c.sendMessage(sender, message, "", false);
			return;
		}
		if (!Legendchat.useAsyncChat()) {
			PlayerChatEvent event = new PlayerChatEvent(sender, message);
			Listeners_old.addFakeChat(event, false);
			Bukkit.getPluginManager().callEvent(event);
			c.sendMessage(sender, message, event.getFormat(), Listeners_old.getFakeChat(event));
			Listeners_old.removeFakeChat(event);
		} else {
			HashSet<Player> p = new HashSet<>();
			p.add(sender);
			final AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, sender, message, p);
			Listeners.addFakeChat(event, false);
			Bukkit.getScheduler().runTaskAsynchronously(Legendchat.getPlugin(), () -> {
				Bukkit.getPluginManager().callEvent(event);
				c.sendMessage(sender, message, event.getFormat(), Listeners.getFakeChat(event));
				Listeners.removeFakeChat(event);
			});
		}
	}

	public static void realMe(Channel c, Player sender, String message, String bukkit_format, boolean cancelled) {
		realMessage(c, sender, "* " + message + " *", "%me%" + c.getMeFormat(), bukkit_format, cancelled);
	}

	public static void realMessage(Channel c, Player sender, String message, String bukkit_format, boolean cancelled) {
		realMessage(c, sender, message, c.getFormat(), bukkit_format, cancelled);
	}

	private static void sendMessage(CommandSender sender, String message) {
		if (sender instanceof Audience) {
			Audience audience = (Audience) sender;
			Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
			audience.sendMessage(component);
		} else {
			sender.sendMessage(message);
		}
	}

	public static void realMessage(Channel c, Player sender, String message, String channel_format, String bukkit_format, boolean cancelled) {
		if (c instanceof TemporaryChannel) {
			if (!((TemporaryChannel) c).user_list().contains(sender)) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error8"));
				return;
			}
		} else if ((!sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".chat")
				|| sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".blockwrite"))
				&& !sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(Legendchat.getMessageManager().getMessage("error2"));
			return;
		}
		if (c.isFocusNeeded()) {
			if (Legendchat.getPlayerManager().getPlayerFocusedChannel(sender) != c) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error12"));
				return;
			}
		}
		int delay = Legendchat.getDelayManager().getPlayerDelayFromChannel(sender.getName(), c);
		if (delay > 0) {
			sender.sendMessage(Legendchat.getMessageManager().getMessage("error11").replace("@time", Integer.toString(delay)));
			return;
		}
		if (Legendchat.getMuteManager().isPlayerMuted(sender.getName())) {
			int time = Legendchat.getMuteManager().getPlayerMuteTimeLeft(sender.getName());
			if (time == 0) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error4"));
			} else {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error5").replace("@time", Integer.toString(time)));
			}
			return;
		}
		if (Legendchat.getMuteManager().isServerMuted()) {
			sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error8"));
			return;
		}
		if (Legendchat.getIgnoreManager().hasPlayerIgnoredChannel(sender, c)) {
			sender.sendMessage(Legendchat.getMessageManager().getMessage("error14"));
			return;
		}
		Set<Player> recipients = new HashSet<>();
		if (c instanceof TemporaryChannel) {
			recipients.addAll(((TemporaryChannel) c).user_list());
		} else {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".chat") || p.hasPermission("legendchat.admin")) {
					recipients.add(p);
				}
			}
		}
		Set<Player> recipients2 = new HashSet<>();
		recipients2.addAll(recipients);

		for (Player p : recipients2) {
			if (c.getMaxDistance() != 0) {
				if (sender.getWorld() != p.getWorld()) {
					recipients.remove(p);
					continue;
				} else if (sender.getLocation().distance(p.getLocation()) > c.getMaxDistance()) {
					recipients.remove(p);
					continue;
				}
			} else {
				if (!c.isCrossworlds()) {
					if (sender.getWorld() != p.getWorld()) {
						recipients.remove(p);
						continue;
					}
				}
			}
			if (Legendchat.getIgnoreManager().hasPlayerIgnoredPlayer(p, sender)) {
				recipients.remove(p);
				continue;
			}
			if (Legendchat.getIgnoreManager().hasPlayerIgnoredChannel(p, c)) {
				recipients.remove(p);
				continue;
			}
			if (c.isFocusNeeded()) {
				if (Legendchat.getPlayerManager().getPlayerFocusedChannel(p) != c) {
					recipients.remove(p);
				}
			}
		}

		// if player paid for message send
		boolean gastou = false;
		if (!Main.block_econ && c.getMessageCost() > 0) {
			if (!sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".free") && !sender.hasPermission("legendchat.admin")) {
				if (Main.econ.getBalance(sender) < c.getMessageCost()) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error3").replace("@price", Double.toString(c.getMessageCost())));
					return;
				}
				// witdraw if not canceled
				gastou = true;
			}
		}
		String n_format_p_p = "";
		String n_format_p = "";
		String n_format_s = "";
		if (bukkit_format.contains("<") && bukkit_format.contains(">")) {
			String name_code = null;
			if (bukkit_format.contains("%1$s")) {
				name_code = "%1$s";
			} else if (bukkit_format.contains("%s")) {
				name_code = "%s";
			}
			int seploc = bukkit_format.indexOf(name_code);
			int finalloc = -1;
			for (int i = seploc; i >= 0; --i) {
				if (bukkit_format.charAt(i) == '<') {
					finalloc = i;
					break;
				}
			}
			if (finalloc != -1) {
				n_format_p_p = bukkit_format.substring(0, finalloc);
				if (name_code != null) {
					String[] n_format = bukkit_format.substring(finalloc + 1).split(">")[0].split(name_code);
					if (n_format.length > 0) {
						n_format_p = n_format[0].replace(name_code, "").replace("{factions_relcolor}", "");
					}
					if (n_format.length > 1) {
						n_format_s = n_format[1];
					}
				}
			}
		}
		HashMap<String, String> tags = new HashMap<>();
		tags.put("name", c.getName());
		tags.put("nick", c.getNickname());
		tags.put("color", c.getColor());
		tags.put("sender", sender.getDisplayName());
		tags.put("plainsender", sender.getName());
		tags.put("world", sender.getWorld().getName());
		tags.put("bprefix", (Legendchat.forceRemoveDoubleSpacesFromBukkit() ? (n_format_p_p.equals(" ") ? "" : n_format_p_p.replace("  ", " ")) : n_format_p_p));
		tags.put("bprefix2", (Legendchat.forceRemoveDoubleSpacesFromBukkit() ? (n_format_p.equals(" ") ? "" : n_format_p.replace("  ", " ")) : n_format_p));
		tags.put("bsuffix", (Legendchat.forceRemoveDoubleSpacesFromBukkit() ? (n_format_s.equals(" ") ? "" : n_format_s.replace("  ", " ")) : n_format_s));
		tags.put("server", Legendchat.getMessageManager().getMessage("bungeecord_server"));
		tags.put("time_hour", Integer.toString(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
		tags.put("time_min", Integer.toString(Calendar.getInstance().get(Calendar.MINUTE)));
		tags.put("time_sec", Integer.toString(Calendar.getInstance().get(Calendar.SECOND)));
		tags.put("date_day", Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
		tags.put("date_month", Integer.toString(Calendar.getInstance().get(Calendar.MONTH)));
		tags.put("date_year", Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		if (!Main.block_chat) {
			tags.put("prefix", tag(Main.chat.getPlayerPrefix(sender)));
			tags.put("suffix", tag(Main.chat.getPlayerSuffix(sender)));
			tags.put("groupprefix", tag(Main.chat.getGroupPrefix(sender.getWorld(), Main.chat.getPrimaryGroup(sender))));
			tags.put("groupsuffix", tag(Main.chat.getGroupSuffix(sender.getWorld(), Main.chat.getPrimaryGroup(sender))));
			for (String g : Main.chat.getPlayerGroups(sender)) {
				tags.put(g.toLowerCase() + "prefix", tag(Main.chat.getGroupPrefix(sender.getWorld(), g)));
				tags.put(g.toLowerCase() + "suffix", tag(Main.chat.getGroupSuffix(sender.getWorld(), g)));
			}
		}
		HashMap<String, String> ttt = Legendchat.textToTag();
		if (!ttt.isEmpty()) {
			int i = 1;
			for (String n : ttt.keySet()) {
				String tag;
				try {
					tag = bukkit_format.split("°" + i + "º°")[1].split("°" + (i + 1) + "º°")[0];
				} catch (Exception e) {
					tag = "";
				}
				tags.put(n, tag);
				++i;
			}
		}
		ChatMessageEvent e = new ChatMessageEvent(c, sender, message, Legendchat.format(channel_format), channel_format, bukkit_format, recipients, tags, cancelled);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return;
		} else if(gastou) {
			Main.econ.withdrawPlayer(sender, c.getMessageCost());
		}
		sender = e.getSender();
		message = e.getMessage();
		String completa = e.getFormat();
		if (completa.startsWith("%me%")) {
			message = message.replaceAll("^\\* | \\*$", "");
			completa = completa.substring("%me%".length());
		}
		if (Legendchat.isCensorActive()) {
			message = Legendchat.getCensorManager().censorFunction(message);
		}
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
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			completa = me.clip.placeholderapi.PlaceholderAPI.setBracketPlaceholders(sender, completa);
		}
		for (String n : e.getTags()) {
			completa = completa.replace("{" + n + "}", ChatColor.translateAlternateColorCodes('&', e.getTagValue(n)));
		}
		completa = completa.replace("{msg}", translateAlternateChatColorsWithPermission(sender, message));

		for (Player p : e.getRecipients()) {
			p.sendMessage(LegacyComponentSerializer.legacySection().serialize(Component.text(completa)));
		}

		if (c.getDelayPerMessage() > 0 && !sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".nodelay") && !sender.hasPermission("legendchat.admin")) {
			Legendchat.getDelayManager().addPlayerDelay(sender.getName(), c);
		}

		if (c.getMaxDistance() != 0) {
			if (Legendchat.showNoOneHearsYou()) {
				boolean show;
				if (e.getRecipients().isEmpty()) {
					show = true;
				} else if (e.getRecipients().size() == 1 && e.getRecipients().contains(sender)) {
					show = true;
				} else {
					show = true;
					for (Player p : e.getRecipients()) {
						if (p != sender && !Legendchat.getPlayerManager().isPlayerHiddenFromRecipients(p)) {
							show = false;
							break;
						}
					}
				}
				if (show) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("special"));
				}
			}
		}

		for (Player p : Legendchat.getPlayerManager().getOnlineSpys()) {
			if (!e.getRecipients().contains(p)) {
				p.sendMessage(LegacyComponentSerializer.legacySection().serialize(Component.text(ChatColor.translateAlternateColorCodes('&', Legendchat.getFormat("spy").replace("{msg}", ChatColor.stripColor(completa))))));
			}
		}

		if (gastou) {
			if (c.showCostMessage()) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message9").replace("@money", Double.toString(c.getCostPerMessage())));
			}
		}

		if (Legendchat.logToBukkit()) {
			Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacySection().serialize(Component.text(completa)));
		}

		if (Legendchat.logToFile()) {
			Legendchat.getLogManager().addLogToCache(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', completa)), sender.getLocation());
		}

		if (Legendchat.useJoinChatHistory()) {
			Legendchat.getChannelHistory().addLogToCache(c, ChatColor.translateAlternateColorCodes('&', completa));
		}

		if (c instanceof BungeecordChannel) {
			if (Legendchat.isBungeecordActive()) {
				if (Legendchat.getBungeecordChannel() == ((BungeecordChannel) c)) {
					ByteArrayOutputStream b = new ByteArrayOutputStream();
					DataOutputStream out = new DataOutputStream(b);
					try {
						HashMap<String, String> tags_packet = new HashMap<>();
						for (String tag_packet : e.getTags()) {
							tags_packet.put(tag_packet, e.getTagValue(tag_packet));
						}
						out.writeUTF(tags_packet.toString());
						out.writeUTF(translateAlternateChatColorsWithPermission(sender, message));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					sender.sendPluginMessage(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("Legendchat")), "agnc:agnc", b.toByteArray());
				}
			}
		}
	}

	public static void otherMessage(Channel c, String message) {
		Set<Player> recipients = new HashSet<>();
		if (c instanceof TemporaryChannel) {
			recipients.addAll(((TemporaryChannel) c).user_list());
		} else {
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".chat") || p.hasPermission("legendchat.admin")) {
					recipients.add(p);
				}
			}
		}
		Set<Player> recipients2 = new HashSet<>(recipients);

		for (Player p : recipients2) {
			if (Legendchat.getIgnoreManager().hasPlayerIgnoredChannel(p, c)) {
				recipients.remove(p);
				continue;
			}
			if (c.isFocusNeeded()) {
				if (Legendchat.getPlayerManager().getPlayerFocusedChannel(p) != c) {
					recipients.remove(p);
				}
			}
		}

		for (Player p : recipients) {
			p.sendMessage(LegacyComponentSerializer.legacySection().serialize(Component.text(message)));
		}

		if (Legendchat.logToBukkit()) {
			Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacySection().serialize(Component.text(message)));
		}

		if (Legendchat.logToFile()) {
			Legendchat.getLogManager().addLogToCache(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
		}

		if (Legendchat.useJoinChatHistory()) {
			Legendchat.getChannelHistory().addLogToCache(c, ChatColor.translateAlternateColorCodes('&', message));
		}
	}

	public static String translateStringColor(String color) {
		return switch (color.toLowerCase().replace("_", "")) {
			case "black" -> ChatColor.BLACK.toString();
			case "darkblue" -> ChatColor.DARK_BLUE.toString();
			case "darkgreen" -> ChatColor.DARK_GREEN.toString();
			case "darkaqua" -> ChatColor.DARK_AQUA.toString();
			case "darkred" -> ChatColor.DARK_RED.toString();
			case "darkpurple" -> ChatColor.DARK_PURPLE.toString();
			case "gold" -> ChatColor.GOLD.toString();
			case "gray" -> ChatColor.GRAY.toString();
			case "darkgray" -> ChatColor.DARK_GRAY.toString();
			case "blue" -> ChatColor.BLUE.toString();
			case "green" -> ChatColor.GREEN.toString();
			case "aqua" -> ChatColor.AQUA.toString();
			case "red" -> ChatColor.RED.toString();
			case "lightpurple" -> ChatColor.LIGHT_PURPLE.toString();
			case "yellow" -> ChatColor.YELLOW.toString();
			default -> ChatColor.WHITE.toString();
		};
	}

	public static ChatColor translateStringColorToChatColor(String color) {
		return switch (color.toLowerCase().replace("_", "")) {
			case "black" -> ChatColor.BLACK;
			case "darkblue" -> ChatColor.DARK_BLUE;
			case "darkgreen" -> ChatColor.DARK_GREEN;
			case "darkaqua" -> ChatColor.DARK_AQUA;
			case "darkred" -> ChatColor.DARK_RED;
			case "darkpurple" -> ChatColor.DARK_PURPLE;
			case "gold" -> ChatColor.GOLD;
			case "gray" -> ChatColor.GRAY;
			case "darkgray" -> ChatColor.DARK_GRAY;
			case "blue" -> ChatColor.BLUE;
			case "green" -> ChatColor.GREEN;
			case "aqua" -> ChatColor.AQUA;
			case "red" -> ChatColor.RED;
			case "lightpurple" -> ChatColor.LIGHT_PURPLE;
			case "yellow" -> ChatColor.YELLOW;
			default -> ChatColor.WHITE;
		};
	}

	public static String translateChatColorToStringColor(ChatColor color) {
		return switch (color) {
			case BLACK -> "black";
			case DARK_BLUE -> "darkblue";
			case DARK_GREEN -> "darkgreen";
			case DARK_AQUA -> "darkaqua";
			case DARK_RED -> "darkred";
			case DARK_PURPLE -> "darkpurple";
			case GOLD -> "gold";
			case GRAY -> "gray";
			case DARK_GRAY -> "darkgray";
			case BLUE -> "blue";
			case GREEN -> "green";
			case AQUA -> "aqua";
			case RED -> "red";
			case LIGHT_PURPLE -> "lightpurple";
			case YELLOW -> "yellow";
			default -> "white";
		};
	}

	public static TextColor translateStringColorToTextColor(String color) {
		return switch (color.toLowerCase().replace("_", "")) {
			case "black" -> NamedTextColor.BLACK;
			case "darkblue" -> NamedTextColor.DARK_BLUE;
			case "darkgreen" -> NamedTextColor.DARK_GREEN;
			case "darkaqua" -> NamedTextColor.DARK_AQUA;
			case "darkred" -> NamedTextColor.DARK_RED;
			case "darkpurple" -> NamedTextColor.DARK_PURPLE;
			case "gold" -> NamedTextColor.GOLD;
			case "gray" -> NamedTextColor.GRAY;
			case "darkgray" -> NamedTextColor.DARK_GRAY;
			case "blue" -> NamedTextColor.BLUE;
			case "green" -> NamedTextColor.GREEN;
			case "aqua" -> NamedTextColor.AQUA;
			case "red" -> NamedTextColor.RED;
			case "lightpurple" -> NamedTextColor.LIGHT_PURPLE;
			case "yellow" -> NamedTextColor.YELLOW;
			default -> NamedTextColor.WHITE;
		};
	}

	public static String translateTextColorToStringColor(TextColor color) {
		if (color.equals(NamedTextColor.BLACK)) return "black";
		if (color.equals(NamedTextColor.DARK_BLUE)) return "darkblue";
		if (color.equals(NamedTextColor.DARK_GREEN)) return "darkgreen";
		if (color.equals(NamedTextColor.DARK_AQUA)) return "darkaqua";
		if (color.equals(NamedTextColor.DARK_RED)) return "darkred";
		if (color.equals(NamedTextColor.DARK_PURPLE)) return "darkpurple";
		if (color.equals(NamedTextColor.GOLD)) return "gold";
		if (color.equals(NamedTextColor.GRAY)) return "gray";
		if (color.equals(NamedTextColor.DARK_GRAY)) return "darkgray";
		if (color.equals(NamedTextColor.BLUE)) return "blue";
		if (color.equals(NamedTextColor.GREEN)) return "green";
		if (color.equals(NamedTextColor.AQUA)) return "aqua";
		if (color.equals(NamedTextColor.RED)) return "red";
		if (color.equals(NamedTextColor.LIGHT_PURPLE)) return "lightpurple";
		if (color.equals(NamedTextColor.YELLOW)) return "yellow";
		return "white";
	}

	private static String tag(String tag) {
		if (tag == null) {
			return "";
		}
		return tag;
	}

	public static String translateAlternateChatColorsWithPermission(Player p, String msg) {
		final boolean admin = p.hasPermission("legendchat.color.allcolors") || p.hasPermission("legendchat.admin");
		msg = replaceColorCode(msg, "&0", ChatColor.BLACK, NamedTextColor.BLACK, admin, p, "legendchat.color.black");
		msg = replaceColorCode(msg, "&1", ChatColor.DARK_BLUE, NamedTextColor.DARK_BLUE, admin, p, "legendchat.color.darkblue");
		msg = replaceColorCode(msg, "&2", ChatColor.DARK_GREEN, NamedTextColor.DARK_GREEN, admin, p, "legendchat.color.darkgreen");
		msg = replaceColorCode(msg, "&3", ChatColor.DARK_AQUA, NamedTextColor.DARK_AQUA, admin, p, "legendchat.color.darkaqua");
		msg = replaceColorCode(msg, "&4", ChatColor.DARK_RED, NamedTextColor.DARK_RED, admin, p, "legendchat.color.darkred");
		msg = replaceColorCode(msg, "&5", ChatColor.DARK_PURPLE, NamedTextColor.DARK_PURPLE, admin, p, "legendchat.color.darkpurple");
		msg = replaceColorCode(msg, "&6", ChatColor.GOLD, NamedTextColor.GOLD, admin, p, "legendchat.color.gold");
		msg = replaceColorCode(msg, "&7", ChatColor.GRAY, NamedTextColor.GRAY, admin, p, "legendchat.color.gray");
		msg = replaceColorCode(msg, "&8", ChatColor.DARK_GRAY, NamedTextColor.DARK_GRAY, admin, p, "legendchat.color.darkgray");
		msg = replaceColorCode(msg, "&9", ChatColor.BLUE, NamedTextColor.BLUE, admin, p, "legendchat.color.blue");
		msg = replaceColorCode(msg, "&a", ChatColor.GREEN, NamedTextColor.GREEN, admin, p, "legendchat.color.green");
		msg = replaceColorCode(msg, "&b", ChatColor.AQUA, NamedTextColor.AQUA, admin, p, "legendchat.color.aqua");
		msg = replaceColorCode(msg, "&c", ChatColor.RED, NamedTextColor.RED, admin, p, "legendchat.color.red");
		msg = replaceColorCode(msg, "&d", ChatColor.LIGHT_PURPLE, NamedTextColor.LIGHT_PURPLE, admin, p, "legendchat.color.lightpurple");
		msg = replaceColorCode(msg, "&e", ChatColor.YELLOW, NamedTextColor.YELLOW, admin, p, "legendchat.color.yellow");
		msg = replaceColorCode(msg, "&f", ChatColor.WHITE, NamedTextColor.WHITE, admin, p, "legendchat.color.white");
		msg = replaceColorCode(msg, "&k", ChatColor.MAGIC, TextDecoration.OBFUSCATED, admin, p, "legendchat.color.obfuscated", "legendchat.color.obfuscate");
		msg = replaceColorCode(msg, "&l", ChatColor.BOLD, TextDecoration.BOLD, admin, p, "legendchat.color.bold");
		msg = replaceColorCode(msg, "&m", ChatColor.STRIKETHROUGH, TextDecoration.STRIKETHROUGH, admin, p, "legendchat.color.strikethrough");
		msg = replaceColorCode(msg, "&n", ChatColor.UNDERLINE, TextDecoration.UNDERLINED, admin, p, "legendchat.color.underline");
		msg = replaceColorCode(msg, "&o", ChatColor.ITALIC, TextDecoration.ITALIC, admin, p, "legendchat.color.italic");
		//msg = replaceColorCode(msg, "&r", ChatColor.RESET, TextDecoration.NONE, admin, p, "legendchat.color.reset");
		return msg;
	}

	private static String replaceColorCode(String msg, String code, ChatColor chatColor, TextColor textColor, boolean admin, Player p, String... permissions) {
		for (String permission : permissions) {
			if (msg.contains(code) && (admin || p.hasPermission(permission))) {
				msg = msg.replace(code, chatColor.toString());
				break;
			}
		}
		return msg;
	}

	private static String replaceColorCode(String msg, String code, ChatColor chatColor, TextDecoration textDecoration, boolean admin, Player p, String... permissions) {
		for (String permission : permissions) {
			if (msg.contains(code) && (admin || p.hasPermission(permission))) {
				msg = msg.replace(code, chatColor.toString());
				break;
			}
		}
		return msg;
	}
}
