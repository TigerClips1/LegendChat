package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.channels.types.TemporaryChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

public class TempChannelCommand implements CommandExecutor, TabCompleter {

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
		if (sender == Bukkit.getConsoleSender()) {
			return false;
		}
		if (!Legendchat.getConfigManager().getTemporaryChannelConfig().isTemporaryChannelsEnabled()) {
			return false;
		}
		if (args.length == 0) {
			Commands.sendHelpTempChannel(sender);
		} else {
			if (args[0].equalsIgnoreCase("create")) {
				if (!sender.hasPermission("legendchat.tempchannel.manager") && !sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error3"));
					return true;
				}
				if (args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc create <name> <nickname>"));
					return true;
				}
				String name = args[1];
				String nick = args[2];
				int name_max = Legendchat.getConfigManager().getTemporaryChannelConfig().getMaxChannelNameLength();
				if (name.length() > name_max) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error11").replace("@max", Integer.toString(name_max)));
					return true;
				}
				int nick_max = Legendchat.getConfigManager().getTemporaryChannelConfig().getMaxChannelNicknameLength();
				if (nick.length() > nick_max) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error12").replace("@max", Integer.toString(nick_max)));
					return true;
				}
				if (Legendchat.getConfigManager().getTemporaryChannelConfig().getBlockedNames().contains(name.toLowerCase())) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error22"));
					return true;
				}
				if (Bukkit.getPluginCommand(name.toLowerCase()) != null) {
					if (Bukkit.getPluginCommand(name.toLowerCase()).isRegistered()) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error22"));
						return true;
					}
				}

				if (Legendchat.getConfigManager().getTemporaryChannelConfig().getBlockedNames().contains(nick.toLowerCase())) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error23"));
					return true;
				}
				if (Bukkit.getPluginCommand(nick.toLowerCase()) != null) {
					if (Bukkit.getPluginCommand(nick.toLowerCase()).isRegistered()) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error23"));
						return true;
					}
				}
				Set<Permission> perms = Bukkit.getPluginManager().getDefaultPermissions(true);
				if (perms.contains(Bukkit.getPluginManager().getPermission("bukkit.command." + name))) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error22"));
					return true;
				}
				if (perms.contains(Bukkit.getPluginManager().getPermission("bukkit.command." + nick))) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error23"));
					return true;
				}
				if (Legendchat.getChannelManager().getChannelByNameOrNickname(name) != null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error1"));
					return true;
				}
				if (Legendchat.getChannelManager().getChannelByNameOrNickname(nick) != null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error2"));
					return true;
				}
				sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_msg2").replace("@channel", name));
				Legendchat.getTemporaryChannelManager().createTempChannel((Player) sender, name, nick);
				return true;
			} else if (args[0].equalsIgnoreCase("delete")) {
				if (!sender.hasPermission("legendchat.tempchannel.manager") && !sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error3"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannelsAdmin((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc delete <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 2 ? l.get(0).getName() : args[1]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (c.leader_get() != (Player) sender) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error5"));
					return true;
				}
				String msg = Legendchat.getMessageManager().getMessage("tc_msg1").replace("@channel", c.getName());
				for (Player p : c.getPlayersWhoCanSeeChannel()) {
					p.sendMessage(msg);
				}
				Legendchat.getChannelManager().deleteChannel(c);
				return true;
			} else if (args[0].equalsIgnoreCase("color")) {
				if (!sender.hasPermission("legendchat.tempchannel.manager") && !sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error3"));
					return true;
				}
				if (!sender.hasPermission("legendchat.tempchannel.color") && !sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error3"));
					return true;
				}
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc color <color-code> [channel]"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannelsAdmin((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc color <color-code> <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 3 ? l.get(0).getName() : args[2]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (c.leader_get() != (Player) sender) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error5"));
					return true;
				}
				if (Legendchat.getConfigManager().getTemporaryChannelConfig().getBlockedColors().contains(args[1].toLowerCase())) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error24"));
					return true;
				}
				boolean changed = false;
				switch (args[1].toLowerCase()) {
					case "0": {
						c.setColorByString("black");
						changed = true;
						break;
					}
					case "1": {
						c.setColorByString("darkblue");
						changed = true;
						break;
					}
					case "2": {
						c.setColorByString("darkgreen");
						changed = true;
						break;
					}
					case "3": {
						c.setColorByString("darkaqua");
						changed = true;
						break;
					}
					case "4": {
						c.setColorByString("darkred");
						changed = true;
						break;
					}
					case "5": {
						c.setColorByString("darkpurple");
						changed = true;
						break;
					}
					case "6": {
						c.setColorByString("gold");
						changed = true;
						break;
					}
					case "7": {
						c.setColorByString("gray");
						changed = true;
						break;
					}
					case "8": {
						c.setColorByString("darkgray");
						changed = true;
						break;
					}
					case "9": {
						c.setColorByString("blue");
						changed = true;
						break;
					}
					case "a": {
						c.setColorByString("green");
						changed = true;
						break;
					}
					case "b": {
						c.setColorByString("aqua");
						changed = true;
						break;
					}
					case "c": {
						c.setColorByString("red");
						changed = true;
						break;
					}
					case "d": {
						c.setColorByString("lightpurple");
						changed = true;
						break;
					}
					case "e": {
						c.setColorByString("yellow");
						changed = true;
						break;
					}
					case "f": {
						c.setColorByString("white");
						changed = true;
						break;
					}
				}
				if (changed) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_ch7").replace("@channel", c.getName()));
				} else {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error6"));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("join")) {
				if (!sender.hasPermission("legendchat.tempchannel.user") && !sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error3"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannelsInvites((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc join <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 2 ? l.get(0).getName() : args[1]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (!sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					if (!c.invite_list().contains((Player) sender)) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error7"));
						return true;
					}
					int max_joins_c = Legendchat.getConfigManager().getTemporaryChannelConfig().getMaxJoinsPerChannel();
					if (max_joins_c > 0) {
						if (c.user_list().size() >= max_joins_c) {
							sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error9"));
							return true;
						}
					}
					int max_joins_p = Legendchat.getConfigManager().getTemporaryChannelConfig().getMaxJoinsPerPlayer();
					if (max_joins_p > 0) {
						if (Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender).size() >= max_joins_p) {
							sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error10"));
							return true;
						}
					}
				}
				c.user_add((Player) sender);
				String msg = Legendchat.getMessageManager().getMessage("tc_ch1").replace("@player", sender.getName()).replace("@channel", c.getName());
				for (Player p : c.getPlayersWhoCanSeeChannel()) {
					p.sendMessage(msg);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("leave")) {
				if (!sender.hasPermission("legendchat.tempchannel.user") && !sender.hasPermission("legendchat.admin.tempchannel") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error3"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc leave <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 2 ? l.get(0).getName() : args[1]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (!c.user_list().contains((Player) sender)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error8"));
					return true;
				}
				String msg = Legendchat.getMessageManager().getMessage("tc_ch2").replace("@player", sender.getName()).replace("@channel", c.getName());
				for (Player p : c.getPlayersWhoCanSeeChannel()) {
					p.sendMessage(msg);
				}
				c.user_remove((Player) sender);
				return true;
			} else if (args[0].equalsIgnoreCase("mod")) {
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc mod <player> [channel]"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannelsAdmin((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc mod <player> <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 3 ? l.get(0).getName() : args[2]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (c.leader_get() != (Player) sender) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error5"));
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
				if (p == (Player) sender) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error9"));
					return true;
				}
				if (c.moderator_list().contains(p)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error13"));
					return true;
				}
				if (!c.user_list().contains(p)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error14"));
					return true;
				}
				int max_mods = Legendchat.getConfigManager().getTemporaryChannelConfig().getMaxModeratorsPerChannel();
				if (max_mods > 0) {
					if (c.moderator_list().size() >= max_mods) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error17"));
						return true;
					}
				}
				c.moderator_add(p);
				String msg = Legendchat.getMessageManager().getMessage("tc_ch3").replace("@player", p.getName()).replace("@channel", c.getName());
				for (Player pl : c.getPlayersWhoCanSeeChannel()) {
					pl.sendMessage(msg);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("member")) {
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc member <player> [channel]"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannelsAdmin((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc member <player> <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 3 ? l.get(0).getName() : args[2]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (c.leader_get() != (Player) sender) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error5"));
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
				if (p == (Player) sender) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error9"));
					return true;
				}
				if (!c.user_list().contains(p)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error14"));
					return true;
				}
				if (!c.moderator_list().contains(p)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error25"));
					return true;
				}
				c.moderator_remove(p);
				String msg = Legendchat.getMessageManager().getMessage("tc_ch10").replace("@player", p.getName()).replace("@channel", c.getName());
				for (Player pl : c.getPlayersWhoCanSeeChannel()) {
					pl.sendMessage(msg);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("mods")) {
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc mods <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 2 ? l.get(0).getName() : args[1]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (!c.user_list().contains((Player) sender)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error8"));
					return true;
				}
				String mods_list = "";
				for (int i = 0; i < c.moderator_list().size(); i++) {
					if (i == c.moderator_list().size() - 1) {
						mods_list += c.moderator_list().get(i).getName();
					} else {
						mods_list += c.moderator_list().get(i).getName() + ", ";
					}
				}
				sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_ch8").replace("@mods", (mods_list.length() == 0 ? "..." : mods_list)).replace("@channel", c.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("members")) {
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc members <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 2 ? l.get(0).getName() : args[1]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (!c.user_list().contains((Player) sender)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error8"));
					return true;
				}
				String members_list = "";
				for (int i = 0; i < c.user_list().size(); i++) {
					if (i == c.user_list().size() - 1) {
						members_list += c.user_list().get(i).getName();
					} else {
						members_list += c.user_list().get(i).getName() + ", ";
					}
				}
				sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_ch11").replace("@members", members_list).replace("@channel", c.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("leader")) {
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc leader <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 2 ? l.get(0).getName() : args[1]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (!c.user_list().contains((Player) sender)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error8"));
					return true;
				}
				sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_ch9").replace("@leader", c.leader_get().getName()).replace("@channel", c.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("invite")) {
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc invite <player> [channel]"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc invite <player> <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 3 ? l.get(0).getName() : args[2]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				boolean can_invite = false;
				if (c.leader_get() == (Player) sender) {
					can_invite = true;
				} else if (Legendchat.getConfigManager().getTemporaryChannelConfig().moderatorsCanInvite() && c.moderator_list().contains((Player) sender)) {
					can_invite = true;
				}
				if (!can_invite) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error18"));
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
				if (c.user_list().contains(p)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error16"));
					return true;
				}
				if (c.invite_list().contains(p)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error17"));
					return true;
				}
				c.invite_add(p);
				String msg = Legendchat.getMessageManager().getMessage("tc_ch4").replace("@player", p.getName()).replace("@channel", c.getName()).replace("@mod", sender.getName());
				for (Player pl : c.getPlayersWhoCanSeeChannel()) {
					pl.sendMessage(msg);
				}
				p.sendMessage(Legendchat.getMessageManager().getMessage("tc_msg3").replace("@player", sender.getName()).replace("@channel", c.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("kick")) {
				if (args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc kick <player> [channel]"));
					return true;
				}
				List<TemporaryChannel> l = Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender);
				if (l.isEmpty() && args.length < 2) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				if (l.size() > 1 && args.length < 3) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tc kick <player> <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
					return true;
				}
				TemporaryChannel c = Legendchat.getTemporaryChannelManager().getTempChannelByNameOrNickname((args.length < 3 ? l.get(0).getName() : args[2]));
				if (c == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error4"));
					return true;
				}
				boolean can_kick = false;
				if (c.leader_get() == (Player) sender) {
					can_kick = true;
				} else if (Legendchat.getConfigManager().getTemporaryChannelConfig().moderatorsCanKick() && c.moderator_list().contains((Player) sender)) {
					can_kick = true;
				}
				if (!can_kick) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error19"));
					return true;
				}
				Player p = Bukkit.getPlayer(args[1]);
				if (p == null) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
				if (!c.user_list().contains(p)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error14"));
					return true;
				}
				if (p == (Player) sender) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error9"));
					return true;
				}
				if (p == c.leader_get()) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error20"));
					return true;
				}
				if (c.moderator_list().contains(p) && (Player) sender != c.leader_get()) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error20"));
					return true;
				}
				String msg = Legendchat.getMessageManager().getMessage("tc_ch5").replace("@player", p.getName()).replace("@channel", c.getName()).replace("@mod", sender.getName());
				for (Player pl : c.getPlayersWhoCanSeeChannel()) {
					pl.sendMessage(msg);
				}
				c.user_remove(p);
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				int page = 1;
				if (args.length > 1) {
					try {
						page = Integer.parseInt(args[1]);
					} catch (Exception e) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error26"));
						return true;
					}
				}
				if (page < 1) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error27"));
					return true;
				}
				List<TemporaryChannel> cs = Legendchat.getTemporaryChannelManager().getAllTempChannels();
				int maxpage = (int) Math.floor(cs.size() / 9.0);
				if (maxpage == 0) {
					maxpage = 1;
				}
				if (page > maxpage) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error28").replace("@maxpage", Integer.toString(maxpage)));
					return true;
				}
				sender.sendMessage(Legendchat.getMessageManager().getMessage("tcs_list1").replace("@page", Integer.toString(page)).replace("@maxpage", Integer.toString(maxpage)));
				for (int i = page * 9 - 9; i < page * 9 - 1; i++) {
					if (cs.size() <= i) {
						if (i == 0) {
							sender.sendMessage(Legendchat.getMessageManager().getMessage("nothing"));
						}
						break;
					}
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tcs_list2").replace("@name", cs.get(i).getName()).replace("@nick", cs.get(i).getNickname()).replace("@leader", cs.get(i).leader_get().getName()));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("mychannels")) {
				String r1 = Legendchat.getMessageManager().getMessage("tc_r1");
				String r2 = Legendchat.getMessageManager().getMessage("tc_r2");
				String r3 = Legendchat.getMessageManager().getMessage("tc_r3");
				sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_msg5_1"));
				for (TemporaryChannel c : Legendchat.getTemporaryChannelManager().getPlayerTempChannels((Player) sender)) {
					String rank = r3;
					if (c.leader_get() == (Player) sender) {
						rank = r1;
					} else if (c.moderator_list().contains((Player) sender)) {
						rank = r2;
					}
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_msg5_2").replace("@name", c.getName()).replace("@nick", c.getNickname()).replace("@rank", rank));
				}
				return true;
			}
			Commands.sendHelpTempChannel(sender);
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
		if (args.length == 1) {
			boolean admin = sender.hasPermission("legendchat.admin.tempchannel") || sender.hasPermission("legendchat.admin");
			boolean manager = admin || sender.hasPermission("legendchat.tempchannel.manager");
			boolean user = admin || sender.hasPermission("legendchat.tempchannel.user");
			ArrayList<String> cmds = new ArrayList(Arrays.asList("mod", "member", "mods", "members", "leader", "invite", "kick", "list", "mychannels"));
			if (manager) {
				cmds.addAll(Arrays.asList("create", "delete", "color"));
			}
			if (user) {
				cmds.addAll(Arrays.asList("join", "leave"));
			}
			return cmds;
		}
		return null;
	}
}
