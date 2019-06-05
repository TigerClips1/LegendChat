package br.com.devpaulo.legendchat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import br.com.devpaulo.legendchat.api.Legendchat;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public class Commands implements CommandExecutor, TabCompleter {

	private final CommandSender console = Bukkit.getConsoleSender();

	public void registerCommand(PluginCommand command) {
		// maybe not the most elegant solution, but cleaner than the alternative
		switch (command.getName().toLowerCase()) {
			case "tempchannel": {
				TempChannelCommand cmd = new TempChannelCommand();
				command.setExecutor(cmd);
				command.setTabCompleter(cmd);
			}
			break;
			case "mute":
			case "channel": {
				ChannelCommand cmd = new ChannelCommand();
				command.setExecutor(cmd);
				command.setTabCompleter(cmd);
			}
			break;
			case "ignore":
			case "tell": {
				TellCommand cmd = new TellCommand();
				command.setExecutor(cmd);
				command.setTabCompleter(cmd);
			}
			break;
			case "legendchat": {
				AdminCommand cmd = new AdminCommand();
				command.setExecutor(cmd);
				command.setTabCompleter(cmd);
			}
			break;
			default:
				command.setExecutor(this);
				command.setTabCompleter(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("reply")) {
			if (sender.hasPermission("legendchat.block.reply") && !sender.hasPermission("legendchat.admin")) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/r <" + Legendchat.getMessageManager().getMessage("message") + ">"));
				return true;
			}
			if (!Legendchat.getPrivateMessageManager().playerHasReply(sender)) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("pm_error1"));
				return true;
			}
			CommandSender sendto = Legendchat.getPrivateMessageManager().getPlayerReply(sender);
			if (sendto != console) {
				if (Legendchat.getAfkManager().isAfk((Player) sendto)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_1"));
					String mot = Legendchat.getAfkManager().getPlayerAfkMotive((Player) sendto);
					if (mot != null) {
						if (mot.length() > 0) {
							sender.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_2").replace("@motive", mot));
						}
					}
					return true;
				}
			}
			String msg = "";
			for (String arg : args) {
				if (msg.length() == 0) {
					msg = arg;
				} else {
					msg += " " + arg;
				}
			}
			Legendchat.getPrivateMessageManager().replyPlayer(sender, msg);
			return true;
		} else if (cmd.getName().equalsIgnoreCase("afk")) {
			if (sender == Bukkit.getConsoleSender()) {
				return false;
			}
			if (sender.hasPermission("legendchat.block.afk") && !sender.hasPermission("legendchat.admin")) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
				return true;
			}
			if (Legendchat.getAfkManager().isAfk((Player) sender) && args.length == 0) {
				Legendchat.getAfkManager().removeAfk((Player) sender);
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message13"));
			} else {
				String mot = "";
				if (args.length > 0) {
					for (String arg : args) {
						if (mot.length() == 0) {
							mot = arg;
						} else {
							mot = " " + arg;
						}
					}
				}
				Legendchat.getAfkManager().setAfk((Player) sender,
						sender.hasPermission("legendchat.block.afkmotive") && !sender.hasPermission("legendchat.admin") ? "" : mot);
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message12"));
				if (mot.length() == 0 && !sender.hasPermission("legendchat.block.afkmotive")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/afk [" + Legendchat.getMessageManager().getMessage("reason") + "]"));
				}
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("me")) {
			final Player p = sender instanceof Player ? (Player) sender : null;
			StringJoiner msg = new StringJoiner(" ");
			for (String s : args) {
				msg.add(s);
			}
			if (p != null) {
				Legendchat.getAfkManager().removeAfk(p);

				if (Legendchat.getPlayerManager().isPlayerFocusedInAnyChannel(p)) {
					Legendchat.getPlayerManager().getPlayerFocusedChannel(p).sendMe(p, msg.toString());
				} else {
					p.sendMessage(Legendchat.getMessageManager().getMessage("error1"));
				}

			} else {
				for (Player pl : Bukkit.getOnlinePlayers()) {
					// todo? custom formatting?
					// not really an important feature, just a bit of fun.
					pl.sendMessage(ChatColor.ITALIC + "Server " + msg.toString());
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return Collections.EMPTY_LIST;
	}

	protected static void sendHelp(CommandSender sender) {
		sender.sendMessage(Legendchat.getMessageManager().getMessage("listcmd1"));
		String msg2 = Legendchat.getMessageManager().getMessage("listcmd2");
		if (sender.hasPermission("legendchat.admin.channel") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc channel <create/delete> <channel>").replace("@description", "Channel manager"));
		}
		if (sender.hasPermission("legendchat.admin.playerch") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc playerch <player> <channel>").replace("@description", "Change player channel"));
		}
		if (Legendchat.getConfigManager().getTemporaryChannelConfig().isTemporaryChannelsEnabled()) {
			if (sender.hasPermission("legendchat.admin.tempchannel") || sender.hasPermission("legendchat.admin")) {
				sender.sendMessage(msg2.replace("@command", "/lc deltc <channel>").replace("@description", "Delete a temp channel"));
			}
		}
		if (sender.hasPermission("legendchat.admin.spy") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc spy").replace("@description", "Listen to all channels"));
		}
		if (sender.hasPermission("legendchat.admin.hide") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc hide").replace("@description", "Hide from distance channels"));
		}
		if (sender.hasPermission("legendchat.admin.mute") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc mute <player> [time {minutes}]").replace("@description", "Mute a player"));
		}
		if (sender.hasPermission("legendchat.admin.unmute") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc unmute <player>").replace("@description", "Unmute a player"));
		}
		if (sender.hasPermission("legendchat.admin.muteall") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc muteall").replace("@description", "Mute all players"));
		}
		if (sender.hasPermission("legendchat.admin.unmuteall") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc unmuteall").replace("@description", "Unmute all players"));
		}
		if (sender.hasPermission("legendchat.admin.reload") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/lc reload").replace("@description", "Configuration and channels reload"));
		}
		sender.sendMessage(Legendchat.getMessageManager().getMessage("listcmd3").replace("@version", Legendchat.getPlugin().getDescription().getVersion()));
	}

	protected static void sendHelpTempChannel(CommandSender sender) {
		sender.sendMessage(Legendchat.getMessageManager().getMessage("listtc1"));
		String msg2 = Legendchat.getMessageManager().getMessage("listtc2");
		if (sender.hasPermission("legendchat.tempchannel.manager") || sender.hasPermission("legendchat.tempchannel.admin") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/tc create <name> <nick>").replace("@description", "Create a temporary channel"));
		}
		if (sender.hasPermission("legendchat.tempchannel.manager") || sender.hasPermission("legendchat.tempchannel.admin") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/tc delete [channel]").replace("@description", "Delete a temporary channel"));
		}
		if (sender.hasPermission("legendchat.tempchannel.color") || sender.hasPermission("legendchat.tempchannel.admin") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/tc color <color-code> [channel]").replace("@description", "Change channel color"));
		}
		if (sender.hasPermission("legendchat.tempchannel.user") || sender.hasPermission("legendchat.tempchannel.admin") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/tc join [channel]").replace("@description", "Join a temporary channel"));
		}
		if (sender.hasPermission("legendchat.tempchannel.user") || sender.hasPermission("legendchat.tempchannel.admin") || sender.hasPermission("legendchat.admin")) {
			sender.sendMessage(msg2.replace("@command", "/tc leave [channel]").replace("@description", "Leave a temporary channel"));
		}
		sender.sendMessage(msg2.replace("@command", "/tc mod <player> [channel]").replace("@description", "Give moderator"));
		sender.sendMessage(msg2.replace("@command", "/tc member <player> [channel]").replace("@description", "Remove moderator"));
		sender.sendMessage(msg2.replace("@command", "/tc leader [channel]").replace("@description", "Show the leader"));
		sender.sendMessage(msg2.replace("@command", "/tc mods [channel]").replace("@description", "List all moderators"));
		sender.sendMessage(msg2.replace("@command", "/tc members [channel]").replace("@description", "List all members"));
		sender.sendMessage(msg2.replace("@command", "/tc list [page]").replace("@description", "List all channels"));
		sender.sendMessage(msg2.replace("@command", "/tc invite <player> [channel]").replace("@description", "Invite to channel"));
		sender.sendMessage(msg2.replace("@command", "/tc kick <player> [channel]").replace("@description", "Kick from channel"));
		sender.sendMessage(msg2.replace("@command", "/tc mychannels").replace("@description", "List your channels"));
		sender.sendMessage(Legendchat.getMessageManager().getMessage("listtc3").replace("@version", Legendchat.getPlugin().getDescription().getVersion()));
	}

}
