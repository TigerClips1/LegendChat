package br.com.devpaulo.legendchat.privatemessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import br.com.devpaulo.legendchat.afk.AfkManager;
import br.com.devpaulo.legendchat.api.Legendchat;
import br.com.devpaulo.legendchat.api.events.PrivateMessageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class PrivateMessageManager {

	private final HashMap<CommandSender, CommandSender> telling = new HashMap<>();
	private final HashMap<CommandSender, CommandSender> reply = new HashMap<>();
	private final CommandSender console = Bukkit.getConsoleSender();

	public void tellPlayer(CommandSender from, CommandSender to, String msg) {
		if (to == null) {
			if (!isPlayerTellLocked(from)) {
				return;
			}
			to = telling.get(from);
		}
		AfkManager afk = Legendchat.getAfkManager();
		if (from != console) {
			afk.removeAfk((Player) from);
		}
		if (to != console) {
			if (afk.isAfk((Player) to)) {
				from.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_1"));
				String mot = afk.getPlayerAfkMotive((Player) to);
				if (mot != null) {
					from.sendMessage(Legendchat.getMessageManager().getMessage("pm_error2_2").replace("@motive", mot));
				}
				return;
			}
		}
		PrivateMessageEvent e = new PrivateMessageEvent(from, to, msg);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			return;
		}
		from = e.getSender();
		to = e.getReceiver();
		final String fromName = from instanceof Player ? ((Player) from).getDisplayName() : from.getName();
		final String toName = to instanceof Player ? ((Player) to).getDisplayName() : to.getName();
		msg = e.getMessage();
		if (Legendchat.isCensorActive()) {
			msg = Legendchat.getCensorManager().censorFunction(msg);
		}

		boolean ignored = false;
		if (to != console && from != console) {
			assert from instanceof Player;
			assert to instanceof Player;
			if (Legendchat.getIgnoreManager().hasPlayerIgnoredPlayer((Player) to, (Player) from)) {
				ignored = true;
			}
		}

		if (!ignored) {
			setPlayerReply(to, from);
		}

		Component fromMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(Legendchat.getPrivateMessageFormat("send")
						.replace("{sender}", fromName)
						.replace("{receiver}", toName)
						.replace("{msg}", msg))
				.hoverEvent(HoverEvent.showText(Component.text("Click to reply")))
				.clickEvent(ClickEvent.suggestCommand("/msg " + toName + " "));

		Component toMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(Legendchat.getPrivateMessageFormat("receive")
						.replace("{sender}", fromName)
						.replace("{receiver}", toName)
						.replace("{msg}", msg))
				.hoverEvent(HoverEvent.showText(Component.text("Click to reply")))
				.clickEvent(ClickEvent.suggestCommand("/msg " + fromName + " "));

		if (from instanceof Audience) {
			((Audience) from).sendMessage(fromMessage);
		} else {
			from.sendMessage(String.valueOf(fromMessage));
		}

		if (!ignored) {
			if (to instanceof Audience) {
				((Audience) to).sendMessage(toMessage);
			} else {
				to.sendMessage(String.valueOf(toMessage));
			}
		}

		String spy = Legendchat.getPrivateMessageFormat("spy")
				.replace("{sender}", fromName)
				.replace("{receiver}", toName)
				.replace("{ignored}", (ignored ? Legendchat.getMessageManager().getMessage("ignored") : ""))
				.replace("{msg}", msg);

		for (Player p : Legendchat.getPlayerManager().getOnlineSpys()) {
			if ((p != from && p != to) || (ignored && p == to)) {
				p.sendMessage(spy);
			}
		}

		if (Legendchat.logToBukkit()) {
			Bukkit.getConsoleSender().sendMessage(spy);
		}

		if (Legendchat.logToFile()) {
			Legendchat.getLogManager().addLogToCache(spy, from instanceof Player ? ((Player) from).getLocation() : null);
		}
	}

	public void replyPlayer(CommandSender from, String msg) {
		if (!playerHasReply(from)) {
			from.sendMessage(Legendchat.getMessageManager().getMessage("pm_error1"));
			return;
		}
		tellPlayer(from, getPlayerReply(from), msg);
	}

	public void lockPlayerTell(CommandSender from, CommandSender to) {
		unlockPlayerTell(from);
		telling.put(from, to);
	}

	public void unlockPlayerTell(CommandSender p) {
		if (isPlayerTellLocked(p)) {
			telling.remove(p);
		}
	}

	public boolean isPlayerTellLocked(CommandSender p) {
		return telling.containsKey(p);
	}

	public CommandSender getPlayerLockedTellWith(CommandSender p) {
		if (isPlayerTellLocked(p)) {
			return telling.get(p);
		}
		return null;
	}

	public List<CommandSender> getAllTellLockedPlayers() {
		List<CommandSender> l = new ArrayList<>();
		l.addAll(telling.keySet());
		return l;
	}

	public void setPlayerReply(CommandSender to, CommandSender from) {
		if (playerHasReply(to)) {
			reply.remove(to);
		}
		reply.put(to, from);
	}

	public CommandSender getPlayerReply(CommandSender p) {
		if (!playerHasReply(p)) {
			return null;
		}
		return reply.get(p);
	}

	public boolean playerHasReply(CommandSender p) {
		return reply.containsKey(p);
	}

	public List<CommandSender> getAllPlayersWithReply() {
		List<CommandSender> l = new ArrayList<>();
		l.addAll(reply.keySet());
		return l;
	}

	public void playerDisconnect(CommandSender p) {
		unlockPlayerTell(p);
		if (reply.containsKey(p)) {
			reply.remove(p);
		}
		List<CommandSender> lista = new ArrayList<>();
		for (CommandSender p2 : getAllTellLockedPlayers()) {
			if (telling.get(p2) == p) {
				lista.add(p2);
			}
		}
		for (CommandSender p3 : lista) {
			telling.remove(p3);
		}
		lista.clear();
		for (CommandSender p2 : getAllPlayersWithReply()) {
			if (reply.get(p2) == p) {
				lista.add(p2);
			}
		}
		for (CommandSender p3 : lista) {
			reply.remove(p3);
		}
	}
}