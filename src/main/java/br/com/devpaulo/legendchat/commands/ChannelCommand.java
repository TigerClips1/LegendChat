package br.com.devpaulo.legendchat.commands;

import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.channels.ChannelManager;
import br.com.devpaulo.legendchat.channels.types.Channel;
import br.com.devpaulo.legendchat.channels.types.TemporaryChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ChannelCommand implements CommandExecutor, TabCompleter {

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
		if (!(sender instanceof Player)) {
			return false;
		}
		if (args.length == 0) {
			sender.sendMessage(Legendchat.getMessageManager().getMessage("wrongcmd").replace("@command", "/" + label + " <" + Legendchat.getMessageManager().getMessage("channel") + ">"));
			String mlist = "";
			for (Channel c : Legendchat.getChannelManager().getChannels()) {
				if (Legendchat.getPlayerManager().canPlayerSeeChannel((Player) sender, c)) {
					if (mlist.length() == 0) {
						mlist = c.getName();
					} else {
						mlist += ", " + c.getName();
					}
				}
			}
			if (cmd.getName().equalsIgnoreCase("mute")) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message20").replace("@channels", (mlist.length() == 0 ? "..." : mlist)));
			} else {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message21").replace("@channels", (mlist.length() == 0 ? Legendchat.getMessageManager().getMessage("nothing") : mlist)));
			}
		} else if (cmd.getName().equalsIgnoreCase("mute")) { // Mute channel
			Channel c = Legendchat.getChannelManager().getChannelByNameOrNickname(args[0]);
			if (c == null) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error4"));
				return true;
			}
			if (Legendchat.getIgnoreManager().hasPlayerIgnoredChannel((Player) sender, c)) {
				Legendchat.getIgnoreManager().playerUnignoreChannel((Player) sender, c);
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message19").replace("@channel", c.getName()));
			} else {
				if (sender.hasPermission("legendchat.channel." + c.getName().toLowerCase() + ".blockmute") && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error13"));
					return true;
				}
				if (!c.getPlayersWhoCanSeeChannel().contains((Player) sender)) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("error4"));
					return true;
				}
				Legendchat.getIgnoreManager().playerIgnoreChannel((Player) sender, c);
				sender.sendMessage(Legendchat.getMessageManager().getMessage("message18").replace("@channel", c.getName()));
			}
		} else if (cmd.getName().equalsIgnoreCase("channel"))  { // Channel focus
			Channel c = null;
			ChannelManager cm = Legendchat.getChannelManager();
			c = cm.getChannelByName(args[0].toLowerCase());
			if (c == null) {
				c = cm.getChannelByNickname(args[0].toLowerCase());
			}
			if (c == null) {
				sender.sendMessage(Legendchat.getMessageManager().getMessage("error4"));
				return true;
			}
			if (c instanceof TemporaryChannel) {
				if (!((TemporaryChannel) c).user_list().contains((Player) sender) && !sender.hasPermission("legendchat.admin")) {
					sender.sendMessage(Legendchat.getMessageManager().getMessage("tc_error8"));
					return true;
				}
			}
			Legendchat.getPlayerManager().setPlayerFocusedChannel((Player) sender, c, true);
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
		if(sender instanceof Player && args.length == 1) {
			ArrayList<String> channels = new ArrayList();
			for (Channel c : Legendchat.getChannelManager().getChannels()) {
				final String cName = c.getName();
				if (cName.toLowerCase().startsWith(args[0].toLowerCase()) && Legendchat.getPlayerManager().canPlayerSeeChannel((Player) sender, c)) {
					channels.add(cName);
				}
			}
			return channels;
		}
		return Collections.EMPTY_LIST;
	}
}
