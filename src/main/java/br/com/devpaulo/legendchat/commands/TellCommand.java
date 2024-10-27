package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.Legendchat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TellCommand implements CommandExecutor, TabCompleter {

	private final CommandSender console = Bukkit.getConsoleSender();

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
	public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("ignore")) {
			if (sender == Bukkit.getConsoleSender()) {
				return false;
			}
			if (args.length == 0) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/ignore <player>"));
				return true;
			}
			Player p = findPlayer(args[0]);
			if (p == null) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
				return true;
			}
			if (p == (Player) sender) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error9"));
				return true;
			}
			if (Legendchat.getIgnoreManager().hasPlayerIgnoredPlayer((Player) sender, p)) {
				Legendchat.getIgnoreManager().playerUnignorePlayer((Player) sender, p);
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message15").replace("@player", p.getName()));
			} else {
				if (p.hasPermission("legendchat.block.ignore")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error10"));
					return true;
				}
				Legendchat.getIgnoreManager().playerIgnorePlayer((Player) sender, p);
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message14").replace("@player", p.getName()));
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("tell")) {
			if (sender.hasPermission("legendchat.block.tell") && !sender.hasPermission("legendchat.admin")) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
				return true;
			}
			if (args.length == 0) {
				if (Legendchat.getPrivateMessageManager().isPlayerTellLocked(sender)) {
					Legendchat.getPrivateMessageManager().unlockPlayerTell(sender);
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message11"));
				} else {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tell <player> [" + Legendchat.getMessageManager().getMessage("message") + "]"));
				}
				return true;
			}
			CommandSender to = findPlayer(args[0]);
			if (to == null) {
				if (args[0].equalsIgnoreCase("console")) {
					to = console;
				} else {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error8"));
					return true;
				}
			}
			if (to == sender) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error9"));
				return true;
			}
			if (args.length == 1) {
				if (sender == console) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/tell <player> [" + Legendchat.getMessageManager().getMessage("message") + "]"));
					return true;
				}
				if (Legendchat.getPrivateMessageManager().isPlayerTellLocked(sender) && Legendchat.getPrivateMessageManager().getPlayerLockedTellWith(sender) == to) {
					Legendchat.getPrivateMessageManager().unlockPlayerTell(sender);
					sender.sendMessage(Legendchat.getMessageManager().getMessage("message11"));
				} else {
					if (sender.hasPermission("legendchat.block.locktell") && !sender.hasPermission("legendchat.admin")) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("error6"));
						return true;
					}
					Legendchat.getPrivateMessageManager().lockPlayerTell(sender, to);
					if (to == console) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("message10").replace("@player", to.getName()));
					} else {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("message10").replace("@player", ((Player) to).getDisplayName()));
						if (Legendchat.getAfkManager().isAfk((Player) to)) {
							sender.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_1"));
							String mot = Legendchat.getAfkManager().getPlayerAfkMotive((Player) to);
							if (mot != null && !mot.isEmpty()) {
								sender.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_2").replace("@motive", mot));
							}
						}
					}
				}
			} else {
				if (to != console) {
					if (Legendchat.getAfkManager().isAfk((Player) to)) {
						sender.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_1"));
						String mot = Legendchat.getAfkManager().getPlayerAfkMotive((Player) to);
						if (mot != null && !mot.isEmpty()) {
							sender.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_2").replace("@motive", mot));
						}
						return true;
					}
				}
				StringBuilder msg = new StringBuilder();
				for (int i = 1; i < args.length; i++) {
					if (!msg.isEmpty()) {
						msg.append(" ");
					}
					msg.append(args[i]);
				}
				Legendchat.getPrivateMessageManager().tellPlayer(sender, to, msg.toString());
			}
			return true;
		}
		return false;
	}

	/**
	 * Requests a list of possible completions for a command argument.
	 *
	 * @param sender Source of the command. For players tab-completing a command
	 * inside a command block, this will be the player, not the command
	 * block.
	 * @param command Command which was executed
	 * @param alias The alias used
	 * @param args The arguments passed to the command, including final partial
	 * argument to be completed and command label
	 * @return A List of possible completions for the final argument, or null to
	 * default to the command executor
	 */
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		if (sender instanceof Player && args.length == 1) {
			return getVisiblePlayerDisplaynames((Player) sender, false, args[0]);
		}
		return Collections.EMPTY_LIST;
	}

	public static List<String> getVisiblePlayerDisplaynames(final Player p, boolean includeSelf, String startingWith) {
		startingWith = startingWith == null || startingWith.trim().isEmpty() ? null : startingWith.toLowerCase();
		List<String> players = new ArrayList<>();
		for (Player p2 : Bukkit.getOnlinePlayers()) {
			if (p.getUniqueId().equals(p2.getUniqueId()) ? includeSelf : p.canSee(p2) && p2.getMetadata("vanished").isEmpty()) {
				String n = p2.getDisplayName().replaceFirst("^[^A-Za-z0-9_]", "");
				if (startingWith == null || n.toLowerCase().startsWith(startingWith)) {
					players.add(n == null ? p2.getName() : n);
				}
			}
		}
		return players;
	}

	public static Player findPlayer(String player) {
		Player found = Bukkit.getServer().getPlayer(player);
		if (found == null) {
			player = player.toLowerCase();
			int d = 999;
			for (Player p2 : Bukkit.getOnlinePlayers()) {
				if (p2.getDisplayName().replaceFirst("^[^A-Za-z0-9_]", "").toLowerCase().startsWith(player)) {
					int d2 = p2.getDisplayName().length() - player.length();
					if (d2 < d) {
						found = p2;
						d = d2;
					} else if (d2 == d) {
						found = null;
					}
				}
			}
		}
		return found;
	}
}