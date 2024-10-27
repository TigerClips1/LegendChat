package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.Main;
import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.channels.ChannelManager;
import br.com.devpaulo.legendchat.channels.types.Channel;
import br.com.devpaulo.legendchat.channels.types.PermanentChannel;
import br.com.devpaulo.legendchat.channels.types.TemporaryChannel;
import br.com.devpaulo.legendchat.listeners.Listeners;
import br.com.devpaulo.legendchat.listeners.Listeners_old;
import br.com.devpaulo.legendchat.updater.Updater;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class AdminCommand implements CommandExecutor, TabCompleter {

	/**
	 * Executes the given command, returning its success. <br />
	 * If false is returned, then the "usage" plugin.yml entry for this command
	 * (if defined) will be sent to the player.
	 *
	 * @param sender Source of the command
	 * @param cmd Command which was executed
	 * @param label Alias of the command which was used
	 * @param args Passed command arguments
	 * @return true if a valid command, otherwise false
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equals("clearchat")) {
			if (!sender.hasPermission("legendchat.admin.clearchat")) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
				return true;
			}
			clearChat(sender);
			return true;
		} else if (args.length == 0) {
			if (!hasAnyPermission(sender)) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
				return true;
			}
			Commands.sendHelp(sender);
		} else {
			if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("legendchat.admin.reload") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				HashMap<Player, String> focusedChannels = new HashMap();
				for (Player p : Bukkit.getOnlinePlayers()) {
					Channel c = Legendchat.getPlayerManager().getPlayerFocusedChannel(p);
					if (c != null) {
						focusedChannels.put(p, c.getName());
					}
				}
				Legendchat.load(false);
				Plugin lc = Bukkit.getPluginManager().getPlugin("Legendchat");
				lc.reloadConfig();
				Legendchat.getCensorManager().loadCensoredWords(lc.getConfig().getStringList("censor.censored_words"));
				Legendchat.getChannelManager().loadChannels();
				new Updater().updateAndLoadLanguage(lc.getConfig().getString("language"));
				Main.bungeeActive = false;
				if (lc.getConfig().getBoolean("bungeecord.use")) {
					if (Legendchat.getChannelManager().existsChannel(lc.getConfig().getString("bungeecord.channel"))) {
						Main.bungeeActive = true;
					}
				}
				PlayerJoinEvent.getHandlerList().unregister(lc);
				PlayerQuitEvent.getHandlerList().unregister(lc);
				PlayerKickEvent.getHandlerList().unregister(lc);
				AsyncPlayerChatEvent.getHandlerList().unregister(lc);
				PlayerCommandPreprocessEvent.getHandlerList().unregister(lc);
				try {
					Class.forName("org.bukkit.event.player.PlayerChatEvent");
					PlayerChatEvent.getHandlerList().unregister(lc);
				} catch (ClassNotFoundException e) {
				}
				if (lc.getConfig().getBoolean("use_async_chat_event", true)) {
					lc.getServer().getPluginManager().registerEvents(new Listeners(), lc);
				} else {
					lc.getServer().getPluginManager().registerEvents(new Listeners_old(), lc);
				}
				Legendchat.load(true);
				for (Map.Entry<Player, String> e : focusedChannels.entrySet()) {
					Channel c = Legendchat.getChannelManager().getChannelByName(e.getValue());
					if (c != null) {
						Legendchat.getPlayerManager().setPlayerFocusedChannel(e.getKey(), c, false);
					}
				}
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message2"));
				return true;
			} else if (args[0].equalsIgnoreCase("channel")) {
				if (!sender.hasPermission("legendchat.admin.channel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc channel <create/delete> <channel-name>"));
					return true;
				}
				if (args[1].equalsIgnoreCase("create")) {
					Channel c = Legendchat.getChannelManager().getChannelByName(args[2].toLowerCase());
					if (c != null) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("error7"));
						return true;
					}
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message3").replace("@channel", args[2]));
					Legendchat.getChannelManager().createPermanentChannel(new PermanentChannel(WordUtils.capitalizeFully(args[2]), Character.toString(args[2].charAt(0)).toLowerCase(), "{default}", "{me}", "GRAY", true, false, 0, true, 0, 0, false));
				} else if (args[1].equalsIgnoreCase("delete")) {
					Channel c = Legendchat.getChannelManager().getChannelByName(args[2].toLowerCase());
					if (c == null) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("error4"));
						return true;
					}
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message4").replace("@channel", c.getName()));
					Legendchat.getChannelManager().deleteChannel(c);
				} else {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc channel <create/delete> <channel-name>"));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("playerch")) {
				if (!sender.hasPermission("legendchat.admin.playerch") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc playerch <player> <channel-name>"));
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
				Channel c;
				ChannelManager cm = Legendchat.getChannelManager();
				c = cm.getChannelByName(args[2]);
				if (c == null) {
					c = cm.getChannelByNickname(args[2]);
				}
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error4"));
					return true;
				}
				Legendchat.getPlayerManager().setPlayerFocusedChannel(p, c, false);
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message16").replace("@player", p.getName()).replace("@channel", c.getName()));
				p.sendMessage(Legendchat.getMessageManager().getMessage("message17").replace("@player", sender.getName()).replace("@channel", c.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("spy")) {
				if (!sender.hasPermission("legendchat.admin.spy") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (sender == Bukkit.getConsoleSender()) {
					return false;
				}
				Player player = (Player) sender;
				boolean spy = Legendchat.getPlayerManager().isSpy(player);
				if (!spy) {
					Legendchat.getPlayerManager().addSpy(player);
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message5"));
				} else {
					Legendchat.getPlayerManager().removeSpy(player);
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message6"));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("hide")) {
				if (!sender.hasPermission("legendchat.admin.hide") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (sender == Bukkit.getConsoleSender()) {
					return false;
				}
				Player player = (Player) sender;
				boolean hidden = Legendchat.getPlayerManager().isPlayerHiddenFromRecipients(player);
				if (!hidden) {
					Legendchat.getPlayerManager().hidePlayerFromRecipients(player);
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message7"));
				} else {
					Legendchat.getPlayerManager().showPlayerToRecipients(player);
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message8"));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("mute")) {
				if (!sender.hasPermission("legendchat.admin.mute") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc mute <player> [time {minutes}]"));
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
				int time = 0;
				if (args.length > 2) {
					try {
						time = Integer.parseInt(args[2]);
					} catch (Exception e) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error1"));
						return true;
					}
				}
				if (time < 0) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error1"));
					return true;
				}
				if (Legendchat.getMuteManager().isPlayerMuted(p.getName())) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error2"));
					return true;
				}
				Legendchat.getMuteManager().mutePlayer(p.getName(), time);
				if (time != 0) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_msg3").replace("@player", p.getName()).replace("@time", Integer.toString(time)));
					p.sendMessage(Legendchat.getMessageManager().getMessage("mute_msg4").replace("@player", sender.getName()).replace("@time", Integer.toString(time)));
				} else {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_msg1").replace("@player", p.getName()));
					p.sendMessage(Legendchat.getMessageManager().getMessage("mute_msg2").replace("@player", sender.getName()));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("unmute")) {
				if (!sender.hasPermission("legendchat.admin.unmute") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc unmute <player>"));
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
				if (!Legendchat.getMuteManager().isPlayerMuted(p.getName())) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error3"));
					return true;
				}
				Legendchat.getMuteManager().unmutePlayer(p.getName());
				sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_msg5").replace("@player", p.getName()));
				p.sendMessage(Legendchat.getMessageManager().getMessage("mute_msg6").replace("@player", sender.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("muteall")) {
				if (!sender.hasPermission("legendchat.admin.muteall") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (Legendchat.getMuteManager().isServerMuted()) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error6"));
					return true;
				}
				Legendchat.getMuteManager().muteServer();
				Bukkit.broadcastMessage(Legendchat.getMessageManager().getMessage("mute_msg7").replace("@player", sender.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("unmuteall")) {
				if (!sender.hasPermission("legendchat.admin.muteall") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (!Legendchat.getMuteManager().isServerMuted()) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("mute_error7"));
					return true;
				}
				Legendchat.getMuteManager().unmuteServer();
				Bukkit.broadcastMessage(Legendchat.getMessageManager().getMessage("mute_msg8").replace("@player", sender.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("deltc")) {
				if (!sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
					return true;
				}
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/lc deltc <channel>"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname(args[1]);
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				String msg = Legendchat.getMessageManager().getMessage("tc_msg4").replace("@channel", c.getName()).replace("@player", sender.getName());
				sender.sendMessage(msg);
				c.leader_get().sendMessage(msg);
				Legendchat.getTemporaryChannelManager().deleteTempChannel(c);
				return true;
			}
			if (!hasAnyPermission(sender)) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
				return true;
			}
			Commands.sendHelp(sender);
		}
		return true;
	}

	/**
	 * Requests a list of possible completions for a command argument.
	 *
	 * @param sender Source of the command. For players tab-completing a command
	 * inside of a command block, this will be the player, not the command
	 * block.
	 * @param command Command which was executed
	 * @param alias The alias used
	 * @param args The arguments passed to the command, including final partial
	 * argument to be completed and command label
	 * @return A List of possible completions for the final argument, or null to
	 * default to the command executor
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1 && command.getName().equals("legendchat")) {
			boolean admin = sender.hasPermission("legendchat.admin");
			ArrayList<String> cmds = new ArrayList();
			for (String cmd : Arrays.asList("reload", "channel", "playerch", "spy", "hide", "mute", "unmute", "muteall")) {
				if (admin || sender.hasPermission("legendchat.admin." + cmd)) {
					cmds.add(cmd);
				}
			}
			if (admin || sender.hasPermission("legendchat.admin.muteall")) {
				cmds.add("unmuteall");
			}
			if (admin || sender.hasPermission("legendchat.admin.tempchannel")) {
				cmds.add("deltc");
			}
			return cmds;
		}
		return Collections.EMPTY_LIST;
	}

	protected static boolean hasAnyPermission(CommandSender sender) {
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

	void clearChat(CommandSender sender) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if(p != sender) {
				if (p.hasPermission("legendchat.admin.clearchat") || p.hasPermission("legendchat.admin")) {
					p.sendMessage(Legendchat.getMessageManager().getMessage("cc1").replace("@player", sender.getName()));
				} else {
					for(int i = 0; i < 100; ++i) {
						p.sendRawMessage("");
					}
				}
			}
		}
		sender.sendMessage(Legendchat.getMessageManager().getMessage("cc1").replace("@player", sender.getName()));
	}
}
