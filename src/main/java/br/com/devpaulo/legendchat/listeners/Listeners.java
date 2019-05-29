package br.com.devpaulo.legendchat.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import br.com.devpaulo.legendchat.Main;
import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.channels.types.Channel;

public class Listeners implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	private void onJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		Legendchat.joinPlayerToDefault(p);
		if (Legendchat.useJoinChatHistory() && p.hasPermission("legendchat.chathistory")) {
			Legendchat.getChannelHistory().sendHistory(p);
		}
		if (Main.need_update != null && hasAnyPermission(p)) {
			Bukkit.getServer().getScheduler().runTaskLater(Legendchat.getPlugin(), () -> {
				p.sendMessage(ChatColor.GOLD + "[Legendchat] " + ChatColor.WHITE + "New update avaible: " + ChatColor.AQUA + "V" + Main.need_update + "!");
				p.sendMessage(ChatColor.GOLD + "Download: " + ChatColor.WHITE + "http://dev.bukkit.org/bukkit-plugins/legendchat/");
			}, 60L);
		}
	}

	@EventHandler
	private void onQuit(PlayerQuitEvent e) {
		Legendchat.getPlayerManager().playerDisconnect(e.getPlayer());
		Legendchat.getPrivateMessageManager().playerDisconnect(e.getPlayer());
		Legendchat.getIgnoreManager().playerDisconnect(e.getPlayer());
		Legendchat.getTemporaryChannelManager().playerDisconnect(e.getPlayer());
		Legendchat.getAfkManager().playerDisconnect(e.getPlayer());
	}

	@EventHandler
	private void onKick(PlayerKickEvent e) {
		Legendchat.getPlayerManager().playerDisconnect(e.getPlayer());
		Legendchat.getPrivateMessageManager().playerDisconnect(e.getPlayer());
		Legendchat.getIgnoreManager().playerDisconnect(e.getPlayer());
		Legendchat.getTemporaryChannelManager().playerDisconnect(e.getPlayer());
		Legendchat.getAfkManager().playerDisconnect(e.getPlayer());
	}

	private static final HashMap<AsyncPlayerChatEvent, Boolean> chats = new HashMap<>();

	public static HashMap<AsyncPlayerChatEvent, Boolean> getChats() {
		HashMap<AsyncPlayerChatEvent, Boolean> clone = new HashMap<>();
		clone.putAll(chats);
		return clone;
	}

	public static void addFakeChat(AsyncPlayerChatEvent e, Boolean b) {
		if (!chats.containsKey(e)) {
			chats.put(e, b);
		}
	}

	public static void removeFakeChat(AsyncPlayerChatEvent e) {
		chats.remove(e);
	}

	public static boolean hasFakeChat(AsyncPlayerChatEvent e) {
		return chats.containsKey(e);
	}

	public static boolean getFakeChat(AsyncPlayerChatEvent e) {
		return chats.containsKey(e) ? chats.get(e) : true;
	}

	@EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
	private void onChat(AsyncPlayerChatEvent e) {
		HashMap<String, String> ttt = Legendchat.textToTag();
		if (ttt.size() > 0) {
			String new_format = "°1º°";
			int i = 2;
			for (String n : ttt.keySet()) {
				new_format += ttt.get(n) + ChatColor.RESET + "°" + i + "º°";
				++i;
			}
			e.setFormat(e.getFormat() + " " + new_format);
		}
	}

	@EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
	private void onChat2(AsyncPlayerChatEvent e) {
		if (e.getMessage() != null && !chats.containsKey(e) && !e.isCancelled()) {
			final Player p = e.getPlayer();
			Legendchat.getAfkManager().removeAfk(p);
			if (Legendchat.getPrivateMessageManager().isPlayerTellLocked(p)) {
				Legendchat.getPrivateMessageManager().tellPlayer(p, null, e.getMessage());
			} else {
				if (Legendchat.getPlayerManager().isPlayerFocusedInAnyChannel(p)) {
					Channel ch = Legendchat.getPlayerManager().getPlayerFocusedChannel(p);
					// Edge case: did this player lose permission to be in this channel?
					if(!p.hasPermission("legendchat.channel." + ch.getName().toLowerCase() + ".focus")) {
						// if so, automagically move them to a channel that they can join
						ch = Legendchat.joinPlayerToDefault(p);
						if(ch == null) {
							p.sendMessage(Legendchat.getMessageManager().getMessage("error1"));
							return;
						}
					}
					ch.sendMessage(p, e.getMessage(), e.getFormat(), e.isCancelled());
				} else {
					p.sendMessage(Legendchat.getMessageManager().getMessage("error1"));
				}
			}
		} else if (chats.containsKey(e)) {
			chats.remove(e);
			chats.put(e, e.isCancelled());
		}
		e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
	private void onChat(PlayerCommandPreprocessEvent e) {
		boolean block = false;
		if (Legendchat.blockShortcutsWhenCancelled()) {
			if (e.isCancelled()) {
				block = true;
			}
		}
		if (!block) {
			for (Channel c : Legendchat.getChannelManager().getChannels()) {
				String lowered_msg = e.getMessage().toLowerCase();
				if (c.isShortcutAllowed()) {
					if (lowered_msg.startsWith("/" + c.getNickname().toLowerCase())) {
						if (e.getMessage().length() == ("/" + c.getNickname()).length()) {
							e.getPlayer().sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/" + c.getNickname().toLowerCase() + " <" + Legendchat.getMessageManager().getMessage("message") + ">"));
							e.setCancelled(true);
						} else if (lowered_msg.startsWith("/" + c.getNickname().toLowerCase() + " ")) {
							String message = "";
							String[] split = e.getMessage().split(" ");
							for (int i = 1; i < split.length; ++i) {
								if (message.length() == 0) {
									message = split[i];
								} else {
									message += " " + split[i];
								}
							}
							c.sendMessage(e.getPlayer(), message);
							e.setCancelled(true);
						}
					}
					if (lowered_msg.startsWith("/" + c.getName().toLowerCase())) {
						if (e.getMessage().length() == ("/" + c.getName()).length()) {
							e.getPlayer().sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/" + c.getName().toLowerCase() + " <" + Legendchat.getMessageManager().getMessage("message") + ">"));
							e.setCancelled(true);
						} else if (lowered_msg.startsWith("/" + c.getName().toLowerCase() + " ")) {
							String message = "";
							String[] split = e.getMessage().split(" ");
							for (int i = 1; i < split.length; ++i) {
								if (message.length() == 0) {
									message = split[i];
								} else {
									message += " " + split[i];
								}
							}
							c.sendMessage(e.getPlayer(), message);
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

	protected static boolean hasAnyPermission(Player sender) {
		return sender.hasPermission("legendchat.admin")
				|| sender.hasPermission("legendchat.admin.channel")
				|| sender.hasPermission("legendchat.admin.spy")
				|| sender.hasPermission("legendchat.admin.hide")
				|| sender.hasPermission("legendchat.admin.mute")
				|| sender.hasPermission("legendchat.admin.unmute")
				|| sender.hasPermission("legendchat.admin.muteall")
				|| sender.hasPermission("legendchat.admin.unmuteall")
				|| sender.hasPermission("legendchat.admin.reload");
	}
}
